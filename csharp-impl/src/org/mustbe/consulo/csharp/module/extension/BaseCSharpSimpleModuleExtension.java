package org.mustbe.consulo.csharp.module.extension;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.mustbe.consulo.csharp.lang.CSharpFileType;
import org.mustbe.consulo.csharp.module.CSharpLanguageVersionPointer;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.psi.PsiElement;
import consulo.annotations.RequiredReadAction;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import consulo.dotnet.compiler.DotNetCompileFailedException;
import consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import consulo.dotnet.module.extension.DotNetModuleLangExtension;
import consulo.extension.impl.ModuleExtensionImpl;
import consulo.roots.ModuleRootLayer;

/**
 * @author VISTALL
 * @since 07.06.2015
 */
public class BaseCSharpSimpleModuleExtension<T extends BaseCSharpSimpleModuleExtension<T>> extends ModuleExtensionImpl<T> implements CSharpSimpleModuleExtension<T>, DotNetModuleLangExtension<T>
{
	protected final CSharpLanguageVersionPointer myLanguageVersionPointer;
	protected boolean myAllowUnsafeCode;

	public BaseCSharpSimpleModuleExtension(@NotNull String id, @NotNull ModuleRootLayer module)
	{
		super(id, module);
		myLanguageVersionPointer = new CSharpLanguageVersionPointer(getProject(), id);
	}

	@Override
	public boolean isSupportedLanguageVersion(@NotNull CSharpLanguageVersion languageVersion)
	{
		// for default we support all language versions
		return true;
	}

	@NotNull
	@Override
	@RequiredReadAction
	public PsiElement[] getEntryPointElements()
	{
		return PsiElement.EMPTY_ARRAY;
	}

	@Nullable
	@Override
	@RequiredReadAction
	public String getAssemblyTitle()
	{
		return null;
	}

	@NotNull
	@Override
	public DotNetCompilerOptionsBuilder createCompilerOptionsBuilder() throws DotNetCompileFailedException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isAllowUnsafeCode()
	{
		return myAllowUnsafeCode;
	}

	public void setLanguageVersion(@NotNull CSharpLanguageVersion languageVersion)
	{
		myLanguageVersionPointer.set(null, languageVersion);
	}

	public void setAllowUnsafeCode(boolean value)
	{
		myAllowUnsafeCode = value;
	}

	public boolean isModifiedImpl(@NotNull T mutableModuleExtension)
	{
		return myIsEnabled != mutableModuleExtension.isEnabled() ||
				myAllowUnsafeCode != mutableModuleExtension.myAllowUnsafeCode ||
				!myLanguageVersionPointer.equals(mutableModuleExtension.myLanguageVersionPointer);
	}

	public CSharpLanguageVersionPointer getLanguageVersionPointer()
	{
		return myLanguageVersionPointer;
	}

	@RequiredReadAction
	@Override
	protected void loadStateImpl(@NotNull Element element)
	{
		myAllowUnsafeCode = Boolean.valueOf(element.getAttributeValue("unsafe-code", "false"));
		myLanguageVersionPointer.fromXml(element);
	}

	@Override
	protected void getStateImpl(@NotNull Element element)
	{
		element.setAttribute("unsafe-code", Boolean.toString(myAllowUnsafeCode));
		myLanguageVersionPointer.toXml(element);
	}

	@Override
	public void commit(@NotNull T mutableModuleExtension)
	{
		super.commit(mutableModuleExtension);

		myAllowUnsafeCode = mutableModuleExtension.myAllowUnsafeCode;
		myLanguageVersionPointer.set(mutableModuleExtension.myLanguageVersionPointer);
	}

	@NotNull
	@Override
	public CSharpLanguageVersion getLanguageVersion()
	{
		return myLanguageVersionPointer.get();
	}

	@NotNull
	@Override
	public LanguageFileType getFileType()
	{
		return CSharpFileType.INSTANCE;
	}
}
