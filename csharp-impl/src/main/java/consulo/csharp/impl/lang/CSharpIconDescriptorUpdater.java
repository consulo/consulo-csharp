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

package consulo.csharp.impl.lang;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.AllIcons;
import consulo.component.util.Iconable;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.impl.CSharpAssemblyConstants;
import consulo.csharp.lang.impl.psi.msil.MsilElementWrapper;
import consulo.csharp.lang.impl.psi.source.CSharpAnonymousMethodExpression;
import consulo.csharp.lang.impl.psi.source.CSharpFileImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLabeledStatementImpl;
import consulo.csharp.lang.impl.psi.source.CSharpPsiUtilImpl;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetNamespaceAsElement;
import consulo.language.icon.IconDescriptor;
import consulo.language.icon.IconDescriptorUpdater;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.ModuleFileIndex;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.ProjectRootManager;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.project.DumbService;
import consulo.ui.image.Image;
import consulo.util.lang.BitUtil;
import consulo.util.lang.StringUtil;
import consulo.virtualFileSystem.VirtualFile;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 09.12.13.
 */
@ExtensionImpl
public class CSharpIconDescriptorUpdater implements IconDescriptorUpdater
{
	@Override
	@RequiredReadAction
	public void updateIcon(@Nonnull IconDescriptor iconDescriptor, @Nonnull PsiElement element, int flags)
	{
		if(element instanceof DotNetNamespaceAsElement)
		{
			iconDescriptor.setMainIcon(PlatformIconGroup.nodesNamespace());
			return;
		}
		else if(element instanceof CSharpAnonymousMethodExpression)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Lambda);
			return;
		}
		else if(element instanceof CSharpPreprocessorDefine)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Value);
			return;
		}

		VirtualFile virtualFile = null;
		if(element instanceof CSharpFileImpl)
		{
			virtualFile = ((CSharpFileImpl) element).getVirtualFile();
			if(virtualFile != null && StringUtil.equals(virtualFile.getNameSequence(), CSharpAssemblyConstants.FileName))
			{
				iconDescriptor.setMainIcon(AllIcons.FileTypes.Config);
				return;
			}

			DotNetNamedElement singleElement = CSharpPsiUtilImpl.findSingleElement((CSharpFile) element);
			if(singleElement != null)
			{
				IconDescriptorUpdaters.processExistingDescriptor(iconDescriptor, singleElement, flags);
				return;
			}
		}

		if(element instanceof DotNetLikeMethodDeclaration)
		{
			iconDescriptor.setMainIcon(((DotNetLikeMethodDeclaration) element).hasModifier(CSharpModifier.ABSTRACT) ? AllIcons.Nodes.AbstractMethod : AllIcons.Nodes.Method);

			processModifierListOwner(element, iconDescriptor, flags);
		}
		else if(element instanceof CSharpTypeDeclaration)
		{
			Image main = null;

			CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) element;
			if(!DumbService.getInstance(element.getProject()).isDumb())
			{
				if(DotNetInheritUtil.isAttribute(typeDeclaration))
				{
					main = typeDeclaration.hasModifier(CSharpModifier.ABSTRACT) ? AllIcons.Nodes.AbstractAttribute : AllIcons.Nodes.Attribute;
				}
				else if(DotNetInheritUtil.isException(typeDeclaration))
				{
					main = typeDeclaration.hasModifier(CSharpModifier.ABSTRACT) ? AllIcons.Nodes.AbstractException : AllIcons.Nodes.ExceptionClass;
				}
			}

			if(main == null)
			{
				if(typeDeclaration.isInterface())
				{
					main = AllIcons.Nodes.Interface;
				}
				else if(typeDeclaration.isEnum())
				{
					main = AllIcons.Nodes.Enum;
				}
				else if(typeDeclaration.isStruct())
				{
					main = AllIcons.Nodes.Struct;
				}
				else
				{
					main = typeDeclaration.hasModifier(CSharpModifier.ABSTRACT) ? AllIcons.Nodes.AbstractClass : AllIcons.Nodes.Class;
				}
			}

			iconDescriptor.setMainIcon(main);

			processModifierListOwner(element, iconDescriptor, flags);
		}
		else if(element instanceof DotNetGenericParameter || element instanceof CSharpTypeDefStatement)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.TypeAlias);
			iconDescriptor.setRightIcon(AllIcons.Nodes.C_public);
		}
		else if(element instanceof CSharpLabeledStatementImpl)
		{
			iconDescriptor.setMainIcon(PlatformIconGroup.nodesTag());
		}
		else if(element instanceof CSharpLocalVariable || element instanceof CSharpLinqVariable || element instanceof CSharpTupleVariable)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Variable);
			iconDescriptor.setRightIcon(AllIcons.Nodes.C_plocal);
		}
		else if(element instanceof DotNetParameter || element instanceof CSharpLambdaParameter)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Parameter);
			iconDescriptor.setRightIcon(AllIcons.Nodes.C_plocal);
		}
		else if(element instanceof DotNetFieldDeclaration)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Field);

			processModifierListOwner(element, iconDescriptor, flags);
		}
		else if(element instanceof CSharpPropertyDeclaration)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Property);

			processModifierListOwner(element, iconDescriptor, flags);
		}
		else if(element instanceof CSharpEventDeclaration)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Event);

			processModifierListOwner(element, iconDescriptor, flags);
		}
		else if(element instanceof CSharpPreprocessorVariable)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Variable);
			if(((CSharpPreprocessorVariable) element).isGlobal())
			{
				iconDescriptor.setRightIcon(AllIcons.Nodes.C_public);
			}
			else
			{
				iconDescriptor.setRightIcon(AllIcons.Nodes.C_private);
			}
		}
		else if(element instanceof CSharpNamespaceProvider)
		{
			iconDescriptor.setMainIcon(PlatformIconGroup.nodesNamespace());
		}
		else if(element instanceof DotNetXAccessor)
		{
			DotNetXAccessor.Kind accessorKind = ((DotNetXAccessor) element).getAccessorKind();
			if(accessorKind != null)
			{
				switch(accessorKind)
				{
					case GET:
						iconDescriptor.setMainIcon(AllIcons.Nodes.PropertyRead);
						break;
					case SET:
						iconDescriptor.setMainIcon(AllIcons.Nodes.PropertyWrite);
						break;
					case ADD:
						iconDescriptor.setMainIcon(AllIcons.Nodes.Event);
						break;
					case REMOVE:
						iconDescriptor.setMainIcon(AllIcons.Nodes.Event);
						break;
				}
			}

			processModifierListOwner(element, iconDescriptor, flags);
		}

		if(virtualFile == null)
		{
			PsiFile containingFile = element.getContainingFile();
			virtualFile = containingFile == null ? null : containingFile.getVirtualFile();
		}

		if(virtualFile != null && virtualFile.getFileType() == CSharpFileType.INSTANCE)
		{
			DotNetModuleExtension extension = ModuleUtilCore.getExtension(element.getProject(), virtualFile, DotNetModuleExtension.class);
			if(extension != null && extension.isAllowSourceRoots() && !isUnderSourceRoot(extension.getModule(), virtualFile))
			{
				ProjectFileIndex fileIndex = ProjectRootManager.getInstance(element.getProject()).getFileIndex();
				if(fileIndex.isInLibrary(virtualFile))
				{
					return;
				}
				iconDescriptor.addLayerIcon(AllIcons.Nodes.ExcludedFromCompile);
			}
		}
	}

	public static boolean isUnderSourceRoot(@Nonnull Module module, @Nonnull VirtualFile file)
	{
		ModuleFileIndex fileIndex = ModuleRootManager.getInstance(module).getFileIndex();
		return fileIndex.isInSourceContent(file) || fileIndex.isInTestSourceContent(file);
	}

	@RequiredReadAction
	private static void processModifierListOwner(PsiElement element, IconDescriptor iconDescriptor, int flags)
	{
		DotNetModifierListOwner owner = (DotNetModifierListOwner) element;
		if(BitUtil.isSet(flags, Iconable.ICON_FLAG_VISIBILITY))
		{
			if(owner.hasModifier(CSharpModifier.PRIVATE))
			{
				iconDescriptor.setRightIcon(AllIcons.Nodes.C_private);
			}
			else if(owner.hasModifier(CSharpModifier.PUBLIC))
			{
				iconDescriptor.setRightIcon(AllIcons.Nodes.C_public);
			}
			else if(owner.hasModifier(CSharpModifier.PROTECTED))
			{
				iconDescriptor.setRightIcon(AllIcons.Nodes.C_protected);
			}
			else
			{
				iconDescriptor.setRightIcon(AllIcons.Nodes.C_plocal);
			}
		}

		if(owner.hasModifier(CSharpModifier.STATIC) && !(owner instanceof CSharpTypeDeclaration))
		{
			iconDescriptor.addLayerIcon(AllIcons.Nodes.StaticMark);
		}

		if(owner.hasModifier(CSharpModifier.SEALED) ||
				owner.hasModifier(CSharpModifier.READONLY) ||
				element instanceof DotNetVariable && ((DotNetVariable) element).isConstant() ||
				element instanceof CSharpPropertyDeclaration && ((CSharpPropertyDeclaration) element).isAutoGet())
		{
			iconDescriptor.addLayerIcon(AllIcons.Nodes.FinalMark);
		}

		// dont check it for msil wrappers
		if(!(element instanceof MsilElementWrapper))
		{
			if(element instanceof DotNetTypeDeclaration && DotNetRunUtil.hasEntryPoint((DotNetTypeDeclaration) element) || element instanceof DotNetMethodDeclaration && DotNetRunUtil.isEntryPoint(
					(DotNetMethodDeclaration) element))

			{
				iconDescriptor.addLayerIcon(AllIcons.Nodes.RunnableMark);
			}
		}
	}
}
