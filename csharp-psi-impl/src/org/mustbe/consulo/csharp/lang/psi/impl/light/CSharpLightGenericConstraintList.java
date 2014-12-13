package org.mustbe.consulo.csharp.lang.psi.impl.light;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraint;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintList;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightElementBuilder;
import com.intellij.openapi.project.Project;

/**
 * @author VISTALL
 * @since 13.12.14
 */
public class CSharpLightGenericConstraintList extends CSharpLightElementBuilder<CSharpLightGenericConstraintList> implements CSharpGenericConstraintList
{
	private final CSharpGenericConstraint[] myGenericConstraints;

	public CSharpLightGenericConstraintList(Project project, CSharpGenericConstraint[] constraints)
	{
		super(project);
		myGenericConstraints = constraints;
	}

	@NotNull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		return myGenericConstraints;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitGenericConstraintList(this);
	}
}
