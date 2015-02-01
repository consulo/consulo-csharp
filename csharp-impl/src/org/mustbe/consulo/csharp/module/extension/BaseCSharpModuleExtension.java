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

import java.util.ArrayList;
import java.util.List;

import org.consulo.module.extension.impl.ModuleExtensionImpl;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.module.CSharpLanguageVersionPointer;
import org.mustbe.consulo.dotnet.DotNetRunUtil;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleLangExtension;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.search.searches.AllClassesSearch;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.roots.ModuleRootLayer;
import com.intellij.psi.PsiElement;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public abstract class BaseCSharpModuleExtension<T extends BaseCSharpModuleExtension<T>> extends ModuleExtensionImpl<T> implements
		DotNetModuleLangExtension<T>, CSharpModuleExtension<T>
{
	protected boolean myAllowUnsafeCode;
	protected boolean myOptimizeCode;
	protected CSharpPlatform myPlatform = CSharpPlatform.ANY_CPU;
	protected CSharpLanguageVersionPointer myLanguageVersionPointer;

	public BaseCSharpModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module)
	{
		super(id, module);
		myLanguageVersionPointer = new CSharpLanguageVersionPointer(getProject(), id);
	}

	@NotNull
	@Override
	public PsiElement[] getEntryPointElements()
	{
		Query<DotNetTypeDeclaration> search = AllClassesSearch.search(getModule().getModuleWithDependenciesScope(), getProject());

		final List<DotNetTypeDeclaration> typeDeclarations = new ArrayList<DotNetTypeDeclaration>();
		search.forEach(new Processor<DotNetTypeDeclaration>()
		{
			@Override
			public boolean process(DotNetTypeDeclaration typeDeclaration)
			{
				if(typeDeclaration.getGenericParametersCount() == 0 && DotNetRunUtil.hasEntryPoint(typeDeclaration))
				{
					typeDeclarations.add(typeDeclaration);
				}
				return true;
			}
		});
		return ContainerUtil.toArray(typeDeclarations, DotNetTypeDeclaration.ARRAY_FACTORY);
	}

	@NotNull
	@Override
	public CSharpPlatform getPlatform()
	{
		return myPlatform;
	}

	@Override
	public boolean isAllowUnsafeCode()
	{
		return myAllowUnsafeCode;
	}

	@Override
	public boolean isOptimizeCode()
	{
		return myOptimizeCode;
	}

	public void setPlatform(@NotNull CSharpPlatform platform)
	{
		myPlatform = platform;
	}

	public void setOptimizeCode(boolean optimizeCode)
	{
		myOptimizeCode = optimizeCode;
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
				myOptimizeCode != mutableModuleExtension.myOptimizeCode ||
				myPlatform != mutableModuleExtension.myPlatform ||
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
		myOptimizeCode = Boolean.valueOf(element.getAttributeValue("optimize-code", "false"));
		myPlatform = CSharpPlatform.valueOf(element.getAttributeValue("platform", CSharpPlatform.ANY_CPU.name()));
		myLanguageVersionPointer.fromXml(element);
	}

	@Override
	protected void getStateImpl(@NotNull Element element)
	{
		element.setAttribute("unsafe-code", Boolean.toString(myAllowUnsafeCode));
		element.setAttribute("optimize-code", Boolean.toString(myOptimizeCode));
		element.setAttribute("platform", myPlatform.name());
		myLanguageVersionPointer.toXml(element);
	}

	@Override
	public void commit(@NotNull T mutableModuleExtension)
	{
		super.commit(mutableModuleExtension);

		myAllowUnsafeCode = mutableModuleExtension.myAllowUnsafeCode;
		myOptimizeCode = mutableModuleExtension.myOptimizeCode;
		myPlatform = mutableModuleExtension.myPlatform;
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
