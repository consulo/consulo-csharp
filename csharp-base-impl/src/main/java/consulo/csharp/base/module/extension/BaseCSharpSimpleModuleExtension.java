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

package consulo.csharp.base.module.extension;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.impl.evaluator.ConstantExpressionEvaluator;
import consulo.csharp.lang.impl.psi.DotNetTypes2;
import consulo.csharp.lang.impl.psi.stub.index.AttributeListIndex;
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpAttributeList;
import consulo.csharp.module.CSharpLanguageVersionPointer;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import consulo.dotnet.compiler.DotNetCompileFailedException;
import consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import consulo.dotnet.module.extension.DotNetModuleLangExtension;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.file.LanguageFileType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.layer.ModuleRootLayer;
import consulo.module.content.layer.extension.ModuleExtensionBase;
import org.jdom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author VISTALL
 * @since 07.06.2015
 */
public class BaseCSharpSimpleModuleExtension<T extends BaseCSharpSimpleModuleExtension<T>> extends ModuleExtensionBase<T> implements CSharpSimpleModuleExtension<T>, DotNetModuleLangExtension<T>
{
	protected final CSharpLanguageVersionPointer myLanguageVersionPointer;
	protected boolean myAllowUnsafeCode;

	public BaseCSharpSimpleModuleExtension(@Nonnull String id, @Nonnull ModuleRootLayer layer)
	{
		super(id, layer);
		myLanguageVersionPointer = new CSharpLanguageVersionPointer(layer, id);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Set<String> getInternalsVisibleToAssemblies()
	{
		return CachedValuesManager.getManager(getProject()).getCachedValue(getProject(), () -> CachedValueProvider.Result.create(calcSourceAllowedAssemblies(getModule()), PsiModificationTracker
				.MODIFICATION_COUNT));
	}

	@RequiredReadAction
	private static Set<String> calcSourceAllowedAssemblies(Module module)
	{
		Set<String> assemblies = new HashSet<>();

		Collection<CSharpAttributeList> attributeLists = AttributeListIndex.getInstance().get(DotNetAttributeTargetType.ASSEMBLY, module.getProject(), GlobalSearchScope.moduleScope(module));

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
