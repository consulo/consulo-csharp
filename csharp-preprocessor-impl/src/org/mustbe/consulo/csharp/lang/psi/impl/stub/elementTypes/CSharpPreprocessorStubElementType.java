package org.mustbe.consulo.csharp.lang.psi.impl.stub.elementTypes;

import java.io.IOException;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.lang.CSharpPreprocessorLanguage;
import org.mustbe.consulo.csharp.lang.psi.impl.source.CSharpPreprocessorFileImpl;
import org.mustbe.consulo.csharp.lang.psi.impl.stub.CSharpPreprocessorFileStub;
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
public class CSharpPreprocessorStubElementType extends IStubFileElementType<CSharpPreprocessorFileStub>
{
	public CSharpPreprocessorStubElementType()
	{
		super("CSHARP_PREPROCESSOR_FILE", CSharpPreprocessorLanguage.INSTANCE);
	}

	@Override
	public StubBuilder getBuilder()
	{
		return new DefaultStubBuilder()
		{
			@NotNull
			@Override
			protected StubElement createStubForFile(@NotNull PsiFile file)
			{
				if(file instanceof CSharpPreprocessorFileImpl)
				{
					return new CSharpPreprocessorFileStub((CSharpPreprocessorFileImpl) file);
				}
				return super.createStubForFile(file);
			}
		};
	}

	@NotNull
	@Override
	public CSharpPreprocessorFileStub deserialize(@NotNull StubInputStream dataStream, StubElement parentStub) throws IOException
	{
		return new CSharpPreprocessorFileStub(null);
	}

	@Override
	public int getStubVersion()
	{
		return 2;
	}

	@NotNull
	@Override
	public String getExternalId()
	{
		return "preprocessor.macro.file";
	}
}
