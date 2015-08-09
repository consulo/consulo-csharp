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

package org.mustbe.consulo.csharp.ide.refactoring.introduceVariable;

import com.intellij.refactoring.RefactoringBundle;

/**
 * @author VISTALL
 * @since 26.03.14
 */
public class CSharpIntroduceVariableHandler extends CSharpIntroduceHandler
{
	public CSharpIntroduceVariableHandler()
	{
		super(RefactoringBundle.message("introduce.variable.title"));
	}

	@Override
	protected String getDeclarationString(CSharpIntroduceOperation operation, String initExpression)
	{
		return "var " + operation.getName() + " = " + initExpression + ";\n";
	}
}
