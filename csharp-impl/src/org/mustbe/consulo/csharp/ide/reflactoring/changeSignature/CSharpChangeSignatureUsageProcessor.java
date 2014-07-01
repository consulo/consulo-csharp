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

package org.mustbe.consulo.csharp.ide.reflactoring.changeSignature;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import com.intellij.openapi.application.ReadActionProcessor;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.refactoring.changeSignature.ChangeInfo;
import com.intellij.refactoring.changeSignature.ChangeSignatureUsageProcessor;
import com.intellij.refactoring.changeSignature.ParameterInfo;
import com.intellij.refactoring.rename.ResolveSnapshotProvider;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Function;
import com.intellij.util.Processor;
import com.intellij.util.containers.MultiMap;
import lombok.val;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpChangeSignatureUsageProcessor implements ChangeSignatureUsageProcessor
{
	@NotNull
	@Override
	public UsageInfo[] findUsages(@NotNull final ChangeInfo info)
	{
		if(!(info instanceof CSharpChangeInfo))
		{
			return UsageInfo.EMPTY_ARRAY;
		}
		val list = new ArrayList<UsageInfo>();

		final ReadActionProcessor<PsiReference> refProcessor = new ReadActionProcessor<PsiReference>()
		{
			@Override
			public boolean processInReadAction(final PsiReference ref)
			{
				final PsiElement resolve = ref.resolve();
				if(resolve != info.getMethod())
				{
					return true;
				}
				TextRange rangeInElement = ref.getRangeInElement();
				list.add(new UsageInfo(ref.getElement(), rangeInElement.getStartOffset(), rangeInElement.getEndOffset(),false));
				return true;
			}
		};

		ReferencesSearch.search(new ReferencesSearch.SearchParameters(info.getMethod(), info.getMethod().getResolveScope(),
				false)).forEach(refProcessor);
		return list.toArray(UsageInfo.EMPTY_ARRAY);
	}

	@NotNull
	@Override
	public MultiMap<PsiElement, String> findConflicts(@NotNull ChangeInfo info, Ref<UsageInfo[]> refUsages)
	{
		return MultiMap.emptyInstance();
	}

	@Override
	public boolean processUsage(
			@NotNull ChangeInfo changeInfo, @NotNull UsageInfo usageInfo, boolean beforeMethodChange, @NotNull UsageInfo[] usages)
	{
		if(!(changeInfo instanceof CSharpChangeInfo))
		{
			return false;
		}
		PsiElement element = usageInfo.getElement();
		if(!(element instanceof DotNetReferenceExpression))
		{
			return false;
		}

		if(!beforeMethodChange)
		{
			return true;
		}

		if(changeInfo.isNameChanged())
		{
			((DotNetReferenceExpression) element).handleElementRename(changeInfo.getNewName());
		}

		if(((CSharpChangeInfo) changeInfo).isParametersChanged())
		{
			PsiElement parent = element.getParent();
			if(parent instanceof CSharpCallArgumentListOwner)
			{
				CSharpCallArgumentList parameterList = ((CSharpCallArgumentListOwner) parent).getParameterList();
				if(parameterList == null)
				{
					return true;
				}

				CSharpParameterInfo[] newParameters = ((CSharpChangeInfo) changeInfo).getNewParameters();

				DotNetExpression[] expressions = parameterList.getExpressions();
				String[] newArguments = new String[newParameters.length];

				for(CSharpParameterInfo newParameter : newParameters)
				{
					if(newParameter.getOldIndex() != -1)
					{
						newArguments[newParameter.getNewIndex()] = expressions[newParameter.getOldIndex()].getText();
					}
					else
					{
						newArguments[newParameter.getNewIndex()] = newParameter.getDefaultValue();
					}
				}

				StringBuilder builder = new StringBuilder("test(");
				builder.append(StringUtil.join(newArguments, ", "));
				builder.append(");");

				val call = (CSharpCallArgumentListOwner) CSharpFileFactory.createExpression(usageInfo.getProject(), builder.toString());
				parameterList.replace(call.getParameterList());
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean processPrimaryMethod(@NotNull ChangeInfo changeInfo)
	{
		if(!(changeInfo instanceof CSharpChangeInfo))
		{
			return false;
		}
		CSharpChangeInfo sharpChangeInfo = (CSharpChangeInfo) changeInfo;

		DotNetLikeMethodDeclaration method = sharpChangeInfo.getMethod();

		if(sharpChangeInfo.isNameChanged())
		{
			assert method instanceof CSharpMethodDeclaration;

			method.setName(sharpChangeInfo.getNewName());
		}

		StringBuilder builder = new StringBuilder();
		CSharpModifier newVisibility = sharpChangeInfo.getNewVisibility();
		if(newVisibility != null)
		{
			builder.append(newVisibility.getPresentableText()).append(" ");
		}
		if(method instanceof CSharpMethodDeclaration)
		{
			if(changeInfo.isReturnTypeChanged())
			{
				builder.append(((CSharpChangeInfo) changeInfo).getNewReturnType()).append(" ");
			}
			else
			{
				builder.append(method.getReturnTypeRef().getQualifiedText()).append(" ");
			}
		}
		builder.append(method.getName());
		builder.append("(");

		builder.append(StringUtil.join(sharpChangeInfo.getNewParameters(), new Function<ParameterInfo, String>()
		{
			@Override
			public String fun(ParameterInfo parameterInfo)
			{
				return parameterInfo.getTypeText() + " " + parameterInfo.getName();
			}
		}, ", "));

		builder.append(");");

		DotNetLikeMethodDeclaration newMethod = CSharpFileFactory.createMethod(method.getProject(), builder);

		if(sharpChangeInfo.isReturnTypeChanged())
		{
			method.getReturnType().replace(newMethod.getReturnType());
		}

		if(newVisibility != null)
		{
			DotNetModifierList modifierList = method.getModifierList();
			assert modifierList != null;

			PsiElement modifier = null;
			for(CSharpModifier accessModifier : CSharpMethodDescriptor.ourAccessModifiers)
			{
				PsiElement modifierElement = modifierList.getModifierElement(accessModifier);
				if(modifierElement != null)
				{
					modifier = modifierElement;
					break;
				}
			}

			if(modifier != null)
			{
				PsiElement modifierElement = newMethod.getModifierList().getModifierElement(newVisibility);
				modifier.replace(modifierElement);
			}
		}

		if(sharpChangeInfo.isParametersChanged())
		{
			CSharpParameterInfo[] newParameters = sharpChangeInfo.getNewParameters();

			for(final CSharpParameterInfo newParameter : newParameters)
			{
				DotNetParameter originalParameter = newParameter.getParameter();
				if(originalParameter != null)
				{
					ReferencesSearch.search(new ReferencesSearch.SearchParameters(originalParameter, originalParameter.getUseScope(),
							false)).forEach(new Processor<PsiReference>()
					{
						@Override
						public boolean process(PsiReference reference)
						{
							reference.handleElementRename(newParameter.getName());
							return true;
						}
					});

					originalParameter.setName(newParameter.getName());
				}
			}

			DotNetParameterList parameterList = method.getParameterList();
			if(parameterList != null)
			{
				parameterList.replace(newMethod.getParameterList());
			}
		}
		return true;
	}

	@Override
	public boolean shouldPreviewUsages(@NotNull ChangeInfo changeInfo, @NotNull UsageInfo[] usages)
	{
		return false;
	}

	@Override
	public void registerConflictResolvers(
			@NotNull List<ResolveSnapshotProvider.ResolveSnapshot> snapshots,
			@NotNull ResolveSnapshotProvider resolveSnapshotProvider,
			@NotNull UsageInfo[] usages,
			@NotNull ChangeInfo changeInfo)
	{

	}
}
