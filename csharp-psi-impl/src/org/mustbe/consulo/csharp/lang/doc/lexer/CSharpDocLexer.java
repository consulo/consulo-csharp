package org.mustbe.consulo.csharp.lang.doc.lexer;

import com.intellij.lexer.Lexer;
import com.intellij.lexer.LookAheadLexer;
import com.intellij.lexer.XmlLexer;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;

/**
 * @author VISTALL
 * @since 15.09.14
 */
public class CSharpDocLexer extends LookAheadLexer
{
	private boolean myNewLine = true;

	public CSharpDocLexer()
	{
		super(new XmlLexer());
	}

	@Override
	protected void lookAhead(Lexer baseLexer)
	{
		IElementType tokenType = baseLexer.getTokenType();
		if(tokenType == XmlTokenType.TAG_WHITE_SPACE)
		{
			CharSequence tokenSequence = baseLexer.getTokenSequence();
			if(StringUtil.containsLineBreak(tokenSequence))
			{
				myNewLine = true;
				super.lookAhead(baseLexer);
				return;
			}
		}

		if(myNewLine)
		{
			if(tokenType == XmlTokenType.XML_DATA_CHARACTERS)
			{
				CharSequence tokenSequence = baseLexer.getTokenSequence();
				if(StringUtil.equals(tokenSequence, "///"))
				{
					myNewLine = false;
					advanceAs(baseLexer, XmlTokenType.XML_WHITE_SPACE);
					return;
				}
			}
		}

		super.lookAhead(baseLexer);
	}
}
