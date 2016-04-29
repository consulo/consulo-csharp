/*
 * Copyright 2013-2015 must-be.org
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

package org.mustbe.consulo.csharp.ide.debugger.expressionEvaluator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.ide.debugger.CSharpEvaluateContext;
import org.mustbe.consulo.csharp.lang.psi.CSharpTypeDeclaration;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import consulo.dotnet.debugger.DotNetDebugContext;
import consulo.dotnet.debugger.proxy.DotNetInvalidObjectException;
import consulo.dotnet.debugger.proxy.DotNetInvalidStackFrameException;
import consulo.dotnet.debugger.proxy.DotNetThrowValueException;
import consulo.dotnet.debugger.proxy.DotNetTypeProxy;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public abstract class Evaluator
{
	public abstract void evaluate(@NotNull CSharpEvaluateContext context) throws DotNetInvalidObjectException, DotNetThrowValueException, DotNetInvalidStackFrameException;

	@Nullable
	public static DotNetTypeProxy findTypeMirror(@NotNull CSharpEvaluateContext context, @Nullable PsiElement element)
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
