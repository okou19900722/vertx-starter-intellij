package tk.okou.vertx.plugin.idea;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class VertxSettingsStep extends ModuleWizardStep {
    private JPanel panel;
    private WizardContext content;
    public VertxSettingsStep(SettingsStep settingsStep, VertxModuleBuilder builder) {
        this.content = settingsStep.getContext();
        panel = new JPanel(new GridBagLayout());
//        CheckboxGroup
        ButtonGroup group = new ButtonGroup();
        JCheckBox onlineCheckbox = new JBCheckBox("Online", false);
        JCheckBox offlineCheckbox = new JBCheckBox("Offline", true);

        onlineCheckbox.addActionListener(e -> {
            ProgressManager.getInstance().run(new Task.Modal(content.getProject(), "Loading Metadata From Server", false) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    indicator.setText("loading metadata from start.vertx.io");
                    try {
                        builder.initFromServer().toCompletionStage().toCompletableFuture().get();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        });

        group.add(onlineCheckbox);
        group.add(offlineCheckbox);
        JPanel p1 = new JPanel(new BorderLayout());
        p1.add(onlineCheckbox, BorderLayout.WEST);
        p1.add(offlineCheckbox, BorderLayout.CENTER);

        settingsStep.addSettingsField("Mode", p1);
        settingsStep.addSettingsField("Version", new ComboBox<>(builder.versionsModel));
        settingsStep.addSettingsField("Language", new ComboBox<>(builder.languagesModel));
        settingsStep.addSettingsField("Build Tool", new ComboBox<>(builder.buildToolsModel));
        settingsStep.addSettingsField("Group Id", new JBTextField(builder.groupId));
        settingsStep.addSettingsField("Artifact Id", new JBTextField(builder.artifactId));

    }



    @Override
    public JComponent getComponent() {
        return panel;
    }

    @Override
    public void updateDataModel() {

    }
}
