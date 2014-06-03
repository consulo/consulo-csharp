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
import org.mustbe.consulo.csharp.ide.msil.representation.builder.CSharpStubBuilderVisitor;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.MsilToCSharpUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpFileImpl;
import org.mustbe.consulo.dotnet.dll.vfs.builder.block.StubBlock;
import org.mustbe.consulo.dotnet.dll.vfs.builder.block.StubBlockUtil;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.msil.lang.psi.MsilFile;
import org.mustbe.consulo.msil.representation.MsilFileRepresentationProvider;
import org.mustbe.consulo.msil.representation.MsilFileRepresentationVirtualFile;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.SingleRootFileViewProvider;
import com.intellij.psi.impl.PsiManagerEx;
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
	public PsiFile transform(String fileName, @NotNull MsilFile msilFile)
	{
		DotNetNamedElement[] msilFileMembers = msilFile.getMembers();
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

		final List<StubBlock> list = new ArrayList<StubBlock>();
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

		CharSequence charSequence = StubBlockUtil.buildText(list);

		val virtualFile = new MsilFileRepresentationVirtualFile(fileName, CSharpFileType.INSTANCE, charSequence);

		SingleRootFileViewProvider viewProvider = new SingleRootFileViewProvider(PsiManager.getInstance(msilFile.getProject()), virtualFile, true);

		CSharpFileImpl file = new CSharpFileImpl(viewProvider);

		viewProvider.forceCachedPsi(file);

		((PsiManagerEx) PsiManager.getInstance(msilFile.getProject())).getFileManager().setViewProvider(virtualFile, viewProvider);

		return file;
	}
}
