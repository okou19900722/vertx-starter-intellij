package tk.okou.vertx.plugin.idea;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class VertxModuleBuilder extends ModuleBuilder {
    private VertxModuleType moduleType;
    ComboBoxModel<String> versionsModel;
    ComboBoxModel<String> languagesModel;
    ComboBoxModel<String> buildToolsModel;
    String groupId = "com.example";
    String artifactId = "starter";
    public VertxModuleBuilder() {
        versionsModel = this.createBoxModel("3.9.4", "3.8.5", "4.0.0.CR2", "4.0.0-SNAPSHOT");
        languagesModel = this.createBoxModel("Java", "Kotlin");
        buildToolsModel = this.createBoxModel("Maven", "Gradle");
    }
    private ComboBoxModel<String> createBoxModel(String... items) {
        return new DefaultComboBoxModel<>(items);
    }
    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) throws ConfigurationException {
//        String contentEntryPath = this.getContentEntryPath();
//        if (contentEntryPath == null) {
//            return;
//        }
//        ContentEntry contentEntry = this.doAddContentEntry(modifiableRootModel);
//        if (contentEntry == null) {
//            return;
//        }
//        VirtualFile sourceRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(FileUtil.toSystemIndependentName(contentEntryPath));
//        contentEntry.addSourceFolder(sourceRoot, false);

    }

    @Nullable
    @Override
    public ModuleWizardStep modifyProjectTypeStep(@NotNull SettingsStep settingsStep) {
        return new VertxSettingsStep(settingsStep, this);
    }

    @Override
    public ModuleType<VertxModuleBuilder> getModuleType() {
        return ModuleTypeManager.getInstance().findByID("Vertx.Starter");
//        return this.moduleType;
    }
}
