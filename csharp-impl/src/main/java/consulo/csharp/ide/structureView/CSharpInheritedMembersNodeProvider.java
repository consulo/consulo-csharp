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

package consulo.csharp.ide.structureView;

import com.intellij.ide.util.InheritedMembersNodeProvider;
import com.intellij.ide.util.treeView.smartTree.TreeElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.csharp.lang.psi.impl.resolve.CSharpResolveContextUtil;
import consulo.csharp.lang.psi.resolve.CSharpResolveContext;
import consulo.dotnet.resolve.DotNetGenericExtractor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

		List<PsiElement> elements = new ArrayList<>();
		context.processElements(element -> {
			elements.add(element);
			return true;
		}, true);

		// remove self elements
		context.processElements(element -> {
			elements.remove(element);
			return true;
		}, false);

		return elements.stream().map(element -> new CSharpNamedTreeElement((PsiNamedElement) element)).collect(Collectors.toList());
	}
}
