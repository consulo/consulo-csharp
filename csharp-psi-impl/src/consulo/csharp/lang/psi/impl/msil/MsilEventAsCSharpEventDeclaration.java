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

package consulo.csharp.lang.psi.impl.msil;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpEventDeclaration;
import consulo.csharp.lang.psi.impl.msil.typeParsing.SomeType;
import consulo.csharp.lang.psi.impl.msil.typeParsing.SomeTypeParser;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetXXXAccessor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.lombok.annotations.Lazy;
import consulo.msil.lang.psi.MsilEventEntry;
import consulo.msil.lang.psi.MsilMethodEntry;

/**
 * @author VISTALL
 * @since 24.05.14
 */
public class MsilEventAsCSharpEventDeclaration extends MsilVariableAsCSharpVariable implements CSharpEventDeclaration
{
	private final DotNetXXXAccessor[] myAccessors;

	public MsilEventAsCSharpEventDeclaration(PsiElement parent, MsilEventEntry variable, List<Pair<DotNetXXXAccessor, MsilMethodEntry>> pairs)
	{
		super(parent, MsilPropertyAsCSharpPropertyDeclaration.getAdditionalModifiers(variable, pairs), variable);
		myAccessors = MsilPropertyAsCSharpPropertyDeclaration.buildAccessors(this, pairs);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitEventDeclaration(this);
	}

	@RequiredReadAction
	@Override
	public PsiElement getLeftBrace()
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public PsiElement getRightBrace()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetXXXAccessor[] getAccessors()
	{
		return myAccessors;
	}

	@NotNull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return getAccessors();
	}

	@Override
	public MsilEventEntry getVariable()
	{
		return (MsilEventEntry) super.getVariable();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		return getVariable().getPresentableParentQName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		return getVariable().getPresentableQName();
	}

	@Nullable
	@Override
	@Lazy(notNull = false)
	public DotNetType getTypeForImplement()
	{
		String nameFromBytecode = getVariable().getNameFromBytecode();
		String typeBeforeDot = StringUtil.getPackageName(nameFromBytecode);
		SomeType someType = SomeTypeParser.parseType(typeBeforeDot, nameFromBytecode);
		if(someType != null)
		{
			return new DummyType(getProject(), MsilEventAsCSharpEventDeclaration.this, someType);
		}
		return null;
	}

	@NotNull
	@Override
	@Lazy
	public DotNetTypeRef getTypeRefForImplement()
	{
		DotNetType typeForImplement = getTypeForImplement();
		return typeForImplement != null ? typeForImplement.toTypeRef() : DotNetTypeRef.ERROR_TYPE;
	}

	@Nullable
	@Override
	protected Class<? extends PsiElement> getNavigationElementClass()
	{
		return CSharpEventDeclaration.class;
	}
}
