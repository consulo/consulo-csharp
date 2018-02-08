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

package consulo.csharp.lang.psi.impl.light;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ui.configuration.ProjectSettingsService;
import com.intellij.openapi.util.Comparing;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.IncorrectOperationException;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.lang.CSharpPreprocessorLanguage;
import consulo.csharp.lang.psi.CSharpPreprocessorVariable;

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

	public CSharpPreprocessorLightVariable(@Nullable Module module, @NotNull PsiElement target, @NotNull String name)
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
			ProjectSettingsService.getInstance(getProject()).openContentEntriesSettings(myModule);
		}
	}

	@Override
	public boolean isValid()
	{
		return myTarget.isValid();
	}

	@RequiredWriteAction
	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
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