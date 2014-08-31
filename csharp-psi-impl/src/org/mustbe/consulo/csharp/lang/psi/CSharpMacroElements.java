package org.mustbe.consulo.csharp.lang.psi;

import org.mustbe.consulo.csharp.lang.CSharpMacroLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.source.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.ElementTypeAsPsiFactory;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public interface CSharpMacroElements
{

	IElementType MACRO_DEFINE = new ElementTypeAsPsiFactory("MACRO_DEFINE", CSharpMacroLanguage.INSTANCE, CSharpMacroDefineImpl.class);

	IElementType MACRO_UNDEF = new ElementTypeAsPsiFactory("MACRO_UNDEF", CSharpMacroLanguage.INSTANCE, CSharpMacroDefineImpl.class);

	IElementType MACRO_IF = new ElementTypeAsPsiFactory("MACRO_IF", CSharpMacroLanguage.INSTANCE, CSharpMacroIfImpl.class);

	IElementType MACRO_IF_CONDITION_BLOCK = new ElementTypeAsPsiFactory("MACRO_IF_CONDITION_BLOCK", CSharpMacroLanguage.INSTANCE,
			CSharpMacroIfConditionBlockImpl.class);

	IElementType MACRO_BLOCK_START = new ElementTypeAsPsiFactory("MACRO_BLOCK_START", CSharpMacroLanguage.INSTANCE,
			CSharpMacroBlockStartImpl.class);

	IElementType MACRO_BLOCK = new ElementTypeAsPsiFactory("MACRO_BLOCK", CSharpMacroLanguage.INSTANCE, CSharpMacroBlockImpl.class);

	IElementType MACRO_BLOCK_STOP = new ElementTypeAsPsiFactory("MACRO_BLOCK_STOP", CSharpMacroLanguage.INSTANCE, CSharpMacroBlockStopImpl.class);

	IElementType PREFIX_EXPRESSION = new ElementTypeAsPsiFactory("PREFIX_EXPRESSION", CSharpMacroLanguage.INSTANCE,
			CSharpMacroPrefixExpressionImpl.class);

	IElementType POLYADIC_EXPRESSION = new ElementTypeAsPsiFactory("POLYADIC_EXPRESSION", CSharpMacroLanguage.INSTANCE,
			CSharpMacroPolyadicExpressionImpl.class);

	IElementType BINARY_EXPRESSION = new ElementTypeAsPsiFactory("BINARY_EXPRESSION", CSharpMacroLanguage.INSTANCE,
			CSharpMacroBinaryExpressionImpl.class);

	IElementType REFERENCE_EXPRESSION = new ElementTypeAsPsiFactory("REFERENCE_EXPRESSION", CSharpMacroLanguage.INSTANCE,
			CSharpMacroReferenceExpressionImpl.class);

	IElementType PARENTHESES_EXPRESSION = new ElementTypeAsPsiFactory("PARENTHESES_EXPRESSION", CSharpMacroLanguage.INSTANCE,
			CSharpMacroParenthesesExpressionImpl.class);
}
