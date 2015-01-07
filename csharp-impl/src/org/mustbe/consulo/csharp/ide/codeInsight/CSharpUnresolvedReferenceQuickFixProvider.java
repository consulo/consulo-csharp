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

package org.mustbe.consulo.csharp.ide.codeInsight;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.CreateUnresolvedEventFix;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.CreateUnresolvedFieldFix;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.CreateUnresolvedMethodByLambdaTypeFix;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.CreateUnresolvedMethodFix;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.CreateUnresolvedPropertyFix;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.UsingNamespaceFix;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeInfo;
import org.mustbe.consulo.csharp.ide.completion.expected.ExpectedTypeRefProvider;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleLikeMethod;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaResolveResult;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.codeInsight.daemon.QuickFixActionRegistrar;
import com.intellij.codeInsight.quickfix.UnresolvedReferenceQuickFixProvider;

/**
 * @author VISTALL
 * @since 30.12.13.
 */
public class CSharpUnresolvedReferenceQuickFixProvider extends UnresolvedReferenceQuickFixProvider<CSharpReferenceExpression>
{
	@Override
	public void registerFixes(CSharpReferenceExpression expression, QuickFixActionRegistrar quickFixActionRegistrar)
	{
		quickFixActionRegistrar.register(new UsingNamespaceFix(expression));
		quickFixActionRegistrar.register(new CreateUnresolvedMethodFix(expression));
		quickFixActionRegistrar.register(new CreateUnresolvedFieldFix(expression));
		quickFixActionRegistrar.register(new CreateUnresolvedPropertyFix(expression));
		quickFixActionRegistrar.register(new CreateUnresolvedEventFix(expression));

		List<ExpectedTypeInfo> expectedTypeRefs = ExpectedTypeRefProvider.findExpectedTypeRefs(expression);
		for(ExpectedTypeInfo expectedTypeRef : expectedTypeRefs)
		{
			DotNetTypeRef typeRef = expectedTypeRef.getTypeRef();
			DotNetTypeResolveResult result = typeRef.resolve(expression);
			if(result instanceof CSharpLambdaResolveResult)
			{
				quickFixActionRegistrar.register(new CreateUnresolvedMethodByLambdaTypeFix(expression, (CSharpSimpleLikeMethod) result));
			}
		}
	}

	@NotNull
	@Override
	public Class<CSharpReferenceExpression> getReferenceClass()
	{
		return CSharpReferenceExpression.class;
	}
}
