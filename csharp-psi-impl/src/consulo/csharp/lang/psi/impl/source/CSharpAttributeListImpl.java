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

package consulo.csharp.lang.psi.impl.source;

import gnu.trove.THashMap;

import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.lang.psi.CSharpAttribute;
import consulo.csharp.lang.psi.CSharpAttributeList;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.CSharpTokenSets;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import consulo.dotnet.psi.DotNetAttributeTargetType;

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

	public CSharpAttributeListImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitAttributeList(this);
	}

	@Nullable
	@Override
	public DotNetAttributeTargetType getTargetType()
	{
		return getAttributeType(findChildByType(CSharpTokenSets.ATTRIBUTE_TARGETS));
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

	@NotNull
	@Override
	public CSharpAttribute[] getAttributes()
	{
		return findChildrenByClass(CSharpAttribute.class);
	}
}
