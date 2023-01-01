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

package consulo.csharp.impl.ide.highlight.check;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.csharp.lang.impl.parser.preprocessor.NullableDirective;
import consulo.csharp.lang.impl.parser.preprocessor.PreprocessorDirective;
import consulo.csharp.lang.impl.parser.preprocessor.PreprocessorLightParser;
import consulo.csharp.lang.impl.psi.CSharpPreprocessorElements;
import consulo.csharp.lang.impl.psi.CSharpRecursiveElementVisitor;
import consulo.csharp.lang.impl.psi.source.CSharpPreprocessorNullableImpl;
import consulo.csharp.module.CSharpNullableOption;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiModificationTracker;
import consulo.language.psi.PsiUtilCore;
import consulo.language.psi.util.LanguageCachedValueUtil;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.util.collection.Lists;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 2020-07-30
 *
 * @see CSharpPragmaContext
 */
public class CSharpNullableContext
{
	private static class NullableEntry
	{
		private CSharpNullableOption option;
		private int startOffset;
		private int endOffset = -1;
	}

	@Nonnull
	@RequiredReadAction
	public static CSharpNullableContext get(PsiFile file)
	{
		return LanguageCachedValueUtil.getCachedValue(file, () -> CachedValueProvider.Result.create(build(file), PsiModificationTracker.MODIFICATION_COUNT));
	}

	@Nonnull
	@RequiredReadAction
	private static CSharpNullableContext build(PsiFile file)
	{
		List<NullableEntry> actions = new ArrayList<>();
		file.accept(new CSharpRecursiveElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitElement(PsiElement element)
			{
				super.visitElement(element);

				if(PsiUtilCore.getElementType(element) == CSharpPreprocessorElements.PREPROCESSOR_DIRECTIVE)
				{
					CSharpPreprocessorNullableImpl nullable = PsiTreeUtil.findChildOfType(element, CSharpPreprocessorNullableImpl.class);
					if(nullable != null)
					{
						PreprocessorDirective directive = PreprocessorLightParser.parse(nullable.getNode().getChars());

						if(directive instanceof NullableDirective)
						{
							String value = ((NullableDirective) directive).getValue();
							switch(value)
							{
								case "restore":
									for(NullableEntry action : Lists.iterateBackward(actions))
									{
										if(action.endOffset == -1)
										{
											action.endOffset = element.getTextRange().getEndOffset();
											break;
										}
									}
									break;
								case "enable":
								case "disable":
									NullableEntry action = new NullableEntry();
									actions.add(action);

									action.option = value.equals("enable") ? CSharpNullableOption.ENABLE : CSharpNullableOption.DISABLE;
									action.startOffset = element.getTextOffset();
									break;
							}
						}
					}
				}
			}
		});

		for(NullableEntry action : actions)
		{
			if(action.endOffset == -1)
			{
				action.endOffset = file.getTextLength();
			}
		}
		Collections.reverse(actions);
		return new CSharpNullableContext(actions);
	}

	private final List<NullableEntry> myStates;

	public CSharpNullableContext(List<NullableEntry> states)
	{
		myStates = states;
	}

	@Nonnull
	public CSharpNullableOption getNullable(@Nonnull PsiElement element, @Nonnull CSharpNullableOption defaultValue)
	{
		int textOffset = element.getTextOffset();

		for(NullableEntry action : myStates)
		{
			if(textOffset >= action.startOffset && textOffset <= action.endOffset)
			{
				return action.option;
			}
		}

		return defaultValue;
	}
}
