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

package consulo.csharp.impl.ide.highlight.check;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValueProvider;
import consulo.csharp.lang.impl.parser.preprocessor.PragmaWarningPreprocessorDirective;
import consulo.csharp.lang.impl.parser.preprocessor.PreprocessorDirective;
import consulo.csharp.lang.impl.parser.preprocessor.PreprocessorLightParser;
import consulo.csharp.lang.impl.psi.CSharpPreprocessorElements;
import consulo.csharp.lang.impl.psi.CSharpRecursiveElementVisitor;
import consulo.csharp.lang.impl.psi.source.CSharpPreprocessorPragmaImpl;
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
 * @since 06-Nov-17
 */
public class CSharpPragmaContext
{
	private static class PragmaSuppressAction
	{
		private String id;
		private int startOffset;
		private int endOffset = -1;
	}

	@Nonnull
	@RequiredReadAction
	public static CSharpPragmaContext get(PsiFile file)
	{
		return LanguageCachedValueUtil.getCachedValue(file, () -> CachedValueProvider.Result.create(build(file), PsiModificationTracker.MODIFICATION_COUNT));
	}

	@Nonnull
	@RequiredReadAction
	private static CSharpPragmaContext build(PsiFile file)
	{
		List<PragmaSuppressAction> actions = new ArrayList<>();
		file.accept(new CSharpRecursiveElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitElement(PsiElement element)
			{
				super.visitElement(element);

				if(PsiUtilCore.getElementType(element) == CSharpPreprocessorElements.PREPROCESSOR_DIRECTIVE)
				{
					CSharpPreprocessorPragmaImpl pragma = PsiTreeUtil.findChildOfType(element, CSharpPreprocessorPragmaImpl.class);
					if(pragma != null)
					{
						PreprocessorDirective directive = PreprocessorLightParser.parse(pragma.getNode().getChars());

						if(directive instanceof PragmaWarningPreprocessorDirective)
						{
							switch(((PragmaWarningPreprocessorDirective) directive).getAction())
							{
								case "restore":
									for(String id : ((PragmaWarningPreprocessorDirective) directive).getArguments())
									{
										for(PragmaSuppressAction action : Lists.iterateBackward(actions))
										{
											if(action.id.equalsIgnoreCase(id) && action.endOffset == -1)
											{
												action.endOffset = element.getTextRange().getEndOffset();
												break;
											}
										}
									}
									break;
								case "disable":
									for(String id : ((PragmaWarningPreprocessorDirective) directive).getArguments())
									{
										PragmaSuppressAction action = new PragmaSuppressAction();
										actions.add(action);

										action.id = id;
										action.startOffset = element.getTextOffset();
									}
									break;
							}
						}
					}
				}
			}
		});

		for(PragmaSuppressAction action : actions)
		{
			if(action.endOffset == -1)
			{
				action.endOffset = file.getTextLength();
			}
		}
		Collections.reverse(actions);
		return new CSharpPragmaContext(actions);
	}

	private final List<PragmaSuppressAction> myActions;

	public CSharpPragmaContext(List<PragmaSuppressAction> actions)
	{
		myActions = actions;
	}

	public boolean isSuppressed(CSharpCompilerChecks classEntry, PsiElement element)
	{
		if(!classEntry.isSuppressable())
		{
			return false;
		}

		String checkId = classEntry.name();
		int textOffset = element.getTextOffset();

		for(PragmaSuppressAction action : myActions)
		{
			if(checkId.equalsIgnoreCase(action.id))
			{
				if(textOffset >= action.startOffset && textOffset <= action.endOffset)
				{
					return true;
				}
			}
		}
		return false;
	}
}
