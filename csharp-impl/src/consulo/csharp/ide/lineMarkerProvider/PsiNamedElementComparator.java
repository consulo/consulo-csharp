package consulo.csharp.ide.lineMarkerProvider;

import java.util.Comparator;

import consulo.dotnet.psi.DotNetGenericParameterListOwner;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiNamedElement;

/**
 * @author VISTALL
 * @since 17.07.2015
 */
public class PsiNamedElementComparator implements Comparator<PsiNamedElement>
{
	public static final PsiNamedElementComparator INSTANCE = new PsiNamedElementComparator();

	@Override
	public int compare(PsiNamedElement o1, PsiNamedElement o2)
	{
		int compare = StringUtil.compare(o1.getName(), o2.getName(), false);
		if(compare == 0)
		{
			if(o1 instanceof DotNetGenericParameterListOwner && o2 instanceof DotNetGenericParameterListOwner)
			{
				return ((DotNetGenericParameterListOwner) o1).getGenericParametersCount() - ((DotNetGenericParameterListOwner) o2)
						.getGenericParametersCount();
			}
		}
		return compare;
	}
}
