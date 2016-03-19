/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.ide;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetConstructorDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetVirtualImplementOwner;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.BitUtil;
import com.intellij.util.Function;

/**
 * @author VISTALL
 * @since 16.12.14
 */
public class CSharpElementPresentationUtil
{
	public static final int METHOD_SCALA_FORMAT = 1 << 0;
	public static final int METHOD_WITH_RETURN_TYPE = 1 << 1;
	public static final int METHOD_PARAMETER_NAME = 1 << 2;
	public static final int METHOD_WITH_VIRTUAL_IMPL_TYPE = 1 << 4;

	public static final int METHOD_SCALA_LIKE_FULL = METHOD_SCALA_FORMAT | METHOD_WITH_RETURN_TYPE | METHOD_PARAMETER_NAME | METHOD_WITH_VIRTUAL_IMPL_TYPE;

	@NotNull
	@RequiredReadAction
	public static String formatField(@NotNull DotNetFieldDeclaration fieldDeclaration)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(fieldDeclaration.getName());
		builder.append(":");
		CSharpTypeRefPresentationUtil.appendTypeRef(fieldDeclaration, builder, fieldDeclaration.toTypeRef(true), CSharpTypeRefPresentationUtil.QUALIFIED_NAME_WITH_KEYWORD);
		return builder.toString();
	}

	@NotNull
	@RequiredReadAction
	public static String formatMethod(@NotNull DotNetLikeMethodDeclaration methodDeclaration, int flags)
	{
		StringBuilder builder = new StringBuilder();

		if(BitUtil.isSet(flags, METHOD_WITH_RETURN_TYPE) && !BitUtil.isSet(flags, METHOD_SCALA_FORMAT))
		{
			if(!(methodDeclaration instanceof DotNetConstructorDeclaration))
			{
				CSharpTypeRefPresentationUtil.appendTypeRef(methodDeclaration, builder, methodDeclaration.getReturnTypeRef(), CSharpTypeRefPresentationUtil.QUALIFIED_NAME_WITH_KEYWORD);
				builder.append(" ");
			}
		}

		if(methodDeclaration instanceof DotNetConstructorDeclaration && ((DotNetConstructorDeclaration) methodDeclaration).isDeConstructor())
		{
			builder.append("~");
		}

		if(BitUtil.isSet(flags, METHOD_WITH_VIRTUAL_IMPL_TYPE))
		{
			if(methodDeclaration instanceof DotNetVirtualImplementOwner)
			{
				DotNetTypeRef typeRefForImplement = ((DotNetVirtualImplementOwner) methodDeclaration).getTypeRefForImplement();
				if(typeRefForImplement != DotNetTypeRef.ERROR_TYPE)
				{
					CSharpTypeRefPresentationUtil.appendTypeRef(methodDeclaration, builder, typeRefForImplement, CSharpTypeRefPresentationUtil.QUALIFIED_NAME_WITH_KEYWORD);
					builder.append(".");
				}
			}
		}

		if(methodDeclaration instanceof CSharpIndexMethodDeclaration)
		{
			builder.append("this");
		}
		else
		{
			builder.append(methodDeclaration.getName());
		}
		formatTypeGenericParameters(methodDeclaration.getGenericParameters(), builder);
		formatParameters(methodDeclaration, builder, flags);
		return builder.toString();
	}

	public static void formatParameters(@NotNull DotNetLikeMethodDeclaration methodDeclaration, @NotNull StringBuilder builder, final int flags)
	{
		boolean indexMethod = methodDeclaration instanceof CSharpIndexMethodDeclaration;

		DotNetParameter[] parameters = methodDeclaration.getParameters();
		if(parameters.length == 0)
		{
			builder.append(indexMethod ? "[]" : "()");
		}
		else
		{
			builder.append(indexMethod ? "[" : "(");
			builder.append(StringUtil.join(parameters, new Function<DotNetParameter, String>()
			{
				@Override
				public String fun(DotNetParameter parameter)
				{
					String text = CSharpTypeRefPresentationUtil.buildTextWithKeyword(parameter.toTypeRef(true), parameter);

					if(!BitUtil.isSet(flags, METHOD_PARAMETER_NAME))
					{
						return text;
					}

					if(BitUtil.isSet(flags, METHOD_SCALA_FORMAT))
					{
						return parameter.getName() + ":" + text;
					}
					else
					{
						return text + " " + parameter.getName();
					}
				}
			}, ", "));
			builder.append(indexMethod ? "]" : ")");
		}

		if(BitUtil.isSet(flags, METHOD_WITH_RETURN_TYPE) && BitUtil.isSet(flags, METHOD_SCALA_FORMAT))
		{
			if(!(methodDeclaration instanceof DotNetConstructorDeclaration))
			{
				builder.append(":");
				CSharpTypeRefPresentationUtil.appendTypeRef(methodDeclaration, builder, methodDeclaration.getReturnTypeRef(), CSharpTypeRefPresentationUtil.QUALIFIED_NAME_WITH_KEYWORD);
			}
		}
	}

	public static void formatTypeGenericParameters(@NotNull DotNetGenericParameter[] parameters, @NotNull StringBuilder builder)
	{
		if(parameters.length > 0)
		{
			builder.append("<");
			builder.append(StringUtil.join(parameters, new Function<DotNetGenericParameter, String>()
			{
				@Override
				public String fun(DotNetGenericParameter dotNetGenericParameter)
				{
					return dotNetGenericParameter.getName();
				}
			}, ", "));
			builder.append(">");
		}
	}

	@NotNull
	public static String formatGenericParameters(@NotNull DotNetGenericParameterListOwner owner)
	{
		DotNetGenericParameter[] genericParameters = owner.getGenericParameters();
		if(genericParameters.length == 0)
		{
			return "";
		}
		return "<" + StringUtil.join(genericParameters, new Function<DotNetGenericParameter, String>()
		{
			@Override
			public String fun(DotNetGenericParameter genericParameter)
			{
				return genericParameter.getName();
			}
		}, ", ") + ">";
	}
}
