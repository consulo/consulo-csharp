/*
 * Copyright 2013-2017 consulo.io
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

package consulo.csharp.impl.ide.actions;

import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ActionImpl;
import consulo.annotation.component.ActionParentRef;
import consulo.annotation.component.ActionRef;
import consulo.annotation.component.ActionRefAnchor;
import consulo.application.AllIcons;
import consulo.application.ReadAction;
import consulo.csharp.api.localize.CSharpLocalize;
import consulo.csharp.impl.ide.refactoring.util.CSharpNameSuggesterUtil;
import consulo.csharp.lang.CSharpFileType;
import consulo.csharp.lang.impl.CSharpAssemblyConstants;
import consulo.csharp.module.extension.CSharpSimpleModuleExtension;
import consulo.dataContext.DataContext;
import consulo.dotnet.module.extension.DotNetModuleExtension;
import consulo.fileTemplate.FileTemplate;
import consulo.fileTemplate.FileTemplateManager;
import consulo.fileTemplate.FileTemplateUtil;
import consulo.ide.IdeView;
import consulo.ide.action.CreateFileFromTemplateAction;
import consulo.ide.action.CreateFileFromTemplateDialog;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiPackage;
import consulo.language.psi.PsiPackageManager;
import consulo.logging.Logger;
import consulo.module.Module;
import consulo.project.Project;
import consulo.ui.annotation.RequiredUIAccess;
import consulo.ui.ex.InputValidatorEx;
import consulo.ui.image.Image;
import consulo.util.lang.ref.SimpleReference;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.fileType.FileType;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import consulo.virtualFileSystem.util.VirtualFileVisitor;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 15.12.13.
 */
@ActionImpl(id = "CSharp.File", parents = @ActionParentRef(value = @ActionRef(id = "NewGroup1"), anchor = ActionRefAnchor.FIRST))
public class CSharpCreateFileAction extends CreateFileFromTemplateAction {
    private static final Logger LOG = Logger.getInstance(CSharpCreateFileAction.class);

    public CSharpCreateFileAction() {
        super(CSharpLocalize.actionCsharpFileText(), CSharpLocalize.actionCsharpFileText(), CSharpFileType.INSTANCE.getIcon());
    }

    @Override
    @SuppressWarnings("unchecked")
    @RequiredReadAction
    protected boolean isAvailable(DataContext dataContext) {
        Map<PsiDirectory, Module> modules = ReadAction.compute(() -> findModules(dataContext));
        if (modules == null || modules.isEmpty()) {
            return false;
        }

        for (Map.Entry<PsiDirectory, Module> entry : modules.entrySet()) {
            PsiDirectory key = entry.getKey();
            Module value = entry.getValue();

            if (checkModule(value, key)) {
                return true;
            }
        }

        return false;
    }

    @RequiredReadAction
    @SuppressWarnings("unchecked")
    private static boolean checkModule(Module module, PsiDirectory directory) {
        DotNetModuleExtension extension = module.getExtension(DotNetModuleExtension.class);
        if (extension != null && extension.isAllowSourceRoots()) {
            PsiPackage aPackage =
                ReadAction.compute(() -> PsiPackageManager.getInstance(module.getProject()).findPackage(directory, DotNetModuleExtension.class));

            if (aPackage == null) {
                return false;
            }
        }
        return module.getExtension(CSharpSimpleModuleExtension.class) != null;
    }

    @RequiredReadAction
    private static Map<PsiDirectory, Module> findModules(DataContext dataContext) {
        Project project = dataContext.getData(Project.KEY);
        if (project == null) {
            return null;
        }

        IdeView view = dataContext.getData(IdeView.KEY);
        if (view == null) {
            return null;
        }

        Map<PsiDirectory, Module> modules = Map.of();
        for (PsiDirectory directory : view.getDirectories()) {
            Module resolved = CSharpCreateFromTemplateHandler.findModuleByPsiDirectory(directory);
            if (resolved == null) {
                continue;
            }

            if (modules.isEmpty()) {
                modules = new HashMap<>();
            }

            modules.put(directory, resolved);
        }
        return modules;
    }

