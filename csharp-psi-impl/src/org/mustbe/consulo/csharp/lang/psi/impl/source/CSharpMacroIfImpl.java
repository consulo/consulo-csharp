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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroElementVisitor;
import com.intellij.lang.ASTNode;

/**
 * @author VISTALL
 * @since 26.01.14
 */
public class CSharpMacroIfImpl extends CSharpMacroElementImpl
{
	public CSharpMacroIfImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@NotNull
	public CSharpMacroIfConditionBlockImpl[] getConditionBlocks()
	{
		return findChildrenByClass(CSharpMacroIfConditionBlockImpl.class);
	}

	@Nullable
	public CSharpPreprocessorCloseTagImpl getCloseTag()
	{
		return findChildByClass(CSharpPreprocessorCloseTagImpl.class);
	}

	@Override
	public void accept(@NotNull CSharpMacroElementVisitor visitor)
	{
		visitor.visitMacroIf(this);
	}
}
