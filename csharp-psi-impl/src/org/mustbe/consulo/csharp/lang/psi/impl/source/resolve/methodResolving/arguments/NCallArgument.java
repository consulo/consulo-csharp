package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import lombok.val;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class NCallArgument
{
	private final DotNetTypeRef myTypeRef;
	@Nullable
	private final CSharpCallArgument myCallArgument;

	/**
	 * It can be DotNetTypeRef or DotNetParameter
	 */
	private final Object myParameterObject;

	private Boolean myValid;

	public NCallArgument(@NotNull DotNetTypeRef typeRef, @Nullable CSharpCallArgument callArgument, @Nullable Object parameterObject)
	{
		myTypeRef = typeRef;
		myCallArgument = callArgument;
		myParameterObject = parameterObject;
	}

	@NotNull
	public Collection<CSharpCallArgument> getCallArguments()
	{
		if(myCallArgument == null)
		{
			return Collections.emptyList();
		}
		return Collections.singletonList(myCallArgument);
	}

	@NotNull
	public DotNetTypeRef getTypeRef()
	{
		return myTypeRef;
	}

	@Nullable
	public DotNetTypeRef getParameterTypeRef()
	{
		if(myParameterObject instanceof DotNetTypeRef)
		{
			return (DotNetTypeRef) myParameterObject;
		}
		else if(myParameterObject instanceof DotNetParameter)
		{
			return ((DotNetParameter) myParameterObject).toTypeRef(true);
		}
		else if(myParameterObject instanceof CSharpSimpleParameterInfo)
		{
			return ((CSharpSimpleParameterInfo) myParameterObject).getTypeRef();
		}
		return null;
	}

	public boolean isValid()
	{
		if(myValid == null)
		{
			throw new IllegalArgumentException("This parameter valid not calculated");
		}
		return myValid == Boolean.TRUE;
	}

	public boolean calcValid(@NotNull PsiElement scope)
	{
		val parameterTypeRef = getParameterTypeRef();
		myValid = parameterTypeRef != null && CSharpTypeUtil.isInheritableWithImplicit(parameterTypeRef, getTypeRef(), scope);
		return isValid();
	}

	@Nullable
	public String getParameterName()
	{
		if(myParameterObject instanceof DotNetParameter)
		{
			return ((DotNetParameter) myParameterObject).getName();
		}
		else if(myParameterObject instanceof CSharpSimpleParameterInfo)
		{
			return ((CSharpSimpleParameterInfo) myParameterObject).getNotNullName();
		}
		return null;
	}

	@Nullable
	public PsiElement getParameterElement()
	{
		if(myParameterObject instanceof DotNetParameter)
		{
			return (PsiElement) myParameterObject;
		}
		else if(myParameterObject instanceof CSharpSimpleParameterInfo)
		{
			return ((CSharpSimpleParameterInfo) myParameterObject).getElement();
		}
		return null;
	}

	@Nullable
	public Object getParameterObject()
	{
		return myParameterObject;
	}

	@Nullable
	public CSharpCallArgument getCallArgument()
	{
		return myCallArgument;
	}
}
