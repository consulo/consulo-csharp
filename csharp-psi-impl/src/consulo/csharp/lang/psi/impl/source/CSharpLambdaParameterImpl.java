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

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ArrayFactory;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.csharp.ide.refactoring.CSharpRefactoringUtil;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpLambdaParameter;
import consulo.csharp.lang.psi.CSharpLambdaParameterList;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.impl.source.resolve.genericInference.GenericInferenceUtil;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 19.01.14
 */
public class CSharpLambdaParameterImpl extends CSharpVariableImpl implements CSharpLambdaParameter
{
	public static final CSharpLambdaParameterImpl[] EMPTY_ARRAY = new CSharpLambdaParameterImpl[0];

	public static ArrayFactory<CSharpLambdaParameterImpl> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new CSharpLambdaParameterImpl[count];

	public CSharpLambdaParameterImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitLambdaParameter(this);
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl(boolean resolveFromInitializer)
	{
		DotNetTypeRef typeRef = toTypeRefImpl0(resolveFromInitializer);
		if(hasModifier(CSharpModifier.REF))
		{
			return new CSharpRefTypeRef(getProject(), CSharpRefTypeRef.Type.ref, typeRef);
		}
		else if(hasModifier(CSharpModifier.OUT))
		{
			return new CSharpRefTypeRef(getProject(), CSharpRefTypeRef.Type.out, typeRef);
		}
		return typeRef;
	}

	@RequiredReadAction
	@NotNull
	private DotNetTypeRef toTypeRefImpl0(boolean resolveFromInitializer)
	{
		DotNetType type = getType();
		if(type == null)
		{
			if(GenericInferenceUtil.isInsideGenericInferenceSession(PsiTreeUtil.getParentOfType(this, CSharpLambdaExpressionImpl.class)))
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

	@NotNull
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
	public PsiElement setName(@NonNls @NotNull String s) throws IncorrectOperationException
	{
		CSharpRefactoringUtil.replaceNameIdentifier(this, s);
		return this;
	}

	@NotNull
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
