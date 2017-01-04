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

package consulo.csharp.ide.lineMarkerProvider;

import java.util.Comparator;

import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiNamedElement;

/**
 * @author VISTALL
 * @since 17.07.2015
 */
public class PsiNamedElementComparator implements Comparator<PsiNamedElement>
{
	public static final PsiNamedElementComparator INSTANCE = new PsiNamedElementComparator();

	@Override
	public int compare(PsiNamedElement o1, PsiNamedElement o2)
	{
		int compare = StringUtil.compare(o1.getName(), o2.getName(), false);
		if(compare == 0)
		{
			if(o1 instanceof DotNetGenericParameterListOwner && o2 instanceof DotNetGenericParameterListOwner)
			{
				return ((DotNetGenericParameterListOwner) o1).getGenericParametersCount() - ((DotNetGenericParameterListOwner) o2)
						.getGenericParametersCount();
			}
		}
		return compare;
	}
}
