package org.mustbe.consulo.csharp.ide.findUsage.groupingRule;

import javax.swing.Icon;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpLanguage;
import org.mustbe.consulo.dotnet.psi.DotNetCodeBlockOwner;
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
public class CSharpCodeBlockOwnerGroupRuleProvider implements FileStructureGroupRuleProvider
{
	private static class CodeBlockOwnerGroupRuleGroup implements UsageGroup, TypeSafeDataProvider
	{
		private final DotNetCodeBlockOwner myCodeBlockOwner;

		public CodeBlockOwnerGroupRuleGroup(DotNetCodeBlockOwner codeBlockOwner)
		{
			myCodeBlockOwner = codeBlockOwner;
		}

		@Nullable
		@Override
		public Icon getIcon(boolean b)
		{
			return IconDescriptorUpdaters.getIcon(myCodeBlockOwner, Iconable.ICON_FLAG_VISIBILITY | Iconable.ICON_FLAG_READ_STATUS);
		}

		@NotNull
		@Override
		public String getText(@Nullable UsageView usageView)
		{
			return LanguageFindUsages.INSTANCE.forKey(CSharpLanguage.INSTANCE).get(0).getNodeText(myCodeBlockOwner, false);
		}

		@Nullable
		@Override
		public FileStatus getFileStatus()
		{
			return isValid() ? NavigationItemFileStatus.get((NavigationItem) myCodeBlockOwner) : null;
		}

		@Override
		public boolean isValid()
		{
			return myCodeBlockOwner.isValid();
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
			if(myCodeBlockOwner instanceof Navigatable)
			{
				((Navigatable) myCodeBlockOwner).navigate(requestFocus);
			}
		}

		@Override
		public boolean canNavigate()
		{
			if(myCodeBlockOwner instanceof Navigatable)
			{
				return ((Navigatable) myCodeBlockOwner).canNavigate();
			}
			return false;
		}

		@Override
		public boolean canNavigateToSource()
		{
			if(myCodeBlockOwner instanceof Navigatable)
			{
				return ((Navigatable) myCodeBlockOwner).canNavigateToSource();
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
				sink.put(LangDataKeys.PSI_ELEMENT, myCodeBlockOwner);
			}
			if(UsageView.USAGE_INFO_KEY == key)
			{
				sink.put(UsageView.USAGE_INFO_KEY, new UsageInfo(myCodeBlockOwner));
			}
		}

		@Override
		public boolean equals(Object obj)
		{
			if(obj instanceof CodeBlockOwnerGroupRuleGroup)
			{
				return compareTo((CodeBlockOwnerGroupRuleGroup) obj) == 0;
			}
			return super.equals(obj);
		}

		@Override
		public int hashCode()
		{
			return myCodeBlockOwner.hashCode();
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

				DotNetCodeBlockOwner codeBlockOwner = PsiTreeUtil.getParentOfType(element, DotNetCodeBlockOwner.class);
				if(codeBlockOwner != null)
				{
					return new CodeBlockOwnerGroupRuleGroup(codeBlockOwner);
				}
				return null;
			}
		};
	}
}
