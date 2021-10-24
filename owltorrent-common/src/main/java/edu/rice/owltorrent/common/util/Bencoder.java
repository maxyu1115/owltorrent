package edu.rice.owltorrent.common.util;

import edu.rice.owltorrent.common.util.Exceptions.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author shijie, yuchen
 *     <p>Reference:
 *     https://github.com/BrightStarry/zx-bt/blob/7bbcf01d613cdb8afc5940b4b6641dcf3996ded6/zx-bt-spider/src/main/java/com/zx/bt/spider/util/Bencode.java
 */
public class Bencoder {
  private static final String LOG = "[Bencode]";

  private Charset charset = StandardCharsets.UTF_8;

  @SuppressWarnings("unchecked")
  private BiFunction<byte[], Integer, MethodResult>[] functions = new BiFunction[4];

  private byte stringTypeSeparator;

  private final String intTypePre = "i";
  private final String listTypePre = "l";
  private final String dictTypePre = "d";
  private final String typeSuf = "e";

  public Bencoder() {
    init();
  }

  public void init() {
    stringTypeSeparator = ":".getBytes()[0];

    functions[0] = (bytes, start) -> decodeDict(bytes, start);

    functions[1] = (bytes, start) -> decodeString(bytes, start);

    functions[2] = (bytes, start) -> decodeInt(bytes, start);

    functions[3] = (bytes, start) -> decodeList(bytes, start);
  }

  public MethodResult<byte[]> decodeString(byte[] bytes, int start) {
    if (start >= bytes.length || bytes[start] < '0' || bytes[start] > '9')
      throw new BTException(LOG + "String:" + start);

    int separatorIndex = ArrayUtils.indexOf(bytes, stringTypeSeparator, start);
    if (separatorIndex == -1) throw new BTException(LOG + "String:" + start);

    int strLen;
    try {
      strLen =
          Integer.parseInt(new String(ArrayUtils.subarray(bytes, start, separatorIndex), charset));
    } catch (NumberFormatException e) {
      throw new BTException(LOG + "String:" + start);
    }
    if (strLen < 0) throw new BTException(LOG + "String:" + start);

    int endIndex = separatorIndex + strLen + 1;
    if (separatorIndex > bytes.length) throw new BTException(LOG + "String:" + start);
    return new MethodResult<>(ArrayUtils.subarray(bytes, separatorIndex + 1, endIndex), endIndex);
  }

  public MethodResult<Long> decodeInt(byte[] bytes, int start) {
    if (start >= bytes.length || (char) bytes[start] != intTypePre.charAt(0))
      throw new BTException(LOG + "Int:" + start);

    int endIndex = ArrayUtils.indexOf(bytes, typeSuf.getBytes(charset)[0], start + 1);
    if (endIndex == -1) throw new BTException(LOG + "Int:" + start);
    long result;
    try {
      result = Long.parseLong(new String(ArrayUtils.subarray(bytes, start + 1, endIndex)));
    } catch (NumberFormatException e) {
      throw new BTException(LOG + "Int:" + start);
    }
    return new MethodResult<>(result, ++endIndex);
  }

  public MethodResult<List<Object>> decodeList(byte[] bytes, int start) {
    List<Object> result = new ArrayList<>();
    if (start >= bytes.length || (char) bytes[start] != listTypePre.charAt(0))
      throw new BTException(LOG + "List:" + start);

    int i = start + 1;
    for (; i < bytes.length; ) {

      if (bytes[i] == typeSuf.getBytes(charset)[0]) break;

      MethodResult methodResult = decodeAny(bytes, i);

      i = methodResult.index;

      result.add(methodResult.value);
    }
    if (i == bytes.length) throw new BTException(LOG + "List:" + start);

    return new MethodResult<>(result, ++i);
  }

