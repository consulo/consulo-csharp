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

package org.mustbe.consulo.csharp.ide.highlight.check;

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.mustbe.consulo.csharp.module.extension.CSharpLanguageVersion;
import com.intellij.codeInsight.daemon.impl.HighlightInfoType;
import com.intellij.psi.PsiElement;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public enum CSharpCompilerChecks
{
	CS0026(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), //  'this' dont exists in static context
	CS0029(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), //  cant convert A to B with implicit
	CS0030(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // cant convert A to B
	CS0077(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // as operator cant be used with notnull types
	CS0100(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // parameter is duplicate
	CS0101(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // members duplicate in namespace declaration
	CS0102(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // members duplicate in type declaration
	CS0106(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // modifier check
	CS0107(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // modifier protection check
	CS0120(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // instance members required instance object reference
	CS0128(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // local variable redeclaration check
	CS0132(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static constructors are parameterless
	//CS0136(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // lambda parameter redeclaration check
	CS0144(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // abstract types or interfaces cant created by new expression
	CS0145(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // const cant be without value
	CS0155(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // throw object must be child of System.Exception
	CS0157(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // return is not allowed inside finally blocks
	//CS0168(CSharpLanguageVersion._1_0, HighlightInfoType.UNUSED_SYMBOL), // local variable usage check
	CS0214(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // fixed can be used inside unsafe context
	CS0122(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // visibility checks
	//CS0219(CSharpLanguageVersion._1_0, HighlightInfoType.UNUSED_SYMBOL), // local variable usage check
	CS0227(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // 'unsafe' modifier check
	CS0231(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // 'params' modifier must be last
	CS0304(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // generic cant be new without new() constraint
	CS0305(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // check for generic count
	CS0401(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // new() constraint must be last
	CS0409(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // generic constraint already defined for generic
	CS0413(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // 'S' operator  cant use to generic without class constraint, or reference
	CS0449(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // struct or class constraint must be first
	CS0453(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // nullable type required notnull type
	CS0500(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // code block with abstract modifier
	CS0501(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // empty code block checks
	CS0516(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // constructor cant call itself
	CS0518(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // dynamic checks
	CS0531(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // checks for body for only abstract items
	CS0534(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // checks for missed impl of interfaces
	//CS0535(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // checks for missed impl of abstract members
	CS0539(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // checks for private impl of abstract methods
	CS0542(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // members cant be named as parent
	CS0568(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // structs cant have parameterless constructor
	CS0693(CSharpLanguageVersion._2_0, HighlightInfoType.WARNING), // check by generic
	CS0702(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // System.Object or System.ValueType cant use by constraints
	CS0708(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // members inside static type need define static modifier
	CS0721(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static classes in parameters
	CS0722(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static classes in methods
	CS0723(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static classes in variables
	CS0815(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // lambdas cant be cast to 'var'
	CS0818(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // var cant be used if not initializer
	CS0820(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // var cant be use it initializer is implicit array initializer
	CS0826(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // fail inherit type of typed array initializer
	CS1004(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // duplicate modifier check
	CS1100(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // 'this' modifier can be only set to first parameter
	CS1105(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // 'this' modifier can be only in method with static modifier
	CS1106(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // 'this' modifier can be only in type with static modifier and no generic
	CS1511(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // 'base' inside static context
	CS1614(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // double resolving to X and XAttribute
	CS1620(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // ref out exp checks
	CS1644(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // features checks
	CS1674(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // using statement expression or variable must be System.IDisposable
	CS1722(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // base type must be first in extend list
	CS1737(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // parameter default values check for order
	CS1738(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // parameter default values check for order
	CS1960(CSharpLanguageVersion._2_0, HighlightInfoType.WRONG_REF), // in and out modifiers can be only for interface(or delegate) generic parameter
	CS1984(CSharpLanguageVersion._4_0, HighlightInfoType.WRONG_REF), // await cant be used inside finally statements
	CS1985(CSharpLanguageVersion._4_0, HighlightInfoType.WRONG_REF), // await cant be used inside catch statements
	CS1998(CSharpLanguageVersion._4_0, HighlightInfoType.UNUSED_SYMBOL), // async modifer - then no await
	CS4009(CSharpLanguageVersion._4_0, HighlightInfoType.WRONG_REF), // async modifier cant be at entry point

	CC0001(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), //reference checks
	CC0002(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), //operator reference checks
	CC0003(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF); //array access expression checks

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
	public List<? extends CompilerCheck.HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, PsiElement element)
	{
		List<? extends CompilerCheck.HighlightInfoFactory> results = myCheck.check(languageVersion, element);
		if(results.isEmpty())
		{
			return Collections.emptyList();
		}
		for(CompilerCheck.HighlightInfoFactory result : results)
		{
			if(result instanceof CompilerCheck.CompilerCheckBuilder)
			{
				CompilerCheck.CompilerCheckBuilder checkResult = (CompilerCheck.CompilerCheckBuilder) result;
				if(checkResult.getHighlightInfoType() == null)
				{
					checkResult.setHighlightInfoType(myType);
				}
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
