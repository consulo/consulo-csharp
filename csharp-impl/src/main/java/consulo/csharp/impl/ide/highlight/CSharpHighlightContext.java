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

package consulo.csharp.impl.ide.highlight;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.NullableLazyValue;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.language.psi.PsiFile;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.util.dataholder.UserDataHolderBase;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 17.04.2016
 */
public class CSharpHighlightContext extends UserDataHolderBase
{
	private NullableLazyValue<Module> myModuleValue = new NullableLazyValue<consulo.module.Module>()
	{
		@Nullable
		@Override
		@RequiredReadAction
		protected Module compute()
		{
			return ModuleUtilCore.findModuleForPsiElement(myFile);
		}
	};

	private NullableLazyValue<DotNetSimpleModuleExtension<?>> myDotNetModuleExtensionValue = new NullableLazyValue<DotNetSimpleModuleExtension<?>>()
	{
		@Nullable
		@Override
		protected DotNetSimpleModuleExtension<?> compute()
		{
			Module value = myModuleValue.getValue();
			if(value == null)
			{
				return null;
			}
			return ModuleUtilCore.getExtension(value, DotNetSimpleModuleExtension.class);
		}
	};

	private NullableLazyValue<CSharpSimpleModuleExtension<?>> myCSharpModuleExtensionValue = new NullableLazyValue<CSharpSimpleModuleExtension<?>>()
	{
		@Nullable
		@Override
		protected CSharpSimpleModuleExtension<?> compute()
		{
			consulo.module.Module value = myModuleValue.getValue();
			if(value == null)
			{
				return null;
			}
			return ModuleUtilCore.getExtension(value, CSharpSimpleModuleExtension.class);
		}
	};

	private PsiFile myFile;

	public CSharpHighlightContext(@Nonnull PsiFile file)
	{
		myFile = file;
	}

	@Nonnull
	public PsiFile getFile()
	{
		return myFile;
	}

	@RequiredReadAction
	@Nullable
	public consulo.module.Module getModule()
	{
		return myModuleValue.getValue();
	}

	@RequiredReadAction
	@Nullable
	public DotNetSimpleModuleExtension<?> getDotNetModuleExtension()
	{
		return myDotNetModuleExtensionValue.getValue();
	}

	@RequiredReadAction
	@Nullable
	public CSharpSimpleModuleExtension<?> getCSharpModuleExtension()
	{
		return myCSharpModuleExtensionValue.getValue();
	}

	@Nonnull
	@RequiredReadAction
	public CSharpLanguageVersion getLanguageVersion()
	{
		CSharpSimpleModuleExtension<?> value = myCSharpModuleExtensionValue.getValue();
		if(value == null)
		{
			return CSharpLanguageVersion.HIGHEST;
		}
		return value.getLanguageVersion().get();
	}
}
