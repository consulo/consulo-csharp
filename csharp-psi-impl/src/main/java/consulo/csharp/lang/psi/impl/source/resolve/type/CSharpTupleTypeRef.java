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

package consulo.csharp.lang.psi.impl.source.resolve.type;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiNameIdentifierOwner;
import com.intellij.psi.search.GlobalSearchScope;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.impl.msil.CSharpTransform;
import consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.CSharpTupleTypeDeclaration;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.resolve.*;

import javax.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 26-Nov-16.
 */
public class CSharpTupleTypeRef extends DotNetTypeRefWithCachedResult
{
	private DotNetTypeRef[] myTypeRefs;
	private PsiNameIdentifierOwner[] myVariables;

	public CSharpTupleTypeRef(Project project, GlobalSearchScope resolveScope, DotNetTypeRef[] typeRefs, @Nonnull PsiNameIdentifierOwner[] variables)
	{
		super(project, resolveScope);
		myTypeRefs = typeRefs;
		myVariables = variables;
	}

	public DotNetTypeRef[] getTypeRefs()
	{
		return myTypeRefs;
	}

	public PsiNameIdentifierOwner[] getVariables()
	{
		return myVariables;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		DotNetTypeDeclaration type = DotNetPsiSearcher.getInstance(myProject).findType("System.ValueTuple`" + myVariables.length, myResolveScope, CSharpTransform.INSTANCE);
		if(type == null)
		{
			return DotNetTypeResolveResult.EMPTY;
		}
		DotNetGenericExtractor extractor = CSharpGenericExtractor.create(type.getGenericParameters(), myTypeRefs);
		return new CSharpUserTypeRef.Result<>(new CSharpTupleTypeDeclaration(type, myVariables, myTypeRefs), extractor);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String toString()
	{
		return StringUtil.join(myTypeRefs, Object::toString, ",");
	}
}
