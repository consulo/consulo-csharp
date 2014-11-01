package org.mustbe.consulo.csharp.ide.findUsage.groupingRule;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.ide.IconDescriptorUpdaters;
import com.intellij.lang.findUsages.LanguageFindUsages;
import com.intellij.navigation.NavigationItem;
import com.intellij.navigation.NavigationItemFileStatus;
import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.TypeSafeDataProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.Usage;
import com.intellij.usages.UsageGroup;
import com.intellij.usages.UsageView;
import com.intellij.usages.impl.FileStructureGroupRuleProvider;
import com.intellij.usages.rules.PsiElementUsage;
import com.intellij.usages.rules.UsageGroupingRule;

/**
 * @author VISTALL
 * @since 01.11.14
 */
public class CSharpTypeGroupRuleProvider implements FileStructureGroupRuleProvider
{
	private static class TypeGroupRuleGroup implements UsageGroup, TypeSafeDataProvider
	{
		private final DotNetTypeDeclaration myTypeDeclaration;

		public TypeGroupRuleGroup(DotNetTypeDeclaration typeDeclaration)
		{
			myTypeDeclaration = typeDeclaration;
		}

		@Nullable
		@Override
		public Icon getIcon(boolean b)
		{
			return IconDescriptorUpdaters.getIcon(myTypeDeclaration, Iconable.ICON_FLAG_VISIBILITY | Iconable.ICON_FLAG_READ_STATUS);
		}

		@NotNull
		@Override
		public String getText(@Nullable UsageView usageView)
		{
			return LanguageFindUsages.INSTANCE.forKey(CSharpLanguage.INSTANCE).get(0).getNodeText(myTypeDeclaration, false);
		}

		@Nullable
		@Override
		public FileStatus getFileStatus()
		{
			return isValid() ? NavigationItemFileStatus.get((NavigationItem) myTypeDeclaration) : null;
		}

		@Override
		public boolean isValid()
		{
			return myTypeDeclaration.isValid();
		}

		@Override
		public void update()
		{

		}

		@Override
		public int compareTo(UsageGroup o)
		{
			return getText(null).compareToIgnoreCase(o.getText(null));
		}

		@Override
		public void navigate(boolean requestFocus)
		{
			if(myTypeDeclaration instanceof Navigatable)
			{
				((Navigatable) myTypeDeclaration).navigate(requestFocus);
			}
		}

		@Override
		public boolean canNavigate()
		{
			if(myTypeDeclaration instanceof Navigatable)
			{
				return ((Navigatable) myTypeDeclaration).canNavigate();
			}
			return false;
		}

		@Override
		public boolean canNavigateToSource()
		{
			if(myTypeDeclaration instanceof Navigatable)
			{
				return ((Navigatable) myTypeDeclaration).canNavigateToSource();
			}
			return false;
		}

		@Override
		public void calcData(final DataKey key, final DataSink sink)
		{
			if(!isValid())
			{
				return;
			}
			if(LangDataKeys.PSI_ELEMENT == key)
			{
				sink.put(LangDataKeys.PSI_ELEMENT, myTypeDeclaration);
			}
			if(UsageView.USAGE_INFO_KEY == key)
			{
				sink.put(UsageView.USAGE_INFO_KEY, new UsageInfo(myTypeDeclaration));
			}
		}
	}

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
					return new TypeGroupRuleGroup(typeDeclaration);
				}
				return null;
			}
		};
	}
}
