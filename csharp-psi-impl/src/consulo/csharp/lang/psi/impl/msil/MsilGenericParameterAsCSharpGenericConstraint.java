package consulo.csharp.lang.psi.impl.msil;

import consulo.csharp.lang.CSharpLanguage;
import consulo.msil.lang.psi.MsilGenericParameter;
import com.intellij.psi.impl.light.LightElement;

/**
 * @author VISTALL
 * @since 13.12.14
 */
public class MsilGenericParameterAsCSharpGenericConstraint extends LightElement
{
	public MsilGenericParameterAsCSharpGenericConstraint(MsilGenericParameter parameter)
	{
		super(parameter.getManager(), CSharpLanguage.INSTANCE);
	}

	@Override
	public String toString()
	{
		return "MsilGenericParameterAsCSharpGenericConstraint";
	}
}
