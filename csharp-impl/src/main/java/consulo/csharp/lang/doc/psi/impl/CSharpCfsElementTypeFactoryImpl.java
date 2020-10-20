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

package consulo.csharp.lang.doc.psi.impl;

import com.intellij.lang.*;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.impl.source.tree.LazyParseablePsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;
import consulo.csharp.cfs.lang.CfsLanguage;
import consulo.csharp.lang.CSharpCfsElementTypeFactory;
import consulo.csharp.lang.CSharpCfsLanguageVersion;
import consulo.csharp.lang.CSharpLanguage;
import consulo.lang.LanguageVersion;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 26-Dec-17
 */
@Singleton
public class CSharpCfsElementTypeFactoryImpl implements CSharpCfsElementTypeFactory
{
	@Nonnull
	@Override
	public IElementType getInterpolationStringElementType()
	{
		return new ILazyParseableElementType("INTERPOLATION_STRING_LITERAL", CSharpLanguage.INSTANCE)
		{
			@Override
			protected ASTNode doParseContents(@Nonnull final ASTNode chameleon, @Nonnull final PsiElement psi)
			{
				final Project project = psi.getProject();
				final Language languageForParser = getLanguageForParser(psi);
				final LanguageVersion languageVersion = CSharpCfsLanguageVersion.getInstance();
				final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser, languageVersion, chameleon.getChars());
				final PsiParser parser = LanguageParserDefinitions.INSTANCE.forLanguage(languageForParser).createParser(languageVersion);
				return parser.parse(this, builder, languageVersion).getFirstChildNode();
			}

			@Override
			protected Language getLanguageForParser(PsiElement psi)
			{
				return CfsLanguage.INSTANCE;
			}

			@Nullable
			@Override
			public ASTNode createNode(CharSequence text)
			{
				return new LazyParseablePsiElement(this, text);
			}
		};
	}
}
