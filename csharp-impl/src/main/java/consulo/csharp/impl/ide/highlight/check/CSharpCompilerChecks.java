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

import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author VISTALL
 * @since 2014-05-15
 */
public enum CSharpCompilerChecks {
    CS0017(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // Main() duplicate check
    CS0019(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // operator checks
    CS0023(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), //  null cant be reference qualifier(dot operator)
    CS0026(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), //  'this' doesn't exist in static context
    CS0027(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), //  this can't used inside field declaration
    CS0029(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), //  cant convert A to B with implicit
    CS0030(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // cant convert A to B
    CS0050(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // inconsistent accessibility - a method return type
    CS0051(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // inconsistent accessibility - a method parameter type
    CS0052(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // inconsistent accessibility - a field type
    CS0053(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // inconsistent accessibility - a property type
    CS0054(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // inconsistent accessibility - an indexer return type
    CS0055(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // inconsistent accessibility - an indexer parameter type
    CS0056(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // inconsistent accessibility - an operator return type
    CS0057(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // inconsistent accessibility - an operator parameter type
    CS0058(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // inconsistent accessibility - a delegate return type
    CS0059(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // inconsistent accessibility - a delegate parameter type
    CS0060(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // inconsistent accessibility - a base class
    CS0061(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // inconsistent accessibility - a base interface
    CS0070(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // events can not be called outside declaration type
    CS0077(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // as operator cant be used with notnull types
    CS0100(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // parameter is duplicate
    CS0101(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // members duplicate in namespace declaration
    CS0102(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // members duplicate in type declaration
    CS0106(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // modifier check
    CS0107(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // modifier protection check
    CS0108(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // member hides an inherited one without new
    CS0109(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // new on a member hiding nothing
    CS0111(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // methods of a name and the same parameters
    CS0113(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // virtual/new can't be with override
    CS0114(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // member hides an inherited virtual one without override or new
    CS0115(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // override of nothing
    CS0116(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // namespaces can contains only classes or delegate methods
    CS0118(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // expected variable but found type
    CS0119(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // a method as the qualifier of a member access
    CS0120(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // instance members required instance object reference
    CS0122(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // visibility checks
    CS0126(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // return without a value where one is required
    CS0127(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // return with a value from a void method
    CS0128(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // local variable redeclaration check
    CS0131(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // assignment to what is not a variable
    CS0132(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static constructors are parameterless
    CS0133(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // constant value must be compile time constant
    CS0136(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // variable redeclaration check in different scopes
    CS0139(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // break or continue out of no loop
    CS0144(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // abstract types or interfaces cant created by new expression
    CS0145(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // const cant be without value
    CS0146(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // circular extends check
    CS0151(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // check for switch expression
    CS0152(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // switch case value duplicated
    CS0153(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // check goto case&default without switch
    CS0155(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // throw object must be child of System.Exception
    CS0156(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // throw; outside a catch
    CS0157(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // return is not allowed inside finally blocks
    CS0159(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // no such label for a goto
    CS0161(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), //  return check
    CS0162(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // unreachable code
    CS0163(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // switch section falls through to the next one
    CS0165(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // use of unassigned local variable
    CS0168(CSharpLanguageVersion._1_0, CSharpHighlightInfoType::getUnusedDeclaration), // local variable usage check
    CS0169(CSharpLanguageVersion._1_0, CSharpHighlightInfoType::getUnusedDeclaration), // field is not used
    CS0171(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // struct field not assigned by a constructor
    CS0173(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // failed to find conditional expression type
    CS0177(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // out parameter not assigned when control leaves
    CS0188(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // this of a struct used before all its fields are assigned
    CS0191(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // readonly field assigned out of a constructor
    CS0198(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static readonly field assigned out of the static constructor
    CS0200(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // readonly check
    CS0201(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // expression statement can be only call, etc
    CS0206(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // ref/out expressions can be only indexer, etc
    CS0211(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // cant take address for expression
    CS0214(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // fixed can be used inside unsafe context
    CS0215(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // operator true or false not returning bool
    CS0216(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // operator without its pair
    CS0219(CSharpLanguageVersion._1_0, CSharpHighlightInfoType::getUnusedDeclaration), // local variable usage check with initializer
    CS0227(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // 'unsafe' modifier check
    CS0231(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // 'params' modifier must be last
    CS0236(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // can't reference to non static field, property, method from field initializer
    CS0260(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // a declaration without partial of a partial type
    CS0261(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // partial declarations of other kinds
    CS0262(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // partial declarations of other access modifiers
    CS0263(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // partial declarations of other base classes
    CS0264(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // generic parameter names must be equal for partial types
    CS0265(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // partial declarations of other constraints of a type parameter
    CS0269(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // use of unassigned out parameter
    CS0304(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // generic cant be new without new() constraint
    CS0305(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // check for generic count
    CS0401(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // new() constraint must be last
    CS0409(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // generic constraint already defined for generic
    CS0413(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // 'S' operator  cant use to generic without class constraint, or reference
    CS0414(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // private field assigned, never read
    CS0418(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // abstract type cant be static or sealed
    CS0441(CSharpLanguageVersion._2_0, HighlightInfoType.WRONG_REF), // static and sealed cant be combined
    CS0448(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // ++ or -- returning another type
    CS0449(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // struct or class constraint must be first
    CS0453(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // nullable type required notnull type
    CS0472(CSharpLanguageVersion._2_0, HighlightInfoType.WARNING), // comparison of a value type with null
    CS0500(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // code block with abstract modifier
    CS0501(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // empty code block checks
    CS0506(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // override of a member not virtual
    CS0507(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // override changing the access
    CS0509(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // classes cant extend sealed type
    CS0516(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // constructor cant call itself
    CS0525(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // interface with an instance field
    CS0526(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // interface with a constructor
    CS0531(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // checks for body for only abstract items
    CS0534(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // checks for missed impl of interfaces
    //CS0535(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // checks for missed impl of abstract members
    CS0535(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // interface member not implemented
    CS0539(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // checks for private impl of abstract methods
    CS0540(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // explicit implementation of an interface not implemented
    CS0542(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // members cant be named as parent
    CS0552(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // conversion to or from an interface
    CS0553(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // conversion to or from a base type
    CS0554(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // conversion to or from a derived type
    CS0555(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // implicit and explicit cant convert to itself
    CS0556(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // implicit and explicit cant be hold type without owner
    CS0557(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // duplicate conversion
    CS0558(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // operator not public static
    CS0559(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // ++ or -- of another type
    CS0562(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // unary operator of another type
    CS0563(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // binary operator of other types
    CS0564(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // shift operator of another type
    CS0568(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // structs cant have parameterless constructor
    CS0579(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // duplicate attribute
    CS0590(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // operator returning void
    CS0592(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // attribute not valid on the declaration
    CS0616(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // not an attribute class
    CS0628(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // protected member of a sealed type
    CS0649(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // private field never assigned
    CS0657(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // invalid attribute location
    CS0659(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // Equals without GetHashCode
    CS0660(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // operator == without Equals
    CS0661(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // operator == without GetHashCode
    CS0665(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // constant assignment in a condition
    CS0673(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // Void cant be used in C#
    CS0692(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // duplicate parameter name
    CS0693(CSharpLanguageVersion._2_0, HighlightInfoType.WARNING), // check by generic
    CS0702(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // System.Object or System.ValueType cant use by constraints
    CS0708(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // members inside static type need define static modifier
    CS0709(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static types cant be parent
    CS0710(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // static class with an instance constructor
    CS0711(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // static class with a destructor
    CS0712(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // new of a static class
    CS0713(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // static class with a base class
    CS0714(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // static class implementing an interface
    CS0715(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // static class with an operator
    CS0716(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // cast to a static class
    CS0717(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // static class as a constraint
    CS0718(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // static class as a type argument
    CS0719(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // array of a static class
    CS0720(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // static class with an indexer
    CS0721(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static classes in parameters
    CS0722(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static classes in methods
    CS0723(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // static classes in variables
    CS0724(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // throw; in a finally in a catch
    CS0736(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // interface member implemented by a static member
    CS0737(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // interface member implemented by a non-public member
    CS0738(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // interface member implemented with another return type
    CS0815(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // lambdas cant be cast to 'var'
    CS0818(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // var cant be used if not initializer
    CS0819(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // var with multiple declarators
    CS0820(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // var cant be use it initializer is implicit array initializer
    CS0822(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // const var
    CS0825(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // var outside a local
    CS0826(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // fail inherit type of typed array initializer
    CS0832(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // assignment in expression tree
    CS0834(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // statement lambda as expression tree
    CS0838(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // multidimensional array initializer in expression tree
    CS0841(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // local used before its declaration
    CS0843(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // struct auto-property not assigned by a constructor
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
    CS1621(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // yield in a lambda
    CS1622(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // return in an iterator
    CS1623(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // iterator with a ref parameter
    CS1624(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // iterator of a non-iterator type
    CS1625(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // yield in finally
    CS1626(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // yield return in try with catch
    CS1631(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // yield return in catch
    CS1632(CSharpLanguageVersion._2_0, HighlightInfoType.ERROR), // break or continue out of a lambda
    CS1643(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // not all code paths of a lambda or an anonymous method return a value
    CS1644(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // features checks
    CS1656(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // value can't be set to foreach
    CS1674(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // using statement expression or variable must be System.IDisposable
    CS1676(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // lambda parameter validation with out / ref params
    CS1717(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // assignment to the same variable
    CS1718(CSharpLanguageVersion._1_0, HighlightInfoType.WARNING), // comparison to the same variable
    CS1722(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // base type must be first in extend list
    CS1729(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // constructor ref to not existed empty base constructor
    CS1730(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // assembly and module attributes must precede all declarations
    CS1736(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // parameter value must be compile time constant
    CS1737(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // parameter default values check for order
    CS1738(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // parameter default values check for order
    CS1741(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // default value cant be specified for ref/out parameters
    CS1945(CSharpLanguageVersion._3_0, HighlightInfoType.ERROR), // anonymous method in expression tree
    CS1960(CSharpLanguageVersion._2_0, HighlightInfoType.WRONG_REF), // in and out modifiers can be only for interface(or delegate) generic parameter
    CS1980(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // dynamic checks
    CS1984(CSharpLanguageVersion._4_0, HighlightInfoType.WRONG_REF), // await cant be used inside finally statements
    CS1985(CSharpLanguageVersion._4_0, HighlightInfoType.WRONG_REF), // await cant be used inside catch statements
    CS1989(CSharpLanguageVersion._5_0, HighlightInfoType.ERROR), // async lambda as expression tree
    CS1996(CSharpLanguageVersion._5_0, HighlightInfoType.ERROR), // await in the body of a lock
    CS1997(CSharpLanguageVersion._5_0, HighlightInfoType.ERROR), // return with a value from an async Task method
    CS1998(CSharpLanguageVersion._4_0, CSharpHighlightInfoType::getUnusedDeclaration), // async modifier - then no await
    CS4009(CSharpLanguageVersion._4_0, HighlightInfoType.WRONG_REF), // async modifier cant be at entry point
    CS4032(CSharpLanguageVersion._5_0, HighlightInfoType.ERROR), // await in a method not async
    CS4033(CSharpLanguageVersion._5_0, HighlightInfoType.ERROR), // await in a void method not async
    CS4034(CSharpLanguageVersion._5_0, HighlightInfoType.ERROR), // await in a lambda or anonymous method not async
    CS7025(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // inconsistent accessibility - an event type
    CS8030(CSharpLanguageVersion._7_0, HighlightInfoType.ERROR), // return with a value from a lambda of a void delegate
    CS8070(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // the final switch section falls out of the switch
    CS8072(CSharpLanguageVersion._6_0, HighlightInfoType.ERROR), // null propagating operator in expression tree
    CS8110(CSharpLanguageVersion._7_0, HighlightInfoType.ERROR), // local function in expression tree
    CS8122(CSharpLanguageVersion._7_0, HighlightInfoType.ERROR), // is pattern in expression tree
    CS8143(CSharpLanguageVersion._7_0, HighlightInfoType.ERROR), // tuple literal in expression tree
    CS8153(CSharpLanguageVersion._7_0, HighlightInfoType.ERROR), // ref returning call in expression tree
    CS8188(CSharpLanguageVersion._7_0, HighlightInfoType.ERROR), // throw expression in expression tree
    CS8198(CSharpLanguageVersion._7_0, HighlightInfoType.ERROR), // out variable in expression tree
    CS8207(CSharpLanguageVersion._7_0, HighlightInfoType.ERROR), // discard in expression tree
    CS8403(CSharpLanguageVersion._8_0, HighlightInfoType.ERROR), // async iterator without async
    CS8514(CSharpLanguageVersion._8_0, HighlightInfoType.ERROR), // switch expression in expression tree
    CS8618(CSharpLanguageVersion._8_0, HighlightInfoType.WARNING), // non-nullable member not assigned by a constructor
    CS8632(CSharpLanguageVersion._8_0, HighlightInfoType.WARNING), // nullable type check
    CS8790(CSharpLanguageVersion._8_0, HighlightInfoType.ERROR), // index or range indexer in expression tree
    CS8791(CSharpLanguageVersion._8_0, HighlightInfoType.ERROR), // index from end in expression tree
    CS8792(CSharpLanguageVersion._8_0, HighlightInfoType.ERROR), // range in expression tree
    CS8803(CSharpLanguageVersion._9_0, HighlightInfoType.ERROR), // top-level statements must precede namespace and type declarations
    CS9014(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // struct auto-property read before assigned in a constructor
    CS9015(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // struct field read before assigned in a constructor
    CS9035(CSharpLanguageVersion._11_0, HighlightInfoType.ERROR), // a required member not set by an object creation
    CS9175(CSharpLanguageVersion._12_0, HighlightInfoType.ERROR), // collection expression in expression tree

    CC0001(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), //reference checks
    CC0002(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), //operator reference checks
    CC0003(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), //array access expression checks
    CC0004(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // method call checks
    CC0005(CSharpLanguageVersion._1_0, HighlightInfoType.ERROR), // super constructor call checks
    CC0006(CSharpLanguageVersion._1_0, HighlightInfoType.WRONG_REF), // checks for string literals
    CC0007(CSharpLanguageVersion._1_0, CSharpHighlightInfoType::getUnusedDeclaration), // a value assigned and never read
    CC0008(CSharpLanguageVersion._1_0, CSharpHighlightInfoType::getUnusedDeclaration), // a private member never used
    CC0009(CSharpLanguageVersion._1_0, CSharpHighlightInfoType::getUnusedType); // a type not of the public API never used

    public static final CSharpCompilerChecks[] VALUES = CSharpCompilerChecks.values();

    private final CSharpLanguageVersion myLanguageVersion;
    private final Supplier<HighlightInfoType> myTypeSupplier;
    private final CompilerCheck<PsiElement> myCheck;
    private final Class<?> myTargetClass;

    CSharpCompilerChecks(CSharpLanguageVersion languageVersion, HighlightInfoType type) {
        this(languageVersion, () -> type);
    }

    @SuppressWarnings("deprecation")
    CSharpCompilerChecks(CSharpLanguageVersion languageVersion, Supplier<HighlightInfoType> typeSupplier) {
        myLanguageVersion = languageVersion;
        myTypeSupplier = typeSupplier;
        try {
            String packageName = getClass().getPackageName();
            Class<?> aClass = Class.forName(packageName + ".impl." + name());
            //noinspection unchecked
            myCheck = (CompilerCheck<PsiElement>) aClass.newInstance();

            ParameterizedType genericType = (ParameterizedType) aClass.getGenericSuperclass();

            myTargetClass = (Class<?>) genericType.getActualTypeArguments()[0];
        }
        catch (Exception e) {
            throw new Error(e);
        }
    }

    @RequiredReadAction
    public List<? extends CompilerCheck.HighlightInfoFactory> check(CSharpLanguageVersion languageVersion, CSharpHighlightContext highlightContext, PsiElement element) {
        List<? extends CompilerCheck.HighlightInfoFactory> results = myCheck.check(languageVersion, highlightContext, element);
        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        for (CompilerCheck.HighlightInfoFactory result : results) {
            if (result instanceof CompilerCheck.CompilerCheckBuilder) {
                CompilerCheck.CompilerCheckBuilder checkResult = (CompilerCheck.CompilerCheckBuilder) result;
                if (checkResult.getHighlightInfoType() == null) {
                    checkResult.withHighlightInfoType(myTypeSupplier.get());
                }
            }

        }
        return results;
    }

    public boolean isSuppressable() {
        HighlightInfoType type = myTypeSupplier.get();
        return type != HighlightInfoType.ERROR;
    }

    public CSharpLanguageVersion getLanguageVersion() {
        return myLanguageVersion;
    }

    public Class<?> getTargetClass() {
        return myTargetClass;
    }
}
