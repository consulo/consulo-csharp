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

package consulo.csharp.lang.impl.psi.light;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.CSharpPreprocessorLanguage;
import consulo.csharp.lang.psi.CSharpPreprocessorVariable;
import consulo.ide.setting.ShowSettingsUtil;
import consulo.language.impl.psi.LightElement;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.util.IncorrectOperationException;
import consulo.module.Module;
import consulo.navigation.Navigatable;
import consulo.util.lang.Comparing;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 18-May-17
 */
public class CSharpPreprocessorLightVariable extends LightElement implements CSharpPreprocessorVariable
{
	@Nullable
	private final Module myModule;
	private final PsiElement myTarget;
	private final String myName;

	public CSharpPreprocessorLightVariable(@Nullable Module module, @Nonnull PsiElement target, @Nonnull String name)
	{
		super(PsiManager.getInstance(target.getProject()), CSharpPreprocessorLanguage.INSTANCE);
		myModule = module;
		myTarget = target;
		myName = name;
	}

	@Override
	public int getTextOffset()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? super.getTextOffset() : nameIdentifier.getTextOffset();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return myTarget;
	}

	@Override
	public PsiFile getContainingFile()
	{
		return myTarget.getContainingFile();
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		return another instanceof CSharpPreprocessorVariable && Comparing.equal(myName, ((CSharpPreprocessorVariable) another).getName());
	}

	@RequiredReadAction
	@Override
	public String getName()
	{
		return myName;
	}

	@Override
	public String toString()
	{
		return myName;
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
	public void navigate(boolean requestFocus)
	{
		if(myModule == null)
		{
			((Navigatable) myTarget).navigate(requestFocus);
		}
		else
		{
			ShowSettingsUtil.getInstance().showProjectStructureDialog(myModule.getProject(), projectStructureSelector -> projectStructureSelector.select(myModule, true));
		}
	}

	@Override
	public boolean isValid()
	{
		return myTarget.isValid();
	}

	@RequiredWriteAction
	@Override
	public PsiElement setName(@NonNls @Nonnull String name) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public boolean isGlobal()
	{
		return myModule != null;
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof PsiElement && isEquivalentTo((PsiElement) obj);
	}
}
