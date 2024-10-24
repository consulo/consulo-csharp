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

package consulo.csharp.lang.impl.psi.source.resolve.handlers;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.function.Processor;
import consulo.csharp.lang.impl.psi.source.CSharpReferenceExpressionImplUtil;
import consulo.csharp.lang.impl.psi.source.CSharpShortObjectInitializerExpressionImpl;
import consulo.csharp.lang.impl.psi.source.resolve.CSharpResolveOptions;
import consulo.csharp.lang.impl.psi.source.resolve.StubScopeProcessor;
import consulo.csharp.lang.impl.psi.source.resolve.util.CSharpResolveUtil;
import consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import consulo.csharp.lang.psi.CSharpFieldOrPropertySetBlock;
import consulo.csharp.lang.psi.CSharpNewExpression;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.resolve.DotNetGenericExtractor;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.dotnet.psi.resolve.DotNetTypeResolveResult;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.language.psi.resolve.ResolveState;
import consulo.language.psi.util.PsiTreeUtil;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 27.07.2015
 */
public class FieldOrPropertyKindProcessor implements KindProcessor
{
	@RequiredReadAction
	@Override
	public void process(@Nonnull CSharpResolveOptions options,
			@Nonnull DotNetGenericExtractor defaultExtractor,
			@Nullable PsiElement forceQualifierElement,
			@Nonnull Processor<ResolveResult> processor)
	{
		PsiElement element = options.getElement();

		DotNetTypeRef resolvedTypeRef = null;

		CSharpFieldOrPropertySetBlock block = PsiTreeUtil.getParentOfType(element, CSharpFieldOrPropertySetBlock.class);
		if(block != null)
		{
			PsiElement parent = block.getParent();
			if(parent instanceof CSharpShortObjectInitializerExpressionImpl)
			{
				resolvedTypeRef = ((CSharpShortObjectInitializerExpressionImpl) parent).toTypeRef(true);
			}
		}

		if(resolvedTypeRef == null)
		{
			CSharpCallArgumentListOwner callArgumentListOwner = PsiTreeUtil.getParentOfType(element, CSharpCallArgumentListOwner.class);

			if(callArgumentListOwner instanceof CSharpNewExpression)
			{
				resolvedTypeRef = ((CSharpNewExpression) callArgumentListOwner).toTypeRef(false);
			}
			else if(callArgumentListOwner instanceof DotNetAttribute)
			{
				resolvedTypeRef = ((DotNetAttribute) callArgumentListOwner).toTypeRef();
			}
			else
			{
				resolvedTypeRef = DotNetTypeRef.ERROR_TYPE;
			}
		}

		if(resolvedTypeRef == DotNetTypeRef.ERROR_TYPE)
		{
			return;
		}

		DotNetTypeResolveResult typeResolveResult = resolvedTypeRef.resolve();

		PsiElement typeElement = typeResolveResult.getElement();
		if(typeElement == null)
		{
			return;
		}

		DotNetGenericExtractor genericExtractor = typeResolveResult.getGenericExtractor();

		StubScopeProcessor scopeProcessor = CSharpReferenceExpressionImplUtil.createMemberProcessor(options, processor);

		ResolveState state = ResolveState.initial();
		state = state.put(CSharpResolveUtil.EXTRACTOR, genericExtractor);
		state = state.put(CSharpResolveUtil.SELECTOR, options.getSelector());
		CSharpResolveUtil.walkChildren(scopeProcessor, typeElement, false, true, state);
	}
}
