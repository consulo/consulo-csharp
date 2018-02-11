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

package consulo.csharp.lang.psi.impl.source;

import javax.annotation.Nonnull;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import com.intellij.psi.util.PsiModificationTracker;
import com.intellij.util.ArrayUtil;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpGenericConstraintUtil;
import consulo.csharp.lang.psi.CSharpGenericParameter;
import consulo.csharp.lang.psi.CSharpStubElements;
import consulo.csharp.lang.psi.impl.stub.CSharpGenericParameterStub;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetGenericParameterList;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 30.11.13.
 */
public class CSharpGenericParameterImpl extends CSharpStubMemberImpl<CSharpGenericParameterStub> implements CSharpGenericParameter
{
	public CSharpGenericParameterImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpGenericParameterImpl(@Nonnull CSharpGenericParameterStub stub)
	{
		super(stub, CSharpStubElements.GENERIC_PARAMETER);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
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
	@Nonnull
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
	@Nonnull
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return CachedValuesManager.getCachedValue(this, () -> CachedValueProvider.Result.create(CSharpGenericConstraintUtil.getExtendTypes(CSharpGenericParameterImpl.this), PsiModificationTracker
				.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT));
	}
}
