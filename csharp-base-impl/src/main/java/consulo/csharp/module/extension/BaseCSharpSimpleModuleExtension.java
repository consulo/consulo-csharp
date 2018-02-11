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

package consulo.csharp.module.extension;

import javax.annotation.Nonnull;

import org.jdom.Element;

import javax.annotation.Nullable;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.module.CSharpLanguageVersionPointer;
import consulo.dotnet.compiler.DotNetCompileFailedException;
import consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import consulo.dotnet.module.extension.DotNetModuleLangExtension;
import consulo.module.extension.impl.ModuleExtensionImpl;
import consulo.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 07.06.2015
 */
public class BaseCSharpSimpleModuleExtension<T extends BaseCSharpSimpleModuleExtension<T>> extends ModuleExtensionImpl<T> implements CSharpSimpleModuleExtension<T>, DotNetModuleLangExtension<T>
{
	protected final CSharpLanguageVersionPointer myLanguageVersionPointer;
	protected boolean myAllowUnsafeCode;

	public BaseCSharpSimpleModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer layer)
	{
		super(id, layer);
		myLanguageVersionPointer = new CSharpLanguageVersionPointer(layer, id);
	}

	@Override
	public boolean isSupportedLanguageVersion(@Nonnull CSharpLanguageVersion languageVersion)
	{
		// for default we support all language versions
		return true;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public PsiElement[] getEntryPointElements()
	{
		return PsiElement.EMPTY_ARRAY;
	}

	@Nullable
	@Override
	@RequiredReadAction
	public String getAssemblyTitle()
	{
		return null;
	}

	@Nonnull
	@Override
	public DotNetCompilerOptionsBuilder createCompilerOptionsBuilder() throws DotNetCompileFailedException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAllowUnsafeCode()
	{
		return myAllowUnsafeCode;
	}

	public void setLanguageVersion(@Nonnull CSharpLanguageVersion languageVersion)
	{
		myLanguageVersionPointer.set(null, languageVersion);
	}

	public void setAllowUnsafeCode(boolean value)
	{
		myAllowUnsafeCode = value;
	}

	public boolean isModifiedImpl(@Nonnull T mutableModuleExtension)
	{
		return myIsEnabled != mutableModuleExtension.isEnabled() ||
				myAllowUnsafeCode != mutableModuleExtension.myAllowUnsafeCode ||
				!myLanguageVersionPointer.equals(mutableModuleExtension.myLanguageVersionPointer);
	}

	public CSharpLanguageVersionPointer getLanguageVersionPointer()
	{
		return myLanguageVersionPointer;
	}

	@RequiredReadAction
	@Override
	protected void loadStateImpl(@Nonnull Element element)
	{
		myAllowUnsafeCode = Boolean.valueOf(element.getAttributeValue("unsafe-code", "false"));
		myLanguageVersionPointer.fromXml(element);
	}

	@Override
	protected void getStateImpl(@Nonnull Element element)
	{
		element.setAttribute("unsafe-code", Boolean.toString(myAllowUnsafeCode));
		myLanguageVersionPointer.toXml(element);
	}

	@Override
	public void commit(@Nonnull T mutableModuleExtension)
	{
		super.commit(mutableModuleExtension);

		myAllowUnsafeCode = mutableModuleExtension.myAllowUnsafeCode;
		myLanguageVersionPointer.set(mutableModuleExtension.myLanguageVersionPointer);
	}

	@Nonnull
	@Override
	public CSharpLanguageVersion getLanguageVersion()
	{
		return myLanguageVersionPointer.get();
	}

	@Nonnull
	@Override
	public LanguageFileType getFileType()
	{
		return CSharpFileType.INSTANCE;
	}
}
