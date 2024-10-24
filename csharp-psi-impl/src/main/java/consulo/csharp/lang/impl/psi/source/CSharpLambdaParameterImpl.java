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

import consulo.language.ast.IElementType;
import consulo.language.psi.PsiElement;
import consulo.language.psi.scope.LocalSearchScope;
import consulo.content.scope.SearchScope;
import consulo.language.psi.PsiModificationTracker;
import consulo.util.collection.ArrayFactory;
import consulo.language.util.IncorrectOperationException;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.impl.ide.refactoring.CSharpRefactoringUtil;
import consulo.csharp.lang.impl.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpLambdaParameter;
import consulo.csharp.lang.psi.CSharpLambdaParameterList;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.impl.psi.source.resolve.genericInference.GenericInferenceManager;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpRefTypeRef;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.ArrayUtil;
import jakarta.annotation.Nullable;
import org.jetbrains.annotations.NonNls;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 19.01.14
 */
public class CSharpLambdaParameterImpl extends CSharpVariableImpl implements CSharpLambdaParameter
{
	public static final CSharpLambdaParameterImpl[] EMPTY_ARRAY = new CSharpLambdaParameterImpl[0];

	public static ArrayFactory<CSharpLambdaParameterImpl> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new CSharpLambdaParameterImpl[count];

	public CSharpLambdaParameterImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitLambdaParameter(this);
	}

	@Nonnull
	@Override
	protected Object[] getCacheKeys()
	{
		return new Object[]{
				PsiModificationTracker.MODIFICATION_COUNT,
				GenericInferenceManager.getInstance(getProject())
		};
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromInitializer)
	{
		DotNetTypeRef typeRef = toTypeRefImpl0(resolveFromInitializer);
		if(hasModifier(CSharpModifier.REF))
		{
			return new CSharpRefTypeRef(getProject(), getResolveScope(), CSharpRefTypeRef.Type.ref, typeRef);
		}
		else if(hasModifier(CSharpModifier.OUT))
		{
			return new CSharpRefTypeRef(getProject(), getResolveScope(), CSharpRefTypeRef.Type.out, typeRef);
		}
		return typeRef;
	}

	@RequiredReadAction
	@Nonnull
	private DotNetTypeRef toTypeRefImpl0(boolean resolveFromInitializer)
	{
		DotNetType type = getType();
		if(type == null)
		{
			if(GenericInferenceManager.getInstance(getProject()).isInsideGenericInferenceSession(PsiTreeUtil.getParentOfType(this, CSharpLambdaExpressionImpl.class)))
			{
				return resolveTypeForParameter();
			}

			return resolveFromInitializer ? resolveTypeForParameter() : DotNetTypeRef.AUTO_TYPE;
		}

		return type.toTypeRef();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetType getType()
	{
		return findChildByClass(DotNetType.class);
	}

	@Nonnull
	@RequiredReadAction
	private DotNetTypeRef resolveTypeForParameter()
	{
		CSharpLambdaExpressionImpl lambdaExpression = PsiTreeUtil.getParentOfType(this, CSharpLambdaExpressionImpl.class);
		if(lambdaExpression == null)
		{
			return DotNetTypeRef.UNKNOWN_TYPE;
		}
		CSharpLambdaParameter[] parameters = ((CSharpLambdaParameterListImpl) getParent()).getParameters();

		int i = ArrayUtil.indexOf(parameters, this);

		return CSharpLambdaExpressionImplUtil.resolveTypeForParameter(lambdaExpression, i);
	}

	@RequiredWriteAction
	@Override
	public PsiElement setName(@NonNls @Nonnull String s) throws IncorrectOperationException
	{
		CSharpRefactoringUtil.replaceNameIdentifier(this, s);
		return this;
	}

	@Nonnull
	@Override
	public SearchScope getUseScope()
	{
		return new LocalSearchScope(getParent().getParent());
	}

	@Override
	public int getIndex()
	{
		CSharpLambdaParameterList parameterList = PsiTreeUtil.getParentOfType(this, CSharpLambdaParameterList.class);
		assert parameterList != null;
		return ArrayUtil.indexOf(parameterList.getParameters(), this);
	}
}
