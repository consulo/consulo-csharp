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

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpGenericConstraintKeywordValue;
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
