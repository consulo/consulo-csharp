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

package consulo.csharp.ide.refactoring.changeSignature;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.psi.util.PsiTreeUtil;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.csharp.lang.psi.CSharpCallArgumentList;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpFileFactory;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import com.intellij.openapi.application.ReadActionProcessor;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.refactoring.changeSignature.ChangeInfo;
import com.intellij.refactoring.changeSignature.ChangeSignatureUsageProcessor;
import com.intellij.refactoring.rename.ResolveSnapshotProvider;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.PairFunction;
import com.intellij.util.Processor;
import com.intellij.util.containers.MultiMap;
import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterList;
import consulo.dotnet.psi.DotNetReferenceExpression;
import consulo.dotnet.psi.DotNetStatement;
import consulo.internal.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;

/**
 * @author VISTALL
 * @since 12.06.14
 */
public class CSharpChangeSignatureUsageProcessor implements ChangeSignatureUsageProcessor
{
	@Nonnull
	@Override
	public UsageInfo[] findUsages(@Nonnull final ChangeInfo info)
	{
		if(!(info instanceof CSharpChangeInfo))
		{
			return UsageInfo.EMPTY_ARRAY;
		}
		final List<UsageInfo> list = new ArrayList<UsageInfo>();

		final ReadActionProcessor<PsiReference> refProcessor = new ReadActionProcessor<PsiReference>()
		{
			@RequiredReadAction
			@Override
			public boolean processInReadAction(final PsiReference ref)
			{
				final PsiElement resolve = ref.resolve();
				if(resolve != info.getMethod())
				{
					return true;
				}
				TextRange rangeInElement = ref.getRangeInElement();
				list.add(new UsageInfo(ref.getElement(), rangeInElement.getStartOffset(), rangeInElement.getEndOffset(), false));
				return true;
			}
		};

		ReferencesSearch.search(new ReferencesSearch.SearchParameters(info.getMethod(), info.getMethod().getResolveScope(), false)).forEach(refProcessor);
		return list.toArray(UsageInfo.EMPTY_ARRAY);
	}

	@Nonnull
	@Override
	public MultiMap<PsiElement, String> findConflicts(@Nonnull ChangeInfo info, Ref<UsageInfo[]> refUsages)
	{
		return MultiMap.emptyInstance();
	}

	@Override
	@RequiredReadAction
	public boolean processUsage(@Nonnull ChangeInfo changeInfo, @Nonnull UsageInfo usageInfo, boolean beforeMethodChange, @Nonnull UsageInfo[] usages)
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

				DotNetStatement statement = CSharpFileFactory.createStatement(usageInfo.getProject(), builder);
				CSharpCallArgumentListOwner call = PsiTreeUtil.getChildOfType(statement, CSharpCallArgumentListOwner.class);
				parameterList.replace(call.getParameterList());
			}
			return true;
		}
		return false;
	}

	@Override
	@RequiredReadAction
	public boolean processPrimaryMethod(@Nonnull ChangeInfo changeInfo)
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
		CSharpAccessModifier newVisibility = sharpChangeInfo.getNewVisibility();
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
				builder.append(CSharpTypeRefPresentationUtil.buildShortText(method.getReturnTypeRef())).append(" ");
			}
		}
		builder.append(method.getName());
		builder.append("(");

		StubBlockUtil.join(builder, sharpChangeInfo.getNewParameters(), new PairFunction<StringBuilder, CSharpParameterInfo, Void>()
		{
			@Nullable
			@Override
			public Void fun(StringBuilder stringBuilder, CSharpParameterInfo parameterInfo)
			{
				CSharpModifier modifier = parameterInfo.getModifier();
				if(modifier != null)
				{
					stringBuilder.append(modifier.getPresentableText()).append(" ");
				}
				stringBuilder.append(parameterInfo.getTypeText());
				stringBuilder.append(" ");
				stringBuilder.append(parameterInfo.getName());
				return null;
			}
		}, ", ");

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

			for(CSharpAccessModifier value : CSharpAccessModifier.VALUES)
			{
				for(CSharpModifier modifier : value.getModifiers())
				{
					modifierList.removeModifier(modifier);
				}
			}

			for(CSharpModifier modifier : newVisibility.getModifiers())
			{
				modifierList.addModifier(modifier);
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
					ReferencesSearch.search(new ReferencesSearch.SearchParameters(originalParameter, originalParameter.getUseScope(), false)).forEach(new Processor<PsiReference>()
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
	public boolean shouldPreviewUsages(@Nonnull ChangeInfo changeInfo, @Nonnull UsageInfo[] usages)
	{
		return false;
	}

	@Override
	public void registerConflictResolvers(@Nonnull List<ResolveSnapshotProvider.ResolveSnapshot> snapshots,
			@Nonnull ResolveSnapshotProvider resolveSnapshotProvider,
			@Nonnull UsageInfo[] usages,
			@Nonnull ChangeInfo changeInfo)
	{

	}
}
