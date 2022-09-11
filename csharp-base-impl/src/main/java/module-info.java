/**
 * @author VISTALL
 * @since 10-Sep-22
 */
module consulo.csharp.base.impl
{
	// TODO remove in future
	requires java.desktop;

	requires transitive consulo.csharp.api;
	requires transitive consulo.csharp.psi.impl;
	requires transitive consulo.dotnet.impl;
	requires transitive consulo.dotnet.psi.api;

	exports consulo.csharp.base.compiler;
	exports consulo.csharp.base.module.extension;
}