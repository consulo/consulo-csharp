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

package consulo.csharp.lang.impl.psi.source.resolve;

import consulo.annotation.access.RequiredReadAction;
import consulo.application.progress.ProgressManager;
import consulo.application.util.function.Processor;
import consulo.language.psi.PsiElement;
import consulo.language.psi.ResolveResult;
import consulo.util.collection.ContainerUtil;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;

/**
 * @author VISTALL
 * @since 24.10.14
 */
public class WeightUtil
{
	public static final int MAX_WEIGHT = Integer.MIN_VALUE;

	public static final Comparator<MethodResolveResult> ourComparator = (o1, o2) -> o2.getCalcResult().getWeight() - o1.getCalcResult().getWeight();

	@RequiredReadAction
	public static void sortAndProcess(@Nonnull List<MethodResolveResult> list, @Nonnull Processor<ResolveResult> processor, @Nonnull PsiElement place)
	{
		if(list.isEmpty())
		{
			return;
		}

		ContainerUtil.sort(list, ourComparator);

		for(MethodResolveResult methodResolveResult : list)
		{
			ProgressManager.checkCanceled();

			methodResolveResult.setAssignable(place);

			if(!processor.process(methodResolveResult))
			{
				return;
			}
		}
	}
}
