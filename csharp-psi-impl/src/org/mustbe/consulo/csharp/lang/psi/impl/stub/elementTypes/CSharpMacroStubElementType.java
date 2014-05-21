package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpMacroLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpMacroFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpMacroFileStub;
import com.intellij.psi.PsiFile;
import com.intellij.psi.StubBuilder;
import com.intellij.psi.stubs.DefaultStubBuilder;
import com.intellij.psi.stubs.StubElement;
import com.intellij.psi.stubs.StubInputStream;
import com.intellij.psi.tree.IStubFileElementType;

/**
 * @author VISTALL
 * @since 23.01.14
 */
public class CSharpMacroStubElementType  extends IStubFileElementType<CSharpMacroFileStub>
{
	public CSharpMacroStubElementType()
	{
		super("CSHARP_MACRO_FILE", CSharpMacroLanguage.INSTANCE);
	}

	@Override
	public StubBuilder getBuilder()
	{
		return new DefaultStubBuilder()
		{
			@Override
			protected StubElement createStubForFile(@NotNull PsiFile file)
			{
				if(file instanceof CSharpMacroFileImpl)
				{
					return new CSharpMacroFileStub((CSharpMacroFileImpl) file);
				}
				return super.createStubForFile(file);
			}
		};
	}

	@NotNull
	@Override
	public CSharpMacroFileStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		return new CSharpMacroFileStub(null);
	}

	@Override
	public int getStubVersion()
	{
		return 1;
	}

	@NotNull
	@Override
	public String getExternalId()
	{
		return "csharp.macro.file";
	}
}
