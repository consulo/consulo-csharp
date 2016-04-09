/*
 * Copyright 2013-2016 must-be.org
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

package org.musbe.consulo.csharp;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.module.extension.CSharpSimpleMutableModuleExtension;
import org.mustbe.consulo.dotnet.module.extension.DotNetSimpleMutableModuleExtension;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.LibraryTable;
import com.intellij.openapi.roots.types.BinariesOrderRootType;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.util.ArchiveVfsUtil;
import com.intellij.testFramework.TestModuleDescriptor;
import com.intellij.util.Consumer;
import consulo.testFramework.util.TestPathUtil;

/**
 * @author VISTALL
 * @since 06.04.2016
 */
public class CSharpMockModuleDescriptor implements TestModuleDescriptor
{
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

		CSharpSimpleMutableModuleExtension<?> csharpExtension = modifiableRootModel.getExtensionWithoutCheck("mono-csharp");
		assert csharpExtension != null;
		csharpExtension.setEnabled(true);
	}
}
