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

package consulo.csharp.lang.doc.psi;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.application.Application;
import consulo.language.ast.IElementType;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 26-Dec-17
 */
@ServiceAPI(ComponentScope.APPLICATION)
public interface CSharpDocElementFactory
{
	static IElementType create()
	{
		CSharpDocElementFactory factory = Application.get().getInstance(CSharpDocElementFactory.class);
		return factory.getDocRootElementType();
	}

	@Nonnull
	IElementType getDocRootElementType();
}
