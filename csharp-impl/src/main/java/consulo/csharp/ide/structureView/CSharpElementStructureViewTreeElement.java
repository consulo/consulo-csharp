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

package consulo.csharp.ide.structureView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.annotations.RequiredDispatchThread;
import consulo.csharp.ide.CSharpElementPresentationUtil;
import consulo.dotnet.ide.DotNetElementPresentationUtil;
import consulo.dotnet.psi.DotNetFieldDeclaration;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetNamespaceDeclaration;
import consulo.dotnet.psi.DotNetPropertyDeclaration;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetTypeDeclaration;
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

	@Nonnull
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
	@RequiredDispatchThread
	public String getPresentableText()
	{
		PsiNamedElement value = getValue();
		if(value instanceof DotNetLikeMethodDeclaration)
		{
			return CSharpElementPresentationUtil.formatMethod((DotNetLikeMethodDeclaration) value, CSharpElementPresentationUtil.METHOD_SCALA_LIKE_FULL);
		}
		else if(value instanceof DotNetTypeDeclaration)
		{
			return DotNetElementPresentationUtil.formatTypeWithGenericParameters((DotNetTypeDeclaration)value);
		}
		else if(value instanceof DotNetFieldDeclaration)
		{
			return CSharpElementPresentationUtil.formatField((DotNetFieldDeclaration) value);
		}
		else if(value instanceof DotNetPropertyDeclaration)
		{
			return CSharpElementPresentationUtil.formatProperty((DotNetPropertyDeclaration) value, CSharpElementPresentationUtil.PROPERTY_SCALA_LIKE_FULL);
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
