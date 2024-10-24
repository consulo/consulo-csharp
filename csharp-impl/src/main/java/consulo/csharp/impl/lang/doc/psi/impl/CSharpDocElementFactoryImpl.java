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

package consulo.csharp.impl.lang.doc.psi.impl;

import consulo.annotation.component.ServiceImpl;
import consulo.csharp.lang.doc.impl.psi.CSharpDocElements;
import consulo.csharp.lang.doc.psi.CSharpDocElementFactory;
import consulo.language.ast.IElementType;
import jakarta.inject.Singleton;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 26-Dec-17
 */
@Singleton
@ServiceImpl
public class CSharpDocElementFactoryImpl implements CSharpDocElementFactory
{
	@Nonnull
	@Override
	public IElementType getDocRootElementType()
	{
		return CSharpDocElements.LINE_DOC_COMMENT;
	}
}
