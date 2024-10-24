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

package consulo.csharp.lang.impl.psi.source;

import consulo.language.psi.PsiElement;
import consulo.language.ast.IElementType;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpMacroElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpPreprocesorTokens;
import consulo.csharp.lang.psi.CSharpPreprocessorDefine;
import consulo.csharp.lang.psi.CSharpPreprocessorVariable;
import consulo.csharp.lang.impl.psi.light.CSharpPreprocessorLightVariable;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 18.12.13.
 */
public class CSharpPreprocessorDefineImpl extends CSharpPreprocessorElementImpl implements CSharpPreprocessorDefine
{
	public CSharpPreprocessorDefineImpl(IElementType type)
	{
		super(type);
	}

	@Override
	public void accept(@Nonnull CSharpMacroElementVisitor visitor)
	{
		visitor.visitMacroDefine(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getVarName()
	{
		PsiElement nameIdentifier = getVarElement();
		return nameIdentifier != null ? nameIdentifier.getText() : null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getVarElement()
	{
		return findPsiChildByType(CSharpPreprocesorTokens.IDENTIFIER);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public CSharpPreprocessorVariable getVariable()
	{
		if(isUnDef())
		{
			return null;
		}

		PsiElement nameIdentifier = getVarElement();
		if(nameIdentifier == null)
		{
			return null;
		}
		return new CSharpPreprocessorLightVariable(null, nameIdentifier, getVarName());
	}

	@RequiredReadAction
	@Override
	public boolean isUnDef()
	{
		return findChildByType(CSharpPreprocesorTokens.MACRO_UNDEF_KEYWORD) != null;
	}
}
