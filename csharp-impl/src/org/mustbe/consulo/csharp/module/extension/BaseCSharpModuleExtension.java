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
import java.util.Collection;
import java.util.List;

import org.consulo.module.extension.MutableModuleInheritableNamedPointer;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.compiler.CSharpPlatform;
import org.mustbe.consulo.csharp.compiler.MSBaseDotNetCompilerOptionsBuilder;
import org.mustbe.consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttributeList;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.index.AttributeListIndex;
import org.mustbe.consulo.dotnet.DotNetRunUtil;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleLangExtension;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeTargetType;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.search.searches.AllTypesSearch;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootLayer;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public abstract class BaseCSharpModuleExtension<T extends BaseCSharpModuleExtension<T>> extends BaseCSharpSimpleModuleExtension<T> implements
		DotNetModuleLangExtension<T>, CSharpModuleExtension<T>
{
	protected boolean myOptimizeCode;
	protected CSharpPlatform myPlatform = CSharpPlatform.ANY_CPU;
	protected String myCompilerTarget;
	protected CSharpCustomCompilerSdkPointer myCustomCompilerSdkPointer;

	public BaseCSharpModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module)
	{
		super(id, module);
		myCustomCompilerSdkPointer = new CSharpCustomCompilerSdkPointer(getProject(), id);
	}

	@Override
	public void setCompilerExecutable(@NotNull DotNetCompilerOptionsBuilder builder, @NotNull VirtualFile executable)
	{
		((MSBaseDotNetCompilerOptionsBuilder) builder).setExecutable(executable.getPath());
	}

	@NotNull
	@Override
	public MutableModuleInheritableNamedPointer<Sdk> getCustomCompilerSdkPointer()
	{
		return myCustomCompilerSdkPointer;
	}

	@NotNull
	@Override
	@RequiredReadAction
	public PsiElement[] getEntryPointElements()
	{
		Query<DotNetTypeDeclaration> search = AllTypesSearch.search(getModule().getModuleWithDependenciesScope(), getProject());

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

	@Nullable
	@Override
	@RequiredReadAction
	public String getAssemblyTitle()
	{
		GlobalSearchScope moduleScope = getModule().getModuleScope();
		Collection<CSharpAttributeList> attributeLists = AttributeListIndex.getInstance().get(DotNetAttributeTargetType.ASSEMBLY, getProject(), moduleScope);

		loop:for(CSharpAttributeList attributeList : attributeLists)
		{
			for(CSharpAttribute attribute : attributeList.getAttributes())
			{
				DotNetTypeDeclaration typeDeclaration = attribute.resolveToType();
				if(typeDeclaration == null)
				{
					continue;
				}

				if(DotNetTypes.System.Reflection.AssemblyTitleAttribute.equals(typeDeclaration.getVmQName()))
				{
					Module attributeModule = ModuleUtilCore.findModuleForPsiElement(attribute);
					if(attributeModule == null || !attributeModule.equals(getModule()))
					{
						continue;
					}
					DotNetExpression[] parameterExpressions = attribute.getParameterExpressions();
					if(parameterExpressions.length == 0)
					{
						break loop;
					}
					String valueAs = new ConstantExpressionEvaluator(parameterExpressions[0]).getValueAs(String.class);
					if(valueAs != null)
					{
						return valueAs;
					}
				}
			}
		}
		return null;
	}

	@NotNull
	@Override
	public CSharpPlatform getPlatform()
	{
		return myPlatform;
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

	@Override
	public boolean isModifiedImpl(@NotNull T mutableModuleExtension)
	{
		return super.isModifiedImpl(mutableModuleExtension) ||
				myOptimizeCode != mutableModuleExtension.myOptimizeCode ||
				myPlatform != mutableModuleExtension.myPlatform ||
				!myCustomCompilerSdkPointer.equals(mutableModuleExtension.myCustomCompilerSdkPointer) ||
				!Comparing.equal(myCompilerTarget, mutableModuleExtension.myCompilerTarget);
	}

	@RequiredReadAction
	@Override
	protected void loadStateImpl(@NotNull Element element)
	{
		super.loadStateImpl(element);

		myOptimizeCode = Boolean.valueOf(element.getAttributeValue("optimize-code", "false"));
		myPlatform = CSharpPlatform.valueOf(element.getAttributeValue("platform", CSharpPlatform.ANY_CPU.name()));
		myCompilerTarget = element.getAttributeValue("compiler-target");
		myCustomCompilerSdkPointer.fromXml(element);
	}

	@Override
	protected void getStateImpl(@NotNull Element element)
	{
		super.getStateImpl(element);

		element.setAttribute("optimize-code", Boolean.toString(myOptimizeCode));
		element.setAttribute("platform", myPlatform.name());
		if(myCompilerTarget != null)
		{
			element.setAttribute("compiler-target", myCompilerTarget);
		}
		myCustomCompilerSdkPointer.toXml(element);
	}

	@Override
	public void commit(@NotNull T mutableModuleExtension)
	{
		super.commit(mutableModuleExtension);

		myOptimizeCode = mutableModuleExtension.myOptimizeCode;
		myPlatform = mutableModuleExtension.myPlatform;
		myCompilerTarget = mutableModuleExtension.myCompilerTarget;
		myCustomCompilerSdkPointer.set(mutableModuleExtension.myCustomCompilerSdkPointer);
	}

	@Nullable
	@Override
	public String getCompilerTarget()
	{
		return myCompilerTarget;
	}

	public void setCompilerTarget(@Nullable String target)
	{
		myCompilerTarget = target;
	}
}
