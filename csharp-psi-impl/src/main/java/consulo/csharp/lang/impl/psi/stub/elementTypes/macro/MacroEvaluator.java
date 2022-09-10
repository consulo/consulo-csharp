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

package consulo.csharp.lang.impl.psi.stub.elementTypes.macro;

import consulo.util.lang.StringUtil;
import gnu.jel.CompilationException;
import gnu.jel.CompiledExpression;
import gnu.jel.DVMap;
import gnu.jel.Evaluator;
import gnu.jel.Library;

import java.util.Set;

/**
 * @author VISTALL
 * @since 21.02.14
 */
public class MacroEvaluator
{
	private static Library ourLibrary = new Library(null, new Class[]{MacroValueProvider.class}, null, new DVMap()
	{
		@Override
		public String getTypeName(String s)
		{
			return "MacroValue";
		}
	}, null);

	public static boolean evaluate(String text, Set<String> variables)
	{
		if(StringUtil.isEmptyOrSpaces(text))
		{
			return true;
		}
		try
		{
			CompiledExpression compile = Evaluator.compile(text, ourLibrary);
			return compile.evaluate_boolean(new Object[]{new MacroValueProvider(variables)});
		}
		catch(Throwable throwable)
		{
			// if expression is invalid do not break the code
			return throwable instanceof CompilationException;
		}
	}
}
