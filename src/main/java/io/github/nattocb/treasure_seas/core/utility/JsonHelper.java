package io.github.nattocb.treasure_seas.core.utility;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

public class JsonHelper {

    private static final Gson GSON = new GsonBuilder().create();

    public static String toStableString(JsonElement jsonElement) {
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonObject sortedObject = new JsonObject();
            // 将 JsonObject 的字段按字典序排序
            jsonObject.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .forEachOrdered(e -> sortedObject.add(e.getKey(), e.getValue()));
            return GSON.toJson(sortedObject);
        } else {
            // 如果不是 JsonObject，就直接返回字符串表示
            return GSON.toJson(jsonElement);
        }
    }

}
