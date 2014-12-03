package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpArrayTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetExpression;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.util.containers.ContainerUtil;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class NParamsCallArgument extends NCallArgument
{
	@NotNull
	private final List<CSharpCallArgument> myCallArguments;

	public NParamsCallArgument(@NotNull List<CSharpCallArgument> callArguments, @Nullable DotNetParameter parameter)
	{
		super(DotNetTypeRef.ERROR_TYPE, null, parameter);
		myCallArguments = callArguments;
	}

	@NotNull
	@Override
	@LazyInstance
	public DotNetTypeRef getTypeRef()
	{
		assert !myCallArguments.isEmpty();
		List<DotNetTypeRef> typeRefs = new ArrayList<DotNetTypeRef>(myCallArguments.size());
		for(CSharpCallArgument expression : myCallArguments)
		{
			DotNetExpression argumentExpression = expression.getArgumentExpression();
			if(argumentExpression == null)
			{
				continue;
			}
			typeRefs.add(argumentExpression.toTypeRef(false));
		}

		ContainerUtil.sort(typeRefs, new Comparator<DotNetTypeRef>()
		{
			@Override
			public int compare(DotNetTypeRef o1, DotNetTypeRef o2)
			{
				int rank1 = CSharpTypeUtil.getNumberRank(o1, myCallArguments.get(0));
				int rank2 = CSharpTypeUtil.getNumberRank(o2, myCallArguments.get(0));
				if(rank1 == -1 || rank2 == -1)
				{
					return 0;
				}

				return rank2 - rank1;
			}
		});
		return new CSharpArrayTypeRef(typeRefs.get(0), 0);
	}

	@NotNull
	@Override
	public Collection<CSharpCallArgument> getCallArguments()
	{
		return myCallArguments;
	}
}
