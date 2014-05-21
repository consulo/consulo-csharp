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
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.resolve.DotNetNativeTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiFacade;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 29.12.13.
 */
public class CSharpNativeTypeRef extends DotNetTypeRef.Adapter implements DotNetNativeTypeRef
{
	public static final CSharpNativeTypeRef BOOL = new CSharpNativeTypeRef("bool", "System.Boolean");
	public static final CSharpNativeTypeRef DOUBLE = new CSharpNativeTypeRef("double", "System.Double");
	public static final CSharpNativeTypeRef FLOAT = new CSharpNativeTypeRef("float", "System.Single");
	public static final CSharpNativeTypeRef CHAR = new CSharpNativeTypeRef("char", "System.Char");
	public static final CSharpNativeTypeRef OBJECT = new CSharpNativeTypeRef("object", "System.Object");
	public static final CSharpNativeTypeRef STRING = new CSharpNativeTypeRef("string", "System.String");
	public static final CSharpNativeTypeRef SBYTE = new CSharpNativeTypeRef("sbyte", "System.SByte");
	public static final CSharpNativeTypeRef BYTE =  new CSharpNativeTypeRef("byte", "System.Byte");
	public static final CSharpNativeTypeRef INT = new CSharpNativeTypeRef("int", "System.Int32");
	public static final CSharpNativeTypeRef UINT =  new CSharpNativeTypeRef("uint", "System.UInt32");
	public static final CSharpNativeTypeRef LONG = new CSharpNativeTypeRef("long", "System.Int64");
	public static final CSharpNativeTypeRef ULONG = new CSharpNativeTypeRef("ulong", "System.UInt64");
	public static final CSharpNativeTypeRef VOID =  new CSharpNativeTypeRef("void", "System.Void");
	public static final CSharpNativeTypeRef SHORT = new CSharpNativeTypeRef("short", "System.Int16");
	public static final CSharpNativeTypeRef USHORT =  new CSharpNativeTypeRef("ushort", "System.UInt16");
	public static final CSharpNativeTypeRef DECIMAL = new CSharpNativeTypeRef("decimal", "System.Decimal");
	public static final CSharpNativeTypeRef IMPLICIT = new CSharpNativeTypeRef("implicit", "System.Object");
	public static final CSharpNativeTypeRef EXPLICIT = new CSharpNativeTypeRef("explicit", "System.Object");

	private final String myPresentableText;
	private final String myWrapperQualifiedClass;

	private CSharpNativeTypeRef(String presentableText, String wrapperQualifiedClass)
	{
		myPresentableText = presentableText;
		myWrapperQualifiedClass = wrapperQualifiedClass;
	}

	@Nullable
	@Override
	public String getPresentableText()
	{
		return myPresentableText;
	}

	@Nullable
	@Override
	public String getQualifiedText()
	{
		return myWrapperQualifiedClass;
	}

	@Override
	public boolean isNullable()
	{
		return false;
	}

	@Nullable
	@Override
	public PsiElement resolve(@NotNull PsiElement scope)
	{
		return DotNetPsiFacade.getInstance(scope.getProject()).findType(myWrapperQualifiedClass, scope.getResolveScope(), 0);
	}

	@Override
	@NotNull
	public String getWrapperQualifiedClass()
	{
		return myWrapperQualifiedClass;
	}
}
