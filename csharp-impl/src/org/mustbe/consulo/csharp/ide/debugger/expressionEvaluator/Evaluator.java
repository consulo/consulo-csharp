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
import org.mustbe.consulo.dotnet.debugger.DotNetDebugContext;
import org.mustbe.consulo.dotnet.debugger.TypeMirrorUnloadedException;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiUtilCore;
import mono.debugger.TypeMirror;

/**
 * @author VISTALL
 * @since 05.08.2015
 */
public abstract class Evaluator
{
	public abstract void evaluate(@NotNull CSharpEvaluateContext context);

	@Nullable
	public TypeMirror findTypeMirror(@NotNull CSharpEvaluateContext context, @Nullable PsiElement element)
	{
		if(element instanceof CSharpTypeDeclaration)
		{
			try
			{
				DotNetDebugContext debuggerContext = context.getDebuggerContext();

				VirtualFile virtualFile = PsiUtilCore.getVirtualFile(element);
				if(virtualFile == null)
				{
					return null;
				}
				return debuggerContext.getVirtualMachine().findTypeMirror(element.getProject(), virtualFile, ((CSharpTypeDeclaration) element).getVmQName());
			}
			catch(TypeMirrorUnloadedException e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}
}
