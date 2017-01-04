/*
 * Copyright 2013-2017 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package consulo.csharp;

import java.io.File;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import consulo.csharp.module.extension.CSharpSimpleMutableModuleExtension;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.TestModuleDescriptor;
import com.intellij.util.Consumer;
import consulo.dotnet.module.extension.DotNetSimpleMutableModuleExtension;
import consulo.roots.types.BinariesOrderRootType;
import consulo.testFramework.util.TestPathUtil;
import consulo.vfs.util.ArchiveVfsUtil;

/**
 * @author VISTALL
 * @since 06.04.2016
 */
public class CSharpMockModuleDescriptor implements TestModuleDescriptor
{
	private String myFullDataPath;
	private String myTestName;

	public CSharpMockModuleDescriptor()
	{
		this(null, null);
	}

	public CSharpMockModuleDescriptor(@Nullable String fullDataPath, @Nullable String testName)
	{
		myFullDataPath = fullDataPath;
		myTestName = testName;
	}

	@Override
	public void configureSdk(@NotNull Consumer<Sdk> consumer)
	{
	}

	@Override
	public void configureModule(@NotNull Module module, @NotNull ModifiableRootModel modifiableRootModel, @NotNull ContentEntry contentEntry)
	{
		DotNetSimpleMutableModuleExtension<?> extension = modifiableRootModel.getExtensionWithoutCheck("mono-dotnet");
		assert extension != null;
		extension.setEnabled(true);

		LibraryTable moduleLibraryTable = modifiableRootModel.getModuleLibraryTable();


		for(String lib : new String[]{
				"mscorlib",
				"System"
		})
		{
			// use zipped library, because read is more faster
			Library library = moduleLibraryTable.createLibrary();
			Library.ModifiableModel modifiableModel = library.getModifiableModel();
			VirtualFile libZip = LocalFileSystem.getInstance().findFileByPath(TestPathUtil.getTestDataPath("/mockSdk/mono4.5/") + lib + ".zip");
			assert libZip != null;
			VirtualFile archiveRootForLocalFile = ArchiveVfsUtil.getArchiveRootForLocalFile(libZip);
			assert archiveRootForLocalFile != null;

			modifiableModel.addRoot(archiveRootForLocalFile, BinariesOrderRootType.getInstance());
			modifiableModel.commit();
		}

		// per test library
		if(myFullDataPath != null && myTestName != null)
		{
			File file = new File(myFullDataPath, myTestName + ".dll");
			if(file.exists())
			{
				VirtualFile fileByIoFile = LocalFileSystem.getInstance().findFileByIoFile(file);
				if(fileByIoFile != null)
				{
					VirtualFile archiveRootForLocalFile = ArchiveVfsUtil.getArchiveRootForLocalFile(fileByIoFile);
					if(archiveRootForLocalFile != null)
					{
						Library library = moduleLibraryTable.createLibrary();
						Library.ModifiableModel modifiableModel = library.getModifiableModel();

						modifiableModel.addRoot(archiveRootForLocalFile, BinariesOrderRootType.getInstance());
						modifiableModel.commit();
					}
				}
			}
		}

		CSharpSimpleMutableModuleExtension<?> csharpExtension = modifiableRootModel.getExtensionWithoutCheck("mono-csharp");
		assert csharpExtension != null;
		csharpExtension.setEnabled(true);
	}
}
