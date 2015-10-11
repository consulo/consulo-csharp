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

package org.mustbe.consulo.csharp.ide.codeInsight.actions;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListChild;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolver;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.ExtensionMethodIndex;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.MethodIndex;
import org.mustbe.consulo.csharp.lang.psi.resolve.AttributeByNameSelector;
import org.mustbe.consulo.dotnet.DotNetBundle;
import org.mustbe.consulo.dotnet.libraryAnalyzer.DotNetLibraryAnalyzerComponent;
import org.mustbe.consulo.dotnet.libraryAnalyzer.NamespaceReference;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetInheritUtil;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.resolve.DotNetShortNameSearcher;
import org.mustbe.consulo.dotnet.resolve.GlobalSearchScopeFilter;
import com.intellij.codeInsight.daemon.impl.ShowAutoImportPass;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInspection.HintAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.CommonProcessors;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ArrayListSet;
import lombok.val;

/**
 * @author VISTALL
 * @since 30.12.13.
 */
public class UsingNamespaceFix implements HintAction, HighPriorityAction
{
	enum PopupResult
	{
		NOT_AVAILABLE,
		SHOW_HIT,
		SHOW_ACTION
	}

	private final SmartPsiElementPointer<CSharpReferenceExpression> myRefPointer;

	public UsingNamespaceFix(@NotNull CSharpReferenceExpression ref)
	{
		myRefPointer = SmartPointerManager.getInstance(ref.getProject()).createSmartPsiElementPointer(ref);
	}

	@NotNull
	public PopupResult doFix(Editor editor)
	{
		CSharpReferenceExpression element = myRefPointer.getElement();
		if(element == null)
		{
			return PopupResult.NOT_AVAILABLE;
		}

		CSharpReferenceExpression.ResolveToKind kind = element.kind();
		if(kind != CSharpReferenceExpression.ResolveToKind.TYPE_LIKE &&
				kind != CSharpReferenceExpression.ResolveToKind.METHOD &&
				element.getQualifier() != null)
		{
			return PopupResult.NOT_AVAILABLE;
		}
		PsiElement resolve = element.resolve();
		if(resolve != null && resolve.isValid())
		{
			return PopupResult.NOT_AVAILABLE;
		}

		Set<NamespaceReference> references = collectAllAvailableNamespaces(element, kind);
		if(references.isEmpty())
		{
			return PopupResult.NOT_AVAILABLE;
		}

		AddUsingAction action = new AddUsingAction(editor, element, references);
		String message = ShowAutoImportPass.getMessage(references.size() != 1, DotNetBundle.message("use.popup",
				AddUsingAction.formatMessage(references.iterator().next())));

		HintManager.getInstance().showQuestionHint(editor, message, element.getTextOffset(), element.getTextRange().getEndOffset(), action);

		return PopupResult.SHOW_HIT;
	}

	@NotNull
	private static Set<NamespaceReference> collectAllAvailableNamespaces(CSharpReferenceExpression ref, CSharpReferenceExpression.ResolveToKind kind)
	{
		if(PsiTreeUtil.getParentOfType(ref, CSharpUsingListChild.class) != null || !ref.isValid())
		{
			return Collections.emptySet();
		}
		String referenceName = ref.getReferenceName();
		if(StringUtil.isEmpty(referenceName))
		{
			return Collections.emptySet();
		}
		Set<NamespaceReference> resultSet = new ArrayListSet<NamespaceReference>();
		if(kind == CSharpReferenceExpression.ResolveToKind.TYPE_LIKE || ref.getQualifier() == null)
		{
			collectAvailableNamespaces(ref, resultSet, referenceName);
		}
		if(kind == CSharpReferenceExpression.ResolveToKind.METHOD)
		{
			collectAvailableNamespacesForMethodExtensions(ref, resultSet, referenceName);
		}

		Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(ref);
		if(moduleForPsiElement != null)
		{
			resultSet.addAll(DotNetLibraryAnalyzerComponent.getInstance(moduleForPsiElement.getProject()).get(moduleForPsiElement, referenceName));
		}
		return resultSet;
	}

