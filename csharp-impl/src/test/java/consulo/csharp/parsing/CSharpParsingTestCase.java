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

package consulo.csharp.parsing;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NonNls;
import com.intellij.lang.LanguageExtensionPoint;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.lang.ParserDefinition;
import com.intellij.mock.MockPsiDocumentManager;
import com.intellij.openapi.extensions.impl.ExtensionsAreaImpl;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.psi.PsiDocumentManager;
import consulo.csharp.lang.CSharpCfsElementTypeFactory;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.CSharpLanguageVersionHelper;
import consulo.csharp.lang.CSharpLanguageVersionHelperImpl;
import consulo.csharp.lang.CSharpParserDefinition;
import consulo.csharp.lang.doc.CSharpDocParserDefinition;
import consulo.csharp.lang.doc.psi.CSharpDocElementFactory;
import consulo.csharp.lang.doc.psi.impl.CSharpCfsElementTypeFactoryImpl;
import consulo.csharp.lang.doc.psi.impl.CSharpDocElementFactoryImpl;
import consulo.csharp.lang.doc.validation.CSharpDocTagManager;
import consulo.injecting.InjectingContainerBuilder;
import consulo.lang.LanguageVersion;
import consulo.psi.tree.PsiElementFactory;
import consulo.psi.tree.impl.DefaultPsiElementFactory;
import consulo.test.light.LightApplicationBuilder;

/**
 * @author VISTALL
 * @since 22.05.2015
 */
public abstract class CSharpParsingTestCase extends ParsingTestCase
{
	public CSharpParsingTestCase(@NonNls @Nonnull String dataPath)
	{
		super(dataPath, "cs");
	}

	@Override
	protected FileType getFileType(String fileName)
	{
		return CSharpFileType.INSTANCE;
	}

	@Override
	protected LightApplicationBuilder.DefaultRegistrator createAppRegistrator()
	{
		return new LightApplicationBuilder.DefaultRegistrator()
		{
			@Override
			public void registerServices(@Nonnull InjectingContainerBuilder builder)
			{
				super.registerServices(builder);
				builder.bind(CSharpLanguageVersionHelper.class).to(CSharpLanguageVersionHelperImpl.class);
				builder.bind(CSharpDocElementFactory.class).to(CSharpDocElementFactoryImpl.class);
				builder.bind(CSharpCfsElementTypeFactory.class).to(CSharpCfsElementTypeFactoryImpl.class);
				builder.bind(PsiDocumentManager.class).to(MockPsiDocumentManager.class);
				builder.bind(CSharpDocTagManager.class).to(CSharpDocTagManager.class);
			}

			@Override
			public void registerExtensionPointsAndExtensions(@Nonnull ExtensionsAreaImpl area)
			{
				super.registerExtensionPointsAndExtensions(area);

				LanguageExtensionPoint<ParserDefinition> value = new LanguageExtensionPoint<>();
				value.language = "C#";
				value.implementationClass = CSharpParserDefinition.class.getName();
				registerExtension(area, LanguageParserDefinitions.INSTANCE.getExtensionPointName(), value);

				value = new LanguageExtensionPoint<>();
				value.language = "C#Doc";
				value.implementationClass = CSharpDocParserDefinition.class.getName();
				registerExtension(area, LanguageParserDefinitions.INSTANCE.getExtensionPointName(), value);

				registerExtensionPoint(area, PsiElementFactory.EP.getExtensionPointName(), PsiElementFactory.class);
				registerExtension(area, PsiElementFactory.EP.getExtensionPointName(), new DefaultPsiElementFactory());
			}
		};
	}

	@Override
	protected boolean checkAllPsiRoots()
	{
		return false;
	}

	@Nonnull
	@Override
	public LanguageVersion resolveLanguageVersion(@Nonnull FileType fileType)
	{
		String name = getName();
		try
		{
			Method declaredMethod = getClass().getDeclaredMethod(name);
			SetLanguageVersion annotation = declaredMethod.getAnnotation(SetLanguageVersion.class);
			if(annotation != null)
			{
				return CSharpLanguageVersionHelper.getInstance().getWrapper(annotation.version());
			}
			else
			{
				throw new IllegalArgumentException("Missed @SetLanguageVersion");
			}
		}
		catch(NoSuchMethodException e)
		{
			throw new Error(e);
		}
	}

	@Override
	protected boolean shouldContainTempFiles()
	{
		return false;
	}
}
