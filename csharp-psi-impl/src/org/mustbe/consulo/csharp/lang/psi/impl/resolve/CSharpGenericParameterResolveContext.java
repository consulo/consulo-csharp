package org.mustbe.consulo.csharp.lang.psi.impl.resolve;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.*;
import org.mustbe.consulo.csharp.lang.psi.impl.light.builder.CSharpLightConstructorDeclarationBuilder;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import org.mustbe.consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import org.mustbe.consulo.dotnet.lang.psi.impl.stub.MsilHelper;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.UserDataHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 26.10.14
 */
public class CSharpGenericParameterResolveContext implements CSharpResolveContext
{
	private CSharpElementGroup<CSharpConstructorDeclaration> myConstructorGroup;

	public CSharpGenericParameterResolveContext(DotNetGenericParameter element)
	{
		Project project = element.getProject();

		CSharpGenericConstraint genericConstraint = CSharpGenericConstraintUtil.findGenericConstraint(element);
		if(genericConstraint != null)
		{
			for(CSharpGenericConstraintValue constraintValue : genericConstraint.getGenericConstraintValues())
			{
				if(constraintValue instanceof CSharpGenericConstraintKeywordValue && ((CSharpGenericConstraintKeywordValue) constraintValue)
						.getKeywordElementType() == CSharpTokens.NEW_KEYWORD)
				{
					CSharpLightConstructorDeclarationBuilder builder = new CSharpLightConstructorDeclarationBuilder(project);
					builder.addModifier(CSharpModifier.PUBLIC);
					builder.setNavigationElement(element);
					builder.withParent(element);

					myConstructorGroup = new CSharpElementGroupImpl<CSharpConstructorDeclaration>(project, MsilHelper.CONSTRUCTOR_NAME,
							Collections.<CSharpConstructorDeclaration>singletonList(builder));
					break;
				}
			}
		}
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpArrayMethodDeclaration> indexMethodGroup()
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup()
	{
		return myConstructorGroup;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup()
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@NotNull IElementType type)
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@NotNull DotNetTypeRef typeRef)
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@NotNull String name)
	{
		return null;
	}

	@NotNull
	@Override
	public Collection<CSharpElementGroup<CSharpMethodDeclaration>> getExtensionMethodGroups()
	{
		return Collections.emptyList();
	}

	@Nullable
	@Override
	public PsiElement findByName(@NotNull String name, @NotNull UserDataHolder holder)
	{
		return null;
	}

	@NotNull
	@Override
	public Collection<? extends PsiElement> getElements()
	{
		return Collections.emptyList();
	}
}
