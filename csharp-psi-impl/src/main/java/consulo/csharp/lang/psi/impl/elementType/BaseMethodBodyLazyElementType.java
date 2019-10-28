/*
 * Copyright 2013-2019 consulo.io
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

package consulo.csharp.lang.psi.impl.elementType;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.ILazyParseableElementType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.parser.ModifierSet;
import consulo.csharp.lang.psi.impl.source.CSharpMethodBodyImpl;
import consulo.lang.LanguageVersion;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 2019-10-03
 */
public abstract class BaseMethodBodyLazyElementType extends ILazyParseableElementType
{
	public BaseMethodBodyLazyElementType(@Nonnull @NonNls String debugName)
	{
		super(debugName, CSharpLanguage.INSTANCE);
	}

	protected abstract void parse(@Nonnull CSharpBuilderWrapper wrapper, @Nonnull ModifierSet set);

	@Override
	protected ASTNode doParseContents(@Nonnull ASTNode chameleon, @Nonnull PsiElement psi)
	{
		final Project project = psi.getProject();
		final Language languageForParser = getLanguageForParser(psi);
		final LanguageVersion tempLanguageVersion = chameleon.getUserData(LanguageVersion.KEY);
		final LanguageVersion languageVersion = tempLanguageVersion == null ? psi.getLanguageVersion() : tempLanguageVersion;
		final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser, languageVersion, chameleon.getChars());

		CSharpBuilderWrapper wrapper = new CSharpBuilderWrapper(builder, languageVersion);

		PsiBuilder.Marker mark = wrapper.mark();

		// todo wrong EMPTY
		parse(wrapper, ModifierSet.EMPTY);

		while(!wrapper.eof())
		{
			PsiBuilder.Marker er = wrapper.mark();
			wrapper.advanceLexer();
			er.error("Unexpected token");
		}

		mark.done(this);

		return wrapper.getTreeBuilt().getFirstChildNode();
	}

	@Override
	public ASTNode createNode(CharSequence text)
	{
		return new CSharpMethodBodyImpl(this, text);
	}
}
