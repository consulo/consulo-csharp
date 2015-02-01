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

package org.mustbe.consulo.csharp.lang.psi.impl.stub.index;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttributeList;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeTargetType;
import com.intellij.psi.stubs.AbstractStubIndex;
import com.intellij.psi.stubs.StubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;
import com.intellij.util.io.KeyDescriptor;

/**
 * @author VISTALL
 * @since 09.12.14
 */
public class AttributeListIndex extends AbstractStubIndex<DotNetAttributeTargetType, CSharpAttributeList>
{
	public static class EnumeratorTypeDescriptor implements KeyDescriptor<DotNetAttributeTargetType>
	{
		public static final EnumeratorTypeDescriptor INSTANCE = new EnumeratorTypeDescriptor();

		@Override
		public int getHashCode(final DotNetAttributeTargetType value)
		{
			return value.ordinal();
		}

		@Override
		public boolean isEqual(final DotNetAttributeTargetType val1, final DotNetAttributeTargetType val2)
		{
			return val1 == val2;
		}

		@Override
		public void save(@NotNull final DataOutput storage, @NotNull final DotNetAttributeTargetType value) throws IOException
		{
			storage.writeInt(value.ordinal());
		}

		@Override
		public DotNetAttributeTargetType read(@NotNull final DataInput storage) throws IOException
		{
			int i = storage.readInt();
			return DotNetAttributeTargetType.values()[i];
		}
	}

	@NotNull
	@LazyInstance
	public static AttributeListIndex getInstance()
	{
		return StubIndexExtension.EP_NAME.findExtension(AttributeListIndex.class);
	}

	@NotNull
	@Override
	public StubIndexKey<DotNetAttributeTargetType, CSharpAttributeList> getKey()
	{
		return CSharpIndexKeys.ATTRIBUTE_LIST_INDEX;
	}

	@Override
	public int getVersion()
	{
		return 1;
	}

	@NotNull
	@Override
	public KeyDescriptor<DotNetAttributeTargetType> getKeyDescriptor()
	{
		return new EnumeratorTypeDescriptor();
	}
}
