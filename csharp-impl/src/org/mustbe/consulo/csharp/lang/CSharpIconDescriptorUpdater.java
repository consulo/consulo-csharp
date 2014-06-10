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

package org.mustbe.consulo.csharp.lang;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.CSharpIcons;
import org.mustbe.consulo.csharp.ide.assemblyInfo.CSharpAssemblyConstants;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpLambdaParameter;
import org.mustbe.consulo.csharp.lang.psi.CSharpLocalVariable;
import org.mustbe.consulo.csharp.lang.psi.CSharpMacroDefine;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpNamespaceAsElement;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpLabeledStatementImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpTypeDefStatementImpl;
import org.mustbe.consulo.dotnet.DotNetRunUtil;
import org.mustbe.consulo.dotnet.lang.psi.DotNetInheritUtil;
import org.mustbe.consulo.dotnet.module.DotNetModuleUtil;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import org.mustbe.consulo.dotnet.psi.*;
import com.intellij.icons.AllIcons;
import com.intellij.ide.IconDescriptor;
import com.intellij.ide.IconDescriptorUpdater;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.BitUtil;

/**
 * @author VISTALL
 * @since 09.12.13.
 */
public class CSharpIconDescriptorUpdater implements IconDescriptorUpdater
{
	@Override
	public void updateIcon(@NotNull IconDescriptor iconDescriptor, @NotNull PsiElement element, int flags)
	{
		if(element instanceof CSharpNamespaceAsElement)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Package);
			return;
		}
		else if(element instanceof CSharpMacroDefine)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Value);
			return;
		}

		PsiFile containingFile = element.getContainingFile();
		if(containingFile != null && CSharpAssemblyConstants.FileName.equals(containingFile.getName()))
		{
			iconDescriptor.setMainIcon(AllIcons.FileTypes.Config);
			return;
		}

		if(element instanceof DotNetLikeMethodDeclaration)
		{
			iconDescriptor.setMainIcon(((DotNetLikeMethodDeclaration) element).hasModifier(CSharpModifier.ABSTRACT) ? AllIcons.Nodes.AbstractMethod
					: AllIcons.Nodes.Method);

			processModifierListOwner(element, iconDescriptor, flags);
		}
		else if(element instanceof CSharpArrayMethodDeclaration)
		{
			iconDescriptor.setMainIcon(((CSharpArrayMethodDeclaration) element).hasModifier(CSharpModifier.ABSTRACT) ? AllIcons.Nodes.AbstractMethod
					: AllIcons.Nodes.Method);

			processModifierListOwner(element, iconDescriptor, flags);
		}
		else if(element instanceof DotNetTypeDeclaration)
		{
			Icon main = null;

			CSharpTypeDeclaration typeDeclaration = (CSharpTypeDeclaration) element;
			if(!DumbService.getInstance(element.getProject()).isDumb())
			{
				if(DotNetInheritUtil.isAttribute(typeDeclaration))
				{
					main = CSharpIcons.Nodes.AnnotationClass;
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
					main = AllIcons.Nodes.Static;  //TODO [VISTALL] icon
				}
				else
				{
					main = typeDeclaration.hasModifier(CSharpModifier.ABSTRACT) ? AllIcons.Nodes.AbstractClass : AllIcons.Nodes.Class;
				}
			}

			iconDescriptor.setMainIcon(main);

			processModifierListOwner(element, iconDescriptor, flags);
		}
		else if(element instanceof DotNetGenericParameter || element instanceof CSharpTypeDefStatementImpl)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.TypeAlias);
		}
		else if(element instanceof CSharpLabeledStatementImpl)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Advice);
		}
		else if(element instanceof CSharpLocalVariable)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Variable);
		}
		else if(element instanceof DotNetParameter || element instanceof CSharpLambdaParameter)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Parameter);
		}
		else if(element instanceof DotNetFieldDeclaration)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Field);

			processModifierListOwner(element, iconDescriptor, flags);
		}
		else if(element instanceof DotNetPropertyDeclaration)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Property);

			processModifierListOwner(element, iconDescriptor, flags);
		}
		else if(element instanceof DotNetEventDeclaration)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Property);  //TODO [VISTALL] icon

			processModifierListOwner(element, iconDescriptor, flags);
		}
		else if(element instanceof DotNetNamespaceDeclaration)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Package);  //TODO [VISTALL] icon
		}
		else if(element instanceof DotNetXXXAccessor)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Method);

			processModifierListOwner(element, iconDescriptor, flags);
		}

		DotNetModuleExtension extension = ModuleUtilCore.getExtension(element, DotNetModuleExtension.class);
		if(containingFile != null && containingFile.getFileType() == CSharpFileType.INSTANCE && extension != null && extension.isAllowSourceRoots()
				&& !DotNetModuleUtil.isUnderSourceRoot(element))
		{
			ProjectFileIndex fileIndex = ProjectRootManager.getInstance(element.getProject()).getFileIndex();
			VirtualFile virtualFile = containingFile.getVirtualFile();
			if(virtualFile == null)
			{
				return;
			}
			if(fileIndex.isInLibraryClasses(virtualFile) || fileIndex.isInLibrarySource(virtualFile))
			{
				return;
			}
			iconDescriptor.addLayerIcon(AllIcons.Nodes.ExcludedFromCompile);
		}

	}

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

		if(owner.hasModifier(CSharpModifier.STATIC))
		{
			iconDescriptor.addLayerIcon(AllIcons.Nodes.StaticMark);
		}

		if(owner.hasModifier(CSharpModifier.SEALED) || owner.hasModifier(CSharpModifier.READONLY) || element instanceof DotNetVariable && (
				(DotNetVariable) element).isConstant())
		{
			iconDescriptor.addLayerIcon(AllIcons.Nodes.FinalMark);
		}

		if(element instanceof DotNetTypeDeclaration && DotNetRunUtil.hasEntryPoint((DotNetTypeDeclaration) element) || element instanceof
				DotNetMethodDeclaration && DotNetRunUtil.isEntryPoint((DotNetMethodDeclaration) element))
		{
			iconDescriptor.addLayerIcon(AllIcons.Nodes.RunnableMark);
		}
	}
}
