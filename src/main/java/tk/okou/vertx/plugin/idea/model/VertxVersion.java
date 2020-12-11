package tk.okou.vertx.plugin.idea.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.function.Function;

public class VertxVersion {
    private String number;
    private String[] exclusions;

    public VertxVersion(JsonObject json) {
        this.number = json.getString("number");
        JsonArray array = json.getJsonArray("exclusions");
        this.exclusions = Metadata.create(json, "exclusions", String[]::new, Function.identity(), JsonArray::getString);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String[] getExclusions() {
        return exclusions;
    }

    public void setExclusions(String[] exclusions) {
        this.exclusions = exclusions;
    }

    @Override
    public String toString() {
        return "VertxVersion{" +
                "number='" + number + '\'' +
                ", exclusions=" + Arrays.toString(exclusions) +
                '}';
    }
}
