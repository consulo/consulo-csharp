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

package consulo.csharp.impl.ide.actions.generate.memberChoose;

import consulo.annotation.access.RequiredReadAction;
import consulo.component.util.Iconable;
import consulo.csharp.impl.ide.completion.expected.ExpectedUsingInfo;
import consulo.dotnet.psi.DotNetElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.editor.generation.ClassMember;
import consulo.language.editor.generation.MemberChooserObject;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiElement;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.ColoredTextContainer;
import consulo.util.collection.ArrayFactory;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import javax.swing.*;

/**
 * @author VISTALL
 * @since 25.06.14
 */
public abstract class CSharpMemberChooseObject<T extends DotNetElement> implements MemberChooserObject, ClassMember
{
	public static final CSharpMemberChooseObject[] EMPTY_ARRAY = new CSharpMemberChooseObject[0];

	public static ArrayFactory<CSharpMemberChooseObject> ARRAY_FACTORY = count -> count == 0 ? EMPTY_ARRAY : new CSharpMemberChooseObject[count];

	protected T myDeclaration;

	public CSharpMemberChooseObject(T declaration)
	{
		myDeclaration = declaration;
	}

	@Nonnull
	public T getDeclaration()
	{
		return myDeclaration;
	}

	@Override
	@RequiredUIAccess
	public void renderTreeNode(ColoredTextContainer component, JTree tree)
	{
		component.setIcon(IconDescriptorUpdaters.getIcon(myDeclaration, Iconable.ICON_FLAG_VISIBILITY));
		component.append(getPresentationText());
	}

	@Nullable
	@RequiredReadAction
	public ExpectedUsingInfo getExpectedUsingInfo()
	{
		return null;
	}

	@Nonnull
	@RequiredReadAction
	public abstract String getPresentationText();

	@Override
	public boolean equals(Object obj)
	{
		if(obj == null)
		{
			return false;
		}
		if(obj.getClass() != getClass())
		{
			return false;
		}
		return ((CSharpMemberChooseObject)obj).myDeclaration.isEquivalentTo(myDeclaration);
	}

	@Override
	public int hashCode()
	{
		return myDeclaration.hashCode() ^ getClass().hashCode();
	}

	@Override
	public MemberChooserObject getParentNodeDelegate()
	{
		final PsiElement parent = myDeclaration.getParent();
		if(parent == null)
		{
			return null;
		}
		return new ClassChooseObject((DotNetTypeDeclaration) parent);
	}
}
