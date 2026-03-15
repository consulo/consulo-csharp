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

package consulo.csharp.lang.impl.psi.source.resolve.type;

import consulo.annotation.access.RequiredReadAction;
import consulo.dotnet.psi.resolve.DotNetRefTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.scope.GlobalSearchScope;
import consulo.project.Project;


/**
 * @author VISTALL
 * @since 02.06.14
 */
public class CSharpRefTypeRef extends DotNetTypeRefWithCachedResult implements DotNetRefTypeRef
{
	public static enum Type
	{
		out,
		ref
	}

	private final Type myType;
	private final DotNetTypeRef myTypeRef;

	public CSharpRefTypeRef(Project project, GlobalSearchScope scope, Type type, DotNetTypeRef typeRef)
	{
		super(project, scope);
		myType = type;
		myTypeRef = typeRef;
	}

	public Type getType()
	{
		return myType;
	}

	@RequiredReadAction
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return myTypeRef.resolve();
	}

	@RequiredReadAction
	@Override
	public String getVmQName()
	{
		return myType.name() + " " + myTypeRef.toString();
	}

	@Override
	public DotNetTypeRef getInnerTypeRef()
	{
		return myTypeRef;
	}
}
