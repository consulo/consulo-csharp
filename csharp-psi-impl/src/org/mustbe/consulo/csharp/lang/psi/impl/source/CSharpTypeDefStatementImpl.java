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

package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.CSharpTokens;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.util.CSharpResolveUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpTypeDefStub;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.typeStub.CSharpStubTypeInfoUtil;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.ResolveState;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.util.IncorrectOperationException;

/**
 * @author VISTALL
 * @since 11.02.14
 */
public class CSharpTypeDefStatementImpl extends CSharpStubElementImpl<CSharpTypeDefStub> implements DotNetNamedElement, PsiNameIdentifierOwner,
		CSharpUsingListChild
{
	public CSharpTypeDefStatementImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpTypeDefStatementImpl(@NotNull CSharpTypeDefStub stub)
	{
		super(stub, CSharpStubElements.TYPE_DEF_STATEMENT);
	}

	@Override
	public String getName()
	{
		CSharpTypeDefStub stub = getStub();
		if(stub != null)
		{
			return stub.getName();
		}
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? null : nameIdentifier.getText();
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitTypeDefStatement(this);
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		return null;
	}

	@Nullable
	public DotNetType getType()
	{
		return findChildByClass(DotNetType.class);
	}

	@NotNull
	public DotNetTypeRef toTypeRef()
	{
		CSharpTypeDefStub stub = getStub();
		if(stub != null)
		{
			return CSharpStubTypeInfoUtil.toTypeRef(stub.getTypeInfo(), this);
		}
		DotNetType type = getType();
		return type == null ? DotNetTypeRef.ERROR_TYPE : type.toTypeRef();
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return findChildByType(CSharpTokens.IDENTIFIER);
	}

	@Nullable
	@Override
	public PsiElement getReferenceElement()
	{
		return getType();
	}

	@Override
	public int getTextOffset()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? super.getTextOffset() : nameIdentifier.getTextOffset();
	}

	@Override
	public boolean processDeclarations(@NotNull PsiScopeProcessor processor, @NotNull ResolveState state, PsiElement lastParent,
			@NotNull PsiElement place)
	{
		if(!processor.execute(this, state))
		{
			return false;
		}

		DotNetTypeRef dotNetTypeRef = toTypeRef();
		PsiElement resolve = dotNetTypeRef.resolve(this);
		if(resolve == null)
		{
			return true;
		}

		DotNetGenericExtractor extractor = dotNetTypeRef.getGenericExtractor(resolve, this);

		ResolveState initial = ResolveState.initial();
		return resolve.processDeclarations(processor, initial.put(CSharpResolveUtil.EXTRACTOR_KEY, extractor), lastParent, place);
	}
}
