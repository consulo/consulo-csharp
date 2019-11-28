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

package consulo.csharp.ide.lineMarkerProvider;

import javax.annotation.Nonnull;
import consulo.annotation.access.RequiredReadAction;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.psi.PsiElement;
import com.intellij.util.Consumer;

/**
 * @author VISTALL
 * @since 25.03.14
 */
public interface LineMarkerCollector
{
	@RequiredReadAction
	void collect(PsiElement psiElement, @Nonnull Consumer<LineMarkerInfo> lineMarkerInfos);
}
