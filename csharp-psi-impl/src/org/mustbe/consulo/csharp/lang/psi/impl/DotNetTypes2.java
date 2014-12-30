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

package org.mustbe.consulo.csharp.lang.psi.impl;

/**
 * @author VISTALL
 * @since 27.11.14
 */
public interface DotNetTypes2
{
	interface System
	{
		interface Runtime
		{
			interface CompilerServices
			{
				String InternalsVisibleToAttribute = "System.Runtime.CompilerServices.InternalsVisibleToAttribute";
				String AsyncStateMachineAttribute = "System.Runtime.CompilerServices.AsyncStateMachineAttribute";
			}
		}

		interface Linq
		{
			String IGrouping$2 = "System.Linq.IGrouping`2";
		}

		interface Threading
		{
			interface Tasks
			{

				String Task = "System.Threading.Tasks.Task";
				String Task$1 = "System.Threading.Tasks.Task`1";
			}
		}

		interface Collections
		{
			interface Generic
			{
				String IEnumerable$1 = "System.Collections.Generic.IEnumerable`1";
				String IEnumerator$1 = "System.Collections.Generic.IEnumerator`1";
				String IList$1 = "System.Collections.Generic.IList`1";
			}

			String IEnumerable = "System.Collections.IEnumerable";
			String IEnumerator = "System.Collections.IEnumerator";
		}
	}
}
