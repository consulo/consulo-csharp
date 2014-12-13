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

package org.mustbe.consulo.csharp.ide.highlight;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.CSharpErrorBundle;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.ConvertToNormalCallFix;
import org.mustbe.consulo.csharp.ide.highlight.check.impl.CS0029;
import org.mustbe.consulo.csharp.ide.highlight.util.ConstructorHighlightUtil;
import org.mustbe.consulo.csharp.ide.highlight.util.GenericParameterHighlightUtil;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayAccessExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLinqExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpOperatorReferenceImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodCalcResult;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.operatorResolving.ImplicitCastInfo;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ReferenceRange;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpHighlightVisitor extends CSharpElementVisitor implements HighlightVisitor
{
	private HighlightInfoHolder myHighlightInfoHolder;

	@Override
	public boolean suitableForFile(@NotNull PsiFile psiFile)
	{
		return psiFile instanceof CSharpFileImpl;
	}

	@Override
	public void visit(@NotNull PsiElement element)
	{
		element.accept(this);
	}

	@Override
	public void visitElement(PsiElement element)
	{
		ProgressIndicatorProvider.checkCanceled();

		IElementType elementType = element.getNode().getElementType();
		if(CSharpSoftTokens.ALL.contains(elementType))
		{
			myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(element).textAttributes(CSharpHighlightKey
					.SOFT_KEYWORD).create());
		}

		if(element.getNode().getElementType() == CSharpTokens.IDENTIFIER)
		{
			PsiElement parent = element.getParent();

			parent.accept(this);
		}

		if(element instanceof XmlTag)
		{
			PsiReference[] references = element.getReferences();
			for(PsiReference reference : references)
			{
				PsiElement resolve = reference.resolve();
				if(resolve == null)
				{
					List<TextRange> range = ReferenceRange.getAbsoluteRanges(reference);

					myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.WRONG_REF).range(range.get(0)).descriptionAndTooltip
							("Unknown tag").create());
				}
			}
		}
		if(element instanceof XmlAttribute)
		{
			PsiReference[] references = element.getReferences();
			for(PsiReference reference : references)
			{
				PsiElement resolve = reference.resolve();
				if(resolve == null)
				{
					List<TextRange> range = ReferenceRange.getAbsoluteRanges(reference);

					myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.WRONG_REF).range(range.get(0)).descriptionAndTooltip
							("Unknown attribute").create());
				}
			}
		}
	}

	@Override
	public void visitGenericParameter(DotNetGenericParameter parameter)
	{
		super.visitGenericParameter(parameter);

		CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, parameter, parameter.getNameIdentifier(), null);

		GenericParameterHighlightUtil.checkInAndOutModifiers(parameter, myHighlightInfoHolder);
	}

	@Override
	public void visitGenericConstraint(CSharpGenericConstraint constraint)
	{
		super.visitGenericConstraint(constraint);

		DotNetGenericParameter resolve = constraint.resolve();

		CSharpReferenceExpression reference = constraint.getGenericParameterReference();
		assert reference != null;

		if(resolve != null)
		{
			CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, resolve, reference, null);
		}
		else
		{
			myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.WRONG_REF).range(reference).create());
		}
	}

	@Override
	public void visitConstructorDeclaration(CSharpConstructorDeclaration declaration)
	{
		super.visitConstructorDeclaration(declaration);

		myHighlightInfoHolder.add(ConstructorHighlightUtil.checkConstructorDeclaration(declaration));
	}

	@Override
	public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
	{
		super.visitTypeDeclaration(declaration);

		CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, declaration, declaration.getNameIdentifier(), null);
	}

	@Override
	public void visitFieldDeclaration(CSharpFieldDeclaration declaration)
	{
		super.visitFieldDeclaration(declaration);

		CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, declaration, declaration.getNameIdentifier(), null);
	}

	@Override
	public void visitLocalVariable(CSharpLocalVariable variable)
	{
		super.visitLocalVariable(variable);

		CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, variable, variable.getNameIdentifier(), null);
	}

	@Override
	public void visitLinqExpression(CSharpLinqExpressionImpl expression)
	{
		super.visitLinqExpression(expression);

		myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(expression).textAttributes(EditorColors
				.INJECTED_LANGUAGE_FRAGMENT).create());
	}

	@Override
	public void visitLinqVariable(CSharpLinqVariable variable)
	{
		super.visitLinqVariable(variable);

		CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, variable, variable.getNameIdentifier(), null);
	}

	@Override
	public void visitEnumConstantDeclaration(CSharpEnumConstantDeclaration declaration)
	{
		super.visitEnumConstantDeclaration(declaration);

		CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, declaration, declaration.getNameIdentifier(), null);
	}

	@Override
	public void visitTypeDefStatement(CSharpTypeDefStatement statement)
	{
		super.visitTypeDefStatement(statement);

		CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, statement, statement.getNameIdentifier(), null);
	}

	@Override
	public void visitParameter(DotNetParameter parameter)
	{
		super.visitParameter(parameter);

		CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, parameter, parameter.getNameIdentifier(), null);
	}

	@Override
	public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
	{
		super.visitPropertyDeclaration(declaration);

		CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, declaration, declaration.getNameIdentifier(), null);
	}

	@Override
	public void visitEventDeclaration(CSharpEventDeclaration declaration)
	{
		super.visitEventDeclaration(declaration);

		CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, declaration, declaration.getNameIdentifier(), null);
	}

	@Override
	public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
	{
		super.visitMethodDeclaration(declaration);
		if(declaration.isDelegate())
		{
			CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, declaration, declaration.getNameIdentifier(), null);
		}
	}

	@Override
	public void visitArrayAccessExpression(CSharpArrayAccessExpressionImpl expression)
	{
		super.visitArrayAccessExpression(expression);
		highlightMaybeImplicit(expression);
	}

	@Override
	public void visitReferenceExpression(CSharpReferenceExpression expression)
	{
		super.visitReferenceExpression(expression);
		PsiElement referenceElement = expression.getReferenceElement();
		if(referenceElement == null || expression.isSoft())
		{
			return;
		}

		highlightResolvedTarget(expression, referenceElement);
	}

	@Override
	public void visitMethodCallExpression(CSharpMethodCallExpressionImpl expression)
	{
		super.visitMethodCallExpression(expression);
		highlightMaybeImplicit(expression);
	}

	@Override
	public void visitOperatorReference(CSharpOperatorReferenceImpl referenceExpression)
	{
		super.visitOperatorReference(referenceExpression);
		highlightMaybeImplicit(referenceExpression);
	}

	private void highlightResolvedTarget(@NotNull PsiReference reference, @NotNull PsiElement referenceElement)
	{
		PsiElement resolved = reference.resolve();

		if(resolved != null)
		{
			HighlightInfo highlightInfo = CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, resolved, referenceElement,
					(PsiElement) reference);

			if(highlightInfo != null && CSharpMethodImplUtil.isExtensionWrapper(resolved))
			{
				QuickFixAction.registerQuickFixAction(highlightInfo, ConvertToNormalCallFix.INSTANCE);
			}
		}
	}

	private void highlightMaybeImplicit(@NotNull CSharpCallArgumentListOwner scope)
	{
		MethodCalcResult methodCalcResult = null;
		ResolveResult[] resolveResults = scope.multiResolve(false);
		ResolveResult firstValidResult = CSharpResolveUtil.findFirstValidResult(resolveResults);
		if(firstValidResult != null)
		{
			if(firstValidResult instanceof MethodResolveResult)
			{
				methodCalcResult = ((MethodResolveResult) firstValidResult).getCalcResult();
			}
		}

		if(methodCalcResult == null)
		{
			return;
		}

		for(NCallArgument nCallArgument : methodCalcResult.getArguments())
		{
			CSharpCallArgument callArgument = nCallArgument.getCallArgument();
			if(callArgument == null)
			{
				continue;
			}
			DotNetExpression argumentExpression = callArgument.getArgumentExpression();
			if(argumentExpression == null)
			{
				continue;
			}
			ImplicitCastInfo implicitCastInfo = nCallArgument.getUserData(ImplicitCastInfo.IMPLICIT_CAST_INFO);
			if(implicitCastInfo != null)
			{
				String text = CSharpErrorBundle.message("impicit.cast.from.0.to.1", CSharpTypeRefPresentationUtil.buildText(implicitCastInfo
						.getFromTypeRef(), scope, CS0029.TYPE_FLAGS), CSharpTypeRefPresentationUtil.buildText(implicitCastInfo
						.getToTypeRef(), scope, CS0029.TYPE_FLAGS));

				HighlightInfo.Builder builder = HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION);
				builder = builder.range(argumentExpression.getTextRange());
				builder = builder.descriptionAndTooltip(text);
				builder = builder.textAttributes(CSharpHighlightKey.IMPLICIT_CAST);
				myHighlightInfoHolder.add(builder.create());
			}
		}
	}

	@Override
	public boolean analyze(@NotNull PsiFile psiFile, boolean b, @NotNull HighlightInfoHolder highlightInfoHolder, @NotNull Runnable runnable)
	{
		myHighlightInfoHolder = highlightInfoHolder;
		runnable.run();
		return true;
	}

	@NotNull
	@Override
	public HighlightVisitor clone()
	{
		return new CSharpHighlightVisitor();
	}

	@Override
	public int order()
	{
		return 0;
	}
}
