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

package consulo.csharp.ide.msil.representation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.io.URLUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.ide.msil.representation.builder.CSharpStubBuilderVisitor;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.psi.impl.msil.MsilToCSharpUtil;
import consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.internal.dotnet.msil.decompiler.file.DotNetAssemblyFileArchiveEntry;
import consulo.internal.dotnet.msil.decompiler.textBuilder.block.LineStubBlock;
import consulo.internal.dotnet.msil.decompiler.textBuilder.block.StubBlock;
import consulo.internal.dotnet.msil.decompiler.textBuilder.util.StubBlockUtil;
import consulo.msil.lang.psi.MsilAssemblyEntry;
import consulo.msil.lang.psi.MsilFile;
import consulo.msil.representation.MsilFileRepresentationProvider;
import consulo.msil.representation.MsilFileRepresentationVirtualFile;
import consulo.vfs.util.ArchiveVfsUtil;

/**
 * @author VISTALL
 * @since 27.05.14
 */
public class CSharpMsilFileRepresentationProvider implements MsilFileRepresentationProvider
{
	@Nullable
	@Override
	public String getRepresentFileName(@Nonnull MsilFile msilFile)
	{
		return FileUtil.getNameWithoutExtension(msilFile.getName()) + CSharpFileType.DOT_EXTENSION;
	}

	@Nonnull
	@Override
	public FileType getFileType()
	{
		return CSharpFileType.INSTANCE;
	}

	@Nonnull
	@Override
	@RequiredReadAction
	public PsiFile transform(String fileName, @Nonnull final MsilFile msilFile)
	{
		List<StubBlock> list = new ArrayList<>();

		VirtualFile virtualFile = msilFile.getVirtualFile();
		if(virtualFile != null)
		{
			VirtualFile archiveRoot = ArchiveVfsUtil.getVirtualFileForArchive(virtualFile);
			if(archiveRoot != null)
			{
				list.add(new LineStubBlock("// Library: " + archiveRoot.getPath()));
				String path = virtualFile.getPath();
				int i = path.indexOf(URLUtil.ARCHIVE_SEPARATOR);
				assert i != -1;
				list.add(new LineStubBlock("// IL File: " + path.substring(i + 2, path.length())));
			}
			else
			{
				list.add(new LineStubBlock("// File: " + virtualFile.getPath()));
			}
		}

		DotNetNamedElement[] msilFileMembers = msilFile.getMembers();
		if(msilFile.getName().equals(DotNetAssemblyFileArchiveEntry.AssemblyInfo))
		{
			MsilAssemblyEntry assemblyEntry = (MsilAssemblyEntry) ContainerUtil.find(msilFileMembers, element -> element instanceof MsilAssemblyEntry);

			if(assemblyEntry != null)
			{
				CSharpStubBuilderVisitor.processAttributeListAsLine(assemblyEntry, list, DotNetAttributeTargetType.ASSEMBLY, assemblyEntry.getAttributes());
			}
		}
		else
		{
			List<DotNetQualifiedElement> wrapped = new ArrayList<>(msilFileMembers.length);
			for(DotNetNamedElement member : msilFileMembers)
			{
				PsiElement wrap = MsilToCSharpUtil.wrap(member, null);
				if(wrap != member)  // wrapped
				{
					wrapped.add((DotNetQualifiedElement) wrap);
				}
			}

			final Map<String, StubBlock> namespaces = new HashMap<>();
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
						namespaces.put(presentableParentQName, stubBlock = new StubBlock("namespace " + presentableParentQName, null, StubBlock.BRACES));

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

		final VirtualFile representationVirtualFile = new MsilFileRepresentationVirtualFile(fileName, CSharpFileType.INSTANCE, charSequence);

		SingleRootFileViewProvider viewProvider = new SingleRootFileViewProvider(PsiManager.getInstance(msilFile.getProject()), representationVirtualFile, true);

		final PsiFile file = new CSharpFileImpl(viewProvider);

		viewProvider.forceCachedPsi(file);

		((PsiManagerEx) PsiManager.getInstance(msilFile.getProject())).getFileManager().setViewProvider(representationVirtualFile, viewProvider);

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

	@Nonnull
	@RequiredReadAction
	public CharSequence buildContent(String fileName, @Nonnull final MsilFile msilFile)
	{
		List<StubBlock> list = new ArrayList<>();

		VirtualFile virtualFile = msilFile.getVirtualFile();
		if(virtualFile != null)
		{
			VirtualFile archiveRoot = ArchiveVfsUtil.getVirtualFileForArchive(virtualFile);
			if(archiveRoot != null)
			{
				list.add(new LineStubBlock("// Library: " + archiveRoot.getPath()));
				String path = virtualFile.getPath();
				int i = path.indexOf(URLUtil.ARCHIVE_SEPARATOR);
				assert i != -1;
				list.add(new LineStubBlock("// IL File: " + path.substring(i + 2, path.length())));
			}
			else
			{
				list.add(new LineStubBlock("// File: " + virtualFile.getPath()));
			}
		}

		DotNetNamedElement[] msilFileMembers = msilFile.getMembers();
		if(msilFile.getName().equals(DotNetAssemblyFileArchiveEntry.AssemblyInfo))
		{
			MsilAssemblyEntry assemblyEntry = (MsilAssemblyEntry) ContainerUtil.find(msilFileMembers, element -> element instanceof MsilAssemblyEntry);

			if(assemblyEntry != null)
			{
				CSharpStubBuilderVisitor.processAttributeListAsLine(assemblyEntry, list, DotNetAttributeTargetType.ASSEMBLY, assemblyEntry.getAttributes());
			}
		}
		else
		{
			List<DotNetQualifiedElement> wrapped = new ArrayList<>(msilFileMembers.length);
			for(DotNetNamedElement member : msilFileMembers)
			{
				PsiElement wrap = MsilToCSharpUtil.wrap(member, null);
				if(wrap != member)  // wrapped
				{
					wrapped.add((DotNetQualifiedElement) wrap);
				}
			}

			final Map<String, StubBlock> namespaces = new HashMap<>();
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
						namespaces.put(presentableParentQName, stubBlock = new StubBlock("namespace " + presentableParentQName, null, StubBlock.BRACES));

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

		return StubBlockUtil.buildText(list);
	}
}
