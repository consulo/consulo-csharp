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

package org.mustbe.consulo.csharp.ide.highlight;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes.CSharpFileStubElementType;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 08.03.14
 */
public class CSharpNonActiveAnnotator extends ExternalAnnotator<List<TextRange>, List<TextRange>>
{
	@Nullable
	@Override
	public List<TextRange> collectInformation(@NotNull PsiFile file)
	{
		if(file instanceof CSharpMacroFileImpl)
		{
			DotNetModuleExtension extension = ModuleUtilCore.getExtension(file, DotNetModuleExtension.class);
			if(extension == null)
			{
				return null;
			}
			return CSharpFileStubElementType.collectDisabledBlocks(file, extension);
		}
		return null;
	}

	@Nullable
	@Override
	public List<TextRange> doAnnotate(List<TextRange> collectedInfo)
	{
		return collectedInfo == null || collectedInfo.isEmpty() ? null : collectedInfo;
	}

	@Override
	public void apply(@NotNull PsiFile file, List<TextRange> annotationResult, @NotNull AnnotationHolder holder)
	{
		if(annotationResult == null)
		{
			return;
		}
		for(TextRange textRange : annotationResult)
		{
			holder.createInfoAnnotation(textRange, null).setTextAttributes(CSharpHighlightKey.DISABLED_BLOCK);
		}
	}
}
