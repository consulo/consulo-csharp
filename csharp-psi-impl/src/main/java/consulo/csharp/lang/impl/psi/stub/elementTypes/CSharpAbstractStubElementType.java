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

package consulo.csharp.lang.impl.psi.stub.elementTypes;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.source.CSharpPsiUtilImpl;
import consulo.csharp.lang.impl.psi.stub.CSharpIdentifierStub;
import consulo.language.ast.IElementTypeAsPsiFactory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.stub.IStubElementType;
import consulo.language.psi.stub.IndexSink;
import consulo.language.psi.stub.StubElement;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
public abstract class CSharpAbstractStubElementType<S extends StubElement, P extends PsiElement> extends IStubElementType<S,
		P> implements IElementTypeAsPsiFactory
{
	public CSharpAbstractStubElementType(@Nonnull @NonNls String debugName)
	{
		super(debugName, CSharpLanguage.INSTANCE);
	}

	@Nonnull
	@Override
	public String getExternalId()
	{
		return "csharp." + toString();
	}

	@Nullable
	@RequiredReadAction
	public static String getNameWithoutAt(StubElement<?> element)
	{
		CSharpIdentifierStub identifierStub = element.findChildStubByType(CSharpStubElements.IDENTIFIER);
		if(identifierStub == null)
		{
			return null;
		}
		return CSharpPsiUtilImpl.getNameWithoutAt(identifierStub.getValue());
	}

	@Override
	public void indexStub(@Nonnull S s, @Nonnull IndexSink indexSink)
	{

	}
}
