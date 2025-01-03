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

package consulo.csharp.impl.ide.highlight.check;

import consulo.annotation.access.RequiredReadAction;
import consulo.csharp.impl.ide.highlight.CSharpHighlightContext;
import consulo.csharp.impl.ide.highlight.CSharpHighlightInfoType;
import consulo.csharp.module.extension.CSharpLanguageVersion;
import consulo.language.editor.rawHighlight.HighlightInfoType;
import consulo.language.psi.PsiElement;

import jakarta.annotation.Nonnull;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;

/**
 * @author VISTALL
 * @since 15.05.14
 */
public enum CSharpCompilerChecks
{
	CS0017(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // Main() duplicate check
	CS0019(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // operator checks
	CS0023(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), //  null cant be reference qualifier(dot operator)
	CS0026(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), //  'this' dont exists in static context
	CS0027(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), //  this can't used inside field declaration
	CS0029(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), //  cant convert A to B with implicit
	CS0030(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // cant convert A to B
	CS0070(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // events can not be called outside declaration type
	CS0077(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // as operator cant be used with notnull types
	CS0100(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // parameter is duplicate
	CS0101(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // members duplicate in namespace declaration
	CS0102(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // members duplicate in type declaration
	CS0106(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // modifier check
	CS0107(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // modifier protection check
	CS0113(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // virtual/new can't be with override
	CS0116(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // namespaces can contains only classes or delegate methods
	CS0118(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // expected variable but found type
	CS0120(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // instance members required instance object reference
	CS0122(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // visibility checks
	CS0128(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // local variable redeclaration check
	CS0132(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static constructors are parameterless
	CS0133(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // constant value must be compile time constant
	CS0136(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // variable redeclaration check in different scopes
	CS0144(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // abstract types or interfaces cant created by new expression
	CS0145(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // const cant be without value
	CS0146(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // circular extends check
	CS0151(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // check for switch expression
	CS0153(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // check goto case&default without switch
	CS0155(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // throw object must be child of System.Exception
	CS0157(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // return is not allowed inside finally blocks
	CS0161(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), //  return check
	CS0168(CSharpLanguageVersion._1_0, CSharpHighlightInfoType.UNUSED), // local variable usage check
	CS0169(CSharpLanguageVersion._1_0, CSharpHighlightInfoType.UNUSED), // field is not used
	CS0173(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // failed to find conditional expression type
	CS0200(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // readonly check
	CS0201(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // expression statement can be only call, etc
	CS0206(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // ref/out expressions can be only indexer, etc
	CS0211(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // cant take address for expression
	CS0214(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // fixed can be used inside unsafe context
	CS0219(CSharpLanguageVersion._1_0, CSharpHighlightInfoType.UNUSED), // local variable usage check with initializer
	CS0227(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // 'unsafe' modifier check
	CS0231(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // 'params' modifier must be last
	CS0236(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // can't reference to non static field, property, method from field initializer
	CS0264(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // generic parameter names must be equal for partial types
	CS0304(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // generic cant be new without new() constraint
	CS0305(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // check for generic count
	CS0401(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // new() constraint must be last
	CS0409(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // generic constraint already defined for generic
	CS0413(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // 'S' operator  cant use to generic without class constraint, or reference
	CS0418(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // abstract type cant be static or sealed
	CS0441(CSharpLanguageVersion._2_0, HighlightInfoType.WRONG_REF), // static and sealed cant be combinded
	CS0449(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // struct or class constraint must be first
	CS0453(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // nullable type required notnull type
	CS0500(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // code block with abstract modifier
	CS0501(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // empty code block checks
	CS0509(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // classes cant extend sealed type
	CS0516(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // constructor cant call itself
	CS0531(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // checks for body for only abstract items
	CS0534(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // checks for missed impl of interfaces
	//CS0535(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // checks for missed impl of abstract members
	CS0539(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // checks for private impl of abstract methods
	CS0542(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // members cant be named as parent
	CS0555(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // implict and explicit cant convert to itself
	CS0556(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // implict and explicit cant be hold type without owner
	CS0568(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // structs cant have parameterless constructor
	CS0673(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // Void cant be used in C#
	CS0692(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // duplicate parameter name
	CS0693(CSharpLanguageVersion._2_0, HighlightInfoType.WARNING), // check by generic
	CS0702(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // System.Object or System.ValueType cant use by constraints
	CS0708(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // members inside static type need define static modifier
	CS0709(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static types cant be parent
	CS0721(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static classes in parameters
	CS0722(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static classes in methods
	CS0723(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static classes in variables
	CS0815(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // lambdas cant be cast to 'var'
	CS0818(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // var cant be used if not initializer
	CS0820(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // var cant be use it initializer is implicit array initializer
	CS0826(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // fail inherit type of typed array initializer
	CS1004(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // duplicate modifier check
	CS1008(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // eum type can be only byte,sbyte,short,ushort,int,uint,long,ulong
	CS1021(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // too big number
	CS1029(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // check for #error
	CS1030(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // check for #warning
	CS1100(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // 'this' modifier can be only set to first parameter
	CS1105(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // 'this' modifier can be only in method with static modifier
	CS1106(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // 'this' modifier can be only in type with static modifier and no generic
	CS1510(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // ref/out can accept only variable
	CS1511(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // 'base' inside static context
	CS1535(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // implicit and explicit can hold only one param
	CS1547(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // 'void' can used only in return type
	CS1612(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // can't modify field value from non local reference
	CS1614(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // double resolving to X and XAttribute
	CS1620(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // ref out exp checks
	CS1644(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // features checks
	CS1656(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // value can't be set to foreach
	CS1674(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // using statement expression or variable must be System.IDisposable
	CS1676(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // lambda parameter validation with out / ref params
	CS1722(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // base type must be first in extend list
	CS1729(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // constructor ref to not existed empty base constructor
	CS1736(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // parameter value must be compile time constant
	CS1737(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // parameter default values check for order
	CS1738(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // parameter default values check for order
	CS1741(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // default value cant be specified for ref/out parameters
	CS1960(CSharpLanguageVersion._2_0, HighlightInfoType.WRONG_REF), // in and out modifiers can be only for interface(or delegate) generic parameter
	CS1980(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // dynamic checks
	CS1984(CSharpLanguageVersion._4_0, HighlightInfoType.WRONG_REF), // await cant be used inside finally statements
	CS1985(CSharpLanguageVersion._4_0, HighlightInfoType.WRONG_REF), // await cant be used inside catch statements
	CS1998(CSharpLanguageVersion._4_0, CSharpHighlightInfoType.UNUSED), // async modifer - then no await
	CS4009(CSharpLanguageVersion._4_0, HighlightInfoType.WRONG_REF), // async modifier cant be at entry point
	CS8632(CSharpLanguageVersion._8_0, HighlightInfoType.WARNING), // nullable type check

	CC0001(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), //reference checks
	CC0002(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), //operator reference checks
	CC0003(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), //array access expression checks
	CC0004(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // method call checks
	CC0005(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // super constructor call checks
	CC0006(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF); // checks for string literals

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
			String packageName = getClass().getPackageName();
			Class<?> aClass = Class.forName(packageName + ".impl." + name());
			//noinspection unchecked
			myCheck = (CompilerCheck<PsiElement>) aClass.newInstance();

			ParameterizedType genericType = (ParameterizedType) aClass.getGenericSuperclass();

			myTargetClass = (Class<?>) genericType.getActualTypeArguments()[0];
		}
		catch(Exception e)
		{
			throw new Error(e);
		}
	}

	@Nonnull
	@RequiredReadAction
	public List<? extends CompilerCheck.HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, PsiElement element)
	{
		List<? extends CompilerCheck.HighlightInfoFactory> results = myCheck.check(languageVersion, highlightContext, element);
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
					checkResult.withHighlightInfoType(myType);
				}
			}
		}
		return results;
	}

	public boolean isSuppressable()
	{
		return myType != HighlightInfoType.ERROR;
	}
	@Nonnull
	public CSharpLanguageVersion getLanguageVersion()
	{
		return myLanguageVersion;
	}

	@Nonnull
	public Class<?> getTargetClass()
	{
		return myTargetClass;
	}
}
