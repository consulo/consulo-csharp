/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.light.builder;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpNamespaceDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpUsingListChild;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetReferenceExpression;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 23.01.15
 */
public class CSharpLightNamespaceDeclarationBuilder extends CSharpLightElementBuilder<CSharpLightNamespaceDeclarationBuilder> implements
		CSharpNamespaceDeclaration
{
	private final String myQualifiedName;

	public CSharpLightNamespaceDeclarationBuilder(Project project, String qualifiedName)
	{
		super(project);
		myQualifiedName = qualifiedName;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitNamespaceDeclaration(this);
	}

	@Nullable
	@Override
	public DotNetReferenceExpression getNamespaceReference()
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public PsiElement getLeftBrace()
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public PsiElement getRightBrace()
	{
		return null;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public CSharpUsingListChild[] getUsingStatements()
	{
		return CSharpUsingListChild.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		throw new IllegalArgumentException("This is light element");
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		return false;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return null;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return StringUtil.getPackageName(myQualifiedName);
	}

	@Override
	public String getName()
	{
		return StringUtil.getShortName(myQualifiedName);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		return myQualifiedName;
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
	{
		return null;
	}
}
