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

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroDefine;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.impl.light.CSharpLightMacroDefine;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import lombok.val;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public class CSharpMacroReferenceExpressionImpl extends CSharpMacroElementImpl implements CSharpMacroExpression, PsiReference
{
	public CSharpMacroReferenceExpressionImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public PsiReference getReference()
	{
		return this;
	}

	@Override
	public void accept(@NotNull CSharpMacroElementVisitor visitor)
	{
		visitor.visitReferenceExpression(this);
	}

	@Override
	public PsiElement getElement()
	{
		return this;
	}

	@Override
	public TextRange getRangeInElement()
	{
		PsiElement element = getElement();
		return new TextRange(0, element.getTextLength());
	}

	@Nullable
	@Override
	public PsiElement resolve()
	{
		PsiFile containingFile = getContainingFile();
		if(!(containingFile instanceof CSharpMacroFileImpl))
		{
			return null;
		}

		val text = getText();

		Map<String, CSharpMacroDefine> map = new HashMap<String, CSharpMacroDefine>();

		DotNetSimpleModuleExtension<?> extension = ModuleUtilCore.getExtension(containingFile, DotNetSimpleModuleExtension.class);
		if(extension != null)
		{
			for(String var : extension.getVariables())
			{
				map.put(var, new CSharpLightMacroDefine(extension.getModule(), text));
			}
		}

		for(CSharpMacroDefine macroDefine : ((CSharpMacroFileImpl) containingFile).getDefines())
		{
			String name = macroDefine.getName();
			if(name == null)
			{
				continue;
			}

			if(macroDefine.isUnDef())
			{
				map.remove(name);
			}
			else
			{
				map.put(name, macroDefine);
			}
		}

		return map.get(text);
	}

	@NotNull
	@Override
	public String getCanonicalText()
	{
		return getText();
	}

	@Override
	public PsiElement handleElementRename(String s) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public boolean isReferenceTo(PsiElement element)
	{
		return resolve() == element;
	}

	@NotNull
	@Override
	public Object[] getVariants()
	{
		return ArrayUtil.EMPTY_OBJECT_ARRAY;
	}

	@Override
	public boolean isSoft()
	{
		return true;
	}
}
