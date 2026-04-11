/**
 * @author VISTALL
 * @since 2026-04-11
 */
module consulo.csharp.editor.api {
    requires consulo.csharp.api;
    requires consulo.csharp.csharp.psi.api;
    requires consulo.color.scheme.api;
    requires consulo.code.editor.api;
    
    exports consulo.csharp.editor.highlight;
}