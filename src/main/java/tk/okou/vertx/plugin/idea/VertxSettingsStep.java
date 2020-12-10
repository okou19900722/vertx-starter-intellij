package tk.okou.vertx.plugin.idea;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.util.ui.JBUI;
import javafx.scene.control.CheckBox;

import javax.swing.*;
import java.awt.*;

public class VertxSettingsStep extends ModuleWizardStep {
    private JPanel panel;
    public VertxSettingsStep(SettingsStep settingsStep, VertxModuleBuilder builder) {
        panel = new JPanel(new GridBagLayout());
//        CheckboxGroup
        ButtonGroup group = new ButtonGroup();
        JCheckBox onlineCheckbox = new JBCheckBox("Create from start.vertx.io", true);
        JCheckBox offlineCheckbox = new JBCheckBox("Create from Local template", true);
        group.add(onlineCheckbox);
        group.add(offlineCheckbox);
        JPanel p1 = new JPanel(new GridLayout(1, 2, 0, 0));
        p1.add(onlineCheckbox);
        p1.add(offlineCheckbox);
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
