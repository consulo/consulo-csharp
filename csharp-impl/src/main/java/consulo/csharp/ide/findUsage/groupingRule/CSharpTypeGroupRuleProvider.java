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

package consulo.csharp.ide.findUsage.groupingRule;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import consulo.dotnet.psi.DotNetTypeDeclaration;
import consulo.language.psi.PsiElement;
import consulo.language.psi.util.PsiTreeUtil;
import consulo.usage.Usage;
import consulo.usage.UsageGroup;
import consulo.usage.rule.FileStructureGroupRuleProvider;
import consulo.usage.rule.PsiElementUsage;
import consulo.usage.rule.UsageGroupingRule;
import consulo.project.Project;

/**
 * @author VISTALL
 * @since 01.11.14
 */
public class CSharpTypeGroupRuleProvider implements FileStructureGroupRuleProvider
{
	@Nullable
	@Override
	public UsageGroupingRule getUsageGroupingRule(Project project)
	{
		return new UsageGroupingRule()
		{
			@Nullable
			@Override
			public UsageGroup groupUsage(@Nonnull Usage usage)
			{
				if(!(usage instanceof PsiElementUsage))
				{
					return null;
				}
				PsiElement element = ((PsiElementUsage) usage).getElement();

				DotNetTypeDeclaration typeDeclaration = PsiTreeUtil.getParentOfType(element, DotNetTypeDeclaration.class);
				if(typeDeclaration != null)
				{
					return new CSharpBaseGroupingRule<DotNetTypeDeclaration>(typeDeclaration);
				}
				return null;
			}
		};
	}
}
