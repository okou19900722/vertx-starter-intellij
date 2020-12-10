package tk.okou.vertx.plugin.idea;

import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.jps.model.module.JpsModuleSourceRootType;

import javax.swing.*;

public class VertxModuleType extends ModuleType<VertxModuleBuilder> {
    static Icon MODULE_ICON = IconLoader.getIcon("/META-INF/icons/vertx.svg");

    public VertxModuleType() {
        super("Vertx.Starter");
    }

    @NotNull
    @Override
    public VertxModuleBuilder createModuleBuilder() {
        return new VertxModuleBuilder();
    }

    @NotNull
    @Override
    public String getName() {
        return "Vert.x";
    }

    @NotNull
    @Override
    public String getDescription() {
        return "Vert.x Starter";
    }

    @Override
    public Icon getNodeIcon(boolean isOpened) {
        return MODULE_ICON;
    }

    @Override
    public boolean isMarkInnerSupportedFor(JpsModuleSourceRootType type) {
        return true;
    }
}
