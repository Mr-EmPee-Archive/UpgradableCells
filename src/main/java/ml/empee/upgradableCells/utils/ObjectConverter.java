package ml.empee.upgradableCells.utils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Converts complex objects into raw one
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ObjectConverter {

  private static final Gson gson = new GsonBuilder().create();
  private static final Pattern LOCATION_PATTERN = Pattern.compile(
      ".+:-?\\d+(\\.\\d+)?:-?\\d+(\\.\\d+)?:-?\\d+(\\.\\d+)?"
  );

  /**
   * Parse a collection to a string
   */
  public static <T> String parseCollection(Collection<T> collection, Function<T, String> mapper) {
    return gson.toJson(
        collection.stream()
            .map(mapper)
            .toList()
    );
  }

  /**
   * Parse a string to a collection
   */
  public static <T> ArrayList<T> parseCollection(String raw, Function<String, T> mapper) {
    return Arrays.stream(gson.fromJson(raw, String[].class))
        .map(mapper)
        .collect(Collectors.toCollection(ArrayList::new));
  }

  /**
   * Parse a map to a string
   */
  public static <T, K> String parseMap(Map<T, K> map, Function<T, String> keyMapper, Function<K, String> valueMapper) {
    Map<String, String> encodedMap = new HashMap<>();
    map.forEach((key, value) -> {
      encodedMap.put(keyMapper.apply(key), valueMapper.apply(value));
    });

    return gson.toJson(encodedMap);
  }

  /**
   * Parse a map from a string
   */
  public static <T, K> Map<T, K> parseMap(String raw, Function<String, T> keyMapper, Function<String, K> valueMapper) {
    Map<String, String> encodedMap = gson.fromJson(raw, Map.class);
    Map<T, K> map = new HashMap<>();
    encodedMap.forEach((key, value) -> {
      map.put(keyMapper.apply(key), valueMapper.apply(value));
    });

    return map;
  }

  /**
   * Parse a location from a string
   *
   * @param rawLoc format "world:x:y:z"
   */
  public static Location parseLocation(String rawLoc) {
    if (rawLoc == null || !LOCATION_PATTERN.matcher(rawLoc).matches()) {
      throw new RuntimeException("The location '" + rawLoc + "' must match 'world:x:y:z'");
    }

    String[] coordinates = rawLoc.split(":");
    World world = Bukkit.getWorld(coordinates[0]);
    if (world == null) {
      throw new RuntimeException("Unable to find world " + coordinates[0]);
    }

    return new Location(
        world,
        Double.parseDouble(coordinates[1]),
        Double.parseDouble(coordinates[2]),
        Double.parseDouble(coordinates[3])
    );
  }

  /**
   * Parse a location into a string
   *
   * @return world:x:y:z
   */
  public static String parseLocation(Location loc) {
    return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ();
  }

  /**
   * Parse a date-time from a string
   *
   * @param rawTime format "2007-12-03T10:15:30"
   */
  public static LocalDateTime parseTime(String rawTime) {
    return LocalDateTime.parse(rawTime);
  }

  /**
   * Parse a date-time to a string
   *
   * @return "2007-12-03T10:15:30"
   */
  public static String parseTime(LocalDateTime time) {
    return time.toString();
  }

  /**
   * A method to serialize an item to a Base64 string.
   */
  public static String parseItem(ItemStack item) throws IOException {
    return parseInventory(new ItemStack[]{item});
  }

  /**
   * A method to get an item from an encoded Base64 string.
   */
  public static ItemStack parseItem(String item) throws IOException {
    return parseInventory(item)[0];
  }

  /**
   * A method to serialize an array of items to a Base64 string.
   */
  public static String parseInventory(ItemStack[] items) throws IOException {
    try (
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ObjectOutput dataOutput = new BukkitObjectOutputStream(outputStream)
    ) {

      dataOutput.writeInt(items.length);

      for (ItemStack item : items) {
        dataOutput.writeObject(item);
      }

      // Serialize that array
      dataOutput.flush();
      outputStream.flush();
      return Base64Coder.encodeLines(outputStream.toByteArray());
    }
  }

  /**
   * A method to get an array of items from an encoded Base64 string.
   */
  public static ItemStack[] parseInventory(String rawItems) throws IOException {
    if (rawItems.isEmpty()) {
      return new ItemStack[0];
    }

    try (
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(rawItems));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)
    ) {
      ItemStack[] items = new ItemStack[dataInput.readInt()];

      // Read the serialized inventory
      for (int i = 0; i < items.length; i++) {
        items[i] = (ItemStack) dataInput.readObject();
      }

      return items;
    } catch (ClassNotFoundException e) {
      throw new IOException("Unable to decode class type.", e);
    }
  }

}

