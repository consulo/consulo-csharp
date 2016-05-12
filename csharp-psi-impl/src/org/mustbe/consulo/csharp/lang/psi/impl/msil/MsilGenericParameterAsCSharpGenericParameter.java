package org.mustbe.consulo.csharp.lang.psi.impl.msil;

import org.consulo.lombok.annotations.LazyInstance;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.RequiredReadAction;
import org.mustbe.consulo.csharp.lang.psi.CSharpElementVisitor;
import org.mustbe.consulo.csharp.lang.psi.CSharpGenericConstraintUtil;
import org.mustbe.consulo.csharp.lang.psi.CSharpModifier;
import org.mustbe.consulo.dotnet.psi.DotNetAttribute;
import org.mustbe.consulo.dotnet.psi.DotNetAttributeListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetModifier;
import org.mustbe.consulo.dotnet.psi.DotNetModifierList;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;
import com.intellij.util.IncorrectOperationException;
import consulo.csharp.psi.CSharpGenericParameter;

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
	@LazyInstance
	public DotNetTypeRef[] getExtendTypeRefs()
	{
		return CSharpGenericConstraintUtil.getExtendTypes(MsilGenericParameterAsCSharpGenericParameter.this);
	}
}
