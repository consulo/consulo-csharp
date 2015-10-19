package org.mustbe.consulo.csharp.lang.parser;

import java.util.ArrayDeque;
import java.util.Iterator;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.parser.macro.CSharpPreprocessorParsing;
import org.mustbe.consulo.csharp.lang.psi.CSharpPreprocessorElements;
import com.intellij.lang.ASTNode;
import com.intellij.lang.LanguageVersion;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 23.01.14
 */
public class CSharpPreprocessorParser implements PsiParser
{
	@NotNull
	@Override
	public ASTNode parse(@NotNull IElementType elementType, @NotNull PsiBuilder builder, @NotNull LanguageVersion languageVersion)
	{
		builder.setDebugMode(true);

		PsiBuilder.Marker mark = builder.mark();
		ArrayDeque<PsiBuilder.Marker> regionMarkers = new ArrayDeque<PsiBuilder.Marker>();
		CSharpPreprocessorParsing.parseDirectives(builder, regionMarkers, false);

		Iterator<PsiBuilder.Marker> markerIterator = regionMarkers.descendingIterator();
		while(markerIterator.hasNext())
		{
			PsiBuilder.Marker next = markerIterator.next();
			next.done(CSharpPreprocessorElements.REGION_BLOCK);
		}

		mark.done(elementType);
		return builder.getTreeBuilt();
	}
}
