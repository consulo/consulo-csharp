package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.methodResolving.arguments;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgument;
import org.mustbe.consulo.csharp.lang.psi.CSharpSimpleParameterInfo;
import org.mustbe.consulo.csharp.lang.psi.impl.CSharpTypeUtil;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.operatorResolving.ImplicitCastInfo;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.CSharpStaticTypeRef;
import org.mustbe.consulo.dotnet.psi.DotNetParameter;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 02.11.14
 */
public class NCallArgument extends UserDataHolderBase
{
	public static final int NOT_CALCULATED = -1;
	public static final int FAIL = 0;
	public static final int EQUAL = 1;
	public static final int INSTANCE_OF = 2;

	private final DotNetTypeRef myTypeRef;
	@Nullable
	private final CSharpCallArgument myCallArgument;

	/**
	 * It can be DotNetTypeRef or DotNetParameter
	 */
	private final Object myParameterObject;

	private int myValid = NOT_CALCULATED;

	public NCallArgument(@NotNull DotNetTypeRef typeRef, @Nullable CSharpCallArgument callArgument, @Nullable Object parameterObject)
	{
		myTypeRef = typeRef;
		myCallArgument = callArgument;
		myParameterObject = parameterObject;

		if(callArgument != null)
		{
			ImplicitCastInfo implicitCastInfo = callArgument.getUserData(ImplicitCastInfo.IMPLICIT_CAST_INFO);
			if(implicitCastInfo != null)
			{
				// copy it
				putUserData(ImplicitCastInfo.IMPLICIT_CAST_INFO, implicitCastInfo);
			}
		}
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
		if(myValid == NOT_CALCULATED)
		{
			throw new IllegalArgumentException("This parameter valid not calculated");
		}
		return myValid != FAIL;
	}

	public int calcValid(@NotNull PsiElement scope)
	{
		DotNetTypeRef parameterTypeRef = getParameterTypeRef();
		int newVal = FAIL;
		if(parameterTypeRef != null)
		{
			if(CSharpTypeUtil.isTypeEqual(parameterTypeRef, getTypeRef(), scope))
			{
				newVal = EQUAL;
			}
			else
			{
				CSharpTypeUtil.InheritResult inheritable = CSharpTypeUtil.isInheritable(parameterTypeRef, getTypeRef(), scope,
						CSharpStaticTypeRef.IMPLICIT);
				if(inheritable.isSuccess())
				{
					if(inheritable.isConversion())
					{
						putUserData(ImplicitCastInfo.IMPLICIT_CAST_INFO, new ImplicitCastInfo(getTypeRef(), parameterTypeRef));
					}

					newVal = INSTANCE_OF;
				}
			}
		}

		myValid = newVal;
		return myValid;
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
