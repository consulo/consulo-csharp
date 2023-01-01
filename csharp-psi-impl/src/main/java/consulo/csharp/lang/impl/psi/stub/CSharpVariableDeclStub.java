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

package consulo.csharp.lang.impl.psi.stub;

import javax.annotation.Nullable;

import consulo.util.lang.BitUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.impl.psi.source.CSharpStubParameterImpl;
import consulo.csharp.lang.impl.psi.source.CSharpStubVariableImpl;
import consulo.csharp.lang.impl.psi.source.CSharpStubVariableImplUtil;
import consulo.csharp.lang.impl.psi.stub.elementTypes.CSharpAbstractStubElementType;
import consulo.dotnet.psi.DotNetVariable;
import consulo.language.psi.stub.StubElement;

/**
 * @author VISTALL
 * @since 21.12.13.
 */
public class CSharpVariableDeclStub<V extends DotNetVariable> extends MemberStub<V>
{
	public static final int CONSTANT_MASK = 1 << 0;
	public static final int MULTIPLE_DECLARATION_MASK = 1 << 1;
	public static final int OPTIONAL = 1 << 2;
	public static final int AUTO_GET = 1 << 3;

	private String myInitializerText;

	public CSharpVariableDeclStub(StubElement parent, CSharpAbstractStubElementType<?, ?> elementType, @Nullable String namespaceQName, int flags, @Nullable String initializerText)
	{
		super(parent, elementType, namespaceQName, flags);
		myInitializerText = initializerText;
	}

	@RequiredReadAction
	public static int getOtherModifierMask(DotNetVariable variable)
	{
		int i = 0;
		i = BitUtil.set(i, CONSTANT_MASK, variable.isConstant());
		if(variable instanceof CSharpStubVariableImpl)
		{
			i = BitUtil.set(i, MULTIPLE_DECLARATION_MASK, CSharpStubVariableImplUtil.isMultipleDeclaration((CSharpStubVariableImpl<?>) variable));
		}
		if(variable instanceof CSharpStubParameterImpl)
		{
			i = BitUtil.set(i, OPTIONAL, variable.getInitializer() != null);
		}
		if(variable instanceof CSharpPropertyDeclaration)
		{
			i = BitUtil.set(i, AUTO_GET, ((CSharpPropertyDeclaration) variable).isAutoGet());
		}
		return i;
	}

	@Nullable
	public String getInitializerText()
	{
		return myInitializerText;
	}

	public boolean isConstant()
	{
		return BitUtil.isSet(getOtherModifierMask(), CONSTANT_MASK);
	}

	public boolean isAutoGet()
	{
		return BitUtil.isSet(getOtherModifierMask(), AUTO_GET);
	}

	public boolean isMultipleDeclaration()
	{
		return BitUtil.isSet(getOtherModifierMask(), MULTIPLE_DECLARATION_MASK);
	}

	public boolean isOptional()
	{
		return BitUtil.isSet(getOtherModifierMask(), OPTIONAL);
	}
}
