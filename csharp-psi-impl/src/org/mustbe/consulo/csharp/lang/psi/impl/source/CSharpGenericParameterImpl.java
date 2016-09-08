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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpStubElements;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpGenericParameterStub;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.ArrayUtil;
import consulo.csharp.psi.CSharpGenericParameter;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpGenericParameterImpl extends CSharpStubMemberImpl<CSharpGenericParameterStub> implements CSharpGenericParameter
{
	public CSharpGenericParameterImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpGenericParameterImpl(@NotNull CSharpGenericParameterStub stub)
	{
		super(stub, CSharpStubElements.GENERIC_PARAMETER);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitGenericParameter(this);
	}

	@Override
	public int getIndex()
	{
		PsiElement parentByStub = getParentByStub();
		if(parentByStub instanceof DotNetGenericParameterList)
		{
			return ArrayUtil.find(((DotNetGenericParameterList) parentByStub).getParameters(), this);
		}
		return -1;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetAttribute[] getAttributes()
	{
		DotNetModifierList modifierList = getModifierList();
		if(modifierList != null)
		{
			return modifierList.getAttributes();
		}
		return DotNetAttribute.EMPTY_ARRAY;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return CachedValuesManager.getCachedValue(this, new CachedValueProvider<DotNetTypeRef[]>()
		{
			@Nullable
			@Override
			@RequiredReadAction
			public Result<DotNetTypeRef[]> compute()
			{
				return Result.create(CSharpGenericConstraintUtil.getExtendTypes(CSharpGenericParameterImpl.this), PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
			}
		});
	}
}
