package consulo.csharp.lang.psi.impl.stub;

import org.jetbrains.annotations.NotNull;
import consulo.csharp.lang.psi.CSharpModifier;
import consulo.dotnet.psi.DotNetModifier;
import consulo.dotnet.psi.DotNetModifierList;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.stubs.StubBase;
import com.intellij.psi.stubs.StubElement;
import com.intellij.util.BitUtil;

/**
 * @author VISTALL
 * @since 17.10.14
 */
public class CSharpModifierListStub extends StubBase<DotNetModifierList>
{
	private final int myModifierMask;

	public CSharpModifierListStub(StubElement parent, IStubElementType elementType, int modifierMask)
	{
		super(parent, elementType);
		myModifierMask = modifierMask;
	}

	public int getModifierMask()
	{
		return myModifierMask;
	}

	public static int getModifierMask(@NotNull DotNetModifierList list)
	{
		int val = 0;
		for(CSharpModifier modifier : CSharpModifier.values())
		{
			if(list.hasModifierInTree(modifier))
			{
				val |= modifier.mask();
			}
		}
		return val;
	}

	public boolean hasModifier(DotNetModifier modifier)
	{
		CSharpModifier as = CSharpModifier.as(modifier);
		return BitUtil.isSet(myModifierMask, as.mask());
	}
}
