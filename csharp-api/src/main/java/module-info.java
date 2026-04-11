/**
 * @author VISTALL
 * @since 10-Sep-22
 */
module consulo.csharp.api {
    requires transitive consulo.dotnet.api;
    requires transitive consulo.module.api;
    requires consulo.compiler.api;
    requires consulo.process.api;
    requires consulo.module.ui.api;

    exports consulo.csharp;
    exports consulo.csharp.api.localize;
    exports consulo.csharp.compiler;
    exports consulo.csharp.module;
    exports consulo.csharp.module.extension;
}