/*
 * Copyright 2013-2016 must-be.org
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

package consulo.csharp.ide.actions.generate.memberChoose;

import consulo.dotnet.psi.DotNetElement;
import com.intellij.psi.PsiElement;
import com.intellij.util.PairConsumer;

/**
 * @author VISTALL
 * @since 02.04.2016
 */
public abstract class ImplementMemberChooseObject<T extends DotNetElement> extends CSharpMemberChooseObject<T>
{
	protected PairConsumer<PsiElement, StringBuilder> myAdditionalModifiersAppender;
	protected final PairConsumer<PsiElement, StringBuilder> myReturnAppender;
	protected final boolean myCanGenerateBlock;

	public ImplementMemberChooseObject(T declaration, PairConsumer<PsiElement, StringBuilder> additionalModifiersAppender, PairConsumer<PsiElement, StringBuilder> returnAppender, boolean canGenerateBlock)
	{
		super(declaration);
		myAdditionalModifiersAppender = additionalModifiersAppender;
		myReturnAppender = returnAppender;
		myCanGenerateBlock = canGenerateBlock;
	}
}
