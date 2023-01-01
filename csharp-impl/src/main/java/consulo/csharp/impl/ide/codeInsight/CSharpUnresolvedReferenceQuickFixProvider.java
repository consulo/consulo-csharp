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

package consulo.csharp.impl.ide.codeInsight;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.impl.ide.codeInsight.actions.*;
import consulo.csharp.impl.ide.completion.expected.ExpectedTypeInfo;
import consulo.csharp.impl.ide.completion.expected.ExpectedTypeVisitor;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.CSharpReferenceExpression;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.editor.intention.QuickFixActionRegistrar;
import consulo.language.editor.intention.UnresolvedReferenceQuickFixProvider;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 30.12.13.
 */
@ExtensionImpl
public class CSharpUnresolvedReferenceQuickFixProvider extends UnresolvedReferenceQuickFixProvider<CSharpReferenceExpression>
{
	@Override
	@RequiredReadAction
	public void registerFixes(CSharpReferenceExpression expression, QuickFixActionRegistrar quickFixActionRegistrar)
	{
		if(UsingNamespaceFix.isValidReference(expression.kind(), expression))
		{
			quickFixActionRegistrar.register(new UsingNamespaceFix(expression));
		}

		CSharpLambdaResolveResult lambdaResolveResult = null;
		List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeVisitor.findExpectedTypeRefs(expression);
		for(ExpectedTypeInfo expectedTypeRef : expectedTypeRefs)
		{
			DotNetTypeRef typeRef = expectedTypeRef.getTypeRef();
			DotNetTypeResolveResult result = typeRef.resolve();
			if(result instanceof CSharpLambdaResolveResult)
			{
				lambdaResolveResult = (CSharpLambdaResolveResult) result;
				break;
			}
		}

		if(lambdaResolveResult != null)
		{
			quickFixActionRegistrar.register(new CreateUnresolvedMethodByLambdaTypeFix(expression, lambdaResolveResult));
		}
		else
		{
			quickFixActionRegistrar.register(new CreateUnresolvedMethodFix(expression));
			quickFixActionRegistrar.register(new CreateUnresolvedFieldFix(expression));
			quickFixActionRegistrar.register(new CreateUnresolvedPropertyFix(expression));
			quickFixActionRegistrar.register(new CreateUnresolvedEventFix(expression));
		}
	}

	@Nonnull
	@Override
	public Class<CSharpReferenceExpression> getReferenceClass()
	{
		return CSharpReferenceExpression.class;
	}
}
