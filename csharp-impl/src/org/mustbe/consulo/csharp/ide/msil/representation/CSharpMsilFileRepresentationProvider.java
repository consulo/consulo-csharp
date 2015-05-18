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

package org.mustbe.consulo.csharp.ide.msil.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.msil.representation.builder.CSharpStubBuilderVisitor;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilToCSharpUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeTargetType;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.msil.lang.psi.MsilAssemblyEntry;
import org.mustbe.consulo.msil.lang.psi.MsilFile;
import org.mustbe.consulo.msil.representation.MsilFileRepresentationProvider;
import org.mustbe.consulo.msil.representation.MsilFileRepresentationVirtualFile;
import org.mustbe.dotnet.msil.decompiler.file.DotNetAssemblyFileArchiveEntry;
import org.mustbe.dotnet.msil.decompiler.textBuilder.block.StubBlock;
import org.mustbe.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.util.containers.ContainerUtil;
import lombok.val;

/**
 * @author VISTALL
 * @since 27.05.14
 */
public class CSharpMsilFileRepresentationProvider implements MsilFileRepresentationProvider
{
	@Nullable
	@Override
	public String getRepresentFileName(@NotNull MsilFile msilFile)
	{
		return FileUtil.getNameWithoutExtension(msilFile.getName()) + CSharpFileType.DOT_EXTENSION;
	}

	@NotNull
	@Override
	public FileType getFileType()
	{
		return CSharpFileType.INSTANCE;
	}

	@NotNull
	@Override
	@RequiredReadAction
	public PsiFile transform(String fileName, @NotNull final MsilFile msilFile)
	{
		final List<StubBlock> list = new ArrayList<StubBlock>();

		DotNetNamedElement[] msilFileMembers = msilFile.getMembers();
		if(msilFile.getName().equals(DotNetAssemblyFileArchiveEntry.AssemblyInfo))
		{
			MsilAssemblyEntry assemblyEntry = (MsilAssemblyEntry) ContainerUtil.find(msilFileMembers, new Condition<DotNetNamedElement>()
			{
				@Override
				public boolean value(DotNetNamedElement dotNetNamedElement)
				{
					return dotNetNamedElement instanceof MsilAssemblyEntry;
				}
			});

			if(assemblyEntry != null)
			{
				CSharpStubBuilderVisitor.processAttributeListAsLine(assemblyEntry, list, DotNetAttributeTargetType.ASSEMBLY,
						assemblyEntry.getAttributes());
			}
		}
		else
		{
			List<DotNetQualifiedElement> wrapped = new ArrayList<DotNetQualifiedElement>(msilFileMembers.length);
			for(DotNetNamedElement member : msilFileMembers)
			{
				PsiElement wrap = MsilToCSharpUtil.wrap(member);
				if(wrap != member)  // wrapped
				{
					wrapped.add((DotNetQualifiedElement) wrap);
				}
			}

			final Map<String, StubBlock> namespaces = new HashMap<String, StubBlock>();
			for(DotNetQualifiedElement msilWrapperElement : wrapped)
			{
				String presentableParentQName = msilWrapperElement.getPresentableParentQName();

				boolean namespace = !StringUtil.isEmpty(presentableParentQName);

				if(namespace)
				{
					List<StubBlock> toAdd = null;

					StubBlock stubBlock = namespaces.get(presentableParentQName);
					if(stubBlock == null)
					{
						namespaces.put(presentableParentQName, stubBlock = new StubBlock("namespace " + presentableParentQName, null,
								StubBlock.BRACES));

						list.add(stubBlock);

						toAdd = stubBlock.getBlocks();
					}
					else
					{
						toAdd = stubBlock.getBlocks();
					}

					toAdd.addAll(CSharpStubBuilderVisitor.buildBlocks(msilWrapperElement));
				}
				else
				{
					list.addAll(CSharpStubBuilderVisitor.buildBlocks(msilWrapperElement));
				}
			}
		}

		CharSequence charSequence = StubBlockUtil.buildText(list);

		val virtualFile = new MsilFileRepresentationVirtualFile(fileName, CSharpFileType.INSTANCE, charSequence);

		SingleRootFileViewProvider viewProvider = new SingleRootFileViewProvider(PsiManager.getInstance(msilFile.getProject()), virtualFile, true);

		val file = new CSharpFileImpl(viewProvider);

		viewProvider.forceCachedPsi(file);

		((PsiManagerEx) PsiManager.getInstance(msilFile.getProject())).getFileManager().setViewProvider(virtualFile, viewProvider);

		new WriteCommandAction.Simple<Object>(file.getProject(), file)
		{
			@Override
			protected void run() throws Throwable
			{
				CodeStyleManager.getInstance(getProject()).reformat(file);
			}
		}.execute();
		return file;
	}
}
