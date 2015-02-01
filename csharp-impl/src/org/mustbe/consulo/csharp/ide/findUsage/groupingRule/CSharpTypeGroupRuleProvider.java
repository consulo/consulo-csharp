package org.mustbe.consulo.csharp.ide.findUsage.groupingRule;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageGroup;
import com.intellij.usages.impl.FileStructureGroupRuleProvider;
import com.intellij.usages.rules.PsiElementUsage;
import com.intellij.usages.rules.UsageGroupingRule;

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
			public UsageGroup groupUsage(@NotNull Usage usage)
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
