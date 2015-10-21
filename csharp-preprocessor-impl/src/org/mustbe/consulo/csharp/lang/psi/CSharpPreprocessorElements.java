package org.mustbe.consulo.csharp.lang.psi;

import org.mustbe.consulo.csharp.lang.CSharpPreprocessorLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.source.*;
import com.intellij.psi.tree.ElementTypeAsPsiFactory;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public interface CSharpPreprocessorElements
{

	IElementType DEFINE_DIRECTIVE = new ElementTypeAsPsiFactory("DEFINE_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorDefineDirectiveImpl.class);

	IElementType UNDEF_DIRECTIVE = new ElementTypeAsPsiFactory("UNDEF_DIRECTIVE", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorDefineDirectiveImpl.class);

	IElementType CONDITION_BLOCK = new ElementTypeAsPsiFactory("CONDITION_BLOCK", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorConditionImpl.class);

	IElementType IF_ELSE_BLOCK = new ElementTypeAsPsiFactory("IF_ELSE_BLOCK", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorIfElseBlockImpl.class);

	IElementType OPEN_TAG = new ElementTypeAsPsiFactory("OPEN_TAG", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorOpenTagImpl.class);

	IElementType REGION_BLOCK = new ElementTypeAsPsiFactory("REGION_BLOCK", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorRegionBlockImpl.class);

	IElementType CLOSE_TAG = new ElementTypeAsPsiFactory("CLOSE_TAG", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorCloseTagImpl.class);

	IElementType PREFIX_EXPRESSION = new ElementTypeAsPsiFactory("PREFIX_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorPrefixExpressionImpl.class);

	IElementType POLYADIC_EXPRESSION = new ElementTypeAsPsiFactory("POLYADIC_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorPolyadicExpressionImpl.class);

	IElementType BINARY_EXPRESSION = new ElementTypeAsPsiFactory("BINARY_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorBinaryExpressionImpl.class);

	IElementType REFERENCE_EXPRESSION = new ElementTypeAsPsiFactory("REFERENCE_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorReferenceExpressionImpl.class);

	IElementType PARENTHESES_EXPRESSION = new ElementTypeAsPsiFactory("PARENTHESES_EXPRESSION", CSharpPreprocessorLanguage.INSTANCE, CSharpPreprocessorParenthesesExpressionImpl.class);
}
