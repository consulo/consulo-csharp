package org.mustbe.consulo.csharp.lang.psi;

import org.mustbe.consulo.csharp.lang.doc.psi.CSharpDocElements;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 15.09.14
 */
public interface CSharpTokensImpl extends CSharpTokens
{
	IElementType LINE_DOC_COMMENT = CSharpDocElements.LINE_DOC_COMMENT;
}
