package org.mustbe.consulo.csharp.lang.psi.impl.source;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.cache.CSharpTypeRefCache;
import org.mustbe.consulo.dotnet.psi.DotNetType;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.lang.ASTNode;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubElement;

/**
 * @author VISTALL
 * @since 09.11.14
 */
public abstract class CSharpStubTypeElementImpl<S extends StubElement> extends CSharpStubElementImpl<S> implements DotNetType
{
	private static class OurResolver extends CSharpTypeRefCache.TypeRefResolver<CSharpStubTypeElementImpl<?>>
	{
		public static final OurResolver INSTANCE = new OurResolver();

		@RequiredReadAction
		@NotNull
		@Override
		public DotNetTypeRef resolveTypeRef(@NotNull CSharpStubTypeElementImpl<?> element, boolean resolveFromParent)
		{
			return element.toTypeRefImpl();
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
	public abstract DotNetTypeRef toTypeRefImpl();

	@RequiredReadAction
	@NotNull
	@Override
	public final DotNetTypeRef toTypeRef()
	{
		return CSharpTypeRefCache.getInstance(getProject()).resolveTypeRef(this, OurResolver.INSTANCE, true);
	}
}
