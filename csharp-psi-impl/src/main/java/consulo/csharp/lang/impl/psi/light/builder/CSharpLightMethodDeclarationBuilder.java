/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp.lang.impl.psi.light.builder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintList;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpOperatorNameHelper;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpMethodImplUtil;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.project.Project;

/**
 * @author VISTALL
 * @since 08.05.14
 */
public class CSharpLightMethodDeclarationBuilder extends CSharpLightLikeMethodDeclarationBuilder<CSharpLightMethodDeclarationBuilder> implements
		CSharpMethodDeclaration
{
	private IElementType myOperatorElementType;
	private List<CSharpGenericConstraint> myGenericConstraints = Collections.emptyList();

	public CSharpLightMethodDeclarationBuilder(Project project)
	{
		super(project);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitMethodDeclaration(this);
	}

	@Override
	public boolean isDelegate()
	{
		return false;
	}

	@RequiredReadAction
	@Override
	public boolean isOperator()
	{
		return myOperatorElementType != null;
	}

	@Override
	public boolean isExtension()
	{
		return CSharpMethodImplUtil.isExtensionMethod(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public IElementType getOperatorElementType()
	{
		return myOperatorElementType;
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@Nullable
	@Override
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return null;
	}

	@Nonnull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		return CSharpGenericConstraint.EMPTY_ARRAY;
	}

	@Nullable
	@Override
	public DotNetType getTypeForImplement()
	{
		return null;
	}

	@Nonnull
	@Override
	public DotNetTypeRef getTypeRefForImplement()
	{
		return DotNetTypeRef.ERROR_TYPE;
	}

	@Override
	public String getName()
	{
		if(isOperator())
		{
			return CSharpOperatorNameHelper.getOperatorName(getOperatorElementType());
		}
		return super.getName();
	}

	@Nonnull
	public CSharpLightMethodDeclarationBuilder setOperator(@Nullable IElementType elementType)
	{
		myOperatorElementType = elementType;
		return this;
	}

	@Nonnull
	public CSharpLightMethodDeclarationBuilder addGenericConstraint(CSharpGenericConstraint genericConstraint)
	{
		if(myGenericConstraints.isEmpty())
		{
			myGenericConstraints = new ArrayList<CSharpGenericConstraint>(2);
		}
		myGenericConstraints.add(genericConstraint);
		return this;
	}
}
