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

package consulo.csharp.impl.ide.actions.navigate;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.codeEditor.Editor;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImpl;
import consulo.csharp.lang.impl.psi.source.CSharpLambdaExpressionImplUtil;
import consulo.csharp.lang.impl.psi.source.resolve.type.CSharpLambdaResolveResult;
import consulo.csharp.lang.psi.CSharpNativeType;
import consulo.csharp.lang.psi.CSharpSoftTokens;
import consulo.csharp.lang.psi.CSharpTokens;
import consulo.dotnet.psi.DotNetVariable;
import consulo.dotnet.psi.resolve.DotNetTypeRef;
import consulo.language.ast.IElementType;
import consulo.language.editor.navigation.GotoDeclarationHandlerBase;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;

import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 2020-06-08
 */
@ExtensionImpl
public class CSharpGotoDeclarationHandler extends GotoDeclarationHandlerBase
{
	@Nullable
	@Override
	@RequiredReadAction
	public PsiElement getGotoDeclarationTarget(PsiElement psiElement, Editor editor)
	{
		IElementType elementType = PsiUtilCore.getElementType(psiElement);
		if(elementType == CSharpSoftTokens.VAR_KEYWORD)
		{
			PsiElement maybeType = psiElement.getParent();
			if(maybeType instanceof CSharpNativeType)
			{
				PsiElement maybeVar = maybeType.getParent();
				if(maybeVar instanceof DotNetVariable)
				{
					DotNetTypeRef ref = ((DotNetVariable) maybeVar).toTypeRef(true);
					return ref.resolve().getElement();
				}
			}
		}
		else if(elementType == CSharpTokens.DARROW)
		{
			if(psiElement.getParent() instanceof CSharpLambdaExpressionImpl)
			{
				CSharpLambdaResolveResult typeRef = CSharpLambdaExpressionImplUtil.resolveLeftLambdaTypeRef(psiElement.getParent());

				if(typeRef != null)
				{
					PsiElement element = typeRef.getElement();
					return element != null ? element.getNavigationElement() : null;
				}
			}
		}
		return null;
	}
}
