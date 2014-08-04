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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.consulo.lombok.annotations.Logger;
import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.module.extension.CSharpModuleExtension;
import org.mustbe.consulo.dotnet.DotNetTarget;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerMessage;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import org.mustbe.consulo.dotnet.compiler.DotNetCompilerUtil;
import org.mustbe.consulo.dotnet.compiler.DotNetMacros;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleExtension;
import org.mustbe.consulo.dotnet.module.extension.DotNetModuleLangExtension;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.application.ApplicationManager;
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
	// impl from monolipse
	// monolipse.core/src/monolipse/core/runtime/CSharpCompilerLauncher.java
	// added support for column parsing by VISTALL
	private static Pattern LINE_ERROR_PATTERN = Pattern.compile("(.+)\\((\\d+),(\\d+)\\):\\s(error|warning) (\\w+\\d+):\\s(.+)");

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

	@Override
	public DotNetCompilerMessage convertToMessage(Module module, String line)
	{
		if(line.startsWith("error"))
		{
			return new DotNetCompilerMessage(CompilerMessageCategory.ERROR, line, null, -1, 1);
		}
		else
		{
			Matcher matcher = LINE_ERROR_PATTERN.matcher(line);
			if(matcher.matches())
			{
				CompilerMessageCategory category = CompilerMessageCategory.INFORMATION;
				if(matcher.group(4).equals("error"))
				{
					category = CompilerMessageCategory.ERROR;
				}
				else if(matcher.group(4).equals("warning"))
				{
					category = CompilerMessageCategory.WARNING;
				}

				String fileUrl = FileUtil.toSystemIndependentName(matcher.group(1));
				if(!FileUtil.isAbsolute(fileUrl))
				{
					fileUrl = module.getModuleDirUrl() + "/" + fileUrl;
				}
				else
				{
					fileUrl = VirtualFileManager.constructUrl(StandardFileSystems.FILE_PROTOCOL, fileUrl);
				}

				int codeLine = Integer.parseInt(matcher.group(2));
				int codeColumn = Integer.parseInt(matcher.group(3));
				String message = matcher.group(6);
				if(ApplicationManager.getApplication().isInternal())
				{
					message += "(" + matcher.group(5) + ")";
				}
				return new DotNetCompilerMessage(category, message, fileUrl, codeLine, codeColumn);
			}
		}
		return null;
	}

	@Override
	@NotNull
	public GeneralCommandLine createCommandLine(@NotNull Module module, @NotNull VirtualFile[] results, @NotNull DotNetModuleExtension<?> extension)
			throws IOException
	{
		CSharpModuleExtension csharpExtension = ModuleUtilCore.getExtension(module, CSharpModuleExtension.class);

		assert csharpExtension != null;

		String target = null;
		switch(extension.getTarget())
		{
			case EXECUTABLE:
				target = "exe";
				break;
			case WIN_EXECUTABLE:
				target = "winexe";
				break;
			case LIBRARY:
				target = "library";
				break;
			case NET_MODULE:
				target = "module";
				break;
		}

		GeneralCommandLine commandLine = new GeneralCommandLine();
		commandLine.setExePath(myExecutable);
		commandLine.setWorkDirectory(module.getModuleDirPath());

		addArgument("/target:" + target);
		String outputFile = DotNetMacros.extract(module, extension);
		addArgument("/out:" + outputFile);

		val libraryFiles = DotNetCompilerUtil.collectDependencies(module, DotNetTarget.LIBRARY, true);
		if(!libraryFiles.isEmpty())
		{
			addArgument("/reference:" + StringUtil.join(libraryFiles, new Function<File, String>()
			{
				@Override
				public String fun(File file)
				{
					return StringUtil.QUOTER.fun(file.getAbsolutePath());
				}
			}, ","));
		}

		val moduleFiles = DotNetCompilerUtil.collectDependencies(module, DotNetTarget.NET_MODULE, true);
		if(!moduleFiles.isEmpty())
		{
			addArgument("/addmodule:" + StringUtil.join(moduleFiles, new Function<File, String>()
			{
				@Override
				public String fun(File file)
				{
					return StringUtil.QUOTER.fun(file.getAbsolutePath());
				}
			}, ","));
		}

		if(extension.isAllowDebugInfo())
		{
			addArgument("/debug");
		}

		if(csharpExtension.isAllowUnsafeCode())
		{
			addArgument("/unsafe");
		}

		if(csharpExtension.isOptimizeCode())
		{
			addArgument("/optimize+");
		}
		addArgument("/nologo");
		addArgument("/nostdlib+");

		String defineVariables = StringUtil.join(extension.getVariables(), ";");
		if(!StringUtil.isEmpty(defineVariables))
		{
			addArgument("/define:" + defineVariables);
		}

		String mainType = extension.getMainType();
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
