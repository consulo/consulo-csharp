/*
 * Copyright 2013-2026 consulo.io
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

package consulo.csharp.it.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.source.CSharpExpressionWithOperatorImpl;
import consulo.csharp.lang.impl.psi.source.CSharpOperatorReferenceImpl;
import consulo.csharp.lang.impl.psi.source.resolve.MethodResolveResult;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.util.PsiTreeUtil;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;

/**
 * One line per operator expression: {@code text => type | operator}, where the operator is the one
 * {@link CSharpOperatorReferenceImpl#resolve()} picks, as {@code op(parameter types): return type}. When several
 * distinct operators tie at the best weight the line ends with {@code ambiguous(n)}.
 * <p>
 * Who declares the operator is left out - an operator stub on one SDK, a generic math interface on another - unless
 * {@code withOwner} asks for it, which is for diagnostics.
 *
 * @author VISTALL
 */
public final class OperatorTypesPrinter
{
	private static final int TYPE_FLAGS = CSharpTypeRefPresentationUtil.QUALIFIED_NAME | CSharpTypeRefPresentationUtil.FORCE_TYPE_KEYWORD;

	private OperatorTypesPrinter()
	{
	}

	@RequiredReadAction
	public static String print(PsiFile file, boolean withOwner)
	{
		StringBuilder builder = new StringBuilder();
		for(CSharpExpressionWithOperatorImpl expression : PsiTreeUtil.findChildrenOfType(file, CSharpExpressionWithOperatorImpl.class))
		{
			CSharpOperatorReferenceImpl operator = expression.getOperatorElement();

			builder.append(expression.getText()).append(" => ").append(type(expression.toTypeRef(true)));
			builder.append(" | ").append(operator(operator.resolve(), withOwner));

			// many operators are applicable (int + int can take long + long too) - ambiguous is a tie at the best weight
			Set<PsiElement> best = new LinkedHashSet<>();
			int bestWeight = Integer.MIN_VALUE;
			for(ResolveResult result : operator.multiResolve(false))
			{
				if(!(result instanceof MethodResolveResult methodResult) || !result.isValidResult() || result.getElement() == null)
				{
					continue;
				}
				int weight = methodResult.getCalcResult().getWeight();
				if(weight > bestWeight)
				{
					bestWeight = weight;
					best.clear();
				}
				if(weight == bestWeight)
				{
					best.add(result.getElement());
				}
			}
			if(best.size() > 1)
			{
				builder.append(" | ambiguous(").append(best.size());
				if(withOwner)
				{
					for(PsiElement element : best)
					{
						builder.append(" ; ").append(operator(element, true));
					}
				}
				builder.append(")");
			}
			builder.append("\n");
		}
		return builder.toString().trim();
	}

	@RequiredReadAction
	private static String operator(PsiElement element, boolean withOwner)
	{
		if(element == null)
		{
			return "unresolved";
		}

		if(!(element instanceof DotNetLikeMethodDeclaration method))
		{
			return element.getClass().getSimpleName();
		}

		StringJoiner parameters = new StringJoiner(", ", "(", ")");
		for(DotNetTypeRef parameterTypeRef : method.getParameterTypeRefs())
		{
			parameters.add(type(parameterTypeRef));
		}

		String signature = method.getName() + parameters + ": " + type(method.getReturnTypeRef());
		if(!withOwner)
		{
			return signature;
		}
		PsiElement parent = method.getParent();
		String owner = parent instanceof DotNetTypeDeclaration typeDeclaration ? typeDeclaration.getVmQName() : "?";
		// an explicit interface implementation is no operator of the type for the C# lookup
		boolean explicit = method instanceof DotNetVirtualImplementOwner implementOwner && implementOwner.getTypeForImplement() != null;
		return owner + "." + signature + (explicit ? " [explicit]" : "");
	}

	@RequiredReadAction
	private static String type(DotNetTypeRef typeRef)
	{
		return CSharpTypeRefPresentationUtil.buildText(typeRef, TYPE_FLAGS);
	}
}
