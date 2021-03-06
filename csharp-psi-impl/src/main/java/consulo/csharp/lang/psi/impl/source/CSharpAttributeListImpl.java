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

package consulo.csharp.lang.psi.impl.source;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import consulo.csharp.lang.psi.*;
import consulo.dotnet.psi.DotNetAttributeTargetType;
import gnu.trove.THashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author VISTALL
 * @since 05.12.14
 */
public class CSharpAttributeListImpl extends CSharpElementImpl implements CSharpAttributeList
{
	private static final Map<IElementType, DotNetAttributeTargetType> ourMap = new THashMap<IElementType, DotNetAttributeTargetType>()
	{
		{
			put(CSharpSoftTokens.ASSEMBLY_KEYWORD, DotNetAttributeTargetType.ASSEMBLY);
			put(CSharpSoftTokens.MODULE_KEYWORD, DotNetAttributeTargetType.MODULE);
			put(CSharpSoftTokens.FIELD_KEYWORD, DotNetAttributeTargetType.FIELD);
			put(CSharpSoftTokens.EVENT_KEYWORD, DotNetAttributeTargetType.EVENT);
			put(CSharpSoftTokens.METHOD_KEYWORD, DotNetAttributeTargetType.METHOD);
			put(CSharpSoftTokens.PARAM_KEYWORD, DotNetAttributeTargetType.PARAMETER);
			put(CSharpSoftTokens.PROPERTY_KEYWORD, DotNetAttributeTargetType.PROPERTY);
			put(CSharpSoftTokens.RETURN_KEYWORD, DotNetAttributeTargetType.RETURN);
			put(CSharpSoftTokens.TYPE_KEYWORD, DotNetAttributeTargetType.TYPE);
		}
	};

	public CSharpAttributeListImpl(@Nonnull IElementType elementType)
	{
		super(elementType);
	}

	@Override
	public void accept(@Nonnull CSharpElementVisitor visitor)
	{
		visitor.visitAttributeList(this);
	}

	@Nullable
	@Override
	public DotNetAttributeTargetType getTargetType()
	{
		return getAttributeType(findPsiChildByType(CSharpTokenSets.ATTRIBUTE_TARGETS));
	}

	@Nullable
	public static DotNetAttributeTargetType getAttributeType(@Nullable PsiElement element)
	{
		if(element == null)
		{
			return null;
		}

		IElementType elementType = element.getNode().getElementType();
		return ourMap.get(elementType);
	}

	@Nonnull
	@Override
	public CSharpAttribute[] getAttributes()
	{
		return findChildrenByClass(CSharpAttribute.class);
	}
}
