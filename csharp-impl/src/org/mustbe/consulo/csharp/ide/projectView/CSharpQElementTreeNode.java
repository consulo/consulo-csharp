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

package org.mustbe.consulo.csharp.ide.projectView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetElement;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetMemberOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiNamedElement;

/**
 * @author VISTALL
 * @since 09.12.13.
 */
public class CSharpQElementTreeNode extends CSharpAbstractElementTreeNode<DotNetNamedElement>
{
	public CSharpQElementTreeNode(DotNetNamedElement dotNetMemberOwner, ViewSettings viewSettings)
	{
		super(dotNetMemberOwner.getProject(), dotNetMemberOwner, viewSettings);
	}

	@Nullable
	@Override
	protected Collection<AbstractTreeNode> getChildrenImpl()
	{
		if(!getSettings().isShowMembers())
		{
			return Collections.emptyList();
		}
		DotNetElement value = getValue();
		if(value instanceof DotNetMemberOwner)
		{
			DotNetNamedElement[] members = ((DotNetMemberOwner) value).getMembers();
			if(members.length == 0)
			{
				return Collections.emptyList();
			}
			List<AbstractTreeNode> list = new ArrayList<AbstractTreeNode>(members.length);
			for(DotNetNamedElement dotNetElement : members)
			{
				list.add(new CSharpElementTreeNode(dotNetElement, getSettings()));
			}
			return list;
		}

		return Collections.emptyList();
	}

	@Override
	protected void updateImpl(PresentationData presentationData)
	{
		DotNetNamedElement value = getValue();

		presentationData.setIcon(IconDescriptorUpdaters.getIcon(value, Iconable.ICON_FLAG_VISIBILITY));

		DotNetQualifiedElement qualifiedElement = (DotNetQualifiedElement) value;

		String presentableParentQName = qualifiedElement.getPresentableParentQName();
		if(!StringUtil.isEmpty(presentableParentQName))
		{
			presentationData.setPresentableText(presentableParentQName + "::" + getPresentableText(qualifiedElement));
		}
		else
		{
			presentationData.setPresentableText(getPresentableText(qualifiedElement));
		}
	}

	public static String getPresentableText(PsiNamedElement value)
	{
		if(value instanceof DotNetLikeMethodDeclaration)
		{
			return DotNetElementPresentationUtil.formatMethod((DotNetLikeMethodDeclaration) value, DotNetElementPresentationUtil.METHOD_SCALA_LIKE_FULL);
		}
		else if(value instanceof DotNetFieldDeclaration)
		{
			return DotNetElementPresentationUtil.formatField((DotNetFieldDeclaration) value);
		}
		else if(value instanceof DotNetNamespaceDeclaration)
		{
			return ((DotNetNamespaceDeclaration) value).getPresentableQName();
		}
		else if(value instanceof DotNetTypeDeclaration)
		{
			return DotNetElementPresentationUtil.formatTypeWithGenericParameters((DotNetTypeDeclaration) value);
		}
		else
		{
			return value.getName();
		}
	}
}
