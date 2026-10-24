/*
 * Copyright 2013-2026 consulo.io
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

package consulo.csharp.lang.impl.psi.source;

import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.language.ast.IElementType;

/**
 * A pattern of a switch expression arm (C# 8/9): discard {@code _}, constant {@code 1}, relational {@code < 0},
 * {@code not}/{@code and}/{@code or}, parenthesized and property {@code { Name: p }} patterns. A declaration pattern
 * {@code Circle c} is a {@link CSharpCaseVariableImpl}.
 *
 * @author VISTALL
 */
public class CSharpPatternImpl extends CSharpElementImpl
{
	public CSharpPatternImpl(IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(CSharpElementVisitor visitor)
	{
		visitor.visitElement(this);
	}
}
