/*
 * Copyright 2013-2026 consulo.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package consulo.csharp.it.library;

import consulo.application.ReadAction;
import consulo.application.WriteAction;
import consulo.content.bundle.Sdk;
import consulo.content.bundle.SdkModificator;
import consulo.content.bundle.SdkTable;
import consulo.csharp.it.resolve.CSharpResolveTesting;
import consulo.csharp.it.resolve.MockSdk;
import consulo.dotnet.assembly.summary.DotNetAddableLibrary;
import consulo.dotnet.assembly.summary.DotNetAddableLibraryLookup;
import consulo.dotnet.assembly.summary.DotNetAddableLibraryProvider;
import consulo.dotnet.assembly.summary.DotNetAssemblySummary;
import consulo.dotnet.assembly.summary.DotNetAssemblySummaryIndex;
import consulo.it.HeadlessProjectExtension;
import consulo.it.HeadlessProjects;
import consulo.application.Application;
import consulo.language.psi.PsiFile;
import consulo.language.util.ModuleUtilCore;
import consulo.module.Module;
import consulo.module.content.ModuleRootManager;
import consulo.module.content.layer.ModifiableRootModel;
import consulo.dotnet.module.extension.DotNetSimpleMutableModuleExtension;
import consulo.mono.dotnet.sdk.MonoSdkType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The libraries a module may add: an assembly in the home of its Mono SDK that the module does not reference
 * ({@code addable/AddableSample.dll}) - what the "using + add reference" fix offers
 *
 * @author VISTALL
 */
@ExtendWith(HeadlessProjectExtension.class)
public class AddableLibraryTest {
    @Test
    public void addableLibraries(HeadlessProjects projects) throws Exception {
        PsiFile file = CSharpResolveTesting.openSource(projects, MockSdk.NET_10, "Use.cs", "class Use { }", List.of());
        Module module = ReadAction.compute(() -> ModuleUtilCore.findModuleForPsiElement(file));
        assertThat(module).isNotNull();

        Path home = projects.newDirectory();
        Path assembly = home.resolve("AddableSample.dll");
        Files.copy(CSharpResolveTesting.resource("addable/AddableSample.dll"), assembly);

        WriteAction.run(() -> {
            Sdk sdk = SdkTable.getInstance().createSdk("addable", MonoSdkType.getInstance());
            SdkModificator modificator = sdk.getSdkModificator();
            modificator.setHomePath(home.toString());
            modificator.commitChanges();
            SdkTable.getInstance().addSdk(sdk);

            ModifiableRootModel rootModel = ModuleRootManager.getInstance(module).getModifiableModel();
            DotNetSimpleMutableModuleExtension<?> dotnet = (DotNetSimpleMutableModuleExtension<?>) rootModel.getExtensionWithoutCheck("mono-dotnet");
            dotnet.getInheritableSdk().set(null, sdk);
            rootModel.commit();
        });

        DotNetAddableLibrary library = ReadAction.compute(() -> addable(module, "AddableSample"));
        assertThat(library).as("the assembly of the SDK, not referenced by the module").isNotNull();
        assertThat(library.getFile()).isEqualTo(assembly);

        // the summary is computed in the background on the first lookup; here directly
        assertThat(DotNetAssemblySummaryIndex.getInstance().getSummary(assembly)).isNotNull();

        ReadAction.run(() -> {
            DotNetAddableLibraryLookup lookup = DotNetAddableLibraryLookup.getInstance();
            assertThat(lookup.findTypes(module, "Widget")).singleElement().satisfies(match -> {
                assertThat(match.library().getName()).isEqualTo("AddableSample");
                assertThat(match.type().namespace()).isEqualTo("Addable.Api");
            });
            assertThat(lookup.findTypes(module, "MarkerAttribute")).singleElement()
                .satisfies(match -> assertThat(match.type().is(DotNetAssemblySummary.ATTRIBUTE)).isTrue());
            assertThat(lookup.findExtensionMethods(module, "Count")).singleElement()
                .satisfies(match -> assertThat(match.namespace()).isEqualTo("Addable.Api"));
            assertThat(lookup.findTypes(module, "Unknown")).isEmpty();
        });

        // added: no longer offered
        WriteAction.run(() -> library.addTo(module));
        assertThat(ReadAction.compute(() -> addable(module, "AddableSample"))).isNull();
        ReadAction.run(() -> assertThat(DotNetAddableLibraryLookup.getInstance().findTypes(module, "Widget")).isEmpty());
    }

    private static DotNetAddableLibrary addable(Module module, String name) {
        List<DotNetAddableLibrary> libraries = new ArrayList<>();
        for (DotNetAddableLibraryProvider provider : Application.get().getExtensionList(DotNetAddableLibraryProvider.class)) {
            libraries.addAll(provider.getAddableLibraries(module));
        }
        return libraries.stream().filter(library -> library.getName().equals(name)).findFirst().orElse(null);
    }
}
