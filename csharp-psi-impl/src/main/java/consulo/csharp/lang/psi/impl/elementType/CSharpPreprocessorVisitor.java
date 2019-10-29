/*
 * Copyright 2013-2019 consulo.io
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

package consulo.csharp.lang.psi.impl.elementType;

import com.intellij.psi.PsiElement;
import com.intellij.util.ObjectUtil;
import consulo.csharp.lang.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.parser.PreprocessorState;
import consulo.csharp.lang.psi.CSharpPreprocessorElements;
import consulo.csharp.lang.psi.CSharpStoppableRecursiveElementVisitor;
import consulo.ui.RequiredUIAccess;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2019-10-29
 */
public class CSharpPreprocessorVisitor extends CSharpStoppableRecursiveElementVisitor<Object>
{
	private final PreprocessorState myState = new PreprocessorState();
	private final Set<String> myVariables = new HashSet<>();
	private final int myStopOffset;

	public CSharpPreprocessorVisitor(Set<String> stableDefines, int stopOffset)
	{
		myVariables.addAll(stableDefines);
		myStopOffset = stopOffset;
	}

	@RequiredUIAccess
	@Override
	public void visitElement(PsiElement element)
	{
		if(element.getStartOffsetInParent() >= myStopOffset)
		{
			stopWalk(ObjectUtil.NULL);
			return;
		}

		if(element.getNode().getElementType() == CSharpPreprocessorElements.PREPROCESSOR_DIRECTIVE)
		{
			CSharpBuilderWrapper.processState(myState, () -> myVariables, it -> {
				myVariables.clear();
				myVariables.addAll(it);
			}, element.getNode().getChars());
		}

		super.visitElement(element);
	}

	@Nonnull
	public Set<String> getVariables()
	{
		return myVariables;
	}
}
