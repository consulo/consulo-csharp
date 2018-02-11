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

package consulo.csharp.lang.psi.impl.light.builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpGenericConstraintTypeValue;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.project.Project;

/**
 * @author VISTALL
 * @since 11.08.14
 */
public class CSharpLightGenericConstraintTypeValueBuilder extends CSharpLightElementBuilder<CSharpLightGenericConstraintTypeValueBuilder>
		implements CSharpGenericConstraintTypeValue
{
	private final DotNetTypeRef myTypeRef;

	public CSharpLightGenericConstraintTypeValueBuilder(@Nonnull Project project, @Nonnull DotNetTypeRef typeRef)
	{
		super(project);
		myTypeRef = typeRef;
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitGenericConstraintTypeValue(this);
	}

	@Nullable
	@Override
	public DotNetType getType()
	{
		return null;
	}

	@Nonnull
	@Override
	public DotNetTypeRef toTypeRef()
	{
		return myTypeRef;
	}
}
