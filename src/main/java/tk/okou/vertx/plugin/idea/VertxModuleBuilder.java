package tk.okou.vertx.plugin.idea;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.jackson.DatabindCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tk.okou.vertx.plugin.idea.model.Metadata;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class VertxModuleBuilder extends ModuleBuilder {
    private VertxModuleType moduleType;
    DefaultComboBoxModel<String> versionsModel;
    DefaultComboBoxModel<String> languagesModel;
    DefaultComboBoxModel<String> buildToolsModel;
    String groupId = "com.example";
    String artifactId = "starter";
    Metadata metadata;
    Metadata localMetadata;
    Metadata remoteMetadata;
    public VertxModuleBuilder(VertxModuleType moduleType) {
        this.moduleType = moduleType;
        versionsModel = this.createBoxModel("4.0.0.CR2", "3.9.4", "3.8.5", "4.0.0-SNAPSHOT");
        languagesModel = this.createBoxModel("Java", "Kotlin");
        buildToolsModel = this.createBoxModel("Maven", "Gradle");

        try {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("META-INF/metadata.json");
            if (inputStream != null) {
                byte[] bytes = new byte[inputStream.available()];
                inputStream.read(bytes);
                JsonObject json = Buffer.buffer(bytes).toJsonObject();
                this.localMetadata = new Metadata(json);
                System.out.println(localMetadata);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        this.setMetadata(this.localMetadata);
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
        buildToolsModel.removeAllElements();
        languagesModel.removeAllElements();
        versionsModel.removeAllElements();
        for (int i = 0; i < this.metadata.getBuildTools().length; i++) {
            buildToolsModel.addElement(this.metadata.getBuildTools()[i]);
        }
        for (int i = 0; i < this.metadata.getLanguages().length; i++) {
            languagesModel.addElement(this.metadata.getLanguages()[i]);
        }
        for (int i = 0; i < this.metadata.getVertxVersions().length; i++) {
            versionsModel.addElement(this.metadata.getVertxVersions()[i]);
        }
    }

    private boolean init = false;
    Future<Void> initFromServer() {
        if (init) {
            if (this.remoteMetadata != null) {
                this.setMetadata(this.remoteMetadata);
            }
            return Future.succeededFuture();
        }
        init = true;
        Vertx vertx = Vertx.vertx();
        HttpClient client = vertx.createHttpClient();
        RequestOptions options = new RequestOptions();
        options.setAbsoluteURI("https://start.vertx.io/metadata");
        options.setMethod(HttpMethod.GET);
        return client
                .request(options)
                .compose(HttpClientRequest::send)
                .compose(response -> {
                    if (response.statusCode() == 200) {
                        return  response.body();
                    } else {
                        return Future.failedFuture("not 200");
                    }
                })
                .compose(buffer -> {
                    JsonObject object = buffer.toJsonObject();
                    this.remoteMetadata = new Metadata(object);
                    this.setMetadata(this.remoteMetadata);
                    return Future.succeededFuture();
                });
    }

    private DefaultComboBoxModel<String> createBoxModel(String... items) {
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
//        return new VertxSettingsStep(settingsStep, this);
        WizardContext content = settingsStep.getContext();
        ButtonGroup group = new ButtonGroup();
        JCheckBox onlineCheckbox = new JBCheckBox("Online", false);
        JCheckBox offlineCheckbox = new JBCheckBox("Offline", true);

        onlineCheckbox.addActionListener(e -> {
            ProgressManager.getInstance().run(new Task.Modal(content.getProject(), "Loading Metadata From Server", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setText("loading metadata from start.vertx.io");
                    try {
                        initFromServer().toCompletionStage().toCompletableFuture().get();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        offlineCheckbox.addActionListener(e -> {
            this.setMetadata(this.localMetadata);
        });

        group.add(onlineCheckbox);
        group.add(offlineCheckbox);
        JPanel p1 = new JPanel(new BorderLayout());
        p1.add(onlineCheckbox, BorderLayout.WEST);
        p1.add(offlineCheckbox, BorderLayout.CENTER);

        settingsStep.addSettingsField("Mode", p1);
        settingsStep.addSettingsField("Version", new ComboBox<>(versionsModel));
        settingsStep.addSettingsField("Language", new ComboBox<>(languagesModel));
        settingsStep.addSettingsField("Build Tool", new ComboBox<>(buildToolsModel));
        settingsStep.addSettingsField("Group Id", new JBTextField(groupId));
        settingsStep.addSettingsField("Artifact Id", new JBTextField(artifactId));

        return super.modifyProjectTypeStep(settingsStep);
    }

    @Override
    public ModuleType<VertxModuleBuilder> getModuleType() {
        return ModuleTypeManager.getInstance().findByID("Vertx.Starter");
//        return this.moduleType;
    }
}
