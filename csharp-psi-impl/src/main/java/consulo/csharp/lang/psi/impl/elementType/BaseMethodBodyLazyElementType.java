/*
 * Copyright 2013-2019 consulo.io
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

package consulo.csharp.lang.psi.impl.elementType;

import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilderFactory;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.ILazyParseableElementType;
import com.intellij.psi.util.CachedValueProvider;
import com.intellij.psi.util.CachedValuesManager;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.lang.CSharpLanguage;
import consulo.csharp.lang.parser.CSharpBuilderWrapper;
import consulo.csharp.lang.parser.ModifierSet;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.csharp.lang.psi.CSharpModifierList;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.impl.source.CSharpMethodBodyImpl;
import consulo.csharp.lang.psi.impl.stub.elementTypes.CSharpFileStubElementType;
import consulo.dotnet.psi.DotNetModifierListOwner;
import consulo.lang.LanguageVersion;
import org.jetbrains.annotations.NonNls;

import javax.annotation.Nonnull;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2019-10-03
 */
public abstract class BaseMethodBodyLazyElementType extends ILazyParseableElementType
{
	public BaseMethodBodyLazyElementType(@Nonnull @NonNls String debugName)
	{
		super(debugName, CSharpLanguage.INSTANCE);
	}

	protected abstract void parse(@Nonnull CSharpBuilderWrapper wrapper, @Nonnull ModifierSet set);

	@Override
	@RequiredReadAction
	protected ASTNode doParseContents(@Nonnull ASTNode chameleon, @Nonnull PsiElement psi)
	{
		final Project project = psi.getProject();
		final Language languageForParser = getLanguageForParser(psi);
		final LanguageVersion tempLanguageVersion = chameleon.getUserData(LanguageVersion.KEY);
		final LanguageVersion languageVersion = tempLanguageVersion == null ? psi.getLanguageVersion() : tempLanguageVersion;
		final PsiBuilder builder = PsiBuilderFactory.getInstance().createBuilder(project, chameleon, null, languageForParser, languageVersion, chameleon.getChars());

		builder.putUserData(CSharpFileStubElementType.PREPROCESSOR_VARIABLES, collectVariableFor(psi));
		CSharpBuilderWrapper wrapper = new CSharpBuilderWrapper(builder, languageVersion);

		PsiBuilder.Marker mark = wrapper.mark();

		ModifierSet modifierSet = ModifierSet.EMPTY;
		if(psi instanceof DotNetModifierListOwner)
		{
			CSharpModifierList modifierList = (CSharpModifierList) ((DotNetModifierListOwner) psi).getModifierList();
			if(modifierList != null && modifierList.hasModifierInTree(CSharpModifier.ASYNC))
			{
				modifierSet = ModifierSet.create(CSharpSoftTokens.ASYNC_KEYWORD);
			}
		}
		parse(wrapper, modifierSet);

		while(!wrapper.eof())
		{
			PsiBuilder.Marker er = wrapper.mark();
			wrapper.advanceLexer();
			er.error("Unexpected token");
		}

		mark.done(this);

		return wrapper.getTreeBuilt().getFirstChildNode();
	}

	@Nonnull
	private static Set<String> collectVariableFor(@Nonnull PsiElement element)
	{
		return CachedValuesManager.getCachedValue(element, () -> {
			PsiFile psiFile = element.getContainingFile();
			Set<String> defines = CSharpFileStubElementType.getStableDefines(psiFile);
			CSharpPreprocessorVisitor visitor = new CSharpPreprocessorVisitor(defines, element.getStartOffsetInParent());
			psiFile.accept(visitor);
			return CachedValueProvider.Result.create(visitor.getVariables(), element);
		}) ;
	}

	@Override
	public ASTNode createNode(CharSequence text)
	{
		return new CSharpMethodBodyImpl(this, text);
	}
}
