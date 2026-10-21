/*
 * Copyright 2013-2026 consulo.io
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

package consulo.csharp.it.resolve;

import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * @author VISTALL
 * @since 06.04.2016
 */
@ExtendWith(HeadlessProjectExtension.class)
public class GenericInferenceTest
{
	@ParameterizedTest(name = "{0}")
	@ValueSource(strings = {"Issue450"})
	public void resolve(String testName, HeadlessProjects projects) throws Exception
	{
		CSharpResolveTesting.doTest(projects, "genericInference", testName);
	}
}
