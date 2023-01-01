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

package consulo.csharp.impl.ide.debugger.expressionEvaluator;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.debugger.CSharpEvaluateContext;
import consulo.csharp.impl.ide.debugger.CSharpStaticValueProxy;
import consulo.csharp.lang.psi.CSharpTypeDeclaration;
import consulo.dotnet.debugger.DotNetDebugContext;
import consulo.dotnet.debugger.proxy.*;
import consulo.dotnet.debugger.proxy.value.DotNetValueProxy;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiUtilCore;
import consulo.virtualFileSystem.VirtualFile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public abstract class Evaluator
{
	public abstract void evaluate(@Nonnull CSharpEvaluateContext context) throws DotNetInvalidObjectException, DotNetThrowValueException, DotNetInvalidStackFrameException,
			DotNetAbsentInformationException, DotNetNotSuspendedException;

	@Nullable
	public static DotNetValueProxy substituteStaticContext(@Nonnull DotNetValueProxy proxy)
	{
		return proxy == CSharpStaticValueProxy.INSTANCE ? null : proxy;
	}

	@Nullable
	@RequiredReadAction
	public static DotNetTypeProxy findTypeMirror(@Nonnull CSharpEvaluateContext context, @Nullable PsiElement element)
	{
		if(element instanceof CSharpTypeDeclaration)
		{
			DotNetDebugContext debuggerContext = context.getDebuggerContext();

			VirtualFile virtualFile = PsiUtilCore.getVirtualFile(element);
			if(virtualFile == null)
			{
				return null;
			}
			return debuggerContext.getVirtualMachine().findType(element.getProject(), ((CSharpTypeDeclaration) element).getVmQName(), virtualFile);
		}
		return null;
	}
}
