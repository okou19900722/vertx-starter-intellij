package tk.okou.vertx.plugin.idea.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

public class Metadata {
    private Default defaults;
    private VertxVersion[] versions;
    private Stack[] stack;
    private String[] buildTools;
    private String[] languages;
    private String[] jdkVersions;
    private Stack[] vertxDependencies;
    private String[] vertxVersions;

    public Metadata(JsonObject json) {
        JsonObject defaults = json.getJsonObject("defaults");
        this.defaults = new Default(defaults);
        this.versions = create(json, "versions", VertxVersion[]::new, VertxVersion::new, JsonArray::getJsonObject);
        this.stack = create(json, "stack", Stack[]::new, Stack::new, JsonArray::getJsonObject);
        this.buildTools = Metadata.create(json, "buildTools", String[]::new, Function.identity(), JsonArray::getString);
        this.languages = Metadata.create(json, "languages", String[]::new, Function.identity(), JsonArray::getString);
        this.jdkVersions = Metadata.create(json, "jdkVersions", String[]::new, Function.identity(), JsonArray::getString);
        this.vertxDependencies = create(json, "vertxDependencies", Stack[]::new, Stack::new, JsonArray::getJsonObject);
        this.vertxVersions = Metadata.create(json, "vertxVersions", String[]::new, Function.identity(), JsonArray::getString);
    }

    static <T, U> T[] create(JsonObject json, String arrayName, IntFunction<T[]> creator, Function<U, T> modelCreator, BiFunction<JsonArray, Integer, U> getter) {
        JsonArray array = json.getJsonArray(arrayName);
        T[] result = creator.apply(array.size());
        for (int i = 0; i < array.size(); i++) {
            result[i] = modelCreator.apply(getter.apply(array, i));
        }
        return result;
    }

    public Default getDefaults() {
        return defaults;
    }

    public void setDefaults(Default defaults) {
        this.defaults = defaults;
    }

    public VertxVersion[] getVersions() {
        return versions;
    }

    public void setVersions(VertxVersion[] versions) {
        this.versions = versions;
    }

    public Stack[] getStack() {
        return stack;
    }

    public void setStack(Stack[] stack) {
        this.stack = stack;
    }

    public String[] getBuildTools() {
        return buildTools;
    }

    public void setBuildTools(String[] buildTools) {
        this.buildTools = buildTools;
    }

    public String[] getLanguages() {
        return languages;
    }

    public void setLanguages(String[] languages) {
        this.languages = languages;
    }

    public String[] getJdkVersions() {
        return jdkVersions;
    }

    public void setJdkVersions(String[] jdkVersions) {
        this.jdkVersions = jdkVersions;
    }

    public Stack[] getVertxDependencies() {
        return vertxDependencies;
    }

    public void setVertxDependencies(Stack[] vertxDependencies) {
        this.vertxDependencies = vertxDependencies;
    }

    public String[] getVertxVersions() {
        return vertxVersions;
    }

    public void setVertxVersions(String[] vertxVersions) {
        this.vertxVersions = vertxVersions;
    }

    @Override
    public String toString() {
        return "Metadata{" +
                "defaults=" + defaults +
                ", versions=" + Arrays.toString(versions) +
                ", stack=" + Arrays.toString(stack) +
                ", buildTools=" + Arrays.toString(buildTools) +
                ", languages=" + Arrays.toString(languages) +
                ", jdkVersions=" + Arrays.toString(jdkVersions) +
                ", vertxDependencies=" + Arrays.toString(vertxDependencies) +
                ", vertxVersions=" + Arrays.toString(vertxVersions) +
                '}';
    }
}
