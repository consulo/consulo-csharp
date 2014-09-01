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
import com.intellij.util.io.StringRef;

/**
 * @author VISTALL
 * @since 15.01.14
 */
public class CSharpStubQualifiedTypeInfo extends CSharpStubTypeInfo
{
	private String myQualifiedText;
	private boolean myNullable;

	public CSharpStubQualifiedTypeInfo(String qualifiedText, boolean nullable)
	{
		myQualifiedText = qualifiedText;
		myNullable = nullable;
	}

	public CSharpStubQualifiedTypeInfo(StubInputStream inputStream) throws IOException
	{
		myQualifiedText = StringRef.toString(inputStream.readName());
		myNullable = inputStream.readBoolean();
	}

	@Override
	public void writeTo(StubOutputStream stubOutputStream) throws IOException
	{
		super.writeTo(stubOutputStream);
		stubOutputStream.writeName(myQualifiedText);
		stubOutputStream.writeBoolean(myNullable);
	}

	public boolean isNullable()
	{
		return myNullable;
	}

	public String getQualifiedText()
	{
		return myQualifiedText;
	}

	@Override
	public Id getId()
	{
		return Id.QUALIFIED;
	}
}
