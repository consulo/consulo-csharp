package consulo.csharp.impl.ide.liveTemplates;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.api.localize.CSharpLocalize;
import consulo.csharp.impl.ide.liveTemplates.context.CSharpClassBodyContextType;
import consulo.language.editor.template.LiveTemplateContributor;
import consulo.localize.LocalizeValue;
import jakarta.annotation.Nonnull;
import java.lang.Override;
import java.lang.String;

@ExtensionImpl
public class CSharpMainLiveTemplateContributor implements LiveTemplateContributor {
  @Override
  @Nonnull
  public String groupId() {
    return "csharpmain";
  }

  @Override
  @Nonnull
  public LocalizeValue groupName() {
    return LocalizeValue.localizeTODO("C# Main");
  }

  @Override
  public void contribute(@Nonnull LiveTemplateContributor.Factory factory) {
    try(Builder builder = factory.newBuilder("csharpmainPsvm", "psvm", "public static void Main(){\r\n"
        + "  $END$\r\n"
        + "}", CSharpLocalize.livetemplatesPsvm())) {
      builder.withReformat();


      builder.withContext(CSharpClassBodyContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("csharpmainPsvms", "psvms", "public static void Main(string[] args){\r\n"
        + "  $END$\r\n"
        + "}", CSharpLocalize.livetemplatesPsvms())) {
      builder.withReformat();


      builder.withContext(CSharpClassBodyContextType.class, true);
    }

    try(Builder builder = factory.newBuilder("csharpmainProp", "prop", "public $TYPE$ $VAR$ { get; set; }", CSharpLocalize.livetemplatesProp())) {
      builder.withReformat();

      builder.withVariable("TYPE", "csharpType()", "TYPE", true);
      builder.withVariable("VAR", "csharpSuggestVariableName()", "Name", true);

      builder.withContext(CSharpClassBodyContextType.class, true);
    }

  }
}
