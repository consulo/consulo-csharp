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

package consulo.csharp.ide.codeInspection.unusedSymbol;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;

/**
 * @author VISTALL
 * @since 20.05.14
 */
class UnusedSymbolVisitor extends CSharpElementVisitor
{
	private Map<PsiNameIdentifierOwner, Boolean> myVariableStates = new HashMap<>();

	@Override
	public void visitLocalVariable(CSharpLocalVariable variable)
	{
		Query<PsiReference> query = ReferencesSearch.search(variable, variable.getUseScope());

		myVariableStates.put(variable, query.findFirst() != null);
	}

	@NotNull
	public Collection<Map.Entry<PsiNameIdentifierOwner, Boolean>> getVariableStates()
	{
		return myVariableStates.entrySet();
	}
}
