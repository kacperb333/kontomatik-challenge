package com.kontomatik.lib;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

public class GsonUtils {
  private static final Gson gson = new Gson();

  public static String extractString(JsonElement element, String elementName) {
    return element.getAsJsonObject()
      .getAsJsonPrimitive(elementName)
      .getAsString();
  }

  public static Map<String, JsonElement> extractMap(JsonElement element, String... path) {
    JsonObject current = element.getAsJsonObject();
    for (String pathElement : path) {
      current = current.getAsJsonObject(pathElement);
    }
    return current.asMap();
  }

  public static JsonObject parseToObject(String json) {
    return gson.fromJson(json, JsonObject.class);
  }

  public static String toJson(Object object) {
    return gson.toJson(object);
  }
}
