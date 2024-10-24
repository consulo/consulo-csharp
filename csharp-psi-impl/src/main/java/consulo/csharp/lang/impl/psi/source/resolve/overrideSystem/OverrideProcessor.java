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

package consulo.csharp.lang.impl.psi.source.resolve.overrideSystem;

import consulo.dotnet.psi.DotNetVirtualImplementOwner;
import consulo.util.collection.SmartList;

import jakarta.annotation.Nonnull;
import java.util.List;

/**
 * @author VISTALL
 * @since 16.12.14
 */
public interface OverrideProcessor
{
	public static final OverrideProcessor ALWAYS_TRUE = new OverrideProcessor()
	{
		@Override
		public boolean elementOverride(DotNetVirtualImplementOwner target, DotNetVirtualImplementOwner hidedMember)
		{
			return true;
		}
	};

	public static class Collector implements OverrideProcessor
	{
		private final List<DotNetVirtualImplementOwner> myResults = new SmartList<DotNetVirtualImplementOwner>();

		@Override
		public boolean elementOverride(DotNetVirtualImplementOwner target, DotNetVirtualImplementOwner hidedMember)
		{
			myResults.add(hidedMember);
			return true;
		}

		@Nonnull
		public List<DotNetVirtualImplementOwner> getResults()
		{
			return myResults;
		}
	}

	public boolean elementOverride(DotNetVirtualImplementOwner target, DotNetVirtualImplementOwner hidedMember);
}
