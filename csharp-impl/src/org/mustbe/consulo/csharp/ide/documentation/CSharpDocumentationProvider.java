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

package org.mustbe.consulo.csharp.ide.documentation;

import java.util.List;

import org.emonic.base.codehierarchy.CodeHierarchyHelper;
import org.emonic.base.documentation.IDocumentation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.ide.parameterInfo.CSharpParametersInfo;
import org.mustbe.consulo.csharp.lang.psi.CSharpArrayMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpEventDeclaration;
import org.mustbe.consulo.csharp.lang.psi.CSharpMethodDeclaration;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.dotnet.documentation.DotNetDocumentationCache;
import org.mustbe.consulo.dotnet.psi.*;
import org.mustbe.consulo.dotnet.resolve.DotNetArrayTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetGenericExtractor;
import org.mustbe.consulo.dotnet.resolve.DotNetPointerTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetPsiSearcher;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeResolveResult;
import com.intellij.lang.documentation.DocumentationProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.roots.OrderEntry;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiNamedElement;
import com.intellij.util.Function;
import com.intellij.xml.util.XmlStringUtil;

/**
 * @author VISTALL
 * @since 15.12.14
 */
public class CSharpDocumentationProvider implements DocumentationProvider
{
	private static final String TYPE_PREFIX = "csharp_type::";

	@Nullable
	@Override
	public String getQuickNavigateInfo(PsiElement element, PsiElement element2)
	{
		if(element instanceof DotNetTypeDeclaration)
		{
			return generateQuickTypeDeclarationInfo((DotNetTypeDeclaration) element);
		}
		else if(element instanceof DotNetVariable)
		{
			return generateQuickVariableInfo((DotNetVariable) element);
		}
		else if(element instanceof DotNetLikeMethodDeclaration)
		{
			return generateQuickLikeMethodDeclarationInfo((DotNetLikeMethodDeclaration) element);
		}
		return null;
	}

	@RequiredReadAction
	private static String generateQuickLikeMethodDeclarationInfo(DotNetLikeMethodDeclaration element)
	{
		StringBuilder builder = new StringBuilder();

		appendModifiers(element, builder);

		if(element instanceof CSharpMethodDeclaration && ((CSharpMethodDeclaration) element).isDelegate())
		{
			builder.append("delegate ");
		}

		if(element instanceof DotNetConstructorDeclaration)
		{
			if(((DotNetConstructorDeclaration) element).isDeConstructor())
			{
				builder.append("~");
			}
			builder.append(element.getName());
		}
		else
		{
			builder.append(generateLinksForType(element.getReturnTypeRef(), element));
			builder.append(" ");
			if(element instanceof CSharpArrayMethodDeclaration)
			{
				builder.append("this");
			}
			else
			{
				builder.append(XmlStringUtil.escapeString(element.getName()));
			}
		}
		char[] openAndCloseTokens = CSharpParametersInfo.getOpenAndCloseTokens(element);
		builder.append(openAndCloseTokens[0]);
		builder.append(StringUtil.join(element.getParameters(), new Function<DotNetParameter, String>()
		{
			@Override
			@RequiredReadAction
			public String fun(DotNetParameter dotNetParameter)
			{
				DotNetTypeRef typeRef = dotNetParameter.toTypeRef(true);
				if(typeRef == CSharpStaticTypeRef.__ARGLIST_TYPE)
				{
					return typeRef.getPresentableText();
				}
				return generateLinksForType(typeRef, dotNetParameter) + " " + dotNetParameter.getName();
			}
		}, ", "));
		builder.append(openAndCloseTokens[1]);
		return builder.toString();
	}

	private static String generateQuickVariableInfo(DotNetVariable element)
	{
		StringBuilder builder = new StringBuilder();

		appendModifiers(element, builder);
		if(element instanceof CSharpEventDeclaration)
		{
			builder.append("event ");
		}
		builder.append(generateLinksForType(element.toTypeRef(true), element));
		builder.append(" ");
		builder.append(element.getName());
		DotNetExpression initializer = element.getInitializer();
		if(initializer != null)
		{
			builder.append(" = ");
			builder.append(initializer.getText());
		}
		builder.append(";");
		return builder.toString();
	}

	private static String generateQuickTypeDeclarationInfo(DotNetTypeDeclaration element)
	{
		StringBuilder builder = new StringBuilder();

		PsiFile containingFile = element.getContainingFile();
		final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(element.getProject()).getFileIndex();
		VirtualFile vFile = containingFile == null ? null : containingFile.getVirtualFile();
		if(vFile != null && (fileIndex.isInLibrarySource(vFile) || fileIndex.isInLibraryClasses(vFile)))
		{
			final List<OrderEntry> orderEntries = fileIndex.getOrderEntriesForFile(vFile);
			if(orderEntries.size() > 0)
			{
				final OrderEntry orderEntry = orderEntries.get(0);
				builder.append("[").append(StringUtil.escapeXml(orderEntry.getPresentableName())).append("] ");
			}
		}
		else
		{
			final Module module = containingFile == null ? null : ModuleUtil.findModuleForPsiElement(containingFile);
			if(module != null)
			{
				builder.append('[').append(module.getName()).append("] ");
			}
		}

		String presentableParentQName = element.getPresentableParentQName();
		if(!StringUtil.isEmpty(presentableParentQName))
		{
			builder.append(presentableParentQName);
		}

		if(builder.length() > 0)
		{
			builder.append("<br>");
		}

		appendModifiers(element, builder);

		appendTypeDeclarationType(element, builder);

		builder.append(" ").append(element.getName());

		return builder.toString();
	}

