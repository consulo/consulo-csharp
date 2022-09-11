/**
 * @author VISTALL
 * @since 10-Sep-22
 */
module consulo.csharp.composite.formatting.string.impl
{
	requires transitive consulo.csharp.composite.formatting.string.api;
	requires transitive consulo.dotnet.composite.formatting.string;
	requires consulo.csharp.psi.impl;

	exports consulo.csharp.cfs.impl;
	exports consulo.csharp.cfs.impl.lexer;
}