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

package consulo.csharp.lang.psi.impl.stub;

import javax.annotation.Nullable;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.BitUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.impl.source.CSharpStubParameterImpl;
import consulo.csharp.lang.psi.impl.source.CSharpStubVariableImpl;
import consulo.csharp.lang.psi.impl.source.CSharpStubVariableImplUtil;
import consulo.csharp.lang.psi.impl.stub.elementTypes.CSharpAbstractStubElementType;
import consulo.dotnet.psi.DotNetVariable;

/**
 * @author VISTALL
 * @since 21.12.13.
 */
public class CSharpVariableDeclStub<V extends DotNetVariable> extends MemberStub<V>
{
	public static final int CONSTANT_MASK = 1 << 0;
	public static final int MULTIPLE_DECLARATION_MASK = 1 << 1;
	public static final int OPTIONAL = 1 << 2;

	public CSharpVariableDeclStub(StubElement parent, CSharpAbstractStubElementType<?, ?> elementType, @Nullable String namespaceQName, int flags)
	{
		super(parent, elementType, namespaceQName, flags);
	}

	@RequiredReadAction
	public static int getOtherModifierMask(DotNetVariable variable)
	{
		int i = 0;
		i |= BitUtil.set(i, CONSTANT_MASK, variable.isConstant());
		if(variable instanceof CSharpStubVariableImpl)
		{
			i |= BitUtil.set(i, MULTIPLE_DECLARATION_MASK, CSharpStubVariableImplUtil.isMultipleDeclaration((CSharpStubVariableImpl<?>) variable));
		}
		if(variable instanceof CSharpStubParameterImpl)
		{
			i |= BitUtil.set(i, OPTIONAL, variable.getInitializer() != null);
		}
		return i;
	}

	public boolean isConstant()
	{
		return BitUtil.isSet(getOtherModifierMask(), CONSTANT_MASK);
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
