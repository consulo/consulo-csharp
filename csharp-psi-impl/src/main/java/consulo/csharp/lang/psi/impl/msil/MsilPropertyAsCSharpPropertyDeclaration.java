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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.openapi.util.NullableLazyValue;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.ArrayUtil;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpAccessModifier;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.csharp.lang.psi.impl.msil.typeParsing.SomeType;
import consulo.csharp.lang.psi.impl.msil.typeParsing.SomeTypeParser;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetXAccessor;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.msil.lang.psi.MsilClassEntry;
import consulo.msil.lang.psi.MsilMethodEntry;
import consulo.msil.lang.psi.MsilPropertyEntry;
import consulo.msil.lang.psi.MsilTokens;

/**
 * @author VISTALL
 * @since 24.05.14
 */
public class MsilPropertyAsCSharpPropertyDeclaration extends MsilVariableAsCSharpVariable implements CSharpPropertyDeclaration
{
	private DotNetXAccessor[] myAccessors;

	private final NullableLazyValue<DotNetType> myTypeForImplementValue;

	@RequiredReadAction
	public MsilPropertyAsCSharpPropertyDeclaration(PsiElement parent, MsilPropertyEntry variable, List<Pair<DotNetXAccessor, MsilMethodEntry>> pairs)
	{
		super(parent, getAdditionalModifiers(variable, pairs), variable);
		myAccessors = buildAccessors(this, pairs);

		myTypeForImplementValue = NullableLazyValue.of(() ->
		{
			String nameFromBytecode = getVariable().getNameFromBytecode();
			String typeBeforeDot = StringUtil.getPackageName(nameFromBytecode);
			SomeType someType = SomeTypeParser.parseType(typeBeforeDot, nameFromBytecode);
			if(someType != null)
			{
				return new DummyType(getProject(), MsilPropertyAsCSharpPropertyDeclaration.this, someType);
			}
			return null;
		});
	}

	public static DotNetXAccessor[] buildAccessors(@Nonnull PsiElement parent, @Nonnull List<Pair<DotNetXAccessor, MsilMethodEntry>> pairs)
	{
		List<DotNetXAccessor> accessors = new ArrayList<DotNetXAccessor>(2);

		for(Pair<DotNetXAccessor, MsilMethodEntry> pair : pairs)
		{
			accessors.add(new MsilXAccessorAsCSharpXAccessor(parent, pair.getFirst(), pair.getSecond()));
		}
		return ContainerUtil.toArray(accessors, DotNetXAccessor.ARRAY_FACTORY);
	}

	@Nonnull
	public static CSharpModifier[] getAdditionalModifiers(PsiElement parent, List<Pair<DotNetXAccessor, MsilMethodEntry>> pairs)
	{
		PsiElement maybeTypeParent = parent.getParent();
		if(maybeTypeParent instanceof MsilClassEntry && ((MsilClassEntry) maybeTypeParent).hasModifier(MsilTokens.INTERFACE_KEYWORD))
		{
			return CSharpModifier.EMPTY_ARRAY;
		}

		boolean staticMod = false;
		List<CSharpAccessModifier> modifiers = new SmartList<CSharpAccessModifier>();
		for(Pair<DotNetXAccessor, MsilMethodEntry> pair : pairs)
		{
			CSharpAccessModifier accessModifier = getAccessModifier(pair.getSecond());
			modifiers.add(accessModifier);

			if(pair.getSecond().hasModifier(MsilTokens.STATIC_KEYWORD))
			{
				staticMod = true;
			}
		}
		ContainerUtil.sort(modifiers);

		CSharpAccessModifier access = modifiers.isEmpty() ? CSharpAccessModifier.PUBLIC : modifiers.get(0);
		return staticMod ? ArrayUtil.append(access.getModifiers(), CSharpModifier.STATIC) : access.getModifiers();
	}

	@RequiredReadAction
	private static CSharpAccessModifier getAccessModifier(MsilMethodEntry second)
	{
		if(second.hasModifier(MsilTokens.PRIVATE_KEYWORD))
		{
			return CSharpAccessModifier.PRIVATE;
		}
		else if(second.hasModifier(MsilTokens.PUBLIC_KEYWORD))
		{
			return CSharpAccessModifier.PUBLIC;
		}
		else if(second.hasModifier(MsilTokens.ASSEMBLY_KEYWORD) && second.hasModifier(MsilTokens.PROTECTED_KEYWORD))
		{
			return CSharpAccessModifier.PROTECTED_INTERNAL;
		}
		else if(second.hasModifier(MsilTokens.ASSEMBLY_KEYWORD))
		{
			return CSharpAccessModifier.INTERNAL;
		}
		else if(second.hasModifier(MsilTokens.PROTECTED_KEYWORD))
		{
			return CSharpAccessModifier.PROTECTED;
		}
		return CSharpAccessModifier.PUBLIC;
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitPropertyDeclaration(this);
	}

	@Override
	public MsilPropertyEntry getVariable()
	{
		return (MsilPropertyEntry) super.getVariable();
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

	@Nonnull
	@Override
	public DotNetXAccessor[] getAccessors()
	{
		return myAccessors;
	}

	@Nonnull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return getAccessors();
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
	public DotNetType getTypeForImplement()
	{
		return myTypeForImplementValue.getValue();
	}

	@Nonnull
	@Override
	public DotNetTypeRef getTypeRefForImplement()
	{
		DotNetType typeForImplement = getTypeForImplement();
		return typeForImplement != null ? typeForImplement.toTypeRef() : DotNetTypeRef.ERROR_TYPE;
	}

	@Nullable
	@Override
	protected Class<? extends PsiElement> getNavigationElementClass()
	{
		return CSharpPropertyDeclaration.class;
	}

	@RequiredReadAction
	@Override
	public boolean isAutoGet()
	{
		return false;
	}
}
