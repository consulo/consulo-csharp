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

package consulo.csharp.lang;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.BitUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.assemblyInfo.CSharpAssemblyConstants;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.msil.MsilElementWrapper;
import consulo.csharp.lang.psi.impl.source.CSharpAnonymousMethodExpression;
import consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import consulo.csharp.lang.psi.impl.source.CSharpLabeledStatementImpl;
import consulo.dotnet.DotNetRunUtil;
import consulo.dotnet.module.DotNetModuleUtil;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.dotnet.psi.*;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;
import consulo.ide.IconDescriptor;
import consulo.ide.IconDescriptorUpdater;

/**
 * @author VISTALL
 * @since 09.12.13.
 */
public class CSharpIconDescriptorUpdater implements IconDescriptorUpdater
{
	@Override
	@RequiredReadAction
	public void updateIcon(@NotNull IconDescriptor iconDescriptor, @NotNull PsiElement element, int flags)
	{
		if(element instanceof DotNetNamespaceAsElement)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Package);
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
		}

		if(element instanceof DotNetLikeMethodDeclaration)
		{
			iconDescriptor.setMainIcon(((DotNetLikeMethodDeclaration) element).hasModifier(CSharpModifier.ABSTRACT) ? AllIcons.Nodes.AbstractMethod : AllIcons.Nodes.Method);

			processModifierListOwner(element, iconDescriptor, flags);
		}
		else if(element instanceof CSharpTypeDeclaration)
		{
			Icon main = null;

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
			iconDescriptor.setMainIcon(AllIcons.Nodes.Advice);
		}
		else if(element instanceof CSharpLocalVariable || element instanceof CSharpLinqVariable)
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
		else if(element instanceof CSharpNamespaceDeclaration)
		{
			iconDescriptor.setMainIcon(AllIcons.Nodes.Package);  //TODO [VISTALL] icon
		}
		else if(element instanceof DotNetXXXAccessor)
		{
			DotNetXXXAccessor.Kind accessorKind = ((DotNetXXXAccessor) element).getAccessorKind();
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
			DotNetModuleExtension extension = ModuleUtilCore.getExtension(element, DotNetModuleExtension.class);
			if(extension != null && extension.isAllowSourceRoots() && !DotNetModuleUtil.isUnderSourceRoot(element))
			{
				ProjectFileIndex fileIndex = ProjectRootManager.getInstance(element.getProject()).getFileIndex();
				if(fileIndex.isInLibraryClasses(virtualFile) || fileIndex.isInLibrarySource(virtualFile))
				{
					return;
				}
				iconDescriptor.addLayerIcon(AllIcons.Nodes.ExcludedFromCompile);
			}
		}
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

		if(owner.hasModifier(CSharpModifier.SEALED) || owner.hasModifier(CSharpModifier.READONLY) || element instanceof DotNetVariable && ((DotNetVariable) element).isConstant())
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
