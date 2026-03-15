package consulo.csharp.impl.ide.liveTemplates;

import consulo.annotation.component.ExtensionImpl;
import consulo.csharp.api.localize.CSharpLocalize;
import consulo.csharp.impl.ide.liveTemplates.context.CSharpStatementContextType;
import consulo.language.editor.template.LiveTemplateContributor;
import consulo.localize.LocalizeValue;
import java.lang.Override;
import java.lang.String;

@ExtensionImpl
public class CSharpOutputLiveTemplateContributor implements LiveTemplateContributor {
  @Override
  public String groupId() {
    return "csharpoutput";
  }

  @Override
  public LocalizeValue groupName() {
    return LocalizeValue.localizeTODO("C# Output");
  }

  @Override
  public void contribute(LiveTemplateContributor.Factory factory) {
    try(Builder builder = factory.newBuilder("csharpoutputCwl", "cwl", "Console.WriteLine($END$);", CSharpLocalize.livetemplatesCwl())) {
      builder.withReformat();

      builder.withContext(CSharpStatementContextType.class, true);
    }

  }
}
