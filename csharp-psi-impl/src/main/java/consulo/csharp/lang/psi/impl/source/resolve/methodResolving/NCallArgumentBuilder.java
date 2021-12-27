/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp.lang.psi.impl.source.resolve.methodResolving;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.RecursionManager;
import com.intellij.openapi.util.Trinity;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.SmartList;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpCallArgument;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpNamedCallArgument;
import consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.*;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.context.MethodParameterResolveContext;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.context.ParameterResolveContext;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.context.SimpleParameterResolveContext;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.util.ArrayUtil2;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class NCallArgumentBuilder
{
	@Nonnull
	private static List<NCallArgument> buildCallArguments(@Nonnull DotNetTypeRef[] callArgumentTypeRefs, @Nonnull DotNetTypeRef[] parameterTypeRefs)
	{
		List<NCallArgument> list = new ArrayList<>(callArgumentTypeRefs.length);

		for(int i = 0; i < callArgumentTypeRefs.length; i++)
		{
			DotNetTypeRef callArgumentTypeRef = callArgumentTypeRefs[i];
			DotNetTypeRef dotNetTypeRef = ArrayUtil2.safeGet(parameterTypeRefs, i);

			list.add(new NCallArgument(callArgumentTypeRef, null, dotNetTypeRef));
		}
		return list;
	}

	@Nonnull
	@RequiredReadAction
	public static List<NCallArgument> buildCallArguments(@Nonnull Project project, @Nonnull CSharpCallArgument[] callArguments, @Nonnull CSharpSimpleParameterInfo[] parameterInfos, @Nonnull GlobalSearchScope scope)
	{
		return buildCallArguments(callArguments, scope, new SimpleParameterResolveContext(project, scope, parameterInfos));
	}

	@Nonnull
	@RequiredReadAction
	public static List<NCallArgument> buildCallArguments(@Nonnull CSharpCallArgument[] callArguments,
														 @Nonnull DotNetParameterListOwner parameterListOwner,
														 @Nonnull GlobalSearchScope scope,
														 boolean resolveFromParent)
	{
		return buildCallArguments(callArguments, scope, new MethodParameterResolveContext(parameterListOwner, resolveFromParent));
	}

	@Nonnull
	@RequiredReadAction
	public static List<NCallArgument> buildCallArguments(@Nonnull CSharpCallArgument[] callArguments, @Nonnull DotNetParameterListOwner parameterListOwner, @Nonnull GlobalSearchScope scope)
	{
		return buildCallArguments(callArguments, parameterListOwner, scope, false);
	}

	@Nonnull
	@RequiredReadAction
	private static <T> List<NCallArgument> buildCallArguments(@Nonnull CSharpCallArgument[] callArguments,
															  @Nonnull GlobalSearchScope implicitCastResolveScope,
															  @Nonnull ParameterResolveContext<T> context)
	{
		List<NCallArgument> list = new ArrayList<>(context.getParametersSize());

		List<CSharpCallArgument> paramsArguments = new SmartList<>();

		boolean paramsAlreadySet = false;
		int i = 0;
		for(CSharpCallArgument argument : callArguments)
		{
			ProgressManager.checkCanceled();

			DotNetTypeRef expressionTypeRef = DotNetTypeRef.ERROR_TYPE;
			String name;

			DotNetExpression argumentExpression = argument.getArgumentExpression();
			if(argumentExpression != null)
			{
				expressionTypeRef = RecursionManager.doPreventingRecursion(argument, false, () -> argumentExpression.toTypeRef(context.isResolveFromParentTypeRef()));
				if(expressionTypeRef == null)
				{
					expressionTypeRef = DotNetTypeRef.ERROR_TYPE;
				}
			}

			if(argument instanceof CSharpNamedCallArgument)
			{
				name = ((CSharpNamedCallArgument) argument).getName();

				T parameterByName = context.getParameterByName(name);
				if(parameterByName != null && parameterByName == context.getParamsParameter())
				{
					paramsAlreadySet = true;
				}

				if(paramsAlreadySet)
				{
					list.add(new NNamedParamsCallArgument(expressionTypeRef, argument, parameterByName, name, context.getInnerParamsParameterTypeRef()));
				}
				else
				{
					list.add(new NNamedCallArgument(expressionTypeRef, argument, parameterByName, name));
				}

				i++;
			}
			else
			{
				T parameter = context.getParameterByIndex(i++);
				if(parameter == null)
				{
					DotNetParameter paramsParameter = context.getParamsParameter();
					if(paramsParameter != null)
					{
						// we already have expression for params parameter
						NCallArgument paramsValue = findByName(list, paramsParameter.getName());
						if(paramsValue != null)
						{
							list.add(new NCallArgument(expressionTypeRef, argument, null));
						}
						else
						{
							// if expression is like inner params type
							if(CSharpTypeUtil.isInheritableWithImplicit(context.getInnerParamsParameterTypeRef(), expressionTypeRef, implicitCastResolveScope))
							{
								// store to params list
								paramsArguments.add(argument);
							}
							// if params type equal expression parameter pull it as argument of parameter
							else if(CSharpTypeUtil.isInheritableWithImplicit(context.getParamsParameterTypeRef(), expressionTypeRef, implicitCastResolveScope))
							{
								list.add(new NCallArgument(expressionTypeRef, argument, paramsParameter));
							}
							else
							{
								list.add(new NCallArgument(expressionTypeRef, argument, null));
							}
						}
					}
					else
					{
						list.add(new NCallArgument(expressionTypeRef, argument, null));
					}
				}
				else if(!paramsAlreadySet)
				{
					DotNetParameter paramsParameter = context.getParamsParameter();

					if(paramsParameter == parameter)
					{
						NCallArgument paramsValue = findByName(list, paramsParameter.getName());
						if(paramsValue != null)
						{
							list.add(new NCallArgument(expressionTypeRef, argument, null));
						}
						else
						{
							// if expression is like inner params type
							if(CSharpTypeUtil.isInheritableWithImplicit(context.getInnerParamsParameterTypeRef(), expressionTypeRef, implicitCastResolveScope))
							{
								// store to params list
								paramsArguments.add(argument);
							}
							// if params type equal expression parameter pull it as argument of parameter
							else if(CSharpTypeUtil.isInheritableWithImplicit(context.getParamsParameterTypeRef(), expressionTypeRef, implicitCastResolveScope))
							{
								list.add(new NCallArgument(expressionTypeRef, argument, paramsParameter));
							}
							else
							{
								list.add(new NCallArgument(expressionTypeRef, argument, null));
							}
						}
					}
					else
					{
						list.add(new NCallArgument(expressionTypeRef, argument, parameter));
					}
				}
			}
		}

		if(!paramsAlreadySet)
		{
			// if we have params arguments add to list it
			if(!paramsArguments.isEmpty())
			{
				list.add(new NParamsCallArgument(context.getProject(), context.getResolveScope(), paramsArguments, context.getParamsParameter()));
			}
			else
			{
				// if we have params parameter
				DotNetParameter paramsParameter = context.getParamsParameter();
				if(paramsParameter != null)
				{
					// but - no arguments for it, add empty argument
					NCallArgument nCallArgument = findByName(list, paramsParameter.getName());
					if(nCallArgument == null)
					{
						list.add(new NEmptyParamsCallArgument(paramsParameter));
					}
				}
			}
		}

		for(T parameter : context.getParameters())
		{
			NCallArgument nCallArgument = findByParameterObject(list, parameter);
			if(nCallArgument != null)
			{
				continue;
			}

			final Trinity<String, DotNetTypeRef, Boolean> parameterInfo = context.getParameterInfo(parameter);

			if(parameterInfo.getThird())
			{
				list.add(new NNamedCallArgument(parameterInfo.getSecond(), null, parameter, parameterInfo.getFirst()));
			}
			else
			{
				list.add(new NErrorCallArgument(parameter));
			}
		}

		return list;
	}

	@Nullable
	private static NCallArgument findByName(List<NCallArgument> arguments, @Nullable String name)
	{
		if(name == null)
		{
			return null;
		}

		for(NCallArgument argument : arguments)
		{
			if(argument instanceof NNamedCallArgument && Comparing.equal(((NNamedCallArgument) argument).getName(), name))
			{
				return argument;
			}

			String parameterName = argument.getParameterName();

			if(parameterName != null && Comparing.equal(parameterName, name))
			{
				return argument;
			}
		}
		return null;
	}

	@Nullable
	private static NCallArgument findByParameterObject(List<NCallArgument> arguments, Object parameterObject)
	{
		for(NCallArgument argument : arguments)
		{
			if(argument.getParameterObject() == parameterObject)
			{
				return argument;
			}
		}
		return null;
	}

	@Nonnull
	@RequiredReadAction
	public static MethodResolvePriorityInfo calc(@Nonnull CSharpCallArgument[] callArguments,
												 @Nonnull DotNetParameterListOwner parameterListOwner,
												 @Nonnull GlobalSearchScope scope,
												 boolean resolveFromParent)
	{
		List<NCallArgument> list = buildCallArguments(callArguments, parameterListOwner, scope, resolveFromParent);
		return calc(list, scope);
	}

	@Nonnull
	@RequiredReadAction
	public static MethodResolvePriorityInfo calc(@Nonnull CSharpCallArgument[] callArguments, @Nonnull DotNetParameterListOwner parameterListOwner, @Nonnull GlobalSearchScope scope)
	{
		return calc(callArguments, parameterListOwner, scope, false);
	}

	@Nonnull
	@RequiredReadAction
	public static MethodResolvePriorityInfo calc(@Nonnull CSharpCallArgumentListOwner callArgumentListOwner, @Nonnull CSharpSimpleParameterInfo[] p, @Nonnull GlobalSearchScope resolveScope)
	{
		List<NCallArgument> list = buildCallArguments(callArgumentListOwner.getCallArguments(), resolveScope, new SimpleParameterResolveContext(callArgumentListOwner.getProject(), resolveScope, p));
		return calc(list, resolveScope);
	}

	@Nonnull
	@RequiredReadAction
	public static MethodResolvePriorityInfo calc(@Nonnull DotNetTypeRef[] expressionTypeRefs, @Nonnull DotNetTypeRef[] parameterTypeRefs, @Nonnull GlobalSearchScope scope)
	{
		List<NCallArgument> list = buildCallArguments(expressionTypeRefs, parameterTypeRefs);
		return calc(list, scope);
	}

	@Nonnull
	@RequiredReadAction
	public static MethodResolvePriorityInfo calc(@Nonnull CSharpCallArgumentListOwner callArgumentListOwner, @Nonnull DotNetParameterListOwner parameterListOwner, @Nonnull GlobalSearchScope scope)
	{
		List<NCallArgument> list = buildCallArguments(callArgumentListOwner.getCallArguments(), parameterListOwner, scope);
		return calc(list, scope);
	}

	@Nonnull
	@RequiredReadAction
	public static MethodResolvePriorityInfo calc(@Nonnull List<NCallArgument> arguments, @Nonnull GlobalSearchScope castResolveScope)
	{
		int weight = 0;
		boolean valid = true;

		for(NCallArgument argument : arguments)
		{
			switch(argument.calcValid(castResolveScope))
			{
				case NCallArgument.EQUAL:
					weight -= 50000;
					break;
				case NCallArgument.PARAMS:
					weight -= 100000;
					break;
				case NCallArgument.INSTANCE_OF:
					weight -= 1000000;
					break;
				case NCallArgument.PARAMS_INSTANCE_OF:
					weight -= 2000000;
					break;
				default:
					valid = false;
					break;
			}
		}

		return new MethodResolvePriorityInfo(valid, weight, arguments);
	}
}
