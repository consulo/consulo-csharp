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

package consulo.csharp.lang.psi.impl.source.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayFactory;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.*;
import consulo.csharp.lang.psi.impl.source.CSharpCaseVariableImpl;
import consulo.csharp.lang.psi.impl.source.CSharpIsVariableImpl;
import consulo.csharp.lang.psi.impl.source.CSharpLabeledStatementImpl;
import consulo.csharp.lang.psi.impl.source.CSharpOutRefVariableImpl;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.resolve.DotNetNamespaceAsElement;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 09.10.14
 */
public enum ExecuteTarget
{
	LABEL
			{
				@RequiredReadAction
				@Override
				public boolean isMyElement(@Nonnull PsiElement element)
				{
					return element instanceof CSharpLabeledStatementImpl;
				}
			},
	TYPE
			{
				@RequiredReadAction
				@Override
				public boolean isMyElement(@Nonnull PsiElement element)
				{
					return element instanceof CSharpTypeDeclaration;
				}
			},
	MEMBER
			{
				@RequiredReadAction
				@Override
				public boolean isMyElement(@Nonnull PsiElement element)
				{
					return element instanceof DotNetQualifiedElement;
				}
			},
	DELEGATE_METHOD
			{
				@RequiredReadAction
				@Override
				public boolean isMyElement(@Nonnull PsiElement element)
				{
					return element instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) element).isDelegate();
				}
			},
	TYPE_DEF
			{
				@RequiredReadAction
				@Override
				public boolean isMyElement(@Nonnull PsiElement element)
				{
					return element instanceof CSharpTypeDefStatement;
				}
			},
	NAMESPACE
			{
				@RequiredReadAction
				@Override
				public boolean isMyElement(@Nonnull PsiElement element)
				{
					return element instanceof DotNetNamespaceAsElement;
				}
			},
	GENERIC_PARAMETER
			{
				@RequiredReadAction
				@Override
				public boolean isMyElement(@Nonnull PsiElement element)
				{
					return element instanceof DotNetGenericParameter;
				}
			},
	FIELD
			{
				@RequiredReadAction
				@Override
				public boolean isMyElement(@Nonnull PsiElement element)
				{
					return element instanceof CSharpFieldDeclaration;
				}
			},
	PROPERTY
			{
				@RequiredReadAction
				@Override
				public boolean isMyElement(@Nonnull PsiElement element)
				{
					return element instanceof CSharpPropertyDeclaration;
				}
			},
	CONSTRUCTOR
			{
				@RequiredReadAction
				@Override
				public boolean isMyElement(@Nonnull PsiElement element)
				{
					return element instanceof CSharpConstructorDeclaration;
				}
			},
	EVENT
			{
				@RequiredReadAction
				@Override
				public boolean isMyElement(@Nonnull PsiElement element)
				{
					return element instanceof CSharpEventDeclaration;
				}
			},
	ELEMENT_GROUP
			{
				@RequiredReadAction
				@Override
				public boolean isMyElement(@Nonnull PsiElement element)
				{
					return element instanceof CSharpElementGroup;
				}
			},
	LOCAL_VARIABLE_OR_PARAMETER_OR_LOCAL_METHOD
			{
				@RequiredReadAction
				@Override
				public boolean isMyElement(@Nonnull PsiElement element)
				{
					return element instanceof CSharpLocalVariable ||
							element instanceof DotNetParameter ||
							element instanceof CSharpLinqVariable ||
							element instanceof CSharpIsVariableImpl ||
							element instanceof CSharpCaseVariableImpl ||
							element instanceof CSharpOutRefVariableImpl ||
							element instanceof CSharpLambdaParameter ||
							element instanceof CSharpTupleVariable ||
							element instanceof CSharpElementGroup && isLocalMethodGroup((CSharpElementGroup) element);
				}

				@RequiredReadAction
				private boolean isLocalMethodGroup(CSharpElementGroup group)
				{
					Object firstItem = ContainerUtil.getFirstItem(group.getElements());
					return firstItem instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) firstItem).isLocal();
				}
			};

	public static final ExecuteTarget[] EMPTY_ARRAY = new ExecuteTarget[0];

	public static ArrayFactory<ExecuteTarget> ARRAY_FACTORY = new ArrayFactory<ExecuteTarget>()
	{
		@Nonnull
		@Override
		public ExecuteTarget[] create(int count)
		{
			return count == 0 ? EMPTY_ARRAY : new ExecuteTarget[count];
		}
	};

	@RequiredReadAction
	public abstract boolean isMyElement(@Nonnull PsiElement element);
}
