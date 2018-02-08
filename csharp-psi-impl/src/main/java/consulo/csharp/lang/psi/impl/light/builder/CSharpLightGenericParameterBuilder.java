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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpGenericConstraintUtil;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import consulo.csharp.lang.psi.CSharpGenericParameter;

/**
 * @author VISTALL
 * @since 11.08.14
 */
public class CSharpLightGenericParameterBuilder extends CSharpLightNamedElementWiModifierListBuilder<CSharpLightGenericParameterBuilder>
		implements CSharpGenericParameter, DotNetModifierListOwner
{
	private int myIndex = -1;

	public CSharpLightGenericParameterBuilder(Project project)
	{
		super(project);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitGenericParameter(this);
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@Override
	public int getIndex()
	{
		return myIndex;
	}

	public void setIndex(int index)
	{
		myIndex = index;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetAttribute[] getAttributes()
	{
		DotNetModifierList modifierList = getModifierList();
		if(modifierList != null)
		{
			return modifierList.getAttributes();
		}
		return DotNetAttribute.EMPTY_ARRAY;
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return CSharpGenericConstraintUtil.getExtendTypes(this);
	}
}