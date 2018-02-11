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

package consulo.csharp.lang.psi.impl.source.injection;

import org.jetbrains.annotations.NonNls;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.ILazyParseableElementType;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.doc.lexer.CSharpReferenceLexer;
import consulo.csharp.lang.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.parser.ModifierSet;
import consulo.csharp.lang.parser.exp.ExpressionParsing;
import consulo.csharp.lang.psi.CSharpReferenceExpression;

/**
 * @author VISTALL
 * @since 12.03.2015
 */
public class CSharpInjectExpressionElementType extends ILazyParseableElementType
{
	private final static PsiParser ourParser = (elementType, builder, languageVersion) ->
	{
		PsiBuilder.Marker mark = builder.mark();
		ExpressionParsing.parse(new CSharpBuilderWrapper(builder, languageVersion), ModifierSet.EMPTY);
		while(!builder.eof())
		{
			builder.error("Unexpected token");
			builder.advanceLexer();
		}
		mark.done(elementType);
		return builder.getTreeBuilt();
	};

	private final CSharpReferenceExpression.ResolveToKind myResolveToKind;

	public CSharpInjectExpressionElementType(@Nonnull @NonNls String debugName, @Nullable Language language, @Nonnull CSharpReferenceExpression.ResolveToKind resolveToKind)
	{
		super(debugName, language);
		myResolveToKind = resolveToKind;
	}

	@Override
	protected ASTNode doParseContents(@Nonnull final ASTNode chameleon, @Nonnull final PsiElement psi)
	{
		final Project project = psi.getProject();
		final Language languageForParser = getLanguageForParser(psi);
		CSharpReferenceLexer lexer = new CSharpReferenceLexer();
		PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, lexer, languageForParser, languageForParser.getVersions()[0], chameleon.getChars());
		return ourParser.parse(this, builder, languageForParser.getVersions()[0]).getFirstChildNode();
	}

	@Override
	protected Language getLanguageForParser(PsiElement psi)
	{
		return CSharpLanguage.INSTANCE;
	}

	@Nullable
	@Override
	public ASTNode createNode(CharSequence text)
	{
		return new CSharpForInjectionFragmentHolder(this, text, myResolveToKind);
	}
}