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

package consulo.csharp.lang.psi.impl;

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

			interface InteropServices
			{
				String InAttribute = "System.Runtime.InteropServices.InAttribute";
				String OutAttribute = "System.Runtime.InteropServices.OutAttribute";
			}
		}

		interface Linq
		{
			String IGrouping$2 = "System.Linq.IGrouping`2";
			String Enumerable = "System.Linq.Enumerable";
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

		interface Diagnostics
		{
			String DebuggerDisplayAttribute = "System.Diagnostics.DebuggerDisplayAttribute";
		}

		String RuntimeArgumentHandle = "System.RuntimeArgumentHandle";
		String FlagsAttribute  = "System.FlagsAttribute";
		String FormattableString  = "System.FormattableString";
		String NullReferenceException  = "System.NullReferenceException";
	}
}
