package org.mustbe.consulo.csharp.lang.psi.impl.stub.index;

import consulo.lombok.annotations.Lazy;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.psi.DotNetTypeDeclaration;
import com.intellij.psi.stubs.StringStubIndexExtension;
import com.intellij.psi.stubs.StubIndexExtension;
import com.intellij.psi.stubs.StubIndexKey;

/**
 * @author VISTALL
 * @since 24.10.14
 */
public class TypeWithExtensionMethodsIndex extends StringStubIndexExtension<DotNetTypeDeclaration>
{
	@NotNull
	@Lazy
	public static TypeWithExtensionMethodsIndex getInstance()
	{
		return StubIndexExtension.EP_NAME.findExtension(TypeWithExtensionMethodsIndex.class);
	}

	@NotNull
	@Override
	public StubIndexKey<String, DotNetTypeDeclaration> getKey()
	{
		return CSharpIndexKeys.TYPE_WITH_EXTENSION_METHODS_INDEX;
	}
}
