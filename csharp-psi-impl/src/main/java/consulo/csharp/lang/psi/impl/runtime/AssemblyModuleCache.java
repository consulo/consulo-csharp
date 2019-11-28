/*
 * Copyright 2013-2019 consulo.io
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

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.impl.source.PsiFileImpl;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpAttributeList;
import consulo.csharp.lang.psi.impl.DotNetTypes2;
import consulo.csharp.lang.psi.impl.stub.index.AttributeListIndex;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.DotNetTypeRefUtil;
import consulo.internal.dotnet.asm.signature.TypeSignature;
import consulo.msil.lang.psi.MsilAssemblyEntry;
import consulo.msil.lang.psi.MsilCustomAttribute;
import consulo.msil.lang.psi.MsilFile;
import consulo.msil.lang.stubbing.MsilCustomAttributeArgumentList;
import consulo.msil.lang.stubbing.MsilCustomAttributeStubber;
import consulo.msil.lang.stubbing.values.MsiCustomAttributeValue;
import consulo.vfs.util.ArchiveVfsUtil;
import gnu.trove.THashSet;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * @author VISTALL
 * @since 2019-10-29
 */
@Singleton
public class AssemblyModuleCache extends UserDataHolderBase implements Disposable
{
	@Nonnull
	public static AssemblyModuleCache getInstance(Project project)
	{
		return ServiceManager.getService(project, AssemblyModuleCache.class);
	}

	private final Project myProject;

	@Inject
	public AssemblyModuleCache(Project project)
	{
		myProject = project;
	}

	@Nonnull
	public Set<String> getSourceAllowedAssemblies(Module module)
	{
		return CachedValuesManager.getManager(myProject).getCachedValue(this, () -> CachedValueProvider.Result.create(calcSourceAllowedAssemblies(module), PsiModificationTracker
				.MODIFICATION_COUNT));
	}

	@Nonnull
	public Set<String> getBinaryAllowedAssemblies(VirtualFile moduleFile)
	{
		return CachedValuesManager.getManager(myProject).getCachedValue(this, () -> CachedValueProvider.Result.create(calcBinaryAllowedAssemblies(myProject, moduleFile), VirtualFileManager
				.VFS_STRUCTURE_MODIFICATIONS));
	}

	@RequiredReadAction
	private static Set<String> calcSourceAllowedAssemblies(Module module)
	{
		Set<String> assemblies = new HashSet<>();

		Collection<CSharpAttributeList> attributeLists = AttributeListIndex.getInstance().get(DotNetAttributeTargetType.ASSEMBLY, module.getProject(), module.getModuleScope());

		for(CSharpAttributeList attributeList : attributeLists)
		{
			for(CSharpAttribute attribute : attributeList.getAttributes())
			{
				DotNetTypeDeclaration dotNetTypeDeclaration = attribute.resolveToType();
				if(dotNetTypeDeclaration == null)
				{
					continue;
				}

				if(DotNetTypes2.System.Runtime.CompilerServices.InternalsVisibleToAttribute.equalsIgnoreCase(dotNetTypeDeclaration.getVmQName()))
				{
					Module attributeModule = ModuleUtilCore.findModuleForPsiElement(attribute);
					if(attributeModule == null || !attributeModule.equals(module))
					{
						continue;
					}

					DotNetExpression[] parameterExpressions = attribute.getParameterExpressions();
					if(parameterExpressions.length == 0)
					{
						continue;
					}
					String valueAs = new ConstantExpressionEvaluator(parameterExpressions[0]).getValueAs(String.class);
					assemblies.add(valueAs);
				}
			}
		}
		return assemblies;
	}

	@Nonnull
	@RequiredReadAction
	private static Set<String> calcBinaryAllowedAssemblies(Project project, VirtualFile moduleFile)
	{
		VirtualFile archiveRootForLocalFile = ArchiveVfsUtil.getArchiveRootForLocalFile(moduleFile);
		if(archiveRootForLocalFile == null)
		{
			return Collections.emptySet();
		}

		VirtualFile assemblyFile = archiveRootForLocalFile.findChild("AssemblyInfo.msil");
		if(assemblyFile == null)
		{
			return Collections.emptySet();
		}
		PsiFileImpl file = (PsiFileImpl) PsiManager.getInstance(project).findFile(assemblyFile);
		if(!(file instanceof MsilFile))
		{
			return Collections.emptySet();
		}

		Set<String> assemblies = new THashSet<>();
		for(PsiElement psiElement : ((MsilFile) file).getMembers())
		{
			if(psiElement instanceof MsilAssemblyEntry)
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
						if(msiCustomAttributeValue.getTypeSignature() == TypeSignature.STRING)
						{
							assemblies.add((String) msiCustomAttributeValue.getValue());
						}
					}
				}
			}
		}
		return assemblies;
	}


	@Override
	public void dispose()
	{
		clearUserData();
	}
}
