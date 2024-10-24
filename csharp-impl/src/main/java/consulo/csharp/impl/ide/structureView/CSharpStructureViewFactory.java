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

package consulo.csharp.impl.ide.structureView;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.lang.CSharpLanguage;
import consulo.fileEditor.structureView.StructureViewModel;
import consulo.language.Language;
import consulo.language.editor.structureView.PsiStructureViewFactory;
import consulo.codeEditor.Editor;
import consulo.csharp.lang.impl.psi.source.CSharpFileImpl;
import consulo.fileEditor.structureView.StructureViewBuilder;
import consulo.fileEditor.structureView.TreeBasedStructureViewBuilder;
import consulo.language.psi.PsiFile;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 31.12.13.
 */
@ExtensionImpl
public class CSharpStructureViewFactory implements PsiStructureViewFactory
{
	@Nullable
	@Override
	public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile)
	{
		if(psiFile instanceof CSharpFileImpl)
		{
			return new TreeBasedStructureViewBuilder()
			{
				@Nonnull
				@Override
				public StructureViewModel createStructureViewModel(@Nullable Editor editor)
				{
					return new CSharpStructureViewModel(psiFile, editor);
				}
			};
		}
		else
		{
			return null;
		}
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
