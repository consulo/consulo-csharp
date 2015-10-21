/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorElementVisitor;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class CSharpPreprocessorUndefDirectiveImpl extends CSharpPreprocessorElementImpl
{
	public CSharpPreprocessorUndefDirectiveImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpPreprocessorElementVisitor visitor)
	{
		visitor.visitUndefDirective(this);
	}

	@Nullable
	@RequiredReadAction
	public String getVariable()
	{
		CSharpPreprocessorReferenceExpressionImpl expression = findChildByClass(CSharpPreprocessorReferenceExpressionImpl.class);
		return expression != null ? expression.getText() : null;
	}
}
