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

package consulo.csharp.lang.doc;

import java.util.List;

import org.emonic.base.documentation.IDocumentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import consulo.csharp.lang.doc.psi.CSharpDocRoot;
import consulo.dotnet.documentation.DotNetDocumentationResolver;
import consulo.dotnet.psi.DotNetQualifiedElement;

/**
 * @author VISTALL
 * @since 04.03.2015
 */
public class CSharpCommentDocumentationResolver implements DotNetDocumentationResolver
{
	@Nullable
	@Override
	public IDocumentation resolveDocumentation(@NotNull List<VirtualFile> virtualFile, @NotNull PsiElement element)
	{
		if(!(element instanceof DotNetQualifiedElement))
		{
			return null;
		}

		CSharpDocRoot docRoot = PsiTreeUtil.getChildOfType(element, CSharpDocRoot.class);
		if(docRoot != null)
		{
			return new CSharpDocAsIDocumentation(docRoot);
		}
		return null;
	}
}
