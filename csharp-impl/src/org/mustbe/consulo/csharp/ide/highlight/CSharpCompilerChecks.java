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

package org.mustbe.consulo.csharp.ide.highlight;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.ide.highlight.check.CompilerCheck;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public enum CSharpCompilerChecks
{
	CS0029(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // assign type check
	CS0100(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // parameter is duplicate
	CS0101(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // members duplicate in namespace declaration
	CS0102(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // members duplicate in type declaration
	CS0106(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // modifier check
	CS0107(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // modifier protection check
	CS0128(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // local variable redeclaration check
	//CS0136(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // lambda parameter redeclaration check
	CS0155(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // throw object must be child of System.Exception
	//CS0168(CSharpLanguageVersion._1_0, HighlightInfoType.UNUSED_SYMBOL), // local variable usage check
	CS0214(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // fixed can be used inside unsafe context
	//CS0219(CSharpLanguageVersion._1_0, HighlightInfoType.UNUSED_SYMBOL), // local variable usage check
	CS0227(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // 'unsafe' modifier check
	CS0231(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // 'params' modifier must be last
	CS0304(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // generic cant be new without new() constraint
	CS0401(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // new() constraint must be last
	CS0409(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // generic constraint already defined for generic
	CS0413(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // 'S' operator  cant use to generic without class constraint, or reference
	CS0449(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // struct or class constraint must be first
	CS0516(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // constructor cant call itself
	CS0518(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // dynamic checks
	CS0815(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // lambdas cant be cast to 'var'
	CS1004(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // duplicate modifier check
	CS1100(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // 'this' modifier can be only set to first parameter
	CS1105(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // 'this' modifier can be only in method with static modifier
	CS1106(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // 'this' modifier can be only in type with static modifier and no generic
	CS1620(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // ref out exp checks
	CS1644(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // features checks
	CS1737(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR); // parameter default values check for order

	public static final CSharpCompilerChecks[] VALUES = CSharpCompilerChecks.values();

	private final CSharpLanguageVersion myLanguageVersion;
	private final HighlightInfoType myType;
	private final CompilerCheck<PsiElement> myCheck;
	private final Class<?> myTargetClass;

	CSharpCompilerChecks(CSharpLanguageVersion languageVersion, HighlightInfoType type)
	{
		myLanguageVersion = languageVersion;
		myType = type;
		try
		{
			Class<?> aClass = Class.forName("org.mustbe.consulo.csharp.ide.highlight.check.impl." + name());
			myCheck = (CompilerCheck<PsiElement>) aClass.newInstance();

			ParameterizedType genericType = (ParameterizedType) aClass.getGenericSuperclass();

			myTargetClass = (Class<?>) genericType.getActualTypeArguments()[0];
		}
		catch(Exception e)
		{
			throw new Error(e);
		}
	}

	@NotNull
	public List<CompilerCheck.CompilerCheckResult> check(CSharpLanguageVersion languageVersion, PsiElement element)
	{
		List<CompilerCheck.CompilerCheckResult> results = myCheck.check(languageVersion, element);
		if(results.isEmpty())
		{
			return Collections.emptyList();
		}
		for(CompilerCheck.CompilerCheckResult result : results)
		{
			if(result.getHighlightInfoType() == null)
			{
				result.setHighlightInfoType(myType);
			}
		}
		return results;
	}

	@NotNull
	public CSharpLanguageVersion getLanguageVersion()
	{
		return myLanguageVersion;
	}

	@NotNull
	public Class<?> getTargetClass()
	{
		return myTargetClass;
	}
}
