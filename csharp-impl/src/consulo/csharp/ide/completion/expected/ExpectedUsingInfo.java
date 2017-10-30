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

package consulo.csharp.ide.completion.expected;

import gnu.trove.THashSet;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.BaseScopeProcessor;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.ide.codeInsight.actions.AddUsingAction;
import consulo.csharp.ide.codeStyle.CSharpCodeGenerationSettings;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.psi.CSharpUsingNamespaceStatement;
import consulo.csharp.lang.psi.CSharpUsingTypeStatement;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.dotnet.resolve.DotNetGenericExtractor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

/**
 * @author VISTALL
 * @since 21-Oct-17
 */
public class ExpectedUsingInfo
{
	@RequiredReadAction
	@Nullable
	public static ExpectedUsingInfo calculateFrom(@NotNull PsiElement element)
	{
		Set<PsiElement> elements = new THashSet<>();

		if(element instanceof DotNetLikeMethodDeclaration)
		{
			DotNetLikeMethodDeclaration likeMethodDeclaration = (DotNetLikeMethodDeclaration) element;

			fillFromTypeRef(likeMethodDeclaration.getReturnTypeRef(), elements);

			for(DotNetTypeRef typeRef : likeMethodDeclaration.getParameterTypeRefs())
			{
				fillFromTypeRef(typeRef, elements);
			}
		}

		if(element instanceof DotNetVirtualImplementOwner)
		{
			fillFromTypeRef(((DotNetVirtualImplementOwner) element).getTypeRefForImplement(), elements);
		}

		return elements.isEmpty() ? null : new ExpectedUsingInfo(elements);
	}

	@RequiredReadAction
	private static void fillFromTypeRef(DotNetTypeRef ref, Set<PsiElement> map)
	{
		DotNetTypeResolveResult resolveResult = ref.resolve();

		PsiElement element = resolveResult.getElement();
		if(element instanceof CSharpTypeDeclaration)
		{
			map.add(element);
		}

		if(element instanceof DotNetGenericParameterListOwner)
		{
			DotNetGenericExtractor genericExtractor = resolveResult.getGenericExtractor();

			DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) element).getGenericParameters();

			for(DotNetGenericParameter genericParameter : genericParameters)
			{
				DotNetTypeRef extractedTypeRef = genericExtractor.extract(genericParameter);
				if(extractedTypeRef != null)
				{
					fillFromTypeRef(extractedTypeRef, map);
				}
			}
		}
	}

	private final Set<PsiElement> myElements;

	public ExpectedUsingInfo(@NotNull Set<PsiElement> elements)
	{
		myElements = elements;
	}

	@RequiredWriteAction
	public void insertUsingBefore(@NotNull PsiElement element)
	{
		boolean useLanguageDataTypes = CSharpCodeGenerationSettings.getInstance(element.getProject()).USE_LANGUAGE_DATA_TYPES;

		Set<String> namespaces = new TreeSet<>();
		for(PsiElement target : myElements)
		{
			if(target instanceof CSharpTypeDeclaration)
			{
				String presentableQName = ((CSharpTypeDeclaration) target).getPresentableQName();

				if(useLanguageDataTypes)
				{
					if(CSharpTypeRefPresentationUtil.ourTypesAsKeywords.containsKey(presentableQName))
					{
						continue;
					}
				}

				String namespace = getNamespace((CSharpTypeDeclaration) target);
				if(StringUtil.isEmpty(namespace))
				{
					continue;
				}

				if(isUsedNamespace(namespace, target, element))
				{
					continue;
				}

				namespaces.add(namespace);
			}
		}

		PsiFile containingFile = element.getContainingFile();
		for(String namespace : namespaces)
		{
			new AddUsingAction(null, containingFile, Collections.emptySet()).addUsingNoCaretMoving(namespace);
		}
	}

	@RequiredReadAction
	private boolean isUsedNamespace(String namespace, PsiElement target, PsiElement element)
	{
		return !CSharpResolveUtil.walkUsing(new BaseScopeProcessor()
		{
			@Override
			@RequiredReadAction
			public boolean execute(@NotNull PsiElement psiElement, ResolveState resolveState)
			{
				if(psiElement instanceof CSharpUsingTypeStatement)
				{
					DotNetTypeResolveResult typeResolveResult = ((CSharpUsingTypeStatement) psiElement).getTypeRef().resolve();
					if(target.isEquivalentTo(typeResolveResult.getElement()))
					{
						return false;
					}
				}
				else if(psiElement instanceof CSharpUsingNamespaceStatement)
				{
					String referenceText = ((CSharpUsingNamespaceStatement) psiElement).getReferenceText();
					if(Objects.equals(namespace, referenceText))
					{
						return false;
					}
				}
				return true;
			}
		}, element, null, ResolveState.initial());
	}

	@RequiredReadAction
	private static String getNamespace(CSharpTypeDeclaration typeDeclaration)
	{
		if(typeDeclaration.isNested())
		{
			return getNamespace((CSharpTypeDeclaration) typeDeclaration.getParent());
		}
		return typeDeclaration.getPresentableParentQName();
	}
}