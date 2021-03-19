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

package consulo.csharp.ide.highlight;

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.codeInsight.template.impl.TemplateColors;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiUtilCore;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.ConvertToNormalCallFix;
import consulo.csharp.ide.highlight.util.ConstructorHighlightUtil;
import consulo.csharp.ide.highlight.util.GenericParameterHighlightUtil;
import consulo.csharp.impl.localize.CSharpErrorLocalize;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.*;
import consulo.csharp.lang.psi.impl.source.resolve.MethodResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.MethodResolvePriorityInfo;
import consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments.NCallArgument;
import consulo.csharp.lang.psi.impl.source.resolve.operatorResolving.ImplicitCastInfo;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetParameter;
import consulo.localize.LocalizeValue;
import gnu.trove.TIntHashSet;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpHighlightVisitor extends CSharpElementVisitor implements HighlightVisitor
{
	private HighlightInfoHolder myHighlightInfoHolder;
	private TIntHashSet myProcessedLines = new TIntHashSet();
	private Document myDocument;

	@Override
	public boolean suitableForFile(@Nonnull PsiFile psiFile)
	{
		return psiFile instanceof CSharpFile;
	}

	@Override
	@RequiredReadAction
	public void visit(@Nonnull PsiElement element)
	{
		if(element instanceof CSharpPreprocessorReferenceExpressionImpl)
		{
			if(((CSharpPreprocessorReferenceExpressionImpl) element).resolve() != null)
			{
				myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(element).textAttributes(TemplateColors.TEMPLATE_VARIABLE_ATTRIBUTES).create());
			}
			else
			{
				myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(element).textAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT).create());
			}
		}
		else if(element instanceof CSharpPreprocessorDefine)
		{
			if(((CSharpPreprocessorDefine) element).isUnDef())
			{
				PsiElement varElement = ((CSharpPreprocessorDefine) element).getVarElement();
				if(varElement != null)
				{
					myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(varElement).textAttributes(DefaultLanguageHighlighterColors.LINE_COMMENT).create());
				}
			}
			else
			{
				CSharpPreprocessorVariable variable = ((CSharpPreprocessorDefine) element).getVariable();
				if(variable != null)
				{
					myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(variable.getNameIdentifier()).textAttributes(TemplateColors
							.TEMPLATE_VARIABLE_ATTRIBUTES).create());
				}
			}
		}
		else
		{
			element.accept(this);
		}
	}

	@Override
	public void visitElement(PsiElement element)
	{
		ProgressIndicatorProvider.checkCanceled();

		IElementType elementType = PsiUtilCore.getElementType(element);
		if(CSharpSoftTokens.ALL.contains(elementType))
		{
			myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(element).textAttributes(CSharpHighlightKey.SOFT_KEYWORD).create());
		}
		else if(elementType == CSharpTokens.NON_ACTIVE_SYMBOL || elementType == CSharpPreprocessorElements.DISABLED_PREPROCESSOR_DIRECTIVE)
		{
			if(myDocument == null)
			{
				return;
			}
			int lineNumber = myDocument.getLineNumber(element.getTextOffset());
			if(!myProcessedLines.contains(lineNumber))
			{
				myProcessedLines.add(lineNumber);

				TextRange textRange = new TextRange(myDocument.getLineStartOffset(lineNumber), myDocument.getLineEndOffset(lineNumber));
				myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range(textRange).textAttributes(CSharpHighlightKey.DISABLED_BLOCK).create());
			}
		}
	}

	@Override
	@RequiredReadAction
	public void visitIdentifier(CSharpIdentifier identifier)
	{
		PsiElement parent = identifier.getParent();

		CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, parent, identifier, null);
	}

	@Override
	@RequiredReadAction
	public void visitGenericParameter(DotNetGenericParameter parameter)
	{
		super.visitGenericParameter(parameter);

		GenericParameterHighlightUtil.checkInAndOutModifiers(parameter, myHighlightInfoHolder);
	}

	@Override
	@RequiredReadAction
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
	@RequiredReadAction
	public void visitConstructorDeclaration(CSharpConstructorDeclaration declaration)
	{
		super.visitConstructorDeclaration(declaration);

		myHighlightInfoHolder.add(ConstructorHighlightUtil.checkConstructorDeclaration(declaration));
	}

	@Override
	@RequiredReadAction
	public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
	{
		super.visitTypeDeclaration(declaration);
	}

	@Override
	@RequiredReadAction
	public void visitFieldDeclaration(CSharpFieldDeclaration declaration)
	{
		super.visitFieldDeclaration(declaration);
	}

	@Override
	@RequiredReadAction
	public void visitLocalVariable(CSharpLocalVariable variable)
	{
		super.visitLocalVariable(variable);
	}

	@Override
	@RequiredReadAction
	public void visitEnumConstantDeclaration(CSharpEnumConstantDeclaration declaration)
	{
		super.visitEnumConstantDeclaration(declaration);
	}

	@Override
	@RequiredReadAction
	public void visitTypeDefStatement(CSharpTypeDefStatement statement)
	{
		super.visitTypeDefStatement(statement);
	}

	@Override
	@RequiredReadAction
	public void visitParameter(DotNetParameter parameter)
	{
		super.visitParameter(parameter);
	}

	@Override
	@RequiredReadAction
	public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
	{
		super.visitPropertyDeclaration(declaration);
	}

	@Override
	@RequiredReadAction
	public void visitEventDeclaration(CSharpEventDeclaration declaration)
	{
		super.visitEventDeclaration(declaration);
	}

	@Override
	@RequiredReadAction
	public void visitMethodDeclaration(CSharpMethodDeclaration declaration)
	{
		super.visitMethodDeclaration(declaration);
	}

	@Override
	public void visitLinqExpression(CSharpLinqExpressionImpl expression)
	{
		super.visitLinqExpression(expression);

		myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION).range((PsiElement) expression).textAttributes(EditorColors.INJECTED_LANGUAGE_FRAGMENT).create());
	}

	@Override
	@RequiredReadAction
	public void visitLinqVariable(CSharpLinqVariable variable)
	{
		super.visitLinqVariable(variable);

		CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, variable, variable.getNameIdentifier(), null);
	}

	@Override
	@RequiredReadAction
	public void visitIndexAccessExpression(CSharpIndexAccessExpressionImpl expression)
	{
		super.visitIndexAccessExpression(expression);
		highlightMaybeImplicit(expression);
	}

	@Override
	@RequiredReadAction
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
	@RequiredReadAction
	public void visitMethodCallExpression(CSharpMethodCallExpressionImpl expression)
	{
		super.visitMethodCallExpression(expression);
		highlightMaybeImplicit(expression);
	}

	@Override
	@RequiredReadAction
	public void visitOperatorReference(CSharpOperatorReferenceImpl referenceExpression)
	{
		super.visitOperatorReference(referenceExpression);
		highlightMaybeImplicit(referenceExpression);
	}

	@RequiredReadAction
	private void highlightResolvedTarget(@Nonnull PsiReference reference, @Nonnull PsiElement referenceElement)
	{
		PsiElement resolved = reference.resolve();

		if(resolved != null)
		{
			HighlightInfo highlightInfo = CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, resolved, referenceElement, (PsiElement) reference);

			if(highlightInfo != null && CSharpMethodImplUtil.isExtensionWrapper(resolved))
			{
				QuickFixAction.registerQuickFixAction(highlightInfo, ConvertToNormalCallFix.INSTANCE);
			}
		}
	}

	@RequiredReadAction
	private void highlightMaybeImplicit(@Nonnull CSharpCallArgumentListOwner scope)
	{
		MethodResolvePriorityInfo methodCalcResult = null;
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
				LocalizeValue text = CSharpErrorLocalize.impicitCastFrom0To1(CSharpTypeRefPresentationUtil.buildTextWithKeyword(implicitCastInfo.getFromTypeRef()), CSharpTypeRefPresentationUtil
						.buildTextWithKeyword(implicitCastInfo.getToTypeRef()));

				HighlightInfo.Builder builder = HighlightInfo.newHighlightInfo(HighlightInfoType.INFORMATION);
				builder = builder.range(argumentExpression.getTextRange());
				builder = builder.descriptionAndTooltip(text);
				builder = builder.textAttributes(CSharpHighlightKey.IMPLICIT_OR_EXPLICIT_CAST);
				myHighlightInfoHolder.add(builder.create());
			}
		}
	}

	@Override
	public boolean analyze(@Nonnull PsiFile psiFile, boolean b, @Nonnull HighlightInfoHolder highlightInfoHolder, @Nonnull Runnable runnable)
	{
		myHighlightInfoHolder = highlightInfoHolder;
		myProcessedLines.clear();
		myDocument = PsiDocumentManager.getInstance(psiFile.getProject()).getCachedDocument(psiFile);
		runnable.run();
		myDocument = null;
		return true;
	}

	@Nonnull
	@Override
	public HighlightVisitor clone()
	{
		return new CSharpHighlightVisitor();
	}

	@Override
	public int order()
	{
		return 1;
	}
}
