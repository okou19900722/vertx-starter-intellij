package tk.okou.vertx.plugin.idea.model;

import io.vertx.core.json.JsonObject;

public class Default {
    private String groupId;
    private String artifactId;
    private String language;
    private String buildTool;
    private String vertxVersion;
    private String archiveFormat;
    private String jdkVersion;

    public Default (JsonObject json) {
        this.groupId = json.getString("groupId");
        this.artifactId = json.getString("artifactId");
        this.language = json.getString("language");
        this.buildTool = json.getString("buildTool");
        this.vertxVersion = json.getString("vertxVersion");
        this.archiveFormat = json.getString("archiveFormat");
        this.jdkVersion = json.getString("jdkVersion");
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getBuildTool() {
        return buildTool;
    }

    public void setBuildTool(String buildTool) {
        this.buildTool = buildTool;
    }

    public String getVertxVersion() {
        return vertxVersion;
    }

    public void setVertxVersion(String vertxVersion) {
        this.vertxVersion = vertxVersion;
    }

    public String getArchiveFormat() {
        return archiveFormat;
    }

    public void setArchiveFormat(String archiveFormat) {
        this.archiveFormat = archiveFormat;
    }

    public String getJdkVersion() {
        return jdkVersion;
    }

    public void setJdkVersion(String jdkVersion) {
        this.jdkVersion = jdkVersion;
    }

}
