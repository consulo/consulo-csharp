package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.sorter;

import org.jetbrains.annotations.NotNull;
import com.intellij.psi.ResolveResult;

/**
 * @author VISTALL
 * @since 27.10.14
 */
public interface ResolveResultSorter
{
	ResolveResultSorter EMPTY = new ResolveResultSorter()
	{
		@Override
		public void sort(@NotNull ResolveResult[] resolveResults)
		{

		}
	};
	void sort(@NotNull ResolveResult[] resolveResults);
}
