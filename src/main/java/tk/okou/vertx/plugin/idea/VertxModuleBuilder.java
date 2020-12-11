package tk.okou.vertx.plugin.idea;

import com.intellij.ide.util.projectWizard.*;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.StdModuleTypes;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.projectRoots.SdkType;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ui.configuration.JdkComboBox;
import com.intellij.openapi.roots.ui.configuration.projectRoot.ProjectSdksModel;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBTextField;
import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tk.okou.vertx.plugin.idea.generator.Generator;
import tk.okou.vertx.plugin.idea.generator.LocalGenerator;
import tk.okou.vertx.plugin.idea.generator.RemoteGenerator;
import tk.okou.vertx.plugin.idea.model.Metadata;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

public class VertxModuleBuilder extends ModuleBuilder {
    private final VertxModuleType moduleType;
    DefaultComboBoxModel<String> versionsModel;
    DefaultComboBoxModel<String> languagesModel;
    DefaultComboBoxModel<String> buildToolsModel;
    DefaultComboBoxModel<String> jdkVersionsModel;
    Metadata metadata;
    Metadata localMetadata;
    Metadata remoteMetadata;

    JBTextField groupIdInput = new JBTextField("com.example.vertx");
    JBTextField artifactIdInput = new JBTextField("vertx-starter");
    WebClient client;
    JCheckBox onlineCheckbox = new JBCheckBox("Online", false);
    JCheckBox offlineCheckbox = new JBCheckBox("Offline", true);
    public VertxModuleBuilder(VertxModuleType moduleType) {
        this.moduleType = moduleType;
        this.client = WebClient.create(moduleType.vertx);
        versionsModel = this.createBoxModel("4.0.0.CR2", "3.9.4", "3.8.5", "4.0.0-SNAPSHOT");
        languagesModel = this.createBoxModel("Java", "Kotlin");
        buildToolsModel = this.createBoxModel("Maven", "Gradle");
        jdkVersionsModel = this.createBoxModel("13");

        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("META-INF/metadata.json");
            if (inputStream != null) {
                byte[] bytes = new byte[inputStream.available()];
                //noinspection ResultOfMethodCallIgnored
                inputStream.read(bytes);
                JsonObject json = Buffer.buffer(bytes).toJsonObject();
                this.localMetadata = new Metadata(json);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        this.setMetadata(this.localMetadata);
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
        this.artifactIdInput.setText(metadata.getDefaults().getArtifactId());
        this.groupIdInput.setText(metadata.getDefaults().getGroupId());
        buildToolsModel.removeAllElements();
        languagesModel.removeAllElements();
        versionsModel.removeAllElements();
        jdkVersionsModel.removeAllElements();
        for (int i = 0; i < this.metadata.getBuildTools().length; i++) {
            buildToolsModel.addElement(this.metadata.getBuildTools()[i]);
        }
        for (int i = 0; i < this.metadata.getLanguages().length; i++) {
            languagesModel.addElement(this.metadata.getLanguages()[i]);
        }
        for (int i = 0; i < this.metadata.getVertxVersions().length; i++) {
            versionsModel.addElement(this.metadata.getVertxVersions()[i]);
        }
        for (int i = 0; i < this.metadata.getJdkVersions().length; i++) {
            jdkVersionsModel.addElement(this.metadata.getJdkVersions()[i]);
        }
        buildToolsModel.setSelectedItem(metadata.getDefaults().getBuildTool());
        languagesModel.setSelectedItem(metadata.getDefaults().getLanguage());
        versionsModel.setSelectedItem(metadata.getDefaults().getVertxVersion());
        jdkVersionsModel.setSelectedItem(metadata.getDefaults().getJdkVersion());

    }

    private boolean init = false;
    void initFromServer() throws ExecutionException, InterruptedException {
        if (init) {
            if (this.remoteMetadata != null) {
                this.setMetadata(this.remoteMetadata);
            }
        }
        init = true;

        client.getAbs("https://start.vertx.io/metadata")
                .timeout(60000)
                .as(BodyCodec.jsonObject())
                .send()
                .map(response -> {
                    if (response.statusCode() == 200){
                        return response.body();
                    } else {
                        throw new RuntimeException("not 200");
                    }
                })
                .compose(object -> {
                    this.remoteMetadata = new Metadata(object);
                    this.setMetadata(this.remoteMetadata);
                    return Future.<Void>succeededFuture();
                }).toCompletionStage().toCompletableFuture().get();
    }

    private DefaultComboBoxModel<String> createBoxModel(String... items) {
        return new DefaultComboBoxModel<>(items);
    }
    @Override
    public void setupRootModel(ModifiableRootModel modifiableRootModel) {
        String contentEntryPath = this.getContentEntryPath();
        if (contentEntryPath == null) {
            return;
        }
        ContentEntry contentEntry = this.doAddContentEntry(modifiableRootModel);
        if (contentEntry == null) {
            return;
        }
        VirtualFile sourceRoot = LocalFileSystem.getInstance().refreshAndFindFileByPath(FileUtil.toSystemIndependentName(contentEntryPath));
        contentEntry.addSourceFolder(sourceRoot, false);

        Generator generator;
        String text;
        if (onlineCheckbox.isSelected()) {
            generator = new RemoteGenerator(moduleType.vertx, client);
            text = "Create Project From start.vertx.io";
        } else {
            generator = new LocalGenerator();
            text = "Create Project By Template";
        }

        ProgressManager.getInstance().run(new Task.Modal(modifiableRootModel.getProject(), "Create Project", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.start();
                indicator.setText(text);
                try {
                    generator.generate(
                            modifiableRootModel.getProject().getBasePath(),
                            groupIdInput.getText(),
                            artifactIdInput.getText(),
                            (String)buildToolsModel.getSelectedItem(),
                            (String)languagesModel.getSelectedItem(),
                            (String)versionsModel.getSelectedItem(),
                            (String)jdkVersionsModel.getSelectedItem()
                    );
                } catch (Exception e) {
                    throw new RuntimeException("create failed", e);
                }
            }
        });
    }
    @Nullable
    @Override
    public ModuleWizardStep modifyProjectTypeStep(@NotNull SettingsStep settingsStep) {
//        return new VertxSettingsStep(settingsStep, this);
        WizardContext content = settingsStep.getContext();
        ButtonGroup group = new ButtonGroup();
        onlineCheckbox.addActionListener(e -> ProgressManager.getInstance().run(new Task.Modal(content.getProject(), "Loading Metadata From Server", false) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText("loading metadata from start.vertx.io");
                try {
                    initFromServer();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }));
        offlineCheckbox.addActionListener(e -> this.setMetadata(this.localMetadata));

        group.add(onlineCheckbox);
        group.add(offlineCheckbox);
        JPanel p1 = new JPanel(new BorderLayout());
        p1.add(onlineCheckbox, BorderLayout.WEST);
        p1.add(offlineCheckbox, BorderLayout.CENTER);

        settingsStep.addSettingsField("Mode", p1);
        settingsStep.addSettingsField("Version", new ComboBox<>(versionsModel));
        settingsStep.addSettingsField("Language", new ComboBox<>(languagesModel));
        settingsStep.addSettingsField("Build Tool", new ComboBox<>(buildToolsModel));
        settingsStep.addSettingsField("Jdk Version", new ComboBox<>(jdkVersionsModel));
        settingsStep.addSettingsField("Group Id", groupIdInput);
        settingsStep.addSettingsField("Artifact Id", artifactIdInput);

//        settingsStep.addExpertField("test", new JBList());
        JBList<String> list = new JBList<>();
        settingsStep.addSettingsComponent(list);

        return super.modifyProjectTypeStep(settingsStep);
    }

    @Nullable
    @Override
    public ModuleWizardStep modifySettingsStep(@NotNull SettingsStep settingsStep) {
        final ModuleNameLocationSettings nameLocationSettings = settingsStep.getModuleNameLocationSettings();
        if (nameLocationSettings != null) {
            nameLocationSettings.setModuleName(artifactIdInput.getText());
        }
        return super.modifySettingsStep(settingsStep);
    }

    @Override
    public ModuleType<?> getModuleType() {
//        return ModuleTypeManager.getInstance().findByID("Vertx.Starter");
        return this.moduleType;
//        return StdModuleTypes.JAVA;
    }
}
