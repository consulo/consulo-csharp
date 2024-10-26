package consulo.csharp.impl.ide.liveTemplates;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.api.localize.CSharpLocalize;
import consulo.csharp.impl.ide.liveTemplates.context.CSharpExpressionContextType;
import consulo.language.editor.template.LiveTemplateContributor;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import java.lang.Override;
import java.lang.String;

@ExtensionImpl
public class CSharpLinqLiveTemplateContributor implements LiveTemplateContributor {
  @Override
  @Nonnull
  public String groupId() {
    return "csharplinq";
  }

  @Override
  @Nonnull
  public LocalizeValue groupName() {
    return LocalizeValue.localizeTODO("C# Linq");
  }

  @Override
  public void contribute(@Nonnull LiveTemplateContributor.Factory factory) {
    try(Builder builder = factory.newBuilder("csharplinqFws", "fws", "from $VAR$ in $FOREACH_VAR$ where $END$ select $VAR$", CSharpLocalize.livetemplatesForv())) {
      builder.withReformat();

      builder.withVariable("FOREACH_VAR", "csharpForeachVariable()", "", true);
      builder.withVariable("VAR", "csharpSuggestIndexName()", "it", false);

      builder.withContext(CSharpExpressionContextType.class, true);
    }

  }
}
