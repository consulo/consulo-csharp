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

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.CSharpLanguageVersionHelper;
import consulo.language.file.LanguageFileType;
import consulo.language.version.LanguageVersion;
import consulo.test.junit.impl.SimpleParsingTest;
import consulo.virtualFileSystem.fileType.FileType;
import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.TestInfo;

import java.lang.reflect.Method;

/**
 * @author VISTALL
 * @since 22.05.2015
 */
public abstract class CSharpBaseParsingTest extends SimpleParsingTest {
    public CSharpBaseParsingTest(@Nonnull String dataPath) {
        super(dataPath, "cs");
    }

    @Nonnull
    @Override
    protected LanguageFileType getFileType() {
        return CSharpFileType.INSTANCE;
    }

    @Override
    protected boolean checkAllPsiRoots() {
        return false;
    }

    @RequiredReadAction
    @Nonnull
    @Override
    public LanguageVersion resolveLanguageVersion(@Nonnull TestInfo testInfo, @Nonnull FileType fileType) {
        Method method = testInfo.getTestMethod().get();
        SetLanguageVersion annotation = method.getAnnotation(SetLanguageVersion.class);
        if (annotation != null) {
            return CSharpLanguageVersionHelper.getInstance().getWrapper(annotation.version());
        }
        else {
            throw new IllegalArgumentException("Missed @SetLanguageVersion");
        }
    }
}
