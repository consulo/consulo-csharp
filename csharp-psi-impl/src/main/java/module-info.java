/**
 * @author VISTALL
 * @since 10-Sep-22
 */
open module consulo.csharp.psi.impl
{
	requires transitive consulo.csharp.api;
	requires transitive consulo.csharp.csharp.psi.api;
	requires transitive consulo.dotnet.psi.impl;
	requires transitive consulo.dotnet.msil.api;
	requires transitive consulo.dotnet.msil.impl;
	requires transitive consulo.csharp.doc.psi.api;
	requires transitive consulo.csharp.composite.formatting.string.api;
	requires transitive consulo.dotnet.composite.formatting.string;
	requires consulo.internal.dotnet.msil.decompiler;

	requires consulo.ide.api;

	requires org.jooq.joou;
	requires gnu.jel;

	exports consulo.csharp.lang.impl;
	exports consulo.csharp.lang.impl.evaluator;
	exports consulo.csharp.lang.impl.ide.codeInspection.unusedUsing;
	exports consulo.csharp.lang.impl.ide.codeStyle;
	exports consulo.csharp.lang.impl.ide.refactoring;
	exports consulo.csharp.lang.impl.lexer;
	exports consulo.csharp.lang.impl.parser;
	exports consulo.csharp.lang.impl.parser.decl;
	exports consulo.csharp.lang.impl.parser.exp;
	exports consulo.csharp.lang.impl.parser.macro;
	exports consulo.csharp.lang.impl.parser.preprocessor;
	exports consulo.csharp.lang.impl.parser.stmt;
	exports consulo.csharp.lang.impl.psi;
	exports consulo.csharp.lang.impl.psi.elementType;
	exports consulo.csharp.lang.impl.psi.fragment;
	exports consulo.csharp.lang.impl.psi.light;
	exports consulo.csharp.lang.impl.psi.light.builder;
	exports consulo.csharp.lang.impl.psi.manipulator;
	exports consulo.csharp.lang.impl.psi.msil;
	exports consulo.csharp.lang.impl.psi.msil.transformer;
	exports consulo.csharp.lang.impl.psi.msil.typeParsing;
	exports consulo.csharp.lang.impl.psi.partial;
	exports consulo.csharp.lang.impl.psi.resolve;
	exports consulo.csharp.lang.impl.psi.resolve.additionalMembersImpl;
	exports consulo.csharp.lang.impl.psi.resolve.baseResolveContext;
	exports consulo.csharp.lang.impl.psi.search;
	exports consulo.csharp.lang.impl.psi.source;
	exports consulo.csharp.lang.impl.psi.source.injection;
	exports consulo.csharp.lang.impl.psi.source.resolve;
	exports consulo.csharp.lang.impl.psi.source.resolve.extensionResolver;
	exports consulo.csharp.lang.impl.psi.source.resolve.genericInference;
	exports consulo.csharp.lang.impl.psi.source.resolve.handlers;
	exports consulo.csharp.lang.impl.psi.source.resolve.methodResolving;
	exports consulo.csharp.lang.impl.psi.source.resolve.methodResolving.arguments;
	exports consulo.csharp.lang.impl.psi.source.resolve.methodResolving.context;
	exports consulo.csharp.lang.impl.psi.source.resolve.operatorResolving;
	exports consulo.csharp.lang.impl.psi.source.resolve.overrideSystem;
	exports consulo.csharp.lang.impl.psi.source.resolve.sorter;
	exports consulo.csharp.lang.impl.psi.source.resolve.type;
	exports consulo.csharp.lang.impl.psi.source.resolve.type.wrapper;
	exports consulo.csharp.lang.impl.psi.source.resolve.util;
	exports consulo.csharp.lang.impl.psi.source.using;
	exports consulo.csharp.lang.impl.psi.stub;
	exports consulo.csharp.lang.impl.psi.stub.elementTypes;
	exports consulo.csharp.lang.impl.psi.stub.elementTypes.macro;
	exports consulo.csharp.lang.impl.psi.stub.index;
	exports consulo.csharp.lang.impl.roots;
}