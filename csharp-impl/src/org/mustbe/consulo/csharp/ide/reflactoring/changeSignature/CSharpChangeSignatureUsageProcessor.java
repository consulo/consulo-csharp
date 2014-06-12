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

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterList;
import org.mustbe.consulo.dotnet.psi.DotNetReferenceExpression;
import com.intellij.openapi.application.ReadActionProcessor;
import com.intellij.openapi.project.Project;
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
import com.intellij.util.CommonProcessors;
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
	@Override
	public UsageInfo[] findUsages(ChangeInfo info)
	{
		if(!(info instanceof CSharpChangeInfo))
		{
			return UsageInfo.EMPTY_ARRAY;
		}
		val collectProcessor = new CommonProcessors.CollectProcessor<UsageInfo>();

		final ReadActionProcessor<PsiReference> refProcessor = new ReadActionProcessor<PsiReference>()
		{
			@Override
			public boolean processInReadAction(final PsiReference ref)
			{
				TextRange rangeInElement = ref.getRangeInElement();
				return collectProcessor.process(new UsageInfo(ref.getElement(), rangeInElement.getStartOffset(), rangeInElement.getEndOffset(),
						false));
			}
		};

		ReferencesSearch.search(new ReferencesSearch.SearchParameters(info.getMethod(), info.getMethod().getResolveScope(),
				false)).forEach(refProcessor);
		return collectProcessor.toArray(UsageInfo.EMPTY_ARRAY);
	}

	@Override
	public MultiMap<PsiElement, String> findConflicts(
			ChangeInfo info, Ref<UsageInfo[]> refUsages)
	{
		return null;
	}

	@Override
	public boolean processUsage(
			ChangeInfo changeInfo, UsageInfo usageInfo, boolean beforeMethodChange, UsageInfo[] usages)
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
		if(changeInfo.isNameChanged())
		{
			((DotNetReferenceExpression) element).handleElementRename(changeInfo.getNewName());
		}
		return true;
	}

	@Override
	public boolean processPrimaryMethod(ChangeInfo changeInfo)
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
	public boolean shouldPreviewUsages(ChangeInfo changeInfo, UsageInfo[] usages)
	{
		return false;
	}

	@Override
	public boolean setupDefaultValues(
			ChangeInfo changeInfo, Ref<UsageInfo[]> refUsages, Project project)
	{
		return false;
	}

	@Override
	public void registerConflictResolvers(
			List<ResolveSnapshotProvider.ResolveSnapshot> snapshots,
			@NotNull ResolveSnapshotProvider resolveSnapshotProvider,
			UsageInfo[] usages,
			ChangeInfo changeInfo)
	{

	}
}