	private static void collectAvailableNamespaces(final CSharpReferenceExpression ref, Set<NamespaceReference> set, String referenceName)
	{
		if(ref.getQualifier() != null)
		{
			return;
		}
		Collection<DotNetTypeDeclaration> tempTypes;
		Collection<DotNetLikeMethodDeclaration> tempMethods;

		PsiElement parent = ref.getParent();
		if(parent instanceof CSharpAttribute)
		{
			val cond = new Condition<DotNetTypeDeclaration>()
			{
				@Override
				public boolean value(DotNetTypeDeclaration typeDeclaration)
				{
					return DotNetInheritUtil.isAttribute(typeDeclaration);
				}
			};
			// if attribute endwith Attribute - collect only with
			if(referenceName.endsWith(AttributeByNameSelector.AttributeSuffix))
			{
				tempTypes = getTypesWithGeneric(ref, referenceName);

				collect(set, tempTypes, cond);
			}
			else
			{
				tempTypes = getTypesWithGeneric(ref, referenceName);

				collect(set, tempTypes, cond);

				tempTypes = getTypesWithGeneric(ref, referenceName + AttributeByNameSelector.AttributeSuffix);

				collect(set, tempTypes, cond);
			}
		}
		else
		{
			tempTypes = getTypesWithGeneric(ref, referenceName);

			collect(set, tempTypes, Conditions.<DotNetTypeDeclaration>alwaysTrue());

			tempMethods = MethodIndex.getInstance().get(referenceName, ref.getProject(), ref.getResolveScope());

			collect(set, tempMethods, new Condition<DotNetLikeMethodDeclaration>()
			{
				@Override
				public boolean value(DotNetLikeMethodDeclaration method)
				{
					return (method.getParent() instanceof DotNetNamespaceDeclaration || method.getParent() instanceof PsiFile) && method instanceof
							CSharpMethodDeclaration && ((CSharpMethodDeclaration) method).isDelegate();
				}
			});
		}
	}

	private static Collection<DotNetTypeDeclaration> getTypesWithGeneric(CSharpReferenceExpression ref, final String refName)
	{
		CommonProcessors.CollectProcessor<DotNetTypeDeclaration> processor = new CommonProcessors.CollectProcessor<DotNetTypeDeclaration>();

		GlobalSearchScopeFilter filter = new GlobalSearchScopeFilter(ref.getResolveScope());
		DotNetShortNameSearcher.getInstance(ref.getProject()).collectTypes(refName, ref.getResolveScope(), filter, processor);

		return processor.getResults();
	}

	@RequiredReadAction
	private static void collectAvailableNamespacesForMethodExtensions(CSharpReferenceExpression ref,
			Set<NamespaceReference> set,
			String referenceName)
	{
		PsiElement qualifier = ref.getQualifier();
		if(qualifier == null)
		{
			return;
		}

		PsiElement parent = ref.getParent();
		if(!(parent instanceof CSharpMethodCallExpressionImpl))
		{
			return;
		}

		CSharpCallArgument[] callArguments = ((CSharpMethodCallExpressionImpl) parent).getCallArguments();


		CSharpCallArgument[] newCallArguments = new CSharpCallArgument[callArguments.length + 1];
		newCallArguments[0] = new CSharpLightCallArgument((DotNetExpression) qualifier);
		System.arraycopy(callArguments, 0, newCallArguments, 1, callArguments.length);

		val list = ExtensionMethodIndex.getInstance().get(referenceName, ref.getProject(), ref.getResolveScope());

		for(DotNetLikeMethodDeclaration possibleMethod : list)
		{
			if(MethodResolver.calc(newCallArguments, possibleMethod, ref).isValidResult())
			{
				PsiElement parentOfMethod = possibleMethod.getParent();
				if(parentOfMethod instanceof DotNetQualifiedElement)
				{
					set.add(new NamespaceReference(((DotNetQualifiedElement) parentOfMethod).getPresentableParentQName(), null));
				}
			}
		}
	}

	private static <T extends DotNetQualifiedElement> void collect(Set<NamespaceReference> result, Collection<T> element, Condition<T> condition)
	{
		for(val type : element)
		{
			String presentableParentQName = type.getPresentableParentQName();
			if(StringUtil.isEmpty(presentableParentQName))
			{
				continue;
			}

			if(!condition.value(type))
			{
				continue;
			}
			result.add(new NamespaceReference(presentableParentQName, null));
		}
	}

	@Override
	public boolean showHint(@NotNull Editor editor)
	{
		return doFix(editor) == PopupResult.SHOW_HIT;
	}

	@NotNull
	@Override
	public String getText()
	{
		return DotNetBundle.message("add.using");
	}

	@NotNull
	@Override
	public String getFamilyName()
	{
		return "Import";
	}

	@Override
	public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile)
	{
		CSharpReferenceExpression element = myRefPointer.getElement();
		if(element == null)
		{
			return false;
		}
		CSharpReferenceExpression.ResolveToKind kind = element.kind();
		return !collectAllAvailableNamespaces(element, kind).isEmpty();
	}

	@Override
	public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException
	{

	}

	@Override
	public boolean startInWriteAction()
	{
		return true;
	}
}