  public MethodResult<Map<String, Object>> decodeDict(byte[] bytes, int start) {
    Map<String, Object> result = new LinkedHashMap<>();
    if (start >= bytes.length || bytes[start] != dictTypePre.charAt(0))
      throw new BTException(LOG + "Dict:" + start);

    int i = start + 1;
    for (; i < bytes.length; ) {
      String item = new String(new byte[] {bytes[i]}, charset);

      if (item.equals(typeSuf)) break;

      if (!StringUtils.isNumeric(item)) throw new BTException(LOG + "Dict:" + start);

      MethodResult<byte[]> keyMethodResult = decodeString(bytes, i);

      i = keyMethodResult.index;

      MethodResult valueMethodResult = decodeAny(bytes, i);

      i = valueMethodResult.index;

      result.put(new String(keyMethodResult.value, charset), valueMethodResult.value);
    }
    if (i == bytes.length) throw new BTException(LOG + "Dict:" + start);
    return new MethodResult<>(result, ++i);
  }

  public MethodResult decodeAny(byte[] bytes, int start) {
    for (BiFunction<byte[], Integer, MethodResult> function : functions) {
      try {
        return function.apply(bytes, start);
      } catch (BTException e) {
      }
    }
    throw new BTException(LOG + "Any:" + start);
  }

  @SuppressWarnings("unchecked")
  public <T> T decode(byte[] bytes, Class<T> tClass) {
    return (T) decodeAny(bytes, 0).value;
  }

  public byte[] encodeString(byte[] string) {
    String prefix = string.length + ":";
    byte[] prefixArray = prefix.getBytes(StandardCharsets.UTF_8);
    byte[] result = Arrays.copyOf(prefixArray, prefixArray.length + string.length);
    System.arraycopy(string, 0, result, prefixArray.length, string.length);
    return result;
  }

  public byte[] encodeLong(long i) {
    String result = intTypePre + i + typeSuf;
    return result.getBytes(StandardCharsets.UTF_8);
  }

  public byte[] encodeList(List<Object> list) {
    byte[][] result = new byte[list.size() + 2][];
    result[0] = listTypePre.getBytes(StandardCharsets.UTF_8);
    for (int i = 1; i <= list.size(); i++) {
      result[i] = encodeAny(list.get(i - 1));
    }
    result[result.length - 1] = typeSuf.getBytes(StandardCharsets.UTF_8);

    return joinArray(result);
  }

  public byte[] encodeDict(Map<String, Object> map) {
    byte[][] result = new byte[map.size() + 2][];
    result[0] = dictTypePre.getBytes(StandardCharsets.UTF_8);
    result[result.length - 1] = typeSuf.getBytes(StandardCharsets.UTF_8);
    int i = 1;
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      byte[] key = encodeString(entry.getKey().getBytes(StandardCharsets.UTF_8));
      byte[] value = encodeAny(entry.getValue());
      result[i] = Arrays.copyOf(key, key.length + value.length);
      System.arraycopy(value, 0, result[i], key.length, value.length);
      i++;
    }
    return joinArray(result);
  }

  private static byte[] joinArray(byte[][] input) {
    int totalLength = 0;
    for (byte[] array : input) {
      totalLength += array.length;
    }

    byte[] result = Arrays.copyOf(input[0], totalLength);
    int offset = input[0].length;
    for (int i = 1; i < input.length; i++) {
      System.arraycopy(input[i], 0, result, offset, input[i].length);
      offset += input[i].length;
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  public byte[] encodeAny(Object obj) {
    try {
      if (obj instanceof Integer) {
        return encodeLong(Integer.toUnsignedLong((int) obj));
      } else if (obj instanceof Long) {
        return encodeLong((long) obj);
      } else if (obj instanceof String) {
        return encodeString(((String) obj).getBytes(StandardCharsets.UTF_8));
      } else if (obj instanceof byte[]) {
        return encodeString((byte[]) obj);
      } else if (obj instanceof Map) {
        return encodeDict((Map<String, Object>) obj);
      } else if (obj instanceof List) {
        return encodeList((List<Object>) obj);
      } else {
        throw new BTException(LOG + "");
      }
    } catch (BTException e) {
      throw e;
    } catch (Exception e) {
      throw new BTException(LOG + "Encode:" + e.getMessage());
    }
  }

  /** string to bytes */
  public byte[] toBytes(String string) {
    return string.getBytes(charset);
  }

  @Getter
  @Setter
  @AllArgsConstructor
  @NoArgsConstructor
  public static class MethodResult<T> {
    private T value;
    private int index;
  }
}
