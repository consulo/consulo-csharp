/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.doc.validation;

/**
 * @author VISTALL
 * @since 03.03.2015
 */
public class CSharpDocAttributeInfo
{
	public enum ValueType
	{
		TYPE_REFERENCE,
		TEXT
	}

	private final String myName;
	private final ValueType myValueType;

	public CSharpDocAttributeInfo(String name, ValueType valueType)
	{
		myName = name;
		myValueType = valueType;
	}

	public ValueType getValueType()
	{
		return myValueType;
	}

	public String getName()
	{
		return myName;
	}
}
