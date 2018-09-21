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

package consulo.csharp.lang.psi.impl.light.builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpPropertyDeclaration;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.DotNetQualifiedElement;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetXXXAccessor;
import consulo.dotnet.resolve.DotNetTypeRef;

/**
 * @author VISTALL
 * @since 26-Nov-16.
 */
public class CSharpLightPropertyDeclarationBuilder extends CSharpLightVariableBuilder<CSharpLightPropertyDeclarationBuilder> implements CSharpPropertyDeclaration
{
	private PsiElement myNameIdentifier;

	public CSharpLightPropertyDeclarationBuilder(PsiElement element)
	{
		super(element);
	}

	public CSharpLightPropertyDeclarationBuilder(Project manager)
	{
		super(manager);
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableParentQName()
	{
		PsiElement parent = getParent();
		if(parent instanceof DotNetQualifiedElement)
		{
			return ((DotNetQualifiedElement) parent).getPresentableQName();
		}
		return "";
	}

	@RequiredReadAction
	@Nullable
	@Override
	public String getPresentableQName()
	{
		String parentQName = getPresentableParentQName();
		if(StringUtil.isEmpty(parentQName))
		{
			return getName();
		}
		return parentQName + "." + getName();
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return myNameIdentifier;
	}

	@Override
	public int getTextOffset()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		return nameIdentifier == null ? super.getTextOffset() : nameIdentifier.getTextOffset();
	}

	@RequiredReadAction
	@Override
	public String getName()
	{
		PsiElement nameIdentifier = getNameIdentifier();
		if(nameIdentifier != null)
		{
			return nameIdentifier.getText();
		}
		return super.getName();
	}

	public void withNameIdentifier(@Nonnull PsiElement element)
	{
		myNameIdentifier = element;
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitPropertyDeclaration(this);
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
	public DotNetXXXAccessor[] getAccessors()
	{
		return DotNetXXXAccessor.EMPTY_ARRAY;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public DotNetNamedElement[] getMembers()
	{
		return DotNetNamedElement.EMPTY_ARRAY;
	}

	@Nullable
	@Override
	public DotNetType getTypeForImplement()
	{
		return null;
	}

	@Nonnull
	@Override
	public DotNetTypeRef getTypeRefForImplement()
	{
		return DotNetTypeRef.ERROR_TYPE;
	}

	@RequiredReadAction
	@Override
	public boolean isAutoGet()
	{
		return false;
	}
}
