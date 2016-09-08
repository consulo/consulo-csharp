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

package consulo.csharp.lang.psi.impl.msil;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import consulo.csharp.lang.psi.impl.DotNetTypes2;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpRefTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpTypeRefByQName;
import consulo.dotnet.DotNetTypes;
import consulo.dotnet.externalAttributes.ExternalAttributeArgumentNode;
import consulo.dotnet.externalAttributes.ExternalAttributeHolder;
import consulo.dotnet.externalAttributes.ExternalAttributeNode;
import consulo.dotnet.externalAttributes.ExternalAttributeSimpleNode;
import consulo.dotnet.externalAttributes.ExternalAttributeWithChildrenNode;
import consulo.dotnet.externalAttributes.nodes.ExternalAttributeNodeImpl;
import consulo.dotnet.psi.DotNetAttributeUtil;
import consulo.dotnet.psi.DotNetLikeMethodDeclaration;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.psi.DotNetParameter;
import consulo.dotnet.psi.DotNetParameterListOwner;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.resolve.DotNetRefTypeRef;
import consulo.dotnet.resolve.DotNetTypeRef;
import consulo.dotnet.util.ArrayUtil2;
import consulo.msil.lang.psi.MsilTokens;
import consulo.msil.lang.psi.impl.MsilTypeByRefImpl;
import com.intellij.openapi.util.Comparing;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 23.05.14
 */
public class MsilParameterAsCSharpParameter extends MsilVariableAsCSharpVariable implements DotNetParameter
{
	private final DotNetLikeMethodDeclaration myMethodDeclaration;
	private final int myIndex;

	@RequiredReadAction
	public MsilParameterAsCSharpParameter(PsiElement parent, DotNetVariable variable, DotNetLikeMethodDeclaration methodDeclaration, int index)
	{
		super(parent, getAdditionalModifiers(index, methodDeclaration, variable), variable);
		myMethodDeclaration = methodDeclaration;
		myIndex = index;
	}

	@RequiredReadAction
	private static CSharpModifier[] getAdditionalModifiers(int index, DotNetLikeMethodDeclaration parent, DotNetVariable variable)
	{
		if(index == 0)
		{
			PsiElement msilElement = parent.getOriginalElement();
			// we can use mirror due ExtensionAttribute is in ban list
			if(DotNetAttributeUtil.hasAttribute(msilElement, DotNetTypes.System.Runtime.CompilerServices.ExtensionAttribute))
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

	@RequiredReadAction
	@NotNull
	@Override
	protected MsilModifierListToCSharpModifierList createModifierList(CSharpModifier[] modifiers, DotNetVariable variable)
	{
		return new MsilModifierListToCSharpModifierList(modifiers, variable, variable.getModifierList())
		{
			@NotNull
			@Override
			@RequiredReadAction
			public List<ExternalAttributeNode> findAttributes(ExternalAttributeHolder holder)
			{
				PsiElement parent = myMethodDeclaration.getParent();
				if(!(parent instanceof DotNetTypeDeclaration))
				{
					return Collections.emptyList();
				}

				String vmQName = ((DotNetTypeDeclaration) parent).getVmQName();
				assert vmQName != null;


				List<ExternalAttributeNode> attributesFromExternal = getAttributesFromExternal(holder, vmQName);
				if(DotNetTypes2.System.Diagnostics.DebuggerDisplayAttribute.equals(vmQName))
				{
					if(getIndex() == 0)
					{
						ExternalAttributeNodeImpl externalAttributeNode = new ExternalAttributeNodeImpl("MustBe.Consulo.Attributes.InjectLanguageAttribute");
						externalAttributeNode.addArgument(new ExternalAttributeArgumentNode(DotNetTypes.System.String, "CFS:C#_EXPRESSION"));
						if(attributesFromExternal.isEmpty())
						{
							return Arrays.<ExternalAttributeNode>asList(externalAttributeNode);
						}
						else
						{
							List<ExternalAttributeNode> list = ContainerUtil.newArrayList(attributesFromExternal);
							list.add(externalAttributeNode);
							return list;
						}
					}
				}
				return attributesFromExternal;
			}

			@NotNull
			@RequiredReadAction
			private List<ExternalAttributeNode> getAttributesFromExternal(ExternalAttributeHolder holder, String vmQName)
			{
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
						if(!CSharpTypeUtil.isTypeEqual(parameterTypeRef, new CSharpTypeRefByQName(myMethodDeclaration, externalAttributeSimpleNode.getName()), myMethodDeclaration))
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
