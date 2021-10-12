package edu.rice.owltorrent.common.util;

import edu.rice.owltorrent.common.util.Exceptions.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * author: shijie, yuchen
 *
 * <p>Reference:
 * https://github.com/BrightStarry/zx-bt/blob/7bbcf01d613cdb8afc5940b4b6641dcf3996ded6/zx-bt-spider/src/main/java/com/zx/bt/spider/util/Bencode.java
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

  public MethodResult<String> decodeString(byte[] bytes, int start) {
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
    return new MethodResult<>(
        new String(ArrayUtils.subarray(bytes, separatorIndex + 1, endIndex), charset), endIndex);
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

      MethodResult<String> keyMethodResult = decodeString(bytes, i);

      i = keyMethodResult.index;

      MethodResult valueMethodResult = decodeAny(bytes, i);

      i = valueMethodResult.index;

      result.put(keyMethodResult.value, valueMethodResult.value);
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

  public String encodeString(String string) {
    return string.length() + ":" + string;
  }

  public String encodeLong(long i) {
    return intTypePre + i + typeSuf;
  }

  public String encodeList(List<Object> list) {
    String[] result = new String[list.size() + 2];
    result[0] = listTypePre;
    for (int i = 1; i <= list.size(); i++) {
      result[i] = encodeAny(list.get(i - 1));
    }
    result[result.length - 1] = typeSuf;

    return String.join("", result);
  }

  public String encodeDict(Map<String, Object> map) {
    String[] result = new String[map.size() + 2];
    result[0] = dictTypePre;
    result[result.length - 1] = typeSuf;
    int i = 1;
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      result[i++] = encodeString(entry.getKey()) + encodeAny(entry.getValue());
    }
    return String.join("", result);
  }

  @SuppressWarnings("unchecked")
  public String encodeAny(Object obj) {
    try {
      if (obj instanceof Integer) {
        return encodeLong(Integer.toUnsignedLong((int) obj));
      } else if (obj instanceof Long) {
        return encodeLong((long) obj);
      } else if (obj instanceof String) {
        return encodeString((String) obj);
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

  public byte[] encode(Object obj) {
    return toBytes(encodeAny(obj));
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
