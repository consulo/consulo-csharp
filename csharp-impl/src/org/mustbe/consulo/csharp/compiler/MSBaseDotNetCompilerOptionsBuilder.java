/*
 * Copyright 2013-2014 must-be.org
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

package org.mustbe.consulo.csharp.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerMessage;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerUtil;
import org.mustbe.consulo.dotnet.compiler.DotNetMacros;
import org.mustbe.consulo.dotnet.module.MainConfigurationLayer;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleLangExtension;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.Function;
import lombok.val;

/**
 * @author VISTALL
 * @since 26.11.13.
 */
@Logger
public class MSBaseDotNetCompilerOptionsBuilder implements DotNetCompilerOptionsBuilder
{
	private String myExecutable;
	private Sdk mySdk;

	private final List<String> myArguments = new ArrayList<String>();

	public MSBaseDotNetCompilerOptionsBuilder(DotNetModuleLangExtension<?> langExtension)
	{
		DotNetModuleExtension extension = ModuleUtilCore.getExtension(langExtension.getModule(), DotNetModuleExtension.class);
		assert extension != null;
		mySdk = extension.getSdk();
	}

	public MSBaseDotNetCompilerOptionsBuilder(Sdk sdk)
	{
		mySdk = sdk;
	}

	public MSBaseDotNetCompilerOptionsBuilder addArgument(@NotNull String arg)
	{
		myArguments.add(arg + "\n");
		return this;
	}

	// src\Test.cs(7,42): error CS1002: ожидалась ;  [microsoft]
	// C:\Users\VISTALL\\ConsuloProjects\\untitled30\mono-test\\Program.cs(7,17): error CS0117: error description [mono]
	@Override
	public DotNetCompilerMessage convertToMessage(Module module, String line)
	{
		String[] split = line.split(": ");
		if(split.length == 3)
		{
			String fileAndPosition = split[0].trim();
			String idAndType = split[1].trim();
			String message = split[2].trim();

			String file = fileAndPosition.substring(0, fileAndPosition.lastIndexOf("("));
			String position = fileAndPosition.substring(fileAndPosition.lastIndexOf("(") + 1, fileAndPosition.length() - 1);
			String[] lineAndColumn = position.split(",");

			String[] idAndTypeArray = idAndType.split(" ");
			CompilerMessageCategory category = CompilerMessageCategory.INFORMATION;
			if(idAndTypeArray[0].equals("error"))
			{
				category = CompilerMessageCategory.ERROR;
			}
			else if(idAndTypeArray[0].equals("warning"))
			{
				category = CompilerMessageCategory.WARNING;
			}

			String fileUrl = FileUtil.toSystemIndependentName(file);
			if(!FileUtil.isAbsolute(fileUrl))
			{
				fileUrl = module.getModuleDirUrl() + "/" + fileUrl;
			}
			else
			{
				fileUrl = VirtualFileManager.constructUrl(StandardFileSystems.FILE_PROTOCOL, fileUrl);
			}


			int lineN = Integer.parseInt(lineAndColumn[0]);
			int columnN = Integer.parseInt(lineAndColumn[1]);

			return new DotNetCompilerMessage(category, message + " (" + idAndTypeArray[1] + ")", fileUrl, lineN, columnN);
		}
		else if(split.length == 2)
		{
			String idAndType = split[0].trim();
			String message = split[1].trim();

			//C:\Users\VISTALL\ConsuloProjects\\untitled30\\mono-test\\Program.cs(5,9): (Location of the symbol related to previous error)
			if(message.startsWith("(")) // only with ?
			{
				String file = idAndType.substring(0, idAndType.lastIndexOf("("));
				String position = idAndType.substring(idAndType.lastIndexOf("(") + 1, idAndType.length() - 1);
				String[] lineAndColumn = position.split(",");

				String fileUrl = FileUtil.toSystemIndependentName(file);
				if(!FileUtil.isAbsolute(fileUrl))
				{
					fileUrl = module.getModuleDirUrl() + "/" + fileUrl;
				}
				else
				{
					fileUrl = VirtualFileManager.constructUrl(StandardFileSystems.FILE_PROTOCOL, fileUrl);
				}

				int lineN = Integer.parseInt(lineAndColumn[0]);
				int columnN = Integer.parseInt(lineAndColumn[0]);

				message = message.substring(1, message.length());
				message = message.substring(0, message.length() - 1);

				return new DotNetCompilerMessage(CompilerMessageCategory.INFORMATION, message, fileUrl, lineN, columnN);
			}
			else
			{
				String[] idAndTypeArray = idAndType.split(" ");
				CompilerMessageCategory category = CompilerMessageCategory.INFORMATION;
				if(idAndTypeArray[0].equals("error"))
				{
					category = CompilerMessageCategory.ERROR;
				}
				else if(idAndTypeArray[0].equals("warning"))
				{
					category = CompilerMessageCategory.WARNING;
				}

				return new DotNetCompilerMessage(category, message + " (" + idAndTypeArray[1] + ")", null, -1, -1);
			}
		}
		else
		{
			return new DotNetCompilerMessage(CompilerMessageCategory.INFORMATION, line, null, -1, -1);
		}
	}

