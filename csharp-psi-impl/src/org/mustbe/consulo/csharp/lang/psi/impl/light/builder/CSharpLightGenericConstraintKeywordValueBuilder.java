package org.mustbe.consulo.csharp.lang.psi.impl.light.builder;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
import com.intellij.openapi.project.Project;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 13.12.14
 */
public class CSharpLightGenericConstraintKeywordValueBuilder extends CSharpLightElementBuilder<CSharpLightGenericConstraintKeywordValueBuilder>
		implements CSharpGenericConstraintKeywordValue
{
	private final IElementType myElementType;

	public CSharpLightGenericConstraintKeywordValueBuilder(@NotNull Project project, @NotNull IElementType elementType)
	{
		super(project);
		myElementType = elementType;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitGenericConstraintKeywordValue(this);
	}

	@NotNull
	@Override
	public IElementType getKeywordElementType()
	{
		return myElementType;
	}
}
