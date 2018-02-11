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

package consulo.csharp.ide.codeInspection.unnecessaryType;

import javax.annotation.Nonnull;

import consulo.annotations.RequiredReadAction;
import consulo.csharp.ide.codeInsight.actions.ChangeVariableToTypeRefFix;
import consulo.csharp.lang.psi.CSharpElementVisitor;
import consulo.csharp.lang.psi.CSharpLocalVariable;
import consulo.csharp.lang.psi.impl.source.CSharpCatchStatementImpl;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpLambdaTypeRef;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpNullTypeRef;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpModuleUtil;
import consulo.dotnet.psi.DotNetExpression;
import consulo.dotnet.psi.DotNetType;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.codeInspection.IntentionWrapper;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElementVisitor;
import consulo.csharp.lang.psi.impl.source.resolve.type.CSharpDynamicTypeRef;

/**
 * @author VISTALL
 * @since 18.05.14
 */
public class UnnecessaryTypeInspection extends LocalInspectionTool
{
	@Nonnull
	@Override
	public PsiElementVisitor buildVisitor(@Nonnull final ProblemsHolder holder, boolean isOnTheFly, @Nonnull LocalInspectionToolSession session)
	{
		CSharpLanguageVersion languageVersion = CSharpModuleUtil.findLanguageVersion(holder.getFile());
		if(!languageVersion.isAtLeast(CSharpLanguageVersion._3_0))
		{
			return CSharpElementVisitor.EMPTY;
		}

		return new CSharpElementVisitor()
		{
			@Override
			@RequiredReadAction
			public void visitLocalVariable(CSharpLocalVariable variable)
			{
				if(variable.isConstant() || variable.getParent() instanceof CSharpCatchStatementImpl)
				{
					return;
				}

				DotNetExpression initializer = variable.getInitializer();
				if(initializer != null)
				{
					DotNetTypeRef typeRef = initializer.toTypeRef(false);
					if(typeRef instanceof CSharpLambdaTypeRef || typeRef instanceof CSharpNullTypeRef)
					{
						return;
					}
				}
				else
				{
					return;
				}

				DotNetTypeRef typeRef = variable.toTypeRef(false);
				if(typeRef == DotNetTypeRef.AUTO_TYPE)
				{
					return;
				}
				else if(typeRef instanceof CSharpDynamicTypeRef)
				{
					return;
				}

				DotNetType type = variable.getType();
				if(type == null)
				{
					return;
				}

				holder.registerProblem(type, "Can replaced by 'var'", ProblemHighlightType.LIKE_UNUSED_SYMBOL,
						new IntentionWrapper(new ChangeVariableToTypeRefFix(variable, DotNetTypeRef.AUTO_TYPE), variable.getContainingFile()));
			}
		};
	}
}
