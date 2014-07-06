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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpUsingNamespaceStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodAcceptorImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.WeightProcessor;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.CSharpIndexKeys;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.ExtensionMethodIndex;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.MethodIndex;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.TypeIndex;
import org.mustbe.consulo.dotnet.DotNetBundle;
import org.mustbe.consulo.dotnet.psi.DotNetInheritUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.msil.MsilHelper;
import com.intellij.codeInsight.daemon.impl.ShowAutoImportPass;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.intention.HighPriorityAction;
import com.intellij.codeInspection.HintAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Conditions;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.stubs.StubIndex;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import com.intellij.util.containers.ArrayListSet;
import com.intellij.util.indexing.IdFilter;
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

	private final CSharpReferenceExpressionImpl myRef;

	public UsingNamespaceFix(CSharpReferenceExpressionImpl ref)
	{
		myRef = ref;
	}

	public PopupResult doFix(Editor editor)
	{
		PsiElement resolve = myRef.resolve();
		if(resolve != null && resolve.isValid())
		{
			return PopupResult.NOT_AVAILABLE;
		}

		Set<String> q = collectAllAvailableNamespaces();
		if(q.isEmpty())
		{
			return PopupResult.NOT_AVAILABLE;
		}

		AddUsingAction action = new AddUsingAction(editor, myRef, q);
		String message = ShowAutoImportPass.getMessage(q.size() != 1, DotNetBundle.message("use.popup", q.iterator().next()));

		HintManager.getInstance().showQuestionHint(editor, message, myRef.getTextOffset(), myRef.getTextRange().getEndOffset(), action);

		return PopupResult.SHOW_HIT;
	}

	private Set<String> collectAllAvailableNamespaces()
	{
		if(myRef.getParent() instanceof CSharpUsingNamespaceStatementImpl || !myRef.isValid())
		{
			return Collections.emptySet();
		}
		String referenceName = myRef.getReferenceName();
		if(StringUtil.isEmpty(referenceName))
		{
			return Collections.emptySet();
		}
		Set<String> q = new ArrayListSet<String>();
		collectAvailableNamespaces(q, referenceName);
		collectAvailableNamespacesForMethodExtensions(q, referenceName);
		return q;
	}

	private void collectAvailableNamespaces(Set<String> set, String referenceName)
	{
		if(myRef.getQualifier() != null)
		{
			return;
		}

		Collection<DotNetTypeDeclaration> tempTypes;
		Collection<DotNetLikeMethodDeclaration> tempMethods;
		val kind = myRef.kind();
		switch(kind)
		{
			case ATTRIBUTE:
				val cond = new Condition<DotNetTypeDeclaration>()
				{
					@Override
					public boolean value(DotNetTypeDeclaration typeDeclaration)
					{
						return DotNetInheritUtil.isAttribute(typeDeclaration);
					}
				};
				// if attribute endwith Attribute - collect only with
				if(referenceName.endsWith(CSharpReferenceExpressionImplUtil.AttributeSuffix))
				{
					tempTypes = getTypesWithGeneric(referenceName);

					collect(set, tempTypes, cond);
				}
				else
				{
					tempTypes = getTypesWithGeneric(referenceName);

					collect(set, tempTypes, cond);

					tempTypes = getTypesWithGeneric(referenceName + CSharpReferenceExpressionImplUtil.AttributeSuffix);

					collect(set, tempTypes, cond);
				}
				break;
			default:
				tempTypes = getTypesWithGeneric(referenceName);

				collect(set, tempTypes, Conditions.<DotNetTypeDeclaration>alwaysTrue());

				tempMethods = MethodIndex.getInstance().get(referenceName, myRef.getProject(), myRef.getResolveScope());

				collect(set, tempMethods, new Condition<DotNetLikeMethodDeclaration>()
				{
					@Override
					public boolean value(DotNetLikeMethodDeclaration method)
					{
						return (method.getParent() instanceof DotNetNamespaceDeclaration || method.getParent() instanceof PsiFile) && method
								instanceof DotNetMethodDeclaration && ((DotNetMethodDeclaration) method).isDelegate();
					}
				});
				break;
		}
	}

	private List<DotNetTypeDeclaration> getTypesWithGeneric(final String ref)
	{
		final Set<String> set = new TreeSet<String>();
		StubIndex.getInstance().processAllKeys(CSharpIndexKeys.TYPE_INDEX, new Processor<String>()
		{
			@Override
			public boolean process(String name)
			{
				String nameNoGeneric = MsilHelper.cutGenericMarker(name);
				if(Comparing.equal(nameNoGeneric, ref))
				{
					set.add(name);
				}
				return true;
			}
		}, myRef.getResolveScope(), IdFilter.getProjectIdFilter(myRef.getProject(), true));

		if(set.isEmpty())
		{
			return Collections.emptyList();
		}

		List<DotNetTypeDeclaration> typeDeclarations = new ArrayList<DotNetTypeDeclaration>();
		for(String t : set)
		{
			typeDeclarations.addAll(TypeIndex.getInstance().get(t, myRef.getProject(), myRef.getResolveScope()));
		}
		return typeDeclarations;
	}

	private void collectAvailableNamespacesForMethodExtensions(Set<String> set, String referenceName)
	{
		PsiElement qualifier = myRef.getQualifier();
		if(qualifier == null)
		{
			return;
		}

		PsiElement parent = myRef.getParent();
		if(!(parent instanceof CSharpMethodCallExpressionImpl))
		{
			return;
		}

		DotNetExpression[] parameterExpressions = ((CSharpMethodCallExpressionImpl) parent).getParameterExpressions();

		DotNetExpression[] newExpressions = new DotNetExpression[parameterExpressions.length + 1];
		newExpressions[0] = (DotNetExpression) qualifier;
		System.arraycopy(parameterExpressions, 0, newExpressions, 1, parameterExpressions.length);

		Collection<DotNetLikeMethodDeclaration> list = ExtensionMethodIndex.getInstance().get(referenceName, myRef.getProject(),
				myRef.getResolveScope());

		for(DotNetLikeMethodDeclaration possibleMethod : list)
		{
			if(MethodAcceptorImpl.calcAcceptableWeight(myRef, newExpressions, possibleMethod.getParameters()) == WeightProcessor.MAX_WEIGHT)
			{
				PsiElement parentOfMethod = possibleMethod.getParent();
				if(parentOfMethod instanceof DotNetQualifiedElement)
				{
					set.add(((DotNetQualifiedElement) parentOfMethod).getPresentableParentQName());
				}
			}
		}
	}

	private static <T extends DotNetQualifiedElement> void collect(Set<String> namespaces, Collection<T> element, Condition<T> condition)
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
			namespaces.add(presentableParentQName);
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
		return !collectAllAvailableNamespaces().isEmpty();
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
