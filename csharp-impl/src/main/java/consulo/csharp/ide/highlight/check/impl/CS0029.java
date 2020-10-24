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

package consulo.csharp.ide.highlight.check.impl;

import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ObjectUtil;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.AddModifierFix;
import consulo.csharp.ide.codeInsight.actions.CastExpressionToTypeRef;
import consulo.csharp.ide.codeInsight.actions.ChangeReturnToTypeRefFix;
import consulo.csharp.ide.codeInsight.actions.ChangeVariableToTypeRefFix;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.CSharpHighlightKey;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.impl.localize.CSharpErrorLocalize;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.CSharpImplicitReturnModel;
import consulo.csharp.lang.psi.impl.CSharpInheritableChecker;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.source.*;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpPointerTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpModuleUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefUtil;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public class CS0029 extends CompilerCheck<PsiElement>
{
	private static class CheckTarget
	{
		private List<DotNetTypeRef> myExpectedTypeRefs = new ArrayList<>();

		private DotNetTypeRef myActualTypeRef;

		private PsiElement myTarget;

		private CheckTarget expect(DotNetTypeRef typeRef)
		{
			myExpectedTypeRefs.add(typeRef);
			return this;
		}

		private CheckTarget actual(DotNetTypeRef typeRef)
		{
			myActualTypeRef = typeRef;
			return this;
		}

		private CheckTarget target(PsiElement target)
		{
			myTarget = target;
			return this;
		}

		private void check()
		{
			if(myExpectedTypeRefs.isEmpty() || myActualTypeRef == null || myTarget == null)
			{
				throw new IllegalArgumentException("not full data");
			}
		}
	}

	private static final String[] ourNewArrayLenghtTypes = new String[]{
			DotNetTypes.System.Int32,
			// set int32 as default type for new array lenght
			DotNetTypes.System.Byte,
			DotNetTypes.System.SByte,
			DotNetTypes.System.Int16,
			DotNetTypes.System.UInt16,
			DotNetTypes.System.UInt32,
			DotNetTypes.System.Int64,
			DotNetTypes.System.UInt64,
	};

	@Nonnull
	private static CheckTarget target(@Nonnull DotNetTypeRef expected, @Nonnull DotNetTypeRef actual, @Nonnull PsiElement target)
	{
		return new CheckTarget().expect(expected).actual(actual).target(target);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CompilerCheckBuilder checkImpl(@Nonnull CSharpLanguageVersion languageVersion, @Nonnull CSharpHighlightContext highlightContext, @Nonnull PsiElement element)
	{
		CheckTarget target = resolve(element);
		if(target == null)
		{
			return null;
		}

		target.check();

		PsiElement elementToHighlight = target.myTarget;

		DotNetTypeRef actualTypeRef = target.myActualTypeRef;
		if(CSharpTypeUtil.isErrorTypeRef(actualTypeRef))
		{
			return null;
		}

		List<DotNetTypeRef> expectedTypeRefs = target.myExpectedTypeRefs;
		for(DotNetTypeRef expectedTypeRef : expectedTypeRefs)
		{
			if(CSharpTypeUtil.isErrorTypeRef(expectedTypeRef))
			{
				return null;
			}
		}

		CSharpTypeUtil.InheritResult successResult = null;
		DotNetTypeRef conversionResultTypeRef = null;

		for(DotNetTypeRef expectedTypeRef : expectedTypeRefs)
		{
			CSharpTypeUtil.InheritResult result = CSharpInheritableChecker.create(expectedTypeRef, actualTypeRef).withCastType(CSharpCastType.IMPLICIT, element.getResolveScope()).check();

			if(result.isSuccess())
			{
				successResult = result;
				break;
			}

			if(result.isConversion())
			{
				conversionResultTypeRef = expectedTypeRef;
			}
		}

		if(successResult == null)
		{
			DotNetTypeRef firstExpectedTypeRef = ContainerUtil.getFirstItem(expectedTypeRefs);

			CompilerCheckBuilder builder = newBuilder(elementToHighlight, formatTypeRef(actualTypeRef), formatTypeRef(firstExpectedTypeRef));

			if(elementToHighlight instanceof DotNetExpression)
			{
				builder.addQuickFix(new CastExpressionToTypeRef((DotNetExpression) elementToHighlight, firstExpectedTypeRef));
			}

			if(element instanceof DotNetVariable)
			{
				builder.addQuickFix(new ChangeVariableToTypeRefFix((DotNetVariable) element, actualTypeRef));
			}

			if(element instanceof CSharpReturnStatementImpl)
			{
				CSharpSimpleLikeMethodAsElement methodElement = PsiTreeUtil.getParentOfType(element, CSharpSimpleLikeMethodAsElement.class);
				if(methodElement instanceof CSharpConversionMethodDeclaration || methodElement instanceof CSharpMethodDeclaration)
				{
					builder.addQuickFix(new ChangeReturnToTypeRefFix((DotNetLikeMethodDeclaration) methodElement, actualTypeRef));
				}

				if(CSharpModuleUtil.findLanguageVersion(element).isAtLeast(CSharpLanguageVersion._4_0))
				{
					DotNetTypeResolveResult typeResolveResult = firstExpectedTypeRef.resolve();
					PsiElement firstElement = typeResolveResult.getElement();
					if(firstElement instanceof CSharpTypeDeclaration && DotNetTypes.System.Threading.Tasks.Task$1.equals(((CSharpTypeDeclaration) firstElement).getVmQName()))
					{
						DotNetGenericParameter[] genericParameters = ((CSharpTypeDeclaration) firstElement).getGenericParameters();
						DotNetTypeRef genericParameterTypeRef = typeResolveResult.getGenericExtractor().extract(genericParameters[0]);
						if(genericParameterTypeRef != null && CSharpTypeUtil.isInheritable(genericParameterTypeRef, actualTypeRef))
						{
							builder.addQuickFix(new AddModifierFix(CSharpModifier.ASYNC, methodElement));
						}
					}
				}
			}
			return builder;
		}
		else if(conversionResultTypeRef != null)
		{
			String text = CSharpErrorLocalize.impicitCastFrom0To1(CSharpTypeRefPresentationUtil.buildTextWithKeywordAndNull(actualTypeRef),
					CSharpTypeRefPresentationUtil.buildTextWithKeywordAndNull(conversionResultTypeRef)).getValue();

			return newBuilder(elementToHighlight).setText(text).setHighlightInfoType(HighlightInfoType.INFORMATION).setTextAttributesKey(CSharpHighlightKey.IMPLICIT_OR_EXPLICIT_CAST);
		}

		return null;
	}

	@RequiredReadAction
	private CheckTarget resolve(PsiElement element)
	{
		if(element instanceof DotNetVariable)
		{
			DotNetExpression initializer = ((DotNetVariable) element).getInitializer();
			if(initializer == null)
			{
				return null;
			}
			DotNetTypeRef variableTypRef = ((DotNetVariable) element).toTypeRef(false);
			if(variableTypRef == DotNetTypeRef.AUTO_TYPE)
			{
				return null;
			}
			DotNetTypeRef initializerTypeRef = initializer.toTypeRef(true);

			PsiElement parent = element.getParent();
			if(parent instanceof CSharpFixedStatementImpl)
			{
				if(initializerTypeRef instanceof CSharpArrayTypeRef)
				{
					if(((CSharpArrayTypeRef) initializerTypeRef).getDimensions() == 0)
					{
						DotNetTypeRef innerTypeRef = ((CSharpArrayTypeRef) initializerTypeRef).getInnerTypeRef();
						initializerTypeRef = new CSharpPointerTypeRef(innerTypeRef);
					}
				}
			}
			return target(variableTypRef, initializerTypeRef, initializer);
		}
		else if(element instanceof DotNetExpression && element.getParent() instanceof CSharpMethodDeclaration)
		{
			CSharpMethodDeclaration parent = (CSharpMethodDeclaration) element.getParent();
			assert parent.getCodeBlock() == element;

			CSharpImplicitReturnModel model = CSharpImplicitReturnModel.None;
			if(parent.hasModifier(CSharpModifier.ASYNC))
			{
				model = CSharpImplicitReturnModel.Async;
			}

			DotNetTypeRef returnTypeRef = model.extractTypeRef(parent.getReturnTypeRef(), element);
			return target(returnTypeRef, ((DotNetExpression) element).toTypeRef(true), element);
		}
		else if(element instanceof CSharpAssignmentExpressionImpl)
		{
			CSharpOperatorReferenceImpl operatorElement = ((CSharpAssignmentExpressionImpl) element).getOperatorElement();
			if(operatorElement.getOperatorElementType() != CSharpTokens.EQ)
			{
				return null;
			}
			DotNetExpression[] expressions = ((CSharpAssignmentExpressionImpl) element).getParameterExpressions();
			if(expressions.length != 2)
			{
				return null;
			}
			return target(expressions[0].toTypeRef(true), expressions[1].toTypeRef(true), expressions[1]);
		}
		else if(element instanceof CSharpWhileStatementImpl)
		{
			DotNetExpression conditionExpression = ((CSharpWhileStatementImpl) element).getConditionExpression();
			if(conditionExpression == null)
			{
				return null;
			}
			return target(new CSharpTypeRefByQName(element, DotNetTypes.System.Boolean), conditionExpression.toTypeRef(true), conditionExpression);
		}
		else if(element instanceof CSharpDoWhileStatementImpl)
		{
			DotNetExpression conditionExpression = ((CSharpDoWhileStatementImpl) element).getConditionExpression();
			if(conditionExpression == null)
			{
				return null;
			}
			return target(new CSharpTypeRefByQName(element, DotNetTypes.System.Boolean), conditionExpression.toTypeRef(true), conditionExpression);
		}
		else if(element instanceof CSharpIfStatementImpl)
		{
			DotNetExpression conditionExpression = ((CSharpIfStatementImpl) element).getConditionExpression();
			if(conditionExpression == null)
			{
				return null;
			}
			return target(new CSharpTypeRefByQName(element, DotNetTypes.System.Boolean), conditionExpression.toTypeRef(true), conditionExpression);
		}
		else if(element instanceof CSharpRefTypeExpressionImpl)
		{
			DotNetExpression expression = ((CSharpRefTypeExpressionImpl) element).getExpression();
			if(expression == null)
			{
				return null;
			}
			return target(new CSharpTypeRefByQName(element, DotNetTypes.System.TypedReference), expression.toTypeRef(true), expression);
		}
		else if(element instanceof CSharpCaseOrDefaultStatementImpl)
		{
			DotNetExpression expression = ((CSharpCaseOrDefaultStatementImpl) element).getExpression();
			if(expression == null)
			{
				return null;
			}

			PsiElement parent = element.getParent().getParent();
			if(!(parent instanceof CSharpSwitchStatementImpl))
			{
				return null;
			}
			DotNetExpression switchExpression = ((CSharpSwitchStatementImpl) parent).getExpression();
			if(switchExpression == null)
			{
				return null;
			}
			return target(switchExpression.toTypeRef(true), expression.toTypeRef(true), expression);
		}
		else if(element instanceof CSharpRefValueExpressionImpl)
		{
			DotNetExpression expression = ((CSharpRefValueExpressionImpl) element).getExpression();
			if(expression == null)
			{
				return null;
			}
			return target(new CSharpTypeRefByQName(element, DotNetTypes.System.TypedReference), expression.toTypeRef(true), expression);
		}
		else if(element instanceof CSharpLambdaExpressionImpl)
		{
			CSharpLambdaExpressionImpl lambdaExpression = (CSharpLambdaExpressionImpl) element;
			DotNetTypeRef typeRefOfLambda = lambdaExpression.toTypeRef(true);
			if(typeRefOfLambda == DotNetTypeRef.ERROR_TYPE)
			{
				return null;
			}

			DotNetTypeResolveResult typeResolveResult = typeRefOfLambda.resolve();
			if(!(typeResolveResult instanceof CSharpLambdaResolveResult))
			{
				return null;
			}
			DotNetExpression singleExpression = lambdaExpression.getCodeBlock().asExpression();
			if(singleExpression != null)
			{
				DotNetTypeRef returnTypeRef = ((CSharpLambdaResolveResult) typeResolveResult).getReturnTypeRef();

				CSharpImplicitReturnModel model = CSharpImplicitReturnModel.None;
				if(lambdaExpression.hasModifier(CSharpModifier.ASYNC))
				{
					model = CSharpImplicitReturnModel.Async;
				}

				returnTypeRef = model.extractTypeRef(returnTypeRef, element);

				// void type allow any type if used expression body
				if(DotNetTypeRefUtil.isVmQNameEqual(returnTypeRef, DotNetTypes.System.Void))
				{
					return null;
				}
				return target(returnTypeRef, ((DotNetExpression) singleExpression).toTypeRef(true), singleExpression);
			}
		}
		else if(element instanceof CSharpNamedFieldOrPropertySetImpl)
		{
			CSharpReferenceExpression nameElement = ((CSharpNamedFieldOrPropertySetImpl) element).getNameElement();
			DotNetExpression valueExpression = ((CSharpNamedFieldOrPropertySetImpl) element).getValueExpression();
			if(valueExpression == null)
			{
				return null;
			}
			return target(nameElement.toTypeRef(true), valueExpression.toTypeRef(true), valueExpression);
		}
		else if(element instanceof CSharpReturnStatementImpl)
		{
			CSharpSimpleLikeMethodAsElement pseudoMethod = PsiTreeUtil.getParentOfType(element, CSharpSimpleLikeMethodAsElement.class);
			if(pseudoMethod == null)
			{
				return null;
			}

			DotNetTypeRef expected = pseudoMethod.getReturnTypeRef();
			if(expected == DotNetTypeRef.UNKNOWN_TYPE)
			{
				return null;
			}

			DotNetTypeRef actual;
			DotNetExpression expression = ((CSharpReturnStatementImpl) element).getExpression();
			if(expression == null)
			{
				actual = new CSharpTypeRefByQName(element, DotNetTypes.System.Void);
			}
			else
			{
				actual = expression.toTypeRef(true);
			}

			CSharpImplicitReturnModel implicitReturnModel = CSharpImplicitReturnModel.getImplicitReturnModel((CSharpReturnStatementImpl) element, pseudoMethod);
			DotNetTypeRef typeRef = implicitReturnModel.extractTypeRef(expected, element);
			if(typeRef != DotNetTypeRef.ERROR_TYPE)
			{
				expected = typeRef;
			}
			return target(expected, actual, ObjectUtil.notNull(expression, element));
		}

		PsiElement parent = element.getParent();
		if(element instanceof DotNetExpression && parent instanceof CSharpNewArrayLengthImpl)
		{
			CheckTarget target = new CheckTarget();
			target.target(element);
			target.actual(((DotNetExpression) element).toTypeRef(true));
			for(String newArrayLenghtType : ourNewArrayLenghtTypes)
			{
				target.expect(new CSharpTypeRefByQName(element, newArrayLenghtType));
			}
			return target;
		}
		return null;
	}
}
