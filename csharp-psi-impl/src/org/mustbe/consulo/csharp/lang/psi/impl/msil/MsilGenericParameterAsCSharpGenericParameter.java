package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.annotations.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetAttribute;
import consulo.dotnet.psi.DotNetAttributeListOwner;
import consulo.dotnet.psi.DotNetGenericParameter;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import consulo.csharp.lang.psi.CSharpGenericParameter;
import consulo.lombok.annotations.Lazy;

/**
 * @author VISTALL
 * @since 21.11.14
 */
public class MsilGenericParameterAsCSharpGenericParameter extends MsilElementWrapper<DotNetGenericParameter> implements CSharpGenericParameter, DotNetAttributeListOwner
{
	public MsilGenericParameterAsCSharpGenericParameter(@NotNull PsiElement parent, DotNetGenericParameter msilElement)
	{
		super(parent, msilElement);
	}

	@RequiredReadAction
	@Override
	public boolean hasModifier(@NotNull DotNetModifier modifier)
	{
		modifier = CSharpModifier.as(modifier);
		if(modifier == CSharpModifier.IN)
		{
			return myOriginal.hasModifier(DotNetModifier.CONTRAVARIANT);
		}
		else if(modifier == CSharpModifier.OUT)
		{
			return myOriginal.hasModifier(DotNetModifier.COVARIANT);
		}
		return false;
	}

	@RequiredReadAction
	@Nullable
	@Override
	public DotNetModifierList getModifierList()
	{
		return null;
	}

	@Override
	public boolean isEquivalentTo(PsiElement another)
	{
		if(another instanceof MsilGenericParameterAsCSharpGenericParameter)
		{
			return myOriginal.isEquivalentTo(((MsilGenericParameterAsCSharpGenericParameter) another).myOriginal);
		}
		return super.isEquivalentTo(another);
	}

	@Override
	public String getName()
	{
		return myOriginal.getName();
	}

	@Override
	public void accept(@NotNull CSharpElementVisitor visitor)
	{
		visitor.visitGenericParameter(this);
	}

	@Override
	public String toString()
	{
		return "MsilGenericParameterAsCSharpGenericParameter: " + myOriginal.getName();
	}

	@Nullable
	@Override
	public PsiElement getNameIdentifier()
	{
		return null;
	}

	@Override
	public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException
	{
		return null;
	}

	@Override
	public int getIndex()
	{
		return myOriginal.getIndex();
	}

	@RequiredReadAction
	@NotNull
	@Override
	public DotNetAttribute[] getAttributes()
	{
		return myOriginal.getAttributes();
	}

	@RequiredReadAction
	@NotNull
	@Override
	@Lazy
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return CSharpGenericConstraintUtil.getExtendTypes(MsilGenericParameterAsCSharpGenericParameter.this);
	}
}
