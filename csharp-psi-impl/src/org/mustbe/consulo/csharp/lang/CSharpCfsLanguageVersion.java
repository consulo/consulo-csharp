package org.mustbe.consulo.csharp.lang;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.source.injection.CSharpInjectExpressionElementType;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.cfs.lang.BaseExpressionCfsLanguageVersion;
import consulo.csharp.cfs.lang.CfsLanguage;
import consulo.lombok.annotations.Lazy;

/**
 * @author VISTALL
 * @since 12.03.2015
 */
public class CSharpCfsLanguageVersion extends BaseExpressionCfsLanguageVersion
{
	@NotNull
	@Lazy
	public static CSharpCfsLanguageVersion getInstance()
	{
		return CfsLanguage.INSTANCE.findVersionByClass(CSharpCfsLanguageVersion.class);
	}

	public CSharpCfsLanguageVersion()
	{
		super(CSharpLanguage.INSTANCE);
	}

	@Override
	public IElementType createExpressionElementType()
	{
		return new CSharpInjectExpressionElementType("EXPRESSION", CfsLanguage.INSTANCE, CSharpReferenceExpression.ResolveToKind.ANY_MEMBER);
	}
}
