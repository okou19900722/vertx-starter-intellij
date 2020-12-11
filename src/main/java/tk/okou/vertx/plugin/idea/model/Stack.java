package tk.okou.vertx.plugin.idea.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;

public class Stack {
    private String code;
    private String category;
    private String description;
    private StackItem[] items;

    public Stack(JsonObject json) {
        this.code = json.getString("code");
        this.category = json.getString("category");
        this.description = json.getString("description");
        this.items = Metadata.create(json, "items", StackItem[]::new, StackItem::new, JsonArray::getJsonObject);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public StackItem[] getItems() {
        return items;
    }

    public void setItems(StackItem[] items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Stack{" +
                "code='" + code + '\'' +
                ", category='" + category + '\'' +
                ", description='" + description + '\'' +
                ", items=" + Arrays.toString(items) +
                '}';
    }
}
