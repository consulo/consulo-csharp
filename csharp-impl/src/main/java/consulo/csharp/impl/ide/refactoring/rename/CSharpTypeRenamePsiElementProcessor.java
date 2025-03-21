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

package consulo.csharp.impl.ide.refactoring.rename;

import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.content.scope.SearchScope;
import consulo.csharp.lang.impl.psi.partial.CSharpCompositeTypeDeclaration;
import consulo.csharp.lang.impl.psi.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.impl.psi.resolve.CSharpTypeResolveContext;
import consulo.csharp.lang.impl.psi.source.CSharpPsiUtilImpl;
import consulo.csharp.lang.psi.CSharpConstructorDeclaration;
import consulo.csharp.lang.psi.CSharpFile;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.dotnet.psi.DotNetNamedElement;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.language.editor.refactoring.rename.RenamePsiElementProcessor;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.MultiMap;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;
import java.util.Map;

/**
 * @author VISTALL
 * @since 17.01.15
 */
@ExtensionImpl
public class CSharpTypeRenamePsiElementProcessor extends RenamePsiElementProcessor
{
	@Nullable
	@Override
	public PsiElement substituteElementToRename(PsiElement element, @Nullable Editor editor)
	{
		if(element instanceof CSharpConstructorDeclaration)
		{
			return PsiTreeUtil.getParentOfType(element, CSharpTypeDeclaration.class);
		}
		return super.substituteElementToRename(element, editor);
	}

	@Override
	public boolean canProcessElement(@Nonnull PsiElement element)
	{
		return element instanceof CSharpTypeDeclaration || element instanceof CSharpConstructorDeclaration;
	}

	@Override
	public void findExistingNameConflicts(PsiElement element,
										  String newName,
										  MultiMap<PsiElement, String> conflicts,
										  Map<PsiElement, String> allRenames)
	{
		for(Map.Entry<PsiElement, String> entry : allRenames.entrySet())
		{
			if(entry.getKey() instanceof CSharpFile)
			{
				CSharpFile key = (CSharpFile) entry.getKey();
				PsiDirectory parent = key.getParent();
				if(parent == null)
				{
					continue;
				}

				PsiFile file = parent.findFile(entry.getValue());
				if(file != null)
				{
					conflicts.putValue(file, entry.getValue() + " already exists in parent directory");
				}
			}
		}
	}

	@Override
	public void prepareRenaming(PsiElement element, String newName, Map<PsiElement, String> allRenames, SearchScope scope)
	{
		CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, element.getResolveScope(), element);

		CSharpElementGroup<CSharpConstructorDeclaration> constructors = context.constructorGroup();
		if(constructors != null)
		{
			for(CSharpConstructorDeclaration declaration : constructors.getElements())
			{
				allRenames.put(declaration, newName);
			}
		}
		constructors = context.deConstructorGroup();
		if(constructors != null)
		{
			for(CSharpConstructorDeclaration declaration : constructors.getElements())
			{
				allRenames.put(declaration, newName);
			}
		}

		CSharpTypeDeclaration[] typesIfPartial = getTypesIfPartial(context);

		for(CSharpTypeDeclaration typeDeclaration : typesIfPartial)
		{
			allRenames.put(typeDeclaration, newName);

			PsiFile containingFile = typeDeclaration.getContainingFile();
			if(containingFile instanceof CSharpFile)
			{
				DotNetNamedElement singleElement = CSharpPsiUtilImpl.findSingleElement((CSharpFile) containingFile);
				if(typeDeclaration.isEquivalentTo(singleElement))
				{
					allRenames.put(containingFile, newName + "." + containingFile.getFileType().getDefaultExtension());
				}
			}
		}
	}

	private static CSharpTypeDeclaration[] getTypesIfPartial(CSharpResolveContext context)
	{
		if(context instanceof CSharpTypeResolveContext)
		{
			// if we have composite - that partial type, need append other types
			CSharpTypeDeclaration maybeCompositeType = ((CSharpTypeResolveContext) context).getElement();
			if(maybeCompositeType instanceof CSharpCompositeTypeDeclaration)
			{
				return ((CSharpCompositeTypeDeclaration) maybeCompositeType).getTypeDeclarations();
			}
			else
			{
				return new CSharpTypeDeclaration[]{maybeCompositeType};
			}
		}
		return CSharpTypeDeclaration.EMPTY_ARRAY;
	}
}
