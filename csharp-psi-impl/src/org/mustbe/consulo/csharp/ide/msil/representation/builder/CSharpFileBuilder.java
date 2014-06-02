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

package org.mustbe.consulo.csharp.ide.msil.representation.builder;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilElementWrapper;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.dotnet.dll.vfs.builder.block.StubBlock;
import org.mustbe.consulo.dotnet.dll.vfs.builder.block.StubBlockUtil;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.psi.DotNetMemberOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.reference.SoftReference;
import com.intellij.testFramework.LightVirtualFile;

/**
 * @author VISTALL
 * @since 02.06.14
 */
public class CSharpFileBuilder
{
	@NotNull
	public static CSharpFileImpl buildRoot(DotNetQualifiedElement msilWrapperElement, final VirtualFile msilVirtualFile)
	{
		List<StubBlock> list;

		String presentableParentQName = msilWrapperElement.getPresentableParentQName();
		boolean namespace = !StringUtil.isEmpty(presentableParentQName);
		if(namespace)
		{
			StubBlock stubBlock = new StubBlock("namespace " + presentableParentQName, null, StubBlock.BRACES);
			stubBlock.getBlocks().addAll(CSharpStubBuilderVisitor.buildBlocks(msilWrapperElement));
			list = Collections.singletonList(stubBlock);
		}
		else
		{
			list = CSharpStubBuilderVisitor.buildBlocks(msilWrapperElement);
		}

		CharSequence charSequence = StubBlockUtil.buildText(list);


		LightVirtualFile virtualFile = new LightVirtualFile(msilWrapperElement.getName() + CSharpFileType.DOT_EXTENSION, CSharpFileType.INSTANCE,
				charSequence, System.currentTimeMillis())
		{
			@Override
			public VirtualFile getParent()
			{
				return msilVirtualFile;
			}
		};

		SingleRootFileViewProvider viewProvider = new SingleRootFileViewProvider(PsiManager.getInstance(msilWrapperElement.getProject()),
				virtualFile, true);

		CSharpFileImpl file = new CSharpFileImpl(viewProvider);

		viewProvider.forceCachedPsi(file);

		((PsiManagerEx) PsiManager.getInstance(msilWrapperElement.getProject())).getFileManager().setViewProvider(virtualFile, viewProvider);

		DotNetQualifiedElement[] members = file.getMembers();

		DotNetElement target = members[0];
		if(namespace)
		{
			target = ((CSharpNamespaceDeclaration) target).getMembers()[0];
		}

		setMirror(msilWrapperElement, target);

		return file;
	}

	public static void setMirror(PsiElement msilWrapper, PsiElement mirrorElement)
	{
		if(msilWrapper instanceof MsilElementWrapper)
		{
			PsiElement msilElement = ((MsilElementWrapper) msilWrapper).getMsilElement();

			mirrorElement.putUserData(CSharpToMsiNavigateUtil.MSIL_ELEMENT, new SoftReference<PsiElement>(msilElement));

			if(msilWrapper instanceof DotNetMemberOwner && mirrorElement instanceof DotNetMemberOwner)
			{
				DotNetNamedElement[] wrappers = ((DotNetMemberOwner) msilWrapper).getMembers();
				DotNetNamedElement[] msils = ((DotNetMemberOwner) mirrorElement).getMembers();

				if(wrappers.length != msils.length)
				{
					return;
				}

				for(int i = 0; i < wrappers.length; i++)
				{
					setMirror(wrappers[i], msils[i]);
				}
			}
		}
	}
}
