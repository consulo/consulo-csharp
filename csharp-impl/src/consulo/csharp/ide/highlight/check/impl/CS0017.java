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

package consulo.csharp.ide.highlight.check.impl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.ide.highlight.CSharpHighlightContext;
import consulo.csharp.ide.highlight.check.CompilerCheck;
import consulo.csharp.lang.psi.CSharpMethodDeclaration;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import consulo.annotations.RequiredDispatchThread;
import consulo.annotations.RequiredReadAction;
import consulo.annotations.RequiredWriteAction;
import consulo.dotnet.DotNetRunUtil;
import consulo.dotnet.DotNetTarget;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.dotnet.module.extension.DotNetMutableModuleExtension;
import consulo.dotnet.module.extension.DotNetSimpleModuleExtension;
import consulo.dotnet.psi.DotNetTypeDeclaration;

/**
 * @author VISTALL
 * @since 17.04.2016
 */
public class CS0017 extends CompilerCheck<CSharpMethodDeclaration>
{
	public static class SetMainTypeFix extends PsiElementBaseIntentionAction
	{
		private final String myVmQName;

		@RequiredReadAction
		public SetMainTypeFix(@NotNull DotNetTypeDeclaration typeDeclaration)
		{
			myVmQName = typeDeclaration.getVmQName();
			setText("Set main to '" + myVmQName + "'");
		}

		@Override
		@RequiredWriteAction
		public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException
		{
			DotNetModuleExtension<?> extension = ModuleUtilCore.getExtension(element, DotNetModuleExtension.class);
			if(extension == null || extension.getMainType() != null)
			{
				return;
			}

			ModuleRootManager rootManager = ModuleRootManager.getInstance(extension.getModule());

			ModifiableRootModel modifiableModel = rootManager.getModifiableModel();

			final DotNetMutableModuleExtension<?> mutable = modifiableModel.getExtension(DotNetMutableModuleExtension.class);
			assert mutable != null;
			mutable.setMainType(myVmQName);

			modifiableModel.commit();
		}

		@Override
		@RequiredDispatchThread
		public boolean isAvailable(@NotNull Project project, Editor editor, @NotNull PsiElement element)
		{
			DotNetModuleExtension extension = ModuleUtilCore.getExtension(element, DotNetModuleExtension.class);
			return extension != null && extension.getMainType() == null;
		}

		@NotNull
		@Override
		public String getFamilyName()
		{
			return "C#";
		}
	}

	@RequiredReadAction
	@Nullable
	@Override
	public HighlightInfoFactory checkImpl(@NotNull CSharpLanguageVersion languageVersion, @NotNull CSharpHighlightContext highlightContext, @NotNull CSharpMethodDeclaration element)
	{
		DotNetSimpleModuleExtension<?> dotNetModuleExtension = highlightContext.getDotNetModuleExtension();
		// simple extensions - skip
		if(!(dotNetModuleExtension instanceof DotNetModuleExtension))
		{
			return null;
		}
		// is not executable - skip
		if(((DotNetModuleExtension) dotNetModuleExtension).getTarget() != DotNetTarget.EXECUTABLE)
		{
			return null;
		}
		// if main type is set - skip
		String mainType = ((DotNetModuleExtension) dotNetModuleExtension).getMainType();
		if(mainType != null)
		{
			return null;
		}

		if(DotNetRunUtil.isEntryPoint(element))
		{
			PsiElement[] entryPointElements = ((DotNetModuleExtension) dotNetModuleExtension).getEntryPointElements();
			if(entryPointElements.length > 1)
			{
				PsiElement nameIdentifier = element.getNameIdentifier();
				assert nameIdentifier != null;

				PsiElement parent = element.getParent();
				return newBuilder(nameIdentifier).addQuickFix(new SetMainTypeFix((DotNetTypeDeclaration) parent));
			}
		}
		return null;
	}
}
