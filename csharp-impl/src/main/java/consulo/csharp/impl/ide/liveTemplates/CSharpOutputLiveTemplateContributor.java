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
public class CSharpOutputLiveTemplateContributor implements LiveTemplateContributor {
  @Override
  @Nonnull
  public String groupId() {
    return "csharpoutput";
  }

  @Override
  @Nonnull
  public LocalizeValue groupName() {
    return LocalizeValue.localizeTODO("C# Output");
  }

  @Override
  public void contribute(@Nonnull LiveTemplateContributor.Factory factory) {
    try(Builder builder = factory.newBuilder("csharpoutputCwl", "cwl", "Console.WriteLine($END$);", CSharpLocalize.livetemplatesCwl())) {
      builder.withReformat();

      builder.withContext(CSharpStatementContextType.class, true);
    }

  }
}