	@Nullable
	@Override
	public List<String> getUrlFor(PsiElement element, PsiElement element2)
	{
		return null;
	}

	@Nullable
	@Override
	public String generateDoc(PsiElement element, @Nullable PsiElement element2)
	{
		IDocumentation documentation = DotNetDocumentationCache.getInstance().findDocumentation(element);
		if(documentation == null)
		{
			return null;
		}
		return CodeHierarchyHelper.getFormText(documentation);
	}

	private static void appendTypeDeclarationType(DotNetTypeDeclaration psiElement, StringBuilder builder)
	{
		if(psiElement.isInterface())
		{
			builder.append("interface");
		}
		else if(psiElement.isEnum())
		{
			builder.append("enum");
		}
		else if(psiElement.isStruct())
		{
			builder.append("struct");
		}
		else
		{
			builder.append("class");
		}
	}

	private static void appendModifiers(DotNetModifierListOwner owner, StringBuilder builder)
	{
		DotNetModifierList modifierList = owner.getModifierList();
		if(modifierList == null)
		{
			return;
		}

		for(DotNetModifier modifier : modifierList.getModifiers())
		{
			builder.append(modifier.getPresentableText()).append(" ");
		}
	}

	@Nullable
	@Override
	public PsiElement getDocumentationElementForLookupItem(PsiManager psiManager, Object o, PsiElement element)
	{
		return null;
	}

	@Nullable
	@Override
	public PsiElement getDocumentationElementForLink(PsiManager psiManager, String s, PsiElement element)
	{
		if(s.startsWith(TYPE_PREFIX))
		{
			String qName = s.substring(TYPE_PREFIX.length(), s.length());

			return DotNetPsiSearcher.getInstance(element.getProject()).findType(qName, element.getResolveScope());
		}
		return null;
	}

	@RequiredReadAction
	private static String generateLinksForType(DotNetTypeRef dotNetTypeRef, PsiElement element)
	{
		StringBuilder builder = new StringBuilder();
		if(dotNetTypeRef == DotNetTypeRef.AUTO_TYPE)
		{
			builder.append("var");
		}
		else if(dotNetTypeRef instanceof DotNetArrayTypeRef)
		{
			builder.append(generateLinksForType(((DotNetArrayTypeRef) dotNetTypeRef).getInnerTypeRef(), element));
			builder.append("[]");
		}
		else if(dotNetTypeRef instanceof DotNetPointerTypeRef)
		{
			builder.append(generateLinksForType(((DotNetPointerTypeRef) dotNetTypeRef).getInnerTypeRef(), element));
			builder.append("*");
		}
		else
		{
			DotNetTypeResolveResult dotNetTypeResolveResult = dotNetTypeRef.resolve(element);
			PsiElement resolved = dotNetTypeResolveResult.getElement();
			if(resolved instanceof DotNetQualifiedElement)
			{
				if(resolved instanceof DotNetGenericParameterListOwner)
				{
					wrapToLink(resolved, builder);

					DotNetGenericParameter[] genericParameters = ((DotNetGenericParameterListOwner) resolved).getGenericParameters();
					if(genericParameters.length > 0)
					{
						DotNetGenericExtractor genericExtractor = dotNetTypeResolveResult.getGenericExtractor();
						builder.append(XmlStringUtil.escapeString("<"));
						for(int i = 0; i < genericParameters.length; i++)
						{
							if(i != 0)
							{
								builder.append(", ");
							}
							DotNetGenericParameter parameter = genericParameters[i];

							DotNetTypeRef extractedTypeRef = genericExtractor.extract(parameter);
							if(extractedTypeRef != null)
							{
								builder.append(generateLinksForType(extractedTypeRef, element));
							}
							else
							{
								builder.append(parameter.getName());
							}
						}
						builder.append(XmlStringUtil.escapeString(">"));
					}
				}
				else
				{
					builder.append(((DotNetQualifiedElement) resolved).getName());
				}
			}
			else
			{
				builder.append(dotNetTypeRef.getPresentableText());
			}
		}

		return builder.toString();
	}

	@RequiredReadAction
	private static void wrapToLink(@NotNull PsiElement resolved, StringBuilder builder)
	{
		builder.append("<a href=\"psi_element://").append(TYPE_PREFIX);
		if(resolved instanceof DotNetTypeDeclaration)
		{
			builder.append(((DotNetTypeDeclaration) resolved).getVmQName());
		}
		else if(resolved instanceof DotNetQualifiedElement)
		{
			builder.append(((DotNetQualifiedElement) resolved).getPresentableQName());
		}
		else
		{
			builder.append(((PsiNamedElement) resolved).getName());
		}

		builder.append("\">").append(((PsiNamedElement) resolved).getName()).append("</a>");
	}
}
