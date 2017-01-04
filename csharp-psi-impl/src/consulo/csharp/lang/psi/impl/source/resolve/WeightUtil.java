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

package consulo.csharp.lang.psi.impl.source.resolve;

import java.util.Comparator;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import consulo.annotations.RequiredReadAction;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveResult;
import com.intellij.util.Processor;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 24.10.14
 */
public class WeightUtil
{
	public static final int MAX_WEIGHT = Integer.MIN_VALUE;

	public static final Comparator<MethodResolveResult> ourComparator = new Comparator<MethodResolveResult>()
	{
		@Override
		public int compare(MethodResolveResult o1, MethodResolveResult o2)
		{
			return o2.getCalcResult().getWeight() - o1.getCalcResult().getWeight();
		}
	};

	@RequiredReadAction
	public static void sortAndProcess(@NotNull List<MethodResolveResult> list, @NotNull Processor<ResolveResult> processor, @NotNull PsiElement place)
	{
		if(list.isEmpty())
		{
			return;
		}

		ContainerUtil.sort(list, ourComparator);

		for(MethodResolveResult methodResolveResult : list)
		{
			methodResolveResult.setAssignable(place);

			if(!processor.process(methodResolveResult))
			{
				return;
			}
		}
	}
}
