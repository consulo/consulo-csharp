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

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.ConvertToNormalCallFix;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpArrayAccessExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpConstructorSuperCallImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMethodCallExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpOperatorReferenceImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDefStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.MethodAcceptorImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.ResolveResultWithWeight;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpMethodImplUtil;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleExtension;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetUserType;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixAction;
import com.intellij.codeInsight.daemon.impl.quickfix.QuickFixActionRegistrarImpl;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.quickfix.UnresolvedReferenceQuickFixProvider;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiPolyVariantReference;
import com.intellij.psi.PsiReference;
import com.intellij.psi.ReferenceRange;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.tree.IElementType;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.JBColor;
import com.intellij.xml.util.XmlStringUtil;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpHighlightVisitor extends CSharpElementVisitor implements HighlightVisitor
{
	public static class ResolveError
	{
		private String description;
		private String tooltip;
		private PsiElement range;

		public ResolveError(String description, String tooltip, PsiElement range)
		{
			this.description = description;
			this.tooltip = tooltip;
			this.range = range;
		}

		@Nullable
		public HighlightInfo create()
		{
			return HighlightInfo.newHighlightInfo(HighlightInfoType.ERROR).description(description).escapedToolTip(tooltip).range(range).create();
		}
	}

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

		if(element instanceof DotNetElement)
		{
			CSharpLanguageVersion languageVersion = CSharpLanguageVersion.HIGHEST;
			CSharpModuleExtension extension = ModuleUtilCore.getExtension(element, CSharpModuleExtension.class);
			if(extension != null)
			{
				languageVersion = extension.getLanguageVersion();
			}

			for(CSharpCompilerChecks classEntry : CSharpCompilerChecks.VALUES)
			{
				if(languageVersion.ordinal() >= classEntry.getLanguageVersion().ordinal() && classEntry.getTargetClass().isAssignableFrom(element
						.getClass()))
				{
					List<CompilerCheck.CompilerCheckResult> results = classEntry.check(languageVersion, element);
					if(results.isEmpty())
					{
						continue;
					}
					for(CompilerCheck.CompilerCheckResult result : results)
					{
						HighlightInfo.Builder builder = HighlightInfo.newHighlightInfo(result.getHighlightInfoType());
						builder = builder.descriptionAndTooltip(result.getText());
						builder = builder.range(result.getTextRange());
						HighlightInfo highlightInfo = builder.create();
						if(highlightInfo != null)
						{
							myHighlightInfoHolder.add(highlightInfo);

							for(IntentionAction intentionAction : result.getQuickFixes())
							{
								QuickFixAction.registerQuickFixAction(highlightInfo, intentionAction);
							}
						}
					}
				}
			}
		}
	}

	@Override
	public void visitGenericParameter(DotNetGenericParameter parameter)
	{
		highlightNamed(parameter, parameter.getNameIdentifier(), null);
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
			highlightNamed(resolve, reference, null);
		}
		else
		{
			myHighlightInfoHolder.add(HighlightInfo.newHighlightInfo(HighlightInfoType.WRONG_REF).range(reference).create());
		}
	}

	@Override
	public void visitTypeDeclaration(CSharpTypeDeclaration declaration)
	{
		super.visitTypeDeclaration(declaration);

		highlightNamed(declaration, declaration.getNameIdentifier(), null);
	}

	@Override
	public void visitFieldDeclaration(DotNetFieldDeclaration declaration)
	{
		super.visitFieldDeclaration(declaration);

		highlightNamed(declaration, declaration.getNameIdentifier(), null);
	}

	@Override
	public void visitEnumConstantDeclaration(CSharpEnumConstantDeclaration declaration)
	{
		super.visitEnumConstantDeclaration(declaration);

		highlightNamed(declaration, declaration.getNameIdentifier(), null);
	}

	@Override
	public void visitTypeDefStatement(CSharpTypeDefStatementImpl statement)
	{
		super.visitTypeDefStatement(statement);

		highlightNamed(statement, statement.getNameIdentifier(), null);
	}

	@Override
	public void visitParameter(DotNetParameter parameter)
	{
		super.visitParameter(parameter);

		highlightNamed(parameter, parameter.getNameIdentifier(), null);
	}

	@Override
	public void visitPropertyDeclaration(CSharpPropertyDeclaration declaration)
	{
		super.visitPropertyDeclaration(declaration);

		highlightNamed(declaration, declaration.getNameIdentifier(), null);
	}

	@Override
	public void visitEventDeclaration(CSharpEventDeclaration declaration)
	{
		super.visitEventDeclaration(declaration);

		highlightNamed(declaration, declaration.getNameIdentifier(), null);
	}

	@Override
	public void visitOperatorReference(CSharpOperatorReferenceImpl referenceExpression)
	{
		highlightCall(referenceExpression, referenceExpression);
	}

	@Override
	public void visitArrayAccessExpression(CSharpArrayAccessExpressionImpl expression)
	{
		super.visitArrayAccessExpression(expression);

		PsiElement resolve = expression.resolve();
		if(resolve == null)
		{
			List<TextRange> absoluteRanges = ReferenceRange.getAbsoluteRanges(expression);

			for(TextRange textRange : absoluteRanges)
			{
				HighlightInfo info = HighlightInfo.newHighlightInfo(HighlightInfoType.WRONG_REF).descriptionAndTooltip("Array method is not " +
						"resolved").range(textRange).create();

				myHighlightInfoHolder.add(info);
			}
		}
	}

	@Override
	public void visitReferenceExpression(CSharpReferenceExpressionImpl expression)
	{
		super.visitReferenceExpression(expression);
		PsiElement referenceElement = expression.getReferenceElement();
		if(referenceElement == null)
		{
			return;
		}

		highlightCall(expression, referenceElement);
	}

	private void highlightCall(@NotNull PsiElement callElement, @NotNull PsiElement referenceElement)
	{
		ResolveResult[] resolveResults = ResolveResult.EMPTY_ARRAY;

		if(callElement instanceof PsiPolyVariantReference)
		{
			resolveResults = ((PsiPolyVariantReference) callElement).multiResolve(false);
		}
		else if(callElement instanceof CSharpOperatorReferenceImpl)
		{
			resolveResults = ((CSharpOperatorReferenceImpl) callElement).multiResolve(false);
		}

		ResolveResult goodResult = resolveResults.length > 0 && ((ResolveResultWithWeight) resolveResults[0]).isGoodResult() ? resolveResults[0] :
				null;

		if(goodResult != null)
		{
			PsiElement element = goodResult.getElement();
			HighlightInfo highlightInfo = highlightNamed(element, referenceElement, callElement);

			if(highlightInfo != null && CSharpMethodImplUtil.isExtensionWrapper(element))
			{
				QuickFixAction.registerQuickFixAction(highlightInfo, ConvertToNormalCallFix.INSTANCE);
			}
		}
		else
		{
			if(resolveResults.length == 0)
			{
				HighlightInfo info = HighlightInfo.newHighlightInfo(HighlightInfoType.WRONG_REF).descriptionAndTooltip("'" + referenceElement
						.getText() + "' is not resolved").range(referenceElement).create();

				myHighlightInfoHolder.add(info);

				if(callElement instanceof PsiReference)
				{
					UnresolvedReferenceQuickFixProvider.registerReferenceFixes((PsiReference) callElement, new QuickFixActionRegistrarImpl(info));
				}
			}
			else
			{
				ResolveError forError = createResolveError(callElement, resolveResults[0].getElement());
				if(forError == null)
				{
					return;
				}

				myHighlightInfoHolder.add(forError.create());
			}
		}
	}

	private static ResolveError createResolveError(@NotNull PsiElement element, @NotNull PsiElement resolveElement)
	{
		CSharpCallArgumentListOwner callOwner = findCallOwner(element);
		if(callOwner != null)
		{
			StringBuilder builder = new StringBuilder();
			builder.append("<b>");
			// sometimes name can be null
			if(element instanceof CSharpOperatorReferenceImpl)
			{
				String canonicalText = ((CSharpOperatorReferenceImpl) element).getCanonicalText();
				builder.append(XmlStringUtil.escapeString(canonicalText));
			}
			else
			{
				String name = ((PsiNamedElement) resolveElement).getName();
				builder.append(name);
			}
			builder.append("&#x9;(");

			List<DotNetTypeRef> typeRefs = new ArrayList<DotNetTypeRef>();
			if(resolveElement instanceof DotNetVariable)
			{
				DotNetTypeRef typeRef = ((DotNetVariable) resolveElement).toTypeRef(false);
				if(!(typeRef instanceof CSharpLambdaTypeRef))
				{
					return null;
				}
				DotNetTypeRef[] parameterTypes = ((CSharpLambdaTypeRef) typeRef).getParameterTypes();
				for(int i = 0; i < parameterTypes.length; i++)
				{
					if(i != 0)
					{
						builder.append(", ");
					}
					DotNetTypeRef parameterType = parameterTypes[i];
					typeRefs.add(parameterType);
					appendType(builder, parameterType);
				}
			}
			else if(resolveElement instanceof DotNetLikeMethodDeclaration)
			{
				DotNetGenericExtractor e = MethodAcceptorImpl.createExtractorFromCall(callOwner, (DotNetGenericParameterListOwner) resolveElement);
				DotNetParameter[] parameters = ((DotNetLikeMethodDeclaration) resolveElement).getParameters();
				for(int i = 0; i < parameters.length; i++)
				{
					if(i != 0)
					{
						builder.append(", ");
					}
					DotNetTypeRef typeRef = MethodAcceptorImpl.calcParameterTypeRef(element, parameters[i], e);
					typeRefs.add(typeRef);
					appendType(builder, typeRef);
				}
			}
			builder.append(")</b> cannot be applied<br>");

			builder.append("to&#x9;<b>(");
			DotNetExpression[] parameterExpressions = callOwner.getParameterExpressions();
			for(int i = 0; i < parameterExpressions.length; i++)
			{
				if(i != 0)
				{
					builder.append(", ");
				}

				DotNetTypeRef requiredType = ArrayUtil2.safeGet(typeRefs, i);
				DotNetTypeRef foundType = parameterExpressions[i].toTypeRef(false);

				boolean isInvalid = requiredType == null || !CSharpTypeUtil.isInheritable(requiredType, foundType, callOwner);

				if(isInvalid)
				{
					builder.append("<font color=\"").append(ColorUtil.toHex(JBColor.RED)).append("\">");
				}
				appendType(builder, foundType);
				if(isInvalid)
				{
					builder.append("</font>");
				}
			}
			builder.append(")</b>");

			PsiElement parameterList = callOwner.getParameterList();
			if(parameterList == null)
			{
				parameterList = callOwner;
			}
			return new ResolveError("", builder.toString(), parameterList);
		}
		return null;
	}

	private static void appendType(StringBuilder builder, DotNetTypeRef typeRef)
	{
		builder.append(XmlStringUtil.escapeString(typeRef.getPresentableText()));
	}

	private static CSharpCallArgumentListOwner findCallOwner(PsiElement element)
	{
		PsiElement parent = element.getParent();
		if(element instanceof CSharpOperatorReferenceImpl)
		{
			return (CSharpCallArgumentListOwner) element;
		}
		else if(parent instanceof CSharpMethodCallExpressionImpl || parent instanceof CSharpConstructorSuperCallImpl || parent instanceof
				CSharpAttribute)
		{
			return (CSharpCallArgumentListOwner) parent;
		}
		else if(parent instanceof DotNetUserType && parent.getParent() instanceof CSharpNewExpression)
		{
			return (CSharpCallArgumentListOwner) parent.getParent();
		}
		return null;
	}

	@Nullable
	public HighlightInfo highlightNamed(@Nullable PsiElement element, @Nullable PsiElement target, @Nullable PsiElement owner)
	{
		return CSharpHighlightUtil.highlightNamed(myHighlightInfoHolder, element, target, owner);
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
