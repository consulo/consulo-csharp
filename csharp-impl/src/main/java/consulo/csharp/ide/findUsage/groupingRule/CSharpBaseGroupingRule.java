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

import consulo.component.util.Iconable;
import consulo.csharp.lang.CSharpLanguage;
import consulo.dataContext.DataSink;
import consulo.dataContext.TypeSafeDataProvider;
import consulo.language.editor.LangDataKeys;
import consulo.language.editor.util.NavigationItemFileStatus;
import consulo.language.findUsage.FindUsagesProvider;
import consulo.language.icon.IconDescriptorUpdaters;
import consulo.language.psi.PsiElement;
import consulo.language.psi.SmartPointerManager;
import consulo.language.psi.SmartPsiElementPointer;
import consulo.navigation.Navigatable;
import consulo.navigation.NavigationItem;
import consulo.ui.image.Image;
import consulo.usage.UsageGroup;
import consulo.usage.UsageInfo;
import consulo.usage.UsageView;
import consulo.util.dataholder.Key;
import consulo.virtualFileSystem.status.FileStatus;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
	public Image getIcon()
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
		return FindUsagesProvider.forLanguage(CSharpLanguage.INSTANCE).getNodeText(element, false);
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
