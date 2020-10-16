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

package consulo.csharp.lang.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.psi.impl.source.tree.CompositePsiElement;
import com.intellij.psi.tree.ICompositeElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ReflectionUtil;
import consulo.logging.Logger;
import consulo.psi.tree.ElementTypeAsPsiFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Constructor;

/**
 * @author VISTALL
 * @since 2020-10-15
 */
public class CompositeElementTypeAsPsiFactory extends IElementType implements ICompositeElementType
{
	private static final Logger LOGGER = Logger.getInstance(ElementTypeAsPsiFactory.class);

	private Constructor<? extends CompositePsiElement> myConstructor;

	public CompositeElementTypeAsPsiFactory(@Nonnull String debugName, @Nullable Language language, @Nonnull Class<? extends CompositePsiElement> clazz)
	{
		this(debugName, language, true, clazz);
	}

	public CompositeElementTypeAsPsiFactory(@Nonnull String debugName,
											@Nullable Language language,
											boolean register,
											@Nonnull Class<? extends CompositePsiElement> clazz)
	{
		super(debugName, language, register);

		try
		{
			myConstructor = clazz.getConstructor(IElementType.class);
		}
		catch(NoSuchMethodException e)
		{
			LOGGER.error("Can't find constructor for " + clazz.getName() + " with argument: " + IElementType.class.getName() + ", or it not public.", e);
		}
	}

	@Nonnull
	@Override
	public ASTNode createCompositeNode()
	{
		if(myConstructor == null)
		{
			throw new UnsupportedOperationException(toString() + " can't initialized");
		}
		return ReflectionUtil.createInstance(myConstructor, this);
	}
}
