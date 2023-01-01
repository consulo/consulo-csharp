/**
 * @author VISTALL
 * @since 10-Sep-22
 */
module consulo.csharp.api
{
	requires transitive consulo.ide.api;
	requires transitive consulo.dotnet.api;
	requires consulo.dotnet.impl;

	exports consulo.csharp;
	exports consulo.csharp.api.localize;
	exports consulo.csharp.compiler;
	exports consulo.csharp.module;
	exports consulo.csharp.module.extension;
}