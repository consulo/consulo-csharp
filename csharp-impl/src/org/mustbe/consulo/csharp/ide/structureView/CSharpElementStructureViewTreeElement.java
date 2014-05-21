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

package org.mustbe.consulo.csharp.ide.structureView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.ide.DotNetElementPresentationUtil;
import org.mustbe.consulo.dotnet.psi.DotNetFieldDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetNamespaceDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetQualifiedElement;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;

/**
 * @author VISTALL
 * @since 31.12.13.
 */
public class CSharpElementStructureViewTreeElement extends PsiTreeElementBase<PsiNamedElement>
{
	public CSharpElementStructureViewTreeElement(PsiNamedElement psiElement)
	{
		super(psiElement);
	}

	@NotNull
	@Override
	public Collection<StructureViewTreeElement> getChildrenBase()
	{
		List<StructureViewTreeElement> list = new ArrayList<StructureViewTreeElement>();
		for(PsiElement psiElement : getValue().getChildren())
		{
			if(psiElement instanceof DotNetQualifiedElement)
			{
				list.add(new CSharpElementStructureViewTreeElement((DotNetQualifiedElement) psiElement));
			}
		}
		return list;
	}


	@Nullable
	@Override
	public String getPresentableText()
	{
		PsiNamedElement value = getValue();
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
		else
		{
			return value.getName();
		}
	}
}
