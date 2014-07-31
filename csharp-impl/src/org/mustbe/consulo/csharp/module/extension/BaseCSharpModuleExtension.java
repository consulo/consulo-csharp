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

package org.mustbe.consulo.csharp.module.extension;

import org.consulo.module.extension.impl.ModuleExtensionImpl;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.module.CSharpLanguageVersionPointer;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleLangExtension;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.roots.ModifiableRootModel;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public abstract class BaseCSharpModuleExtension<T extends BaseCSharpModuleExtension<T>> extends ModuleExtensionImpl<T> implements
		DotNetModuleLangExtension<T>, CSharpModuleExtension<T>
{
	protected boolean myAllowUnsafeCode;
	protected CSharpLanguageVersionPointer myLanguageVersionPointer;

	public BaseCSharpModuleExtension(@NotNull String id, @NotNull ModifiableRootModel module)
	{
		super(id, module);
		myLanguageVersionPointer = new CSharpLanguageVersionPointer(getProject(), id);
	}

	@Override
	public boolean isAllowUnsafeCode()
	{
		return myAllowUnsafeCode;
	}

	public void setLanguageVersion(@NotNull CSharpLanguageVersion languageVersion)
	{
		myLanguageVersionPointer.set(null, languageVersion);
	}

	public void setAllowUnsafeCode(boolean value)
	{
		myAllowUnsafeCode = value;
	}

	public boolean isModifiedImpl(@NotNull T mutableModuleExtension)
	{
		return myIsEnabled != mutableModuleExtension.isEnabled() ||
				myAllowUnsafeCode != mutableModuleExtension.myAllowUnsafeCode ||
				!myLanguageVersionPointer.equals(mutableModuleExtension.myLanguageVersionPointer);
	}

	public CSharpLanguageVersionPointer getLanguageVersionPointer()
	{
		return myLanguageVersionPointer;
	}

	@Override
	protected void loadStateImpl(@NotNull Element element)
	{
		myAllowUnsafeCode = Boolean.valueOf(element.getAttributeValue("unsafe-code", "false"));
		myLanguageVersionPointer.fromXml(element);
	}

	@Override
	protected void getStateImpl(@NotNull Element element)
	{
		element.setAttribute("unsafe-code", Boolean.toString(myAllowUnsafeCode));
		myLanguageVersionPointer.toXml(element);
	}

	@Override
	public void commit(@NotNull T mutableModuleExtension)
	{
		super.commit(mutableModuleExtension);

		myAllowUnsafeCode = mutableModuleExtension.myAllowUnsafeCode;
		myLanguageVersionPointer.set(mutableModuleExtension.myLanguageVersionPointer);
	}

	@NotNull
	@Override
	public CSharpLanguageVersion getLanguageVersion()
	{
		return myLanguageVersionPointer.get();
	}

	@NotNull
	@Override
	public LanguageFileType getFileType()
	{
		return CSharpFileType.INSTANCE;
	}
}
