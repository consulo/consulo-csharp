/**
 * @author VISTALL
 * @since 10-Sep-22
 */
module consulo.csharp.csharp.psi.api
{
	requires transitive consulo.csharp.api;
	requires transitive consulo.ide.api;

	requires transitive consulo.dotnet.psi.api;

	exports consulo.csharp.lang;
	exports consulo.csharp.lang.psi;
	exports consulo.csharp.lang.psi.resolve;
	exports consulo.csharp.lang.psi.icon;
	exports consulo.csharp.lang.util;
}