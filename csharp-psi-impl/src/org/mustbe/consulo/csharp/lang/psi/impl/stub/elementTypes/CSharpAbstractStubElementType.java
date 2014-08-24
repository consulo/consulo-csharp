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

package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.tree.IElementTypeAsPsiFactory;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public abstract class CSharpAbstractStubElementType<S extends StubElement, P extends PsiElement> extends IStubElementType<S,
		P> implements IElementTypeAsPsiFactory
{
	public CSharpAbstractStubElementType(@NotNull @NonNls String debugName)
	{
		super(debugName, CSharpLanguage.INSTANCE);
	}

	@NotNull
	@Override
	public String getExternalId()
	{
		return "csharp." + toString();
	}

	@Override
	public void indexStub(@NotNull S s, @NotNull IndexSink indexSink)
	{

	}
}
