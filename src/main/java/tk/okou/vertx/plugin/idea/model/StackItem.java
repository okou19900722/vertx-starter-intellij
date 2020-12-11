package tk.okou.vertx.plugin.idea.model;

import io.vertx.core.json.JsonObject;

public class StackItem {
    private String artifactId;
    private String name;
    private String description;

    public StackItem(JsonObject json) {
        this.artifactId = json.getString("artifactId");
        this.name = json.getString("name");
        this.description = json.getString("description");
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "StackItem{" +
                "artifactId='" + artifactId + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
