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

package consulo.csharp.ide.structureView.sorters;

import java.util.Comparator;

import javax.annotation.Nonnull;

import consulo.csharp.ide.projectView.CSharpElementTreeNode;
import consulo.csharp.ide.structureView.CSharpNamedTreeElement;
import consulo.fileEditor.structureView.tree.ActionPresentation;
import consulo.fileEditor.structureView.tree.Sorter;
import consulo.language.psi.PsiNamedElement;

/**
 * @author VISTALL
 * @since 27.10.2015
 */
public class CSharpMemberSorter implements Sorter
{
	private static final String ID = "C#Member";

	public static final CSharpMemberSorter INSTANCE = new CSharpMemberSorter();

	private Comparator<?> myComparator = new Comparator<Object>()
	{
		@Override
		public int compare(Object o1, Object o2)
		{
			if(o1 instanceof CSharpNamedTreeElement && o2 instanceof CSharpNamedTreeElement)
			{
				PsiNamedElement value1 = ((CSharpNamedTreeElement) o1).getValue();
				PsiNamedElement value2 = ((CSharpNamedTreeElement) o2).getValue();

				return CSharpElementTreeNode.getWeight(value2) - CSharpElementTreeNode.getWeight(value1);
			}
			return 0;
		}
	};

	@Override
	public Comparator getComparator()
	{
		return myComparator;
	}

	@Override
	public boolean isVisible()
	{
		return false;
	}

	@Nonnull
	@Override
	public ActionPresentation getPresentation()
	{
		throw new IllegalArgumentException();
	}

	@Nonnull
	@Override
	public String getName()
	{
		return ID;
	}
}
