package org.mustbe.consulo.csharp.lang.psi.impl.source.resolve;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.psi.CSharpCallArgumentListOwner;
import org.mustbe.consulo.csharp.lang.psi.impl.source.resolve.type.wrapper.GenericUnwrapTool;
import org.mustbe.consulo.dotnet.lang.psi.impl.source.resolve.type.SimpleGenericExtractorImpl;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameter;
import org.mustbe.consulo.dotnet.psi.DotNetGenericParameterListOwner;
import org.mustbe.consulo.dotnet.psi.DotNetNamedElement;
import org.mustbe.consulo.dotnet.resolve.DotNetTypeRef;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 17.09.14
 */
public abstract class CallWeightProcessor<T extends PsiElement> implements WeightProcessor<T>, PreProcessor<T>
{
	private CSharpCallArgumentListOwner myArgumentListOwner;

	public CallWeightProcessor(PsiElement e)
	{
		PsiElement parent = e.getParent();
		if(parent instanceof CSharpCallArgumentListOwner)
		{
			myArgumentListOwner = (CSharpCallArgumentListOwner) parent;
		}
	}

	@NotNull
	@Override
	public T transform(T element)
	{
		if(!(element instanceof DotNetGenericParameterListOwner))
		{
			return element;
		}

		DotNetGenericParameter[] genericParameters =((DotNetGenericParameterListOwner) element).getGenericParameters();
		DotNetTypeRef[] typeArguments = myArgumentListOwner.getTypeArgumentListRefs();
		if(typeArguments.length > 0 && genericParameters.length == typeArguments.length)
		{
			SimpleGenericExtractorImpl simpleGenericExtractor = new SimpleGenericExtractorImpl(genericParameters, typeArguments);
			return (T) GenericUnwrapTool.extract((DotNetNamedElement) element, simpleGenericExtractor, true);
		}

		return element;
	}
}
