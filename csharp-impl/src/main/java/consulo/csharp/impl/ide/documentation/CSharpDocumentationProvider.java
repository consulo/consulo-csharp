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

package consulo.csharp.impl.ide.documentation;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.impl.ide.parameterInfo.CSharpParametersInfo;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.impl.psi.CSharpTypeRefPresentationUtil;
import consulo.csharp.lang.impl.psi.source.resolve.overrideSystem.OverrideUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpRefTypeRef;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpStaticTypeRef;
import consulo.csharp.lang.psi.CSharpEventDeclaration;
import consulo.csharp.lang.psi.CSharpIndexMethodDeclaration;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.lang.psi.CSharpTypeDefStatement;
import consulo.dotnet.impl.documentation.DotNetDocumentationCache;
import consulo.dotnet.psi.*;
import consulo.dotnet.psi.resolve.*;
import consulo.language.Language;
import consulo.language.editor.documentation.LanguageDocumentationProvider;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.language.psi.PsiNamedElement;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.ProjectRootManager;
import consulo.module.content.layer.orderEntry.OrderEntry;
import consulo.util.lang.StringUtil;
import consulo.util.lang.xml.XmlStringUtil;
import consulo.virtualFileSystem.VirtualFile;
import org.emonic.base.codehierarchy.CodeHierarchyHelper;
import org.emonic.base.documentation.IDocumentation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * @author VISTALL
 * @since 15.12.14
 */
@ExtensionImpl
public class CSharpDocumentationProvider implements LanguageDocumentationProvider
{
	private static final String TYPE_PREFIX = "csharp_type::";

	@Nullable
	@Override
	@RequiredReadAction
	public String getQuickNavigateInfo(PsiElement element, PsiElement element2)
	{
		return getNavigateInfo(element, false);
	}

	@Nullable
	@RequiredReadAction
	private static String getNavigateInfo(PsiElement element, boolean isFullDocumentation)
	{
		if(element instanceof DotNetTypeDeclaration)
		{
			return generateQuickTypeDeclarationInfo((DotNetTypeDeclaration) element, isFullDocumentation);
		}
		else if(element instanceof DotNetVariable)
		{
			return generateQuickVariableInfo((DotNetVariable) element, isFullDocumentation);
		}
		else if(element instanceof DotNetLikeMethodDeclaration)
		{
			return generateQuickLikeMethodDeclarationInfo((DotNetLikeMethodDeclaration) element, isFullDocumentation);
		}
		else if(element instanceof CSharpTypeDefStatement)
		{
			return generateQuickTypeAliasInfo((CSharpTypeDefStatement) element, isFullDocumentation);
		}
		return null;
	}

	@RequiredReadAction
	private static String generateQuickTypeAliasInfo(CSharpTypeDefStatement typeDefStatement, boolean isFullDocumentation)
	{
		StringBuilder builder = new StringBuilder();
		builder.append("type alias ");
		appendName(typeDefStatement, builder, isFullDocumentation);

		DotNetTypeRef typeRef = typeDefStatement.toTypeRef();
		if(typeRef != DotNetTypeRef.ERROR_TYPE)
		{
			builder.append(" = ");
			builder.append(generateLinksForType(typeRef, typeDefStatement, true));
		}
		return builder.toString();
	}

