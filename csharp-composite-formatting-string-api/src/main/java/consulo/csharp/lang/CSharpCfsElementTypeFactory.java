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

package consulo.csharp.lang;

import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.psi.tree.IElementType;

/**
 * @author VISTALL
 * @since 26-Dec-17
 */
public interface CSharpCfsElementTypeFactory
{
	@NotNull
	static IElementType create()
	{
		CSharpCfsElementTypeFactory factory = ServiceManager.getService(CSharpCfsElementTypeFactory.class);
		return factory.getInterpolationStringElementType();
	}

	@NotNull
	IElementType getInterpolationStringElementType();
}