    @Override
    @RequiredReadAction
    protected PsiFile createFile(String name, String templateName, final PsiDirectory dir) {
        FileTemplate template = FileTemplateManager.getInstance(dir.getProject()).getInternalTemplate(templateName);
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("psiDirectory", dir);

            return (PsiFile) FileTemplateUtil.createFromTemplate(template, name, map, dir, getClass().getClassLoader());
        }
        catch (Exception e) {
            LOG.error(e);
            return null;
        }
    }

    @Nullable
    @Override
    protected FileType getFileTypeForModuleResolve() {
        return CSharpFileType.INSTANCE;
    }

    @Override
    @RequiredUIAccess
    protected void buildDialog(Project project, PsiDirectory psiDirectory, CreateFileFromTemplateDialog.Builder builder) {
        Set<String> used = new HashSet<>();
        builder.setValidator(new InputValidatorEx() {
            @Nullable
            @Override
            public String getErrorText(String text) {
                return "Illegal name";
            }

            @RequiredUIAccess
            @Override
            public boolean checkInput(String text) {
                return !CSharpNameSuggesterUtil.isKeyword(text);
            }

            @RequiredUIAccess
            @Override
            public boolean canClose(String text) {
                return checkInput(text);
            }
        });

        addKind(builder, used, "Class", AllIcons.Nodes.Class, "CSharpClass");
        addKind(builder, used, "Interface", AllIcons.Nodes.Interface, "CSharpInterface");
        addKind(builder, used, "Enum", AllIcons.Nodes.Enum, "CSharpEnum");
        addKind(builder, used, "Struct", AllIcons.Nodes.Struct, "CSharpStruct");
        addKind(builder, used, "Attribute", AllIcons.Nodes.Attribute, "CSharpAttribute");
        if (isCreationOfAssemblyFileAvailable(psiDirectory)) {
            addKind(builder, used, "Assembly File", AllIcons.FileTypes.Config, "CSharpAssemblyFile");
        }
        addKind(builder, used, "Empty File", CSharpFileType.INSTANCE.getIcon(), "CSharpFile");

        final CSharpCreateFromTemplateHandler handler = CSharpCreateFromTemplateHandler.getInstance();
        for (FileTemplate template : FileTemplateManager.getInstance(project).getAllTemplates()) {
            if (handler.handlesTemplate(template)) {
                String name = template.getName().replaceFirst("CSharp", "");
                if (!used.add(name)) {
                    name = template.getName();
                }
                addKind(builder, used, name, CSharpFileType.INSTANCE.getIcon(), template.getName());
            }
        }

        builder.setTitle("Create New File");
    }

    private static void addKind(CreateFileFromTemplateDialog.Builder builder, @Nonnull Set<String> used, @Nonnull String kind, @Nullable Image icon, @Nonnull String templateName) {
        used.add(kind);

        builder.addKind(kind, icon, templateName);
    }

    @RequiredReadAction
    @SuppressWarnings("unchecked")
    private static boolean isCreationOfAssemblyFileAvailable(PsiDirectory directory) {
        Module module = directory.getModule();
        if (module != null) {
            DotNetModuleExtension extension = module.getExtension(DotNetModuleExtension.class);
            if (extension != null && extension.isAllowSourceRoots()) {
                return false;
            }
        }
        if (module == null || module.getExtension(CSharpSimpleModuleExtension.class) == null) {
            return false;
        }

        SimpleReference<VirtualFile> ref = SimpleReference.create();
        VirtualFile moduleDir = module.getModuleDir();
        if (moduleDir == null) {
            return false;
        }

        VirtualFileUtil.visitChildrenRecursively(moduleDir, new VirtualFileVisitor<>() {
            @Override
            public boolean visitFile(@Nonnull VirtualFile file) {
                if (file.getName().equals(CSharpAssemblyConstants.FileName)) {
                    ref.set(file);
                    return false;
                }
                return true;
            }
        });

        return ref.get() == null;
    }

    @Override
    protected String getActionName(PsiDirectory psiDirectory, String s, String s2) {
        return "Create C# File";
    }
}