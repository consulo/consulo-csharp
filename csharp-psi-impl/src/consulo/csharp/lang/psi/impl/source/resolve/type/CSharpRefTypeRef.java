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

package consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.resolve.DotNetRefTypeRef;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import consulo.dotnet.resolve.DotNetTypeResolveResult;

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
	private DotNetTypeRef myTypeRef;

	public CSharpRefTypeRef(@NotNull Type type, @NotNull DotNetTypeRef typeRef)
	{
		myType = type;
		myTypeRef = typeRef;
	}

	public Type getType()
	{
		return myType;
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return myTypeRef.resolve();
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String toString()
	{
		return myType.name() + " " + myTypeRef.toString();
	}

	@NotNull
	@Override
	public DotNetTypeRef getInnerTypeRef()
	{
		return myTypeRef;
	}
}
