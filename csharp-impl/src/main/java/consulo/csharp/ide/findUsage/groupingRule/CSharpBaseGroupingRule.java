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
import javax.swing.Icon;

import javax.annotation.Nullable;
import com.intellij.lang.findUsages.LanguageFindUsages;
import com.intellij.navigation.NavigationItem;
import com.intellij.navigation.NavigationItemFileStatus;
import com.intellij.openapi.actionSystem.DataSink;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.TypeSafeDataProvider;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vcs.FileStatus;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.usageView.UsageInfo;
import com.intellij.usages.UsageGroup;
import com.intellij.usages.UsageView;
import consulo.csharp.lang.CSharpLanguage;
import consulo.ide.IconDescriptorUpdaters;

/**
 * @author VISTALL
 * @since 23.12.14
 */
public class CSharpBaseGroupingRule<T extends PsiElement> implements UsageGroup, TypeSafeDataProvider
{
	private final SmartPsiElementPointer<T> myPointer;

	public CSharpBaseGroupingRule(T pointer)
	{
		myPointer = SmartPointerManager.getInstance(pointer.getProject()).createSmartPsiElementPointer(pointer);
	}

	@Nullable
	@Override
	public Icon getIcon(boolean b)
	{
		T element = myPointer.getElement();
		if(element == null)
		{
			return null;
		}
		return IconDescriptorUpdaters.getIcon(element, Iconable.ICON_FLAG_VISIBILITY | Iconable.ICON_FLAG_READ_STATUS);
	}

	@Nonnull
	@Override
	public String getText(@Nullable UsageView usageView)
	{
		T element = myPointer.getElement();
		if(element == null)
		{
			return "INVALID";
		}
		return LanguageFindUsages.INSTANCE.forKey(CSharpLanguage.INSTANCE).get(0).getNodeText(element, false);
	}

	@Nullable
	@Override
	public FileStatus getFileStatus()
	{
		T element = myPointer.getElement();
		if(element == null)
		{
			return null;
		}
		return NavigationItemFileStatus.get((NavigationItem) element);
	}

	@Override
	public boolean isValid()
	{
		return myPointer.getElement() != null;
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
		T element = myPointer.getElement();
		if(element instanceof Navigatable)
		{
			((Navigatable) element).navigate(requestFocus);
		}
	}

	@Override
	public boolean canNavigate()
	{
		T element = myPointer.getElement();
		return element instanceof Navigatable && ((Navigatable) element).canNavigate();
	}

	@Override
	public boolean canNavigateToSource()
	{
		T element = myPointer.getElement();
		return element instanceof Navigatable && ((Navigatable) element).canNavigateToSource();
	}

	@Override
	public void calcData(final Key<?> key, final DataSink sink)
	{
		T element = myPointer.getElement();
		if(element == null)
		{
			return;
		}
		if(LangDataKeys.PSI_ELEMENT == key)
		{
			sink.put(LangDataKeys.PSI_ELEMENT, element);
		}
		if(UsageView.USAGE_INFO_KEY == key)
		{
			sink.put(UsageView.USAGE_INFO_KEY, new UsageInfo(element));
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj != null && obj.getClass() == getClass())
		{
			return compareTo((CSharpBaseGroupingRule) obj) == 0;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return myPointer.hashCode();
	}
}
