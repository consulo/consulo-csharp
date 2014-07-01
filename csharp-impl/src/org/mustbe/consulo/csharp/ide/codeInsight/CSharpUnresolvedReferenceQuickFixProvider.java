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

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.CreateMethodFix;
import org.mustbe.consulo.csharp.ide.codeInsight.actions.UsingNamespaceFix;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpReferenceExpressionImpl;
import com.intellij.codeInsight.daemon.QuickFixActionRegistrar;
import com.intellij.codeInsight.quickfix.UnresolvedReferenceQuickFixProvider;

/**
 * @author VISTALL
 * @since 30.12.13.
 */
public class CSharpUnresolvedReferenceQuickFixProvider extends UnresolvedReferenceQuickFixProvider<CSharpReferenceExpressionImpl>
{
	@Override
	public void registerFixes(CSharpReferenceExpressionImpl cSharpReferenceExpression, QuickFixActionRegistrar quickFixActionRegistrar)
	{
		quickFixActionRegistrar.register(new UsingNamespaceFix(cSharpReferenceExpression));
		quickFixActionRegistrar.register(new CreateMethodFix(cSharpReferenceExpression.getReferenceName()));
	}

	@NotNull
	@Override
	public Class<CSharpReferenceExpressionImpl> getReferenceClass()
	{
		return CSharpReferenceExpressionImpl.class;
	}
}
