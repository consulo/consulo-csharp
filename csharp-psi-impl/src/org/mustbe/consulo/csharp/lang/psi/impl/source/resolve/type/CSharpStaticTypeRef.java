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

package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRefWithCachedResult;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpStaticTypeRef extends DotNetTypeRefWithCachedResult
{
	public static final CSharpStaticTypeRef IMPLICIT = new CSharpStaticTypeRef("implicit");
	public static final CSharpStaticTypeRef EXPLICIT = new CSharpStaticTypeRef("explicit");
	public static final CSharpStaticTypeRef __ARGLIST_TYPE = new CSharpStaticTypeRef("__arglist");

	private final String myPresentableText;

	private CSharpStaticTypeRef(String presentableText)
	{
		myPresentableText = presentableText;
	}

	@NotNull
	public String getText()
	{
		return myPresentableText;
	}

	@RequiredReadAction
	@NotNull
	@Override
	protected DotNetTypeResolveResult resolveResult()
	{
		return DotNetTypeResolveResult.EMPTY;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public String toString()
	{
		return myPresentableText;
	}
}
