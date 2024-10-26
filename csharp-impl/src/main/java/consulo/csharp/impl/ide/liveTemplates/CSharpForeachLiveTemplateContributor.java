package consulo.csharp.impl.ide.liveTemplates;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.api.localize.CSharpLocalize;
import consulo.csharp.impl.ide.liveTemplates.context.CSharpStatementContextType;
import consulo.language.editor.template.LiveTemplateContributor;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import java.lang.Override;
import java.lang.String;

@ExtensionImpl
public class CSharpForeachLiveTemplateContributor implements LiveTemplateContributor {
  @Override
  @Nonnull
  public String groupId() {
    return "csharpforeach";
  }

  @Override
  @Nonnull
  public LocalizeValue groupName() {
    return LocalizeValue.localizeTODO("C# Foreach");
  }

  @Override
  public void contribute(@Nonnull LiveTemplateContributor.Factory factory) {
    try(Builder builder = factory.newBuilder("csharpforeachIter", "iter", "foreach ($FOREACH_COMPONENT_TYPE$ $VAR$ in $FOREACH_TYPE$) {\n"
        + "  $END$\n"
        + "}", CSharpLocalize.livetemplatesForv())) {
      builder.withReformat();

      builder.withVariable("FOREACH_TYPE", "csharpForeachVariable()", "", true);
      builder.withVariable("FOREACH_COMPONENT_TYPE", "csharpForeachComponentType(FOREACH_TYPE)", "var", false);
      builder.withVariable("VAR", "csharpSuggestVariableName()", "it", true);

      builder.withContext(CSharpStatementContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("csharpforeachFori", "fori", "for(int $INDEX$ = 0; $INDEX$ < $LIMIT$; $INDEX$++) {\n"
        + "  $END$\n"
        + "}", LocalizeValue.localizeTODO("Create iteration loop"))) {
      builder.withReformat();

      builder.withVariable("INDEX", "csharpSuggestIndexName()", "", true);
      builder.withVariable("LIMIT", "", "", true);

      builder.withContext(CSharpStatementContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("csharpforeachItar", "itar", "for(int $INDEX$ = 0; $INDEX$ < $ARRAY$.Length; $INDEX$++) {\n"
        + "  $ELEMENT_TYPE$ $VAR$ = $ARRAY$[$INDEX$];\n"
        + "  $END$\n"
        + "}", LocalizeValue.localizeTODO("Iterate array by index"))) {
      builder.withReformat();

      builder.withVariable("INDEX", "csharpSuggestIndexName()", "", true);
      builder.withVariable("ARRAY", "csharpArrayVariable()", "\"array\"", true);
      builder.withVariable("ELEMENT_TYPE", "csharpForeachComponentType(ARRAY)", "", false);
      builder.withVariable("VAR", "csharpSuggestVariableName()", "", true);

      builder.withContext(CSharpStatementContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("csharpforeachItli", "itli", "for(int $INDEX$ = 0; $INDEX$ < $LIST$.Count; $INDEX$++) {\n"
        + "  $ELEMENT_TYPE$ $VAR$ = $LIST$[$INDEX$];\n"
        + "  $END$\n"
        + "}", LocalizeValue.localizeTODO("Iterate list by index"))) {
      builder.withReformat();

      builder.withVariable("INDEX", "csharpSuggestIndexName()", "", true);
      builder.withVariable("LIST", "csharpIListVariable()", "\"array\"", true);
      builder.withVariable("ELEMENT_TYPE", "csharpForeachComponentType(LIST)", "", false);
      builder.withVariable("VAR", "csharpSuggestVariableName()", "", true);

      builder.withContext(CSharpStatementContextType.class, true);
    }
  }
}