	@Override
	@NotNull
	public GeneralCommandLine createCommandLine(@NotNull Module module, @NotNull VirtualFile[] results, @NotNull String layerName,
			@NotNull MainConfigurationLayer dotNetLayer) throws IOException
	{
		DotNetModuleExtension<?> extension = ModuleUtilCore.getExtension(module, DotNetModuleExtension.class);

		assert extension != null;

		String target = null;
		switch(dotNetLayer.getTarget())
		{
			case EXECUTABLE:
				target = "exe";
				break;
			case LIBRARY:
				target = "library";
				break;
		}


		GeneralCommandLine commandLine = new GeneralCommandLine();
		commandLine.setExePath(myExecutable);
		commandLine.setWorkDirectory(module.getModuleDirPath());

		addArgument("/target:" + target);
		String outputFile = DotNetMacros.extract(module, layerName, dotNetLayer);
		addArgument("/out:" + outputFile);

		val dependFiles = DotNetCompilerUtil.collectDependencies(module, true);
		if(!dependFiles.isEmpty())
		{
			addArgument("/reference:" + StringUtil.join(dependFiles, new Function<File, String>()
			{
				@Override
				public String fun(File file)
				{
					return StringUtil.QUOTER.fun(file.getAbsolutePath());
				}
			}, ","));
		}

		if(dotNetLayer.isAllowDebugInfo())
		{
			addArgument("/debug");
		}

		String defineVariables = StringUtil.join(dotNetLayer.getVariables(), ";");
		if(!StringUtil.isEmpty(defineVariables))
		{
			addArgument("/define:" + defineVariables);
		}

		String mainType = dotNetLayer.getMainType();
		if(!StringUtil.isEmpty(mainType))
		{
			addArgument("/main:" + mainType);
		}

		File tempFile = FileUtil.createTempFile("consulo-dotnet-rsp", ".rsp");
		for(String argument : myArguments)
		{
			FileUtil.appendToFile(tempFile, argument);
		}

		for(VirtualFile result : results)
		{
			FileUtil.appendToFile(tempFile, FileUtil.toSystemDependentName(result.getPath()) + "\n");
		}

		//LOGGER.warn("Compiler def file: " + tempFile);
		//LOGGER.warn(FileUtil.loadFile(tempFile));

		FileUtil.createParentDirs(new File(outputFile));

		commandLine.addParameter("@" + tempFile.getAbsolutePath());
		commandLine.setRedirectErrorStream(true);
		return commandLine;
	}

	public void setExecutable(String executable)
	{
		myExecutable = executable;
	}

	public void setExecutableFromSdk(String executableFromSdk)
	{
		myExecutable = mySdk.getHomePath() + File.separatorChar + executableFromSdk;
	}
}
