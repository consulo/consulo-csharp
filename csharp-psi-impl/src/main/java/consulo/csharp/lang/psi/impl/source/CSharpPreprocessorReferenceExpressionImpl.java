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

package consulo.csharp.lang.psi.impl.source;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementWalkingVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.ResolveCache;
import com.intellij.util.CommonProcessors;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.Processor;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.access.RequiredWriteAction;
import consulo.csharp.lang.psi.CSharpMacroElementVisitor;
import consulo.csharp.lang.psi.CSharpPreprocesorTokens;
import consulo.csharp.lang.psi.CSharpPreprocessorDefine;
import consulo.csharp.lang.psi.CSharpPreprocessorVariable;
import consulo.csharp.lang.psi.impl.light.CSharpPreprocessorLightVariable;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import gnu.trove.THashMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * @author VISTALL
 * @since 24.01.14
 */
public class CSharpPreprocessorReferenceExpressionImpl extends CSharpPreprocessorElementImpl implements CSharpPreprocessorExpression, PsiReference
{
	public CSharpPreprocessorReferenceExpressionImpl(@Nonnull ASTNode node)
	{
		super(node);
	}

	@Override
	public PsiReference getReference()
	{
		return this;
	}

	@Override
	public void accept(@Nonnull CSharpMacroElementVisitor visitor)
	{
		visitor.visitReferenceExpression(this);
	}

	@RequiredReadAction
	@Override
	public PsiElement getElement()
	{
		return findNotNullChildByType(CSharpPreprocesorTokens.IDENTIFIER);
	}

	@Nonnull
	@RequiredReadAction
	@Override
	public TextRange getRangeInElement()
	{
		PsiElement element = getElement();
		return new TextRange(0, element.getTextLength());
	}

	@RequiredReadAction
	@Nullable
	@Override
	public PsiElement resolve()
	{
		return ResolveCache.getInstance(getProject()).resolveWithCaching(this, (expression, incompleteCode) ->
		{
			Ref<CSharpPreprocessorVariable> ref = Ref.create();
			collect(it ->
			{
				if(Comparing.equal(it.getName(), getElement().getText()))
				{
					ref.set(it);
					return false;
				}
				return true;
			});
			return ref.get();
		}, true, true);
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public String getCanonicalText()
	{
		return getText();
	}

	@RequiredWriteAction
	@Override
	public PsiElement handleElementRename(String s) throws IncorrectOperationException
	{
		return null;
	}

	@RequiredWriteAction
	@Override
	public PsiElement bindToElement(@Nonnull PsiElement element) throws IncorrectOperationException
	{
		return null;
	}

	@RequiredReadAction
	@Override
	public boolean isReferenceTo(PsiElement element)
	{
		return resolve() == element;
	}

	@RequiredReadAction
	@Nonnull
	@Override
	public Object[] getVariants()
	{
		CommonProcessors.CollectUniquesProcessor<CSharpPreprocessorVariable> processor = new CommonProcessors.CollectUniquesProcessor<>();
		collect(processor);
		return processor.toArray(CSharpPreprocessorVariable.ARRAY_FACTORY);
	}

	@RequiredReadAction
	protected void collect(@Nonnull Processor<CSharpPreprocessorVariable> processor)
	{
		DotNetSimpleModuleExtension<?> extension = ModuleUtilCore.getExtension(this, DotNetSimpleModuleExtension.class);
		if(extension == null)
		{
			return;
		}

		Map<String, CSharpPreprocessorVariable> map = new THashMap<>();
		for(String name : extension.getVariables())
		{
			CSharpPreprocessorLightVariable variable = new CSharpPreprocessorLightVariable(extension.getModule(), getElement(), name);
			map.put(name, variable);
		}

		getContainingFile().accept(new PsiRecursiveElementWalkingVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitElement(PsiElement element)
			{
				super.visitElement(element);

				if(element instanceof CSharpPreprocessorDefine)
				{
					String varName = ((CSharpPreprocessorDefine) element).getVarName();

					if(varName == null)
					{
						return;
					}

					if(((CSharpPreprocessorDefine) element).isUnDef())
					{
						map.remove(varName);
					}
					else
					{
						map.put(varName, ((CSharpPreprocessorDefine) element).getVariable());
					}
				}

				if(CSharpPreprocessorReferenceExpressionImpl.this.isEquivalentTo(element))
				{
					stopWalking();
				}
			}
		});

		for(CSharpPreprocessorVariable variable : map.values())
		{
			if(!processor.process(variable))
			{
				break;
			}
		}
	}

	@RequiredReadAction
	@Override
	public boolean isSoft()
	{
		return true;
	}
}
