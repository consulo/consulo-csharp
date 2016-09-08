package org.mustbe.consulo.csharp.lang.parser;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.parser.macro.MacroParsing;
import com.intellij.lang.ASTNode;
import consulo.lang.LanguageVersion;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 23.01.14
 */
public class CSharpMacroParser implements PsiParser
{
	@NotNull
	@Override
	public ASTNode parse(@NotNull IElementType elementType, @NotNull PsiBuilder builder, @NotNull LanguageVersion languageVersion)
	{
		PsiBuilder.Marker mark = builder.mark();

		while(!builder.eof())
		{
			MacroParsing.parse(builder);
		}

		mark.done(elementType);
		return builder.getTreeBuilt();
	}
}
