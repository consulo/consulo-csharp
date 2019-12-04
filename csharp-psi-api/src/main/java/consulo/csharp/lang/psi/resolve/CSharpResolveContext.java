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

package consulo.csharp.lang.psi.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.ArrayFactory;
import com.intellij.util.Processor;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpCastType;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpConversionMethodDeclaration;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.util.dataholder.UserDataHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 29.09.14
 */
public interface CSharpResolveContext
{
	CSharpResolveContext[] EMPTY_ARRAY = new CSharpResolveContext[0];

	ArrayFactory<CSharpResolveContext> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new CSharpResolveContext[count];

	CSharpResolveContext EMPTY = new CSharpResolveContextAdapter();

	@Nullable
	@RequiredReadAction
	default CSharpElementGroup<CSharpIndexMethodDeclaration> indexMethodGroup(boolean deep)
	{
		return null;
	}

	@Nullable
	@RequiredReadAction
	default CSharpElementGroup<CSharpConstructorDeclaration> constructorGroup()
	{
		return null;
	}

	@Nullable
	@RequiredReadAction
	default CSharpElementGroup<CSharpConstructorDeclaration> deConstructorGroup()
	{
		return null;
	}

	@Nullable
	@RequiredReadAction
	default CSharpElementGroup<CSharpMethodDeclaration> findOperatorGroupByTokenType(@Nonnull IElementType type, boolean deep)
	{
		return null;
	}

	@Nullable
	@RequiredReadAction
	default CSharpElementGroup<CSharpConversionMethodDeclaration> findConversionMethodGroup(@Nonnull CSharpCastType castType, boolean deep)
	{
		return null;
	}

	@Nullable
	@RequiredReadAction
	default CSharpElementGroup<CSharpMethodDeclaration> findExtensionMethodGroupByName(@Nonnull String name)
	{
		return null;
	}

	@RequiredReadAction
	default boolean processExtensionMethodGroups(@Nonnull Processor<CSharpElementGroup<CSharpMethodDeclaration>> processor)
	{
		return true;
	}

	@RequiredReadAction
	@Nonnull
	default Collection<PsiElement> findByName(@Nonnull String name, boolean deep, @Nonnull UserDataHolder holder)
	{
		return Collections.emptySet();
	}

	@RequiredReadAction
	default boolean processElements(@Nonnull Processor<PsiElement> processor, boolean deep)
	{
		return true;
	}


	@Nonnull
	PsiElement getElement();
}
