/*
 * Copyright 2013-2014 must-be.org
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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetXXXAccessor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.project.Project;

/**
 * @author VISTALL
 * @since 07.12.2014
 */
public class CSharpLightArrayMethodDeclarationBuilder extends CSharpLightLikeMethodDeclarationBuilder<CSharpLightArrayMethodDeclarationBuilder> implements CSharpArrayMethodDeclaration
{
	public CSharpLightArrayMethodDeclarationBuilder(Project project)
	{
		super(project);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitArrayMethodDeclaration(this);
	}

	@Nullable
	@Override
	public DotNetType getTypeForImplement()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef getTypeRefForImplement()
	{
		return DotNetTypeRef.ERROR_TYPE;
	}

	@Override
	public String getName()
	{
		return "[]";
	}

	@NotNull
	@Override
	public DotNetXXXAccessor[] getAccessors()
	{
		return DotNetXXXAccessor.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return DotNetNamedElement.EMPTY_ARRAY;
	}
}
