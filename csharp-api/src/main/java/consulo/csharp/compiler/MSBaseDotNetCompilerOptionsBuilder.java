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

package consulo.csharp.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.openapi.compiler.CompilerMessageCategory;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.StandardFileSystems;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import consulo.application.ApplicationProperties;
import consulo.csharp.module.extension.CSharpModuleExtension;
import consulo.dotnet.DotNetTarget;
import consulo.dotnet.compiler.DotNetCompileFailedException;
import consulo.dotnet.compiler.DotNetCompilerMessage;
import consulo.dotnet.compiler.DotNetCompilerOptionsBuilder;
import consulo.dotnet.compiler.DotNetCompilerUtil;
import consulo.dotnet.compiler.DotNetMacroUtil;
import consulo.dotnet.module.extension.DotNetModuleExtension;

/**
 * @author VISTALL
 * @since 26.11.13.
 */
public class MSBaseDotNetCompilerOptionsBuilder implements DotNetCompilerOptionsBuilder
{
	private static final Logger LOGGER = Logger.getInstance(MSBaseDotNetCompilerOptionsBuilder.class);

	// impl from monolipse
	// monolipse.core/src/monolipse/core/runtime/CSharpCompilerLauncher.java
	// added support for column parsing by VISTALL
	private static Pattern LINE_ERROR_PATTERN = Pattern.compile("(.+)\\((\\d+),(\\d+)\\):\\s(error|warning) (\\w+\\d+):\\s(.+)");

	@Nullable
	private String myExecutable;

	private final List<String> myArguments = new ArrayList<>();
	private final List<String> myProgramArguments = new ArrayList<>();

	public MSBaseDotNetCompilerOptionsBuilder addArgument(@NotNull String arg)
	{
		myArguments.add(arg + "\n");
		return this;
	}

	public MSBaseDotNetCompilerOptionsBuilder addProgramArgument(@NotNull String arg)
	{
		myProgramArguments.add(arg);
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
				if(ApplicationProperties.isInSandbox())
				{
					message += "(" + matcher.group(5) + ")";
				}
				return new DotNetCompilerMessage(category, message, fileUrl, codeLine, codeColumn - 1);
			}
		}
		return null;
	}

	@Override
	@NotNull
	public GeneralCommandLine createCommandLine(@NotNull Module module,
			@NotNull VirtualFile[] results,
			@NotNull DotNetModuleExtension<?> extension) throws Exception
	{
		if(myExecutable == null)
		{
			throw new DotNetCompileFailedException("C# compiler is not found");
		}
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
		commandLine.addParameters(myProgramArguments);

		addArgument("/target:" + target);
		String outputFile = DotNetMacroUtil.expandOutputFile(extension);
		addArgument("/out:" + StringUtil.QUOTER.fun(outputFile));

		Set<File> libraryFiles = DotNetCompilerUtil.collectDependencies(module, DotNetTarget.LIBRARY, false, DotNetCompilerUtil.ACCEPT_ALL);
		if(!libraryFiles.isEmpty())
		{
			addArgument("/reference:" + StringUtil.join(libraryFiles, file -> StringUtil.QUOTER.fun(file.getAbsolutePath()), ","));
		}

		Set<File> moduleFiles = DotNetCompilerUtil.collectDependencies(module, DotNetTarget.NET_MODULE, false, DotNetCompilerUtil.ACCEPT_ALL);
		if(!moduleFiles.isEmpty())
		{
			addArgument("/addmodule:" + StringUtil.join(moduleFiles, file -> StringUtil.QUOTER.fun(file.getAbsolutePath()), ","));
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

		switch(csharpExtension.getPlatform())
		{
			case ANY_CPU:
				addArgument("/platform:anycpu");
				break;
			case ANY_CPU_32BIT_PREFERRED:
				addArgument("/platform:anycpu32bitpreferred");
				break;
			case ARM:
				addArgument("/platform:ARM");
				break;
			case X86:
				addArgument("/platform:x86");
				break;
			case X64:
				addArgument("/platform:x64");
				break;
			case ITANIUM:
				addArgument("/platform:Itanium");
				break;
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

		for(VirtualFile result : results)
		{
			addArgument(StringUtil.QUOTER.fun(FileUtil.toSystemDependentName(result.getPath())));
		}

		File tempFile = FileUtil.createTempFile("consulo-dotnet-rsp", ".rsp");
		for(String argument : myArguments)
		{
			FileUtil.appendToFile(tempFile, argument);
		}

		//LOGGER.warn("Compiler def file: " + tempFile);
		//LOGGER.warn(FileUtil.loadFile(tempFile));

		FileUtil.createParentDirs(new File(outputFile));

		commandLine.addParameter("@" + tempFile.getAbsolutePath());
		commandLine.setRedirectErrorStream(true);
		return commandLine;
	}

	@Nullable
	public String getExecutable()
	{
		return myExecutable;
	}

	public void setExecutable(@NotNull String executable)
	{
		myExecutable = executable;
	}

	public void setExecutableFromSdk(@NotNull Sdk sdk, @NotNull String executableFromSdk)
	{
		myExecutable = sdk.getHomePath() + File.separatorChar + executableFromSdk;
	}
}
