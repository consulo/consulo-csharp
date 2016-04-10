/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import java.util.LinkedHashSet;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.Processor;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class AsPsiElementProcessor implements Processor<ResolveResult>
{
	private Set<PsiElement> myElements = new LinkedHashSet<PsiElement>();

	@Override
	public boolean process(ResolveResult resolveResult)
	{
		PsiElement element = resolveResult.getElement();
		myElements.add(element);
		return true;
	}

	@NotNull
	public Set<PsiElement> getElements()
	{
		return myElements;
	}
}
