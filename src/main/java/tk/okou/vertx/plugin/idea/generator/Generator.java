package tk.okou.vertx.plugin.idea.generator;


public interface Generator {
    void generate(String path, String groupId, String artifactId, String buildTool, String language, String vertxVersion, String jdkVersion);
}
