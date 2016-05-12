package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.dotnet.lang.psi.impl.DotNetTypeRefCacheUtil;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.NotNullFunction;

/**
 * @author VISTALL
 * @since 09.11.14
 */
public abstract class CSharpStubTypeElementImpl<S extends StubElement> extends CSharpStubElementImpl<S> implements DotNetType
{
	private static class OurResolver implements NotNullFunction<CSharpStubTypeElementImpl<?>, DotNetTypeRef>
	{
		public static final OurResolver INSTANCE = new OurResolver();

		@NotNull
		@Override
		@RequiredReadAction
		public DotNetTypeRef fun(CSharpStubTypeElementImpl<?> typeElement)
		{
			return typeElement.toTypeRefImpl();
		}
	}

	public CSharpStubTypeElementImpl(@NotNull ASTNode node)
	{
		super(node);
	}

	public CSharpStubTypeElementImpl(@NotNull S stub, @NotNull IStubElementType<? extends S, ?> nodeType)
	{
		super(stub, nodeType);
	}

	@NotNull
	@RequiredReadAction
	protected abstract DotNetTypeRef toTypeRefImpl();

	@RequiredReadAction
	@NotNull
	@Override
	public final DotNetTypeRef toTypeRef()
	{
		return DotNetTypeRefCacheUtil.cacheTypeRef(this, OurResolver.INSTANCE);
	}
}
