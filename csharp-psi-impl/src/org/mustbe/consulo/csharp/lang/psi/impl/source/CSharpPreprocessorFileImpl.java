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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpPreprocessorLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorFileType;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorDefineDirective;
import com.intellij.extapi.psi.PsiFileBase;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.FileViewProvider;

/**
 * @author VISTALL
 * @since 23.01.14
 */
public class CSharpPreprocessorFileImpl extends PsiFileBase
{
	public CSharpPreprocessorFileImpl(@NotNull FileViewProvider viewProvider)
	{
		super(viewProvider, CSharpPreprocessorLanguage.INSTANCE);
	}

	@NotNull
	public CSharpPreprocessorDefineDirective[] getDefines()
	{
		return findChildrenByClass(CSharpPreprocessorDefineDirective.class);
	}

	@NotNull
	@Override
	public FileType getFileType()
	{
		return CSharpPreprocessorFileType.INSTANCE;
	}
}
