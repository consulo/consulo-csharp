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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.msil.lang.psi.MsilFile;
import org.mustbe.consulo.msil.representation.MsilFileRepresentationProvider;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;

/**
 * @author VISTALL
 * @since 27.05.14
 */
public class CSharpMsilFileRepresentationProvider implements MsilFileRepresentationProvider
{
	@Nullable
	@Override
	public Pair<String, ? extends FileType> getRepresentResult(@NotNull MsilFile msilFile)
	{
		return Pair.create(FileUtil.getNameWithoutExtension(msilFile.getName()) + CSharpFileType.DOT_EXTENSION, CSharpFileType.INSTANCE);
	}

	@NotNull
	@Override
	public PsiFile transform(@NotNull MsilFile msilFile)
	{
		return null;
	}
}
