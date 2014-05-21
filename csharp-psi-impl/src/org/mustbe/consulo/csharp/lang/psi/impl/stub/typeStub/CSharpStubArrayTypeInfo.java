/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.stub.typeStub;

import java.io.IOException;

import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.stubs.StubOutputStream;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpStubArrayTypeInfo extends CSharpStubTypeInfo
{
	private final CSharpStubTypeInfo myInnerType;
	private final int myDimensions;

	public CSharpStubArrayTypeInfo(CSharpStubTypeInfo inner, int dimensions)
	{
		myInnerType = inner;
		myDimensions = dimensions;
	}

	public CSharpStubArrayTypeInfo(StubInputStream stubInputStream) throws IOException
	{
		myInnerType = CSharpStubTypeInfoUtil.read(stubInputStream);
		myDimensions = stubInputStream.readVarInt();
	}

	@Override
	public void writeTo(StubOutputStream stubOutputStream) throws IOException
	{
		super.writeTo(stubOutputStream);

		myInnerType.writeTo(stubOutputStream);
		stubOutputStream.writeVarInt(myDimensions);
	}

	public CSharpStubTypeInfo getInnerType()
	{
		return myInnerType;
	}

	@Override
	public Id getId()
	{
		return Id.ARRAY;
	}

	public int getDimensions()
	{
		return myDimensions;
	}
}
