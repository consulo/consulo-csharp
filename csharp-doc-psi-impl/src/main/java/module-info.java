/**
 * @author VISTALL
 * @since 10-Sep-22
 */
module consulo.csharp.doc.psi.impl
{
	requires transitive consulo.csharp.psi.impl;
	requires transitive consulo.csharp.doc.psi.api;
	requires transitive consulo.dotnet.documentation.api;

	exports consulo.csharp.lang.doc.impl;
	exports consulo.csharp.lang.doc.impl.ide.codeInsight.editorActions;
	exports consulo.csharp.lang.doc.impl.ide.competion;
	exports consulo.csharp.lang.doc.impl.ide.highlight;
	exports consulo.csharp.lang.doc.impl.inspection;
	exports consulo.csharp.lang.doc.impl.lexer;
	exports consulo.csharp.lang.doc.impl.parser;
	exports consulo.csharp.lang.doc.impl.psi;
	exports consulo.csharp.lang.doc.impl.validation;
}