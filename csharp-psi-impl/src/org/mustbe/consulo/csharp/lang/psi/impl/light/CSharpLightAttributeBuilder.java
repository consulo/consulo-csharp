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

package org.mustbe.consulo.csharp.lang.psi.impl.light;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.csharp.lang.psi.CSharpAttribute;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentList;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpReferenceExpression;
import org.mustbe.consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.DotNetTypeRefByQName;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetTypeList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.impl.light.LightElement;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class CSharpLightAttributeBuilder extends LightElement implements CSharpAttribute
{
	private final List<DotNetExpression> myParameterExpressions = new SmartList<DotNetExpression>();
	private final PsiElement myScope;
	private final String myQualifiedName;

	public CSharpLightAttributeBuilder(PsiElement scope, String qualifiedName)
	{
		super(PsiManager.getInstance(scope.getProject()), CSharpLanguage.INSTANCE);
		myScope = scope;
		myQualifiedName = qualifiedName;
	}

	public void addParameterExpression(Object o)
	{
		if(o instanceof String)
		{
			o = StringUtil.QUOTER.fun((String)o);
		}

		myParameterExpressions.add(CSharpFileFactory.createExpression(getProject(), String.valueOf(o)));
	}

	@Nullable
	@Override
	public DotNetTypeDeclaration resolveToType()
	{
		return (DotNetTypeDeclaration) toTypeRef().resolve(myScope);
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRef()
	{
		return new DotNetTypeRefByQName(myQualifiedName, CSharpTransform.INSTANCE);
	}

	@Override
	public String toString()
	{
		return "CSharpLightAttributeBuilder: " + myQualifiedName;
	}

	@Nullable
	@Override
	public CSharpReferenceExpression getReferenceExpression()
	{
		return null;
	}

	@Override
	public boolean canResolve()
	{
		return true;
	}

	@Nullable
	@Override
	public CSharpCallArgumentList getParameterList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetExpression[] getParameterExpressions()
	{
		return ContainerUtil.toArray(myParameterExpressions, DotNetExpression.ARRAY_FACTORY);
	}

	@Nullable
	@Override
	public DotNetTypeList getTypeArgumentList()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetTypeRef[] getTypeArgumentListRefs()
	{
		return new DotNetTypeRef[0];
	}

	@Nullable
	@Override
	public PsiElement resolveToCallable()
	{
		return null;
	}

	@NotNull
	@Override
	public ResolveResult[] multiResolve(boolean incompleteCode)
	{
		return new ResolveResult[0];
	}
}
