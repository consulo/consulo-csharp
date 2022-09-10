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

package consulo.csharp.lang.impl.psi.stub.index;

import consulo.csharp.lang.psi.CSharpAttributeList;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import consulo.index.io.KeyDescriptor;
import consulo.language.psi.stub.AbstractStubIndex;
import consulo.language.psi.stub.StubIndexExtension;
import consulo.language.psi.stub.StubIndexKey;

import javax.annotation.Nonnull;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

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
		public int hashCode(final DotNetAttributeTargetType value)
		{
			return value.ordinal();
		}

		@Override
		public boolean equals(final DotNetAttributeTargetType val1, final DotNetAttributeTargetType val2)
		{
			return val1 == val2;
		}

		@Override
		public void save(@Nonnull final DataOutput storage, @Nonnull final DotNetAttributeTargetType value) throws IOException
		{
			storage.writeInt(value.ordinal());
		}

		@Override
		public DotNetAttributeTargetType read(@Nonnull final DataInput storage) throws IOException
		{
			int i = storage.readInt();
			return DotNetAttributeTargetType.values()[i];
		}
	}

	@Nonnull
	public static AttributeListIndex getInstance()
	{
		return StubIndexExtension.EP_NAME.findExtensionOrFail(AttributeListIndex.class);
	}

	@Nonnull
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

	@Nonnull
	@Override
	public KeyDescriptor<DotNetAttributeTargetType> getKeyDescriptor()
	{
		return new EnumeratorTypeDescriptor();
	}
}
