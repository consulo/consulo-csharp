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

package consulo.csharp.lang.psi.impl.stub.elementTypes;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.impl.stub.CSharpIdentifierStub;
import com.intellij.psi.PsiElement;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.IndexSink;
import com.intellij.psi.stubs.StubElement;
import consulo.annotations.RequiredReadAction;
import consulo.psi.tree.IElementTypeAsPsiFactory;

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

	@Nullable
	@RequiredReadAction
	public static String getName(StubElement<?> element)
	{
		CSharpIdentifierStub identifierStub = (CSharpIdentifierStub) element.findChildStubByType(CSharpStubElements.IDENTIFIER);
		if(identifierStub == null)
		{
			return null;
		}
		return identifierStub.getValue();
	}

	@Override
	public void indexStub(@NotNull S s, @NotNull IndexSink indexSink)
	{

	}
}
