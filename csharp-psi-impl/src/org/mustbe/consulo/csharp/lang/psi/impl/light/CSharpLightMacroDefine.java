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

package org.mustbe.consulo.csharp.lang.psi.impl.light;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpMacroLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroDefine;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 26.01.14
 */
public class CSharpLightMacroDefine extends LightElement implements CSharpMacroDefine
{
	@NotNull
	private final Module myModule;
	private String myVariableName;

	public CSharpLightMacroDefine(@NotNull Module module, @NotNull String variableName)
	{
		super(PsiManager.getInstance(module.getProject()), CSharpMacroLanguage.INSTANCE);
		myModule = module;

		myVariableName = variableName;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public void navigate(boolean requestFocus)
	{
		ProjectSettingsService.getInstance(getProject()).openContentEntriesSettings(myModule);
	}

	@NotNull
	@Override
	public PsiElement getNavigationElement()
	{
		return this;
	}

	@Override
	public boolean canNavigate()
	{
		return true;
	}

	@Override
	public boolean canNavigateToSource()
	{
		return true;
	}

	@Override
	public String toString()
	{
		return  "MacroDefine: " + getName();
	}

	@Override
	public String getName()
	{
		return myVariableName;
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@Override
	public boolean isUnDef()
	{
		return false;
	}
}
