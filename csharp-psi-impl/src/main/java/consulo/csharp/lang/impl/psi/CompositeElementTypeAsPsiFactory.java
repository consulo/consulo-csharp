/*
 * Copyright 2013-2020 consulo.io
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

package consulo.csharp.lang.impl.psi;


import consulo.language.Language;
import consulo.language.ast.ASTNode;
import consulo.language.ast.ICompositeElementType;
import consulo.language.ast.IElementType;
import consulo.language.impl.psi.CompositePsiElement;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.function.Function;

/**
 * @author VISTALL
 * @since 2020-10-15
 */
public class CompositeElementTypeAsPsiFactory extends IElementType implements ICompositeElementType
{
	private final Function<IElementType, ? extends CompositePsiElement> myFactory;

	public CompositeElementTypeAsPsiFactory(@Nonnull String debugName, @Nullable Language language, @Nonnull Function<IElementType, ? extends CompositePsiElement> factory)
	{
		this(debugName, language, true, factory);
	}

	public CompositeElementTypeAsPsiFactory(@Nonnull String debugName,
											@Nullable Language language,
											boolean register,
											@Nonnull Function<IElementType, ? extends CompositePsiElement> factory)
	{
		super(debugName, language, register);

		myFactory = factory;
	}

	@Nonnull
	@Override
	public ASTNode createCompositeNode()
	{
		return myFactory.apply(this);
	}
}
