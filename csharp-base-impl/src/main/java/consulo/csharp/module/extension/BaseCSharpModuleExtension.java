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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jdom.Element;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ObjectUtil;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.compiler.CSharpPlatform;
import consulo.csharp.compiler.MSBaseDotNetCompilerOptionsBuilder;
import consulo.csharp.lang.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpAttributeList;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.impl.stub.index.AttributeListIndex;
import consulo.csharp.lang.psi.impl.stub.index.MethodIndex;
import consulo.dotnet.DotNetRunUtil;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import consulo.dotnet.module.extension.DotNetModuleLangExtension;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetMethodDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.module.extension.MutableModuleInheritableNamedPointer;
import consulo.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public abstract class BaseCSharpModuleExtension<T extends BaseCSharpModuleExtension<T>> extends BaseCSharpSimpleModuleExtension<T> implements DotNetModuleLangExtension<T>, CSharpModuleExtension<T>
{
	protected boolean myOptimizeCode;
	protected CSharpPlatform myPlatform = CSharpPlatform.ANY_CPU;
	protected String myCompilerTarget;
	protected CSharpCustomCompilerSdkPointer myCustomCompilerSdkPointer;

	public BaseCSharpModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer layer)
	{
		super(id, layer);
		myCustomCompilerSdkPointer = new CSharpCustomCompilerSdkPointer(layer, id);
	}

	@Override
	public void setCompilerExecutable(@Nonnull DotNetCompilerOptionsBuilder builder, @Nonnull VirtualFile executable)
	{
		((MSBaseDotNetCompilerOptionsBuilder) builder).setExecutable(executable.getPath());
	}

	@Nonnull
	@Override
	public MutableModuleInheritableNamedPointer<Sdk> getCustomCompilerSdkPointer()
	{
		return myCustomCompilerSdkPointer;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public PsiElement[] getEntryPointElements()
	{
		final List<DotNetTypeDeclaration> typeDeclarations = new ArrayList<DotNetTypeDeclaration>();
		Collection<DotNetLikeMethodDeclaration> methods = MethodIndex.getInstance().get("Main", getProject(), getModule().getModuleScope());
		for(DotNetLikeMethodDeclaration method : methods)
		{
			if(method instanceof CSharpMethodDeclaration && DotNetRunUtil.isEntryPoint((DotNetMethodDeclaration) method))
			{
				Module moduleForPsiElement = ModuleUtilCore.findModuleForPsiElement(method);
				// scope is broken?
				if(!getModule().equals(moduleForPsiElement))
				{
					continue;
				}
				ContainerUtil.addIfNotNull(typeDeclarations, ObjectUtil.tryCast(method.getParent(), DotNetTypeDeclaration.class));
			}
		}
		return ContainerUtil.toArray(typeDeclarations, DotNetTypeDeclaration.ARRAY_FACTORY);
	}

	@Nullable
	@Override
	@RequiredReadAction
	public String getAssemblyTitle()
	{
		GlobalSearchScope moduleScope = getModule().getModuleScope();
		Collection<CSharpAttributeList> attributeLists = AttributeListIndex.getInstance().get(DotNetAttributeTargetType.ASSEMBLY, getProject(), moduleScope);

		loop:
		for(CSharpAttributeList attributeList : attributeLists)
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

	@Nonnull
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

	public void setPlatform(@Nonnull CSharpPlatform platform)
	{
		myPlatform = platform;
	}

	public void setOptimizeCode(boolean optimizeCode)
	{
		myOptimizeCode = optimizeCode;
	}

	@Override
	public boolean isModifiedImpl(@Nonnull T mutableModuleExtension)
	{
		return super.isModifiedImpl(mutableModuleExtension) || myOptimizeCode != mutableModuleExtension.myOptimizeCode || myPlatform != mutableModuleExtension.myPlatform ||
				!myCustomCompilerSdkPointer.equals(mutableModuleExtension.myCustomCompilerSdkPointer) || !Comparing.equal(myCompilerTarget, mutableModuleExtension.myCompilerTarget);
	}

	@RequiredReadAction
	@Override
	protected void loadStateImpl(@Nonnull Element element)
	{
		super.loadStateImpl(element);

		myOptimizeCode = Boolean.valueOf(element.getAttributeValue("optimize-code", "false"));
		myPlatform = CSharpPlatform.valueOf(element.getAttributeValue("platform", CSharpPlatform.ANY_CPU.name()));
		myCompilerTarget = element.getAttributeValue("compiler-target");
		myCustomCompilerSdkPointer.fromXml(element);
	}

	@Override
	protected void getStateImpl(@Nonnull Element element)
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
	public void commit(@Nonnull T mutableModuleExtension)
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
