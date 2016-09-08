package org.mustbe.consulo.csharp.lang.psi.impl.source.injection;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.parser.CSharpBuilderWrapper;
import org.mustbe.consulo.csharp.lang.parser.ModifierSet;
import org.mustbe.consulo.csharp.lang.parser.exp.ExpressionParsing;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import consulo.lang.LanguageVersion;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.lang.PsiParser;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ILazyParseableElementType;

/**
 * @author VISTALL
 * @since 12.03.2015
 */
public class CSharpInjectExpressionElementType extends ILazyParseableElementType
{
	private final static PsiParser ourParser = new PsiParser()
	{
		@NotNull
		@Override
		public ASTNode parse(@NotNull IElementType elementType, @NotNull PsiBuilder builder, @NotNull LanguageVersion languageVersion)
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
		}
	};

	private final CSharpReferenceExpression.ResolveToKind myResolveToKind;

	public CSharpInjectExpressionElementType(@NotNull @NonNls String debugName,
			@Nullable Language language,
			@NotNull CSharpReferenceExpression.ResolveToKind resolveToKind)
	{
		super(debugName, language);
		myResolveToKind = resolveToKind;
	}

	@Override
	protected ASTNode doParseContents(@NotNull final ASTNode chameleon, @NotNull final PsiElement psi)
	{
		final Project project = psi.getProject();
		final Language languageForParser = getLanguageForParser(psi);
		final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser,
				languageForParser.getVersions()[0], chameleon.getChars());
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