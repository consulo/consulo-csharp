/*
 * Copyright 2013-2020 consulo.io
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

package consulo.csharp.impl.ide.structureView;

import consulo.fileEditor.structureView.tree.InheritedMembersNodeProvider;
import consulo.fileEditor.structureView.tree.TreeElement;
import consulo.language.psi.PsiElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.impl.psi.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.psi.resolve.CSharpElementGroup;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.language.psi.PsiNamedElement;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author VISTALL
 * @since 2020-06-28
 */
public class CSharpInheritedMembersNodeProvider extends InheritedMembersNodeProvider<CSharpNamedTreeElement>
{
	public static final CSharpInheritedMembersNodeProvider INSTANCE = new CSharpInheritedMembersNodeProvider();

	@Override
	@RequiredReadAction
	public Collection<CSharpNamedTreeElement> provideNodes(TreeElement treeElement)
	{
		if(!(treeElement instanceof CSharpNamedTreeElement))
		{
			return Collections.emptyList();
		}

		PsiNamedElement value = ((CSharpNamedTreeElement) treeElement).getValue();

		if(!(value instanceof CSharpTypeDeclaration))
		{
			return Collections.emptyList();
		}

		CSharpResolveContext context = CSharpResolveContextUtil.createContext(DotNetGenericExtractor.EMPTY, value.getResolveScope(), value);

		Set<PsiElement> elements = new LinkedHashSet<>();
		context.processElements(element -> {
			if(element instanceof CSharpElementGroup g)
			{
				elements.addAll(g.getElements());
			}
			else
			{
				elements.add(element);
			}
			return true;
		}, true);

		// remove self elements
		context.processElements(element -> {
			if(element instanceof CSharpElementGroup g)
			{
				elements.removeAll(g.getElements());
			}
			else
			{
				elements.remove(element);
			}
			return true;
		}, false);

		return elements.stream().map(element -> new CSharpNamedTreeElement((PsiNamedElement) element)).collect(Collectors.toList());
	}
}
