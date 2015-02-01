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

package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpFileFactory;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import org.mustbe.consulo.dotnet.DotNetTypes;
import org.mustbe.consulo.dotnet.externalAttributes.ExternalAttributeHolder;
import org.mustbe.consulo.dotnet.externalAttributes.ExternalAttributeNode;
import org.mustbe.consulo.dotnet.externalAttributes.ExternalAttributeSimpleNode;
import org.mustbe.consulo.dotnet.externalAttributes.ExternalAttributeWithChildrenNode;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.psi.DotNetParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import org.mustbe.consulo.dotnet.psi.DotNetVariable;
import org.mustbe.consulo.dotnet.resolve.DotNetRefTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.util.ArrayUtil2;
import org.mustbe.consulo.msil.lang.psi.MsilTokens;
import org.mustbe.consulo.msil.lang.psi.impl.MsilTypeByRefImpl;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilParameterAsCSharpParameter extends MsilVariableAsCSharpVariable implements DotNetParameter
{
	private final DotNetLikeMethodDeclaration myMethodDeclaration;
	private final int myIndex;

	private DotNetExpression myInitializer;

	public MsilParameterAsCSharpParameter(PsiElement parent, DotNetVariable variable, DotNetLikeMethodDeclaration methodDeclaration, int index)
	{
		super(parent, getAdditionalModifiers(index, methodDeclaration, variable), variable);
		myMethodDeclaration = methodDeclaration;
		myIndex = index;

		if(variable.hasModifier(MsilTokens.BRACKET_OPT_KEYWORD))
		{
			myInitializer = CSharpFileFactory.createExpression(getProject(), StringUtil.QUOTER.fun("not supported. See consulo-dotnet#47"));
		}
	}

	private static CSharpModifier[] getAdditionalModifiers(int index, DotNetLikeMethodDeclaration parent, DotNetVariable variable)
	{
		if(index == 0)
		{
			PsiElement msilElement = parent.getOriginalElement();
			// we can use mirror due ExtensionAttribute is in ban list
			if(DotNetAttributeUtil.hasAttribute(msilElement, DotNetTypes.System.Runtime.CompilerServices
					.ExtensionAttribute))
			{
				return new CSharpModifier[]{CSharpModifier.THIS};
			}
		}

		DotNetModifierList modifierList = variable.getModifierList();
		if(modifierList != null && modifierList.hasModifier(MsilTokens.BRACKET_OUT_KEYWORD))
		{
			DotNetType type = variable.getType();
			if(type instanceof MsilTypeByRefImpl)
			{
				return new CSharpModifier[]{CSharpModifier.OUT};
			}
		}
		return CSharpModifier.EMPTY_ARRAY;
	}

	@NotNull
	@Override
	protected MsilModifierListToCSharpModifierList createModifierList(CSharpModifier[] modifiers, DotNetVariable variable)
	{
		return new MsilModifierListToCSharpModifierList(modifiers, variable, variable.getModifierList())
		{
			@NotNull
			@Override
			public List<ExternalAttributeNode> findAttributes(ExternalAttributeHolder holder)
			{
				PsiElement parent = myMethodDeclaration.getParent();
				if(!(parent instanceof DotNetTypeDeclaration))
				{
					return Collections.emptyList();
				}

				String vmQName = ((DotNetTypeDeclaration) parent).getVmQName();
				ExternalAttributeWithChildrenNode classNode = holder.findClassNode(vmQName);
				if(classNode == null)
				{
					return Collections.emptyList();
				}

				DotNetTypeRef[] parameterTypeRefs = myMethodDeclaration.getParameterTypeRefs();

				ExternalAttributeWithChildrenNode correctExternalMethod = null;
				topLoop:
				for(ExternalAttributeSimpleNode simpleNode : classNode.getChildren())
				{
					if(!(simpleNode instanceof ExternalAttributeWithChildrenNode))
					{
						continue;
					}

					if(!Comparing.equal(simpleNode.getName(), myMethodDeclaration.getName()))
					{
						continue;
					}

					List<ExternalAttributeSimpleNode> children = ((ExternalAttributeWithChildrenNode) simpleNode).getChildren();

					if(parameterTypeRefs.length != children.size())
					{
						continue;
					}

					for(int i = 0; i < parameterTypeRefs.length; i++)
					{
						DotNetTypeRef parameterTypeRef = parameterTypeRefs[i];
						ExternalAttributeSimpleNode externalAttributeSimpleNode = children.get(i);
						if(!Comparing.equal(parameterTypeRef.getQualifiedText(), externalAttributeSimpleNode.getName()))
						{
							continue topLoop;
						}
					}
					correctExternalMethod = (ExternalAttributeWithChildrenNode) simpleNode;
					break;
				}

				if(correctExternalMethod == null)
				{
					return Collections.emptyList();
				}

				List<ExternalAttributeSimpleNode> children = correctExternalMethod.getChildren();
				ExternalAttributeSimpleNode parameterNode = ArrayUtil2.safeGet(children, myIndex);
				if(parameterNode == null)
				{
					return Collections.emptyList();
				}
				else
				{
					return parameterNode.getAttributes();
				}
			}
		};
	}

	@Nullable
	@Override
	public DotNetExpression getInitializer()
	{
		return myInitializer;
	}

	@NotNull
	@Override
	public DotNetTypeRef toTypeRefImpl()
	{
		DotNetTypeRef typeRef = super.toTypeRefImpl();
		// check to ref not needed - it default wrapping to ref
		if(hasModifier(CSharpModifier.OUT))
		{
			if(!(typeRef instanceof DotNetRefTypeRef))
			{
				return typeRef;
			}

			return new CSharpRefTypeRef(CSharpRefTypeRef.Type.out, ((DotNetRefTypeRef) typeRef).getInnerTypeRef());
		}
		return typeRef;
	}

	@Override
	public String getName()
	{
		String name = super.getName();
		return name == null ? "p" + myIndex : name;
	}

	@Nullable
	@Override
	public DotNetParameterListOwner getOwner()
	{
		return myMethodDeclaration;
	}

	@Override
	public int getIndex()
	{
		return myIndex;
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitParameter(this);
	}
}
