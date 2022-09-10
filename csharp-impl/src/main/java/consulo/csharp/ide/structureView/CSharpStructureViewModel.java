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

package consulo.csharp.ide.structureView;

import consulo.fileEditor.structureView.StructureViewTreeElement;
import consulo.fileEditor.structureView.tree.NodeProvider;
import consulo.codeEditor.Editor;
import consulo.csharp.ide.structureView.sorters.CSharpMemberSorter;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.fileEditor.structureView.StructureViewModel;
import consulo.fileEditor.structureView.tree.Sorter;
import consulo.language.editor.structureView.StructureViewModelBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author VISTALL
 * @since 31.12.13.
 */
public class CSharpStructureViewModel extends StructureViewModelBase implements StructureViewModel.ElementInfoProvider
{
	public CSharpStructureViewModel(@Nonnull PsiFile psiFile, @Nullable Editor editor)
	{
		super(psiFile, editor, new CSharpNamedTreeElement(psiFile));

		withSorters(CSharpMemberSorter.INSTANCE, Sorter.ALPHA_SORTER);
		withSuitableClasses(DotNetQualifiedElement.class, CSharpLambdaExpressionImpl.class);
	}

	@Override
	public boolean shouldEnterElement(Object element)
	{
		return element instanceof CSharpTypeDeclaration;
	}

	@Nonnull
	@Override
	public Collection<NodeProvider> getNodeProviders()
	{
		return Arrays.asList(CSharpInheritedMembersNodeProvider.INSTANCE, CSharpLambdaNodeProvider.INSTANCE);
	}

	@Override
	public boolean isAlwaysShowsPlus(StructureViewTreeElement structureViewTreeElement)
	{
		Object value = structureViewTreeElement.getValue();
		return value instanceof CSharpFile || value instanceof CSharpTypeDeclaration;
	}

	@Override
	protected boolean isSuitable(PsiElement element)
	{
		if(element instanceof CSharpLambdaExpressionImpl)
		{
			return true;
		}
		return super.isSuitable(element);
	}

	@Override
	public boolean isAlwaysLeaf(StructureViewTreeElement structureViewTreeElement)
	{
		return false;
	}
}
