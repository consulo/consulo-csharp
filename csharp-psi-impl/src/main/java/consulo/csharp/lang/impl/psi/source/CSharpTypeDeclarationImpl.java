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

package consulo.csharp.lang.impl.psi.source;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.impl.psi.CSharpStubElementSets;
import consulo.csharp.lang.impl.psi.CSharpStubElements;
import consulo.csharp.lang.impl.psi.source.resolve.ExecuteTarget;
import consulo.csharp.lang.impl.psi.source.resolve.ExecuteTargetUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpTypeRefByQName;
import consulo.csharp.lang.impl.psi.stub.CSharpTypeDeclStub;
import consulo.csharp.lang.psi.CSharpGenericConstraint;
import consulo.csharp.lang.psi.CSharpGenericConstraintList;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.ASTNode;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.resolve.PsiScopeProcessor;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.util.LanguageCachedValueUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 28.11.13.
 */
public class CSharpTypeDeclarationImpl extends CSharpStubMemberImpl<CSharpTypeDeclStub> implements CSharpTypeDeclaration
{
	public CSharpTypeDeclarationImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	public CSharpTypeDeclarationImpl(@Nonnull CSharpTypeDeclStub stub)
	{
		super(stub, CSharpStubElements.TYPE_DECLARATION);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitTypeDeclaration(this);
	}

	@RequiredReadAction
	@Override
	public PsiElement getLeftBrace()
	{
		return findChildByType(CSharpTokens.LBRACE);
	}

	@RequiredReadAction
	@Override
	public PsiElement getRightBrace()
	{
		return findChildByType(CSharpTokens.RBRACE);
	}

	@Nullable
	@Override
	public DotNetGenericParameterList getGenericParameterList()
	{
		return getStubOrPsiChild(CSharpStubElements.GENERIC_PARAMETER_LIST);
	}

	@Nonnull
	@Override
	public DotNetGenericParameter[] getGenericParameters()
	{
		DotNetGenericParameterList genericParameterList = getGenericParameterList();
		return genericParameterList == null ? DotNetGenericParameter.EMPTY_ARRAY : genericParameterList.getParameters();
	}

	@Override
	public int getGenericParametersCount()
	{
		DotNetGenericParameterList genericParameterList = getGenericParameterList();
		return genericParameterList == null ? 0 : genericParameterList.getGenericParametersCount();
	}

	@RequiredReadAction
	@Override
	public String getVmQName()
	{
		return DotNetTypeDeclarationUtil.getVmQName(this);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getVmName()
	{
		return DotNetTypeDeclarationUtil.getVmName(this);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetQualifiedElement[] getMembers()
	{
		return LanguageCachedValueUtil.getCachedValue(this, () -> CachedValueProvider.Result.create(getStubOrPsiChildren(CSharpStubElementSets.QUALIFIED_MEMBERS,
				DotNetQualifiedElement.ARRAY_FACTORY),

				PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT));
	}

	@RequiredReadAction
	@Override
	public boolean isInterface()
	{
		CSharpTypeDeclStub stub = getGreenStub();
		if(stub != null)
		{
			return stub.isInterface();
		}
		return findChildByType(CSharpTokens.INTERFACE_KEYWORD) != null;
	}

	@RequiredReadAction
	@Override
	public boolean isStruct()
	{
		CSharpTypeDeclStub stub = getGreenStub();
		if(stub != null)
		{
			return stub.isStruct();
		}
		return findChildByType(CSharpTokens.STRUCT_KEYWORD) != null;
	}

	@RequiredReadAction
	@Override
	public boolean isEnum()
	{
		CSharpTypeDeclStub stub = getGreenStub();
		if(stub != null)
		{
			return stub.isEnum();
		}
		return findChildByType(CSharpTokens.ENUM_KEYWORD) != null;
	}

	@RequiredReadAction
	@Override
	public boolean isNested()
	{
		PsiElement parentByStub = getParentByStub();
		return parentByStub instanceof DotNetTypeDeclaration;
	}

	@Override
	public boolean processDeclarations(@Nonnull PsiScopeProcessor processor, @Nonnull ResolveState state, PsiElement lastParent, @Nonnull PsiElement place)
	{
		if(ExecuteTargetUtil.canProcess(processor, ExecuteTarget.GENERIC_PARAMETER))
		{
			for(DotNetGenericParameter dotNetGenericParameter : getGenericParameters())
			{
				if(!processor.execute(dotNetGenericParameter, state))
				{
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		return CSharpTypeDeclarationImplUtil.isEquivalentTo(this, another);
	}

	@RequiredReadAction
	@Override
	public DotNetTypeList getExtendList()
	{
		return getStubOrPsiChild(CSharpStubElements.EXTENDS_LIST);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return LanguageCachedValueUtil.getCachedValue(this, new CachedValueProvider<DotNetTypeRef[]>()
		{
			@Nullable
			@Override
			@RequiredReadAction
			public Result<DotNetTypeRef[]> compute()
			{
				DotNetTypeRef[] extendTypeRefs = CSharpTypeDeclarationImplUtil.getExtendTypeRefs(CSharpTypeDeclarationImpl.this);
				return Result.create(extendTypeRefs, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
			}
		});
	}

	@RequiredReadAction
	@Override
	public boolean isInheritor(@Nonnull String other, boolean deep)
	{
		return DotNetInheritUtil.isInheritor(this, other, deep);
	}

	@Nonnull
	@RequiredReadAction
	@Override
	public DotNetTypeRef getTypeRefForEnumConstants()
	{
		return LanguageCachedValueUtil.getCachedValue(this, new CachedValueProvider<DotNetTypeRef>()
		{
			@Nullable
			@Override
			@RequiredReadAction
			public Result<DotNetTypeRef> compute()
			{
				DotNetTypeRef typeRef;
				DotNetTypeList extendList = getExtendList();
				if(extendList == null)
				{
					typeRef = new CSharpTypeRefByQName(CSharpTypeDeclarationImpl.this, DotNetTypes.System.Int32);
				}
				else
				{
					DotNetTypeRef[] typeRefs = extendList.getTypeRefs();
					typeRef = typeRefs.length == 0 ? new CSharpTypeRefByQName(CSharpTypeDeclarationImpl.this, DotNetTypes.System.Int32) : typeRefs[0];
				}
				return Result.create(typeRef, PsiModificationTracker.OUT_OF_CODE_BLOCK_MODIFICATION_COUNT);
			}
		});
	}

	@Nullable
	@Override
	public CSharpGenericConstraintList getGenericConstraintList()
	{
		return getStubOrPsiChild(CSharpStubElements.GENERIC_CONSTRAINT_LIST);
	}

	@Nonnull
	@Override
	public CSharpGenericConstraint[] getGenericConstraints()
	{
		CSharpGenericConstraintList genericConstraintList = getGenericConstraintList();
		return genericConstraintList == null ? CSharpGenericConstraint.EMPTY_ARRAY : genericConstraintList.getGenericConstraints();
	}
}
