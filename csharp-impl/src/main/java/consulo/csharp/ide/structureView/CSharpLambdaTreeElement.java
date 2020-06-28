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

import com.intellij.icons.AllIcons;
import com.intellij.ide.structureView.StructureViewTreeElement;
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase;
import consulo.csharp.lang.psi.impl.source.CSharpAnonymousMethodExpression;
import consulo.csharp.lang.psi.impl.source.CSharpDelegateExpressionImpl;
import consulo.ui.image.Image;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 2020-06-28
 */
public class CSharpLambdaTreeElement extends PsiTreeElementBase<CSharpAnonymousMethodExpression>
{
	public CSharpLambdaTreeElement(CSharpAnonymousMethodExpression psiElement)
	{
		super(psiElement);
	}

	@Nonnull
	@Override
	public Collection<StructureViewTreeElement> getChildrenBase()
	{
		return Collections.emptyList();
	}

	@Override
	public Image getIcon()
	{
		return AllIcons.Nodes.Lambda;
	}

	@Nullable
	@Override
	public String getPresentableText()
	{
		CSharpAnonymousMethodExpression value = getValue();
		if(value instanceof CSharpDelegateExpressionImpl)
		{
			return "Delegate";
		}
		return "Lambda";
	}
}