	@RequiredReadAction
	private static String generateQuickLikeMethodDeclarationInfo(DotNetLikeMethodDeclaration element, boolean isFullDocumentation)
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
			appendName(element, builder, isFullDocumentation);
		}
		else
		{
			builder.append(generateLinksForType(element.getReturnTypeRef(), element, isFullDocumentation));
			builder.append(" ");
			if(element instanceof CSharpIndexMethodDeclaration)
			{
				builder.append("this");
			}
			else
			{
				if(isFullDocumentation)
				{
					builder.append("<b>");
				}

				builder.append(XmlStringUtil.escapeString(element.getName()));

				if(isFullDocumentation)
				{
					builder.append("</b>");
				}
			}
		}
		DotNetGenericParameter[] genericParameters = element.getGenericParameters();
		if(genericParameters.length > 0)
		{
			builder.append("&lt;");
			for(int i = 0; i < genericParameters.length; i++)
			{
				DotNetGenericParameter genericParameter = genericParameters[i];
				if(i != 0)
				{
					builder.append(", ");
				}
				builder.append(genericParameter.getName());
			}
			builder.append("&gt;");
		}
		char[] openAndCloseTokens = CSharpParametersInfo.getOpenAndCloseTokens(element);
		builder.append(openAndCloseTokens[0]);
		builder.append(StringUtil.join(element.getParameters(), new Function<DotNetParameter, String>()
		{
			@Override
			@RequiredReadAction
			public String apply(DotNetParameter dotNetParameter)
			{
				DotNetTypeRef typeRef = dotNetParameter.toTypeRef(true);
				if(typeRef == CSharpStaticTypeRef.__ARGLIST_TYPE)
				{
					return typeRef.toString();
				}
				return generateLinksForType(typeRef, dotNetParameter, false) + " " + dotNetParameter.getName();
			}
		}, ", "));
		builder.append(openAndCloseTokens[1]);
		return builder.toString();
	}

	@RequiredReadAction
	private static String generateQuickVariableInfo(DotNetVariable element, boolean isFullDocumentation)
	{
		StringBuilder builder = new StringBuilder();

		appendModifiers(element, builder);
		if(element instanceof CSharpEventDeclaration)
		{
			builder.append("event ");
		}
		builder.append(generateLinksForType(element.toTypeRef(true), element, isFullDocumentation));
		builder.append(" ");
		appendName(element, builder, isFullDocumentation);
		DotNetExpression initializer = element.getInitializer();
		if(initializer != null)
		{
			builder.append(" = ");
			builder.append(initializer.getText());
		}
		builder.append(";");
		return builder.toString();
	}

	@RequiredReadAction
	private static String generateQuickTypeDeclarationInfo(DotNetTypeDeclaration element, boolean isFullDocumentation)
	{
		StringBuilder builder = new StringBuilder();

		if(isFullDocumentation)
		{
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
				final Module module = containingFile == null ? null : ModuleUtilCore.findModuleForPsiElement(containingFile);
				if(module != null)
				{
					builder.append('[').append(module.getName()).append("] ");
				}
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

		builder.append(" ");

		appendName(element, builder, isFullDocumentation);

		return builder.toString();
	}

	@RequiredReadAction
	private static void appendName(PsiNamedElement element, StringBuilder builder, boolean isFullDocumentation)
	{
		if(isFullDocumentation)
		{
			builder.append("<b>").append(element.getName()).append("</b>");
		}
		else
		{
			builder.append(element.getName());
		}
	}

	@Nullable
	@Override
	public List<String> getUrlFor(PsiElement element, PsiElement element2)
	{
		return null;
	}

	@Nullable
	@Override
	@RequiredReadAction
	public String generateDoc(PsiElement element, @Nullable PsiElement element2)
	{
		if(element instanceof CSharpTypeDefStatement)
		{
			PsiElement resolvedElement = ((CSharpTypeDefStatement) element).toTypeRef().resolve().getElement();
			if(resolvedElement != null)
			{
				element = resolvedElement;
			}
		}

		StringBuilder builder = new StringBuilder();
		String info = getNavigateInfo(element, true);
		if(info != null)
		{
			builder.append(info);
			builder.append("<br><br>");
		}

		IDocumentation documentation = DotNetDocumentationCache.getInstance().findDocumentation(element);
		if(documentation == null)
		{
			documentation = tryToFindFromOverrideParent(element);
		}

		if(documentation != null)
		{
			builder.append(CodeHierarchyHelper.getFormText(documentation));
		}

		return builder.length() > 0 ? builder.toString() : null;
	}

	@RequiredReadAction
	private IDocumentation tryToFindFromOverrideParent(PsiElement element)
	{
		if(!(element instanceof DotNetVirtualImplementOwner))
		{
			return null;
		}

		Collection<DotNetVirtualImplementOwner> members = OverrideUtil.collectOverridingMembers((DotNetVirtualImplementOwner) element);

		for(DotNetVirtualImplementOwner owner : members)
		{
			IDocumentation documentation = DotNetDocumentationCache.getInstance().findDocumentation(owner);
			if(documentation != null)
			{
				return documentation;
			}
		}
		return null;
	}

	@RequiredReadAction
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

	@RequiredReadAction
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
	@RequiredReadAction
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
	private static String generateLinksForType(DotNetTypeRef typeRef, PsiElement element, boolean qualified)
	{
		StringBuilder builder = new StringBuilder();
		if(typeRef == DotNetTypeRef.AUTO_TYPE)
		{
			builder.append("var");
		}
		else if(typeRef instanceof DotNetArrayTypeRef)
		{
			builder.append(generateLinksForType(((DotNetArrayTypeRef) typeRef).getInnerTypeRef(), element, qualified));
			builder.append("[]");
		}
		else if(typeRef instanceof CSharpRefTypeRef)
		{
			builder.append(((CSharpRefTypeRef) typeRef).getType().name());
			builder.append(" ");
			builder.append(generateLinksForType(((CSharpRefTypeRef) typeRef).getInnerTypeRef(), element, qualified));
		}
		else if(typeRef instanceof DotNetPointerTypeRef)
		{
			builder.append(generateLinksForType(((DotNetPointerTypeRef) typeRef).getInnerTypeRef(), element, qualified));
			builder.append("*");
		}
		else
		{
			DotNetTypeResolveResult dotNetTypeResolveResult = typeRef.resolve();
			PsiElement resolved = dotNetTypeResolveResult.getElement();
			if(resolved instanceof DotNetQualifiedElement)
			{
				if(resolved instanceof DotNetGenericParameterListOwner)
				{
					wrapToLink(resolved, builder, qualified);

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
								builder.append(generateLinksForType(extractedTypeRef, element, qualified));
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
				builder.append(CSharpTypeRefPresentationUtil.buildShortText(typeRef));
			}
		}

		return builder.toString();
	}

	@RequiredReadAction
	private static void wrapToLink(@Nonnull PsiElement resolved, StringBuilder builder, boolean qualified)
	{
		String parentQName = qualified ? resolved instanceof DotNetQualifiedElement ? ((DotNetQualifiedElement) resolved).getPresentableParentQName() : null : null;

		if(!StringUtil.isEmpty(parentQName))
		{
			builder.append(parentQName).append('.');
		}

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

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return CSharpLanguage.INSTANCE;
	}
}
