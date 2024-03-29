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

package consulo.csharp.lang.impl.psi.msil.typeParsing;

/**
 * @author VISTALL
 * @since 11.07.14
 */
public class UserType implements SomeType
{
	private final String myText;

	public UserType(String text)
	{
		myText = text;
	}

	public String getText()
	{
		return myText;
	}

	@Override
	public String toString()
	{
		return myText;
	}

	@Override
	public void accept(SomeTypeVisitor visitor)
	{
		visitor.visitUserType(this);
	}
}
