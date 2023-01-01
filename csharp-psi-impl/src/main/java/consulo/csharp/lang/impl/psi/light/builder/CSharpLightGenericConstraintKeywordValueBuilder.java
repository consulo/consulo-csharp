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

import javax.annotation.Nonnull;

import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
import consulo.project.Project;
import consulo.language.ast.IElementType;

/**
 * @author VISTALL
 * @since 13.12.14
 */
public class CSharpLightGenericConstraintKeywordValueBuilder extends CSharpLightElementBuilder<CSharpLightGenericConstraintKeywordValueBuilder>
		implements CSharpGenericConstraintKeywordValue
{
	private final IElementType myElementType;

	public CSharpLightGenericConstraintKeywordValueBuilder(@Nonnull Project project, @Nonnull IElementType elementType)
	{
		super(project);
		myElementType = elementType;
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitGenericConstraintKeywordValue(this);
	}

	@Nonnull
	@Override
	public IElementType getKeywordElementType()
	{
		return myElementType;
	}
}
