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

package consulo.csharp.lang.psi.impl.runtime;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.DotNetTypes2;
import consulo.dotnet.resolve.DotNetTypeRefUtil;
import consulo.internal.dotnet.asm.signature.TypeSignature;
import consulo.msil.lang.psi.MsilAssemblyEntry;
import consulo.msil.lang.psi.MsilCustomAttribute;
import consulo.msil.lang.psi.MsilFile;
import consulo.msil.lang.psi.MsilStubElements;
import consulo.msil.lang.stubbing.MsilCustomAttributeArgumentList;
import consulo.msil.lang.stubbing.MsilCustomAttributeStubber;
import consulo.msil.lang.stubbing.values.MsiCustomAttributeValue;
import consulo.vfs.util.ArchiveVfsUtil;

/**
 * @author VISTALL
 * @since 14-Jun-17
 */
class DotNetModuleAsAssemblyModule implements AssemblyModule
{
	private final Project myProject;
	private final VirtualFile myModuleFile;

	DotNetModuleAsAssemblyModule(Project project, VirtualFile moduleFile)
	{
		myProject = project;
		myModuleFile = moduleFile;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String getName()
	{
		return myModuleFile.getNameWithoutExtension();
	}

	@RequiredReadAction
	@Override
	public boolean isAllowedAssembly(@NotNull String assemblyName)
	{
		VirtualFile archiveRootForLocalFile = ArchiveVfsUtil.getArchiveRootForLocalFile(myModuleFile);
		if(archiveRootForLocalFile == null)
		{
			return false;
		}

		VirtualFile assemblyFile = archiveRootForLocalFile.findChild("AssemblyInfo.msil");
		if(assemblyFile == null)
		{
			return false;
		}
		PsiFileImpl file = (PsiFileImpl) PsiManager.getInstance(myProject).findFile(assemblyFile);
		if(!(file instanceof MsilFile))
		{
			return false;
		}

		PsiElement[] children = file.getGreenStub().getChildrenByType(MsilStubElements.ASSEMBLY, PsiElement.ARRAY_FACTORY);

		for(PsiElement psiElement : children)
		{
			MsilAssemblyEntry assemblyEntry = (MsilAssemblyEntry) psiElement;

			MsilCustomAttribute[] attributes = assemblyEntry.getAttributes();
			for(MsilCustomAttribute attribute : attributes)
			{
				if(DotNetTypeRefUtil.isVmQNameEqual(attribute.toTypeRef(), attribute, DotNetTypes2.System.Runtime.CompilerServices.InternalsVisibleToAttribute))
				{
					MsilCustomAttributeArgumentList argumentList = MsilCustomAttributeStubber.build(attribute);
					List<MsiCustomAttributeValue> constructorArguments = argumentList.getConstructorArguments();
					if(constructorArguments.size() != 1)
					{
						continue;
					}

					MsiCustomAttributeValue msiCustomAttributeValue = constructorArguments.get(0);
					if(msiCustomAttributeValue.getTypeSignature() == TypeSignature.STRING && Comparing.equal(msiCustomAttributeValue.getValue(), assemblyName))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean equals(@NotNull AssemblyModule module)
	{
		return module instanceof DotNetModuleAsAssemblyModule && myModuleFile.equals(((DotNetModuleAsAssemblyModule) module).myModuleFile);
	}
}
