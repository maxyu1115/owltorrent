package edu.rice.owltorrent.core.serialization;

import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.common.util.Bencoder;
import edu.rice.owltorrent.common.util.SHA1Encryptor;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * Torrent file parser.
 *
 * @author bhaveshshah
 */
@Log4j2(topic = "general")
public class TorrentParser {

  public static final String infoField = "info";
  public static final String announceField = "announce";
  public static final String nameField = "name";
  public static final String pieceLengthField = "piece length";
  public static final String piecesField = "pieces";
  public static final String filesField = "files";
  public static final String lengthField = "length";
  public static final String pathField = "path";
  public static final Bencoder bencoder = new Bencoder();

  private static byte[] infoPart;
  /**
   * Bencode a Torrent file and extract important attributes
   *
   * @param file Torrent file path
   * @return pruned Torrent object
   */
  public static Torrent parse(@NonNull File file) throws Exception {
    var dict = bencode(file);
    Torrent torrent = extractAttributes(dict);
    return torrent;
  }

  /**
   * Bencode a Torrent file
   *
   * @param file Torrent file
   * @return dictionary containing all file attributes
   */
  public static Map<String, Object> bencode(@NonNull File file) throws Exception {
    byte[] input = Files.readAllBytes(file.toPath());
    infoPart = input;
    Map<String, Object> dict = (Map<String, Object>) bencoder.decodeAny(input, 0).getValue();

    return dict;
  }

  /**
   * Extract important attributes to store for later use
   *
   * @param dict Containing all file attributes
   * @return pruned Torrent object
   */
  public static Torrent extractAttributes(@NonNull Map<String, Object> dict) {
    String announceURL = (String) dict.get(announceField);
    Map<String, Object> infoDict = (Map<String, Object>) dict.get(infoField);

    String name = (String) infoDict.get(nameField);
    Long pieceLength = (Long) infoDict.get(pieceLengthField);
    List<String> pieces = extractPieces((String) infoDict.get(piecesField));
    HashMap<String, Long> fileLengths = new HashMap<>();
    if (infoDict.containsKey(filesField)) { // Multiple files
      fileLengths =
          getFileLengths((ArrayList<LinkedHashMap<String, String>>) infoDict.get(filesField));
    } else { // Single file
      fileLengths.put(name, (Long) infoDict.get(lengthField));
    }

    String infoHashString = bencoder.encodeDict(infoDict);
    int startIndex =
        new String(infoPart, StandardCharsets.UTF_8)
            .indexOf("4:info"); // Get the start of the info dict
    byte[] infoHashBytes =
        Arrays.copyOfRange(
            infoPart,
            startIndex + 6,
            startIndex + 7 + infoHashString.length()); // Retrieve the original info dict
    byte[] encryptedInfoHashBytes = SHA1Encryptor.encrypt(infoHashBytes);

    return new Torrent(
        announceURL,
        name,
        pieceLength,
        pieces,
        fileLengths,
        new TwentyByteId(encryptedInfoHashBytes));
  }

  /**
   * Extract SHA1 hashes of all the pieces
   *
   * @param rawPieceData String containing all the piece hashes
   * @return list of SHA1 hashes
   */
  public static List<String> extractPieces(@NonNull String rawPieceData) {
    // TODO: do not encode raw bytes here into string
    List<String> pieces = new ArrayList<>();

    // Take chunks of size 20 at a time.
    for (int i = 0; i < rawPieceData.length(); i += 20) {
      int startIndex = i;
      int endIndex = Math.min(rawPieceData.length(), i + 20);

      pieces.add(rawPieceData.substring(startIndex, endIndex));
    }

    // TODO: fix bug, piece one off
    log.info("Found " + pieces.size() + " pieces");

    return pieces;
  }

  /**
   * Determines length for each file in Torrent
   *
   * @param files List of all files in Torrent
   * @return map storing length for each file
   */
  public static HashMap<String, Long> getFileLengths(
      @NonNull ArrayList<LinkedHashMap<String, String>> files) {
    HashMap<String, Long> fileLengths = new HashMap<>();

    // Iterate all files and store path + length for each file.
    for (LinkedHashMap<String, String> file : files) {
      String path = String.valueOf(file.get(pathField));
      String fileLengthStr = String.valueOf(file.get(lengthField));
      long fileLength = Long.parseLong(fileLengthStr);

      fileLengths.put(path, fileLength);
    }

    return fileLengths;
  }

  private static String byteUrlEncode(byte[] bs) {
    StringBuffer sb = new StringBuffer(bs.length * 3);
    for (int i = 0; i < bs.length; i++) {
      int c = bs[i] & 0xFF;
      sb.append('%');
      if (c < 16) {
        sb.append('0');
      }
      sb.append(Integer.toHexString(c));
    }
    return sb.toString();
  }

  public static String hexEncodeURL(String hexString) throws Exception {
    if (hexString == null || hexString.isEmpty()) {
      return "";
    }
    if (hexString.length() % 2 != 0) {
      throw new Exception("String is not hex, length NOT divisible by 2: " + hexString);
    }
    int len = hexString.length();
    char[] output = new char[len + len / 2];
    int i = 0;
    int j = 0;
    while (i < len) {
      output[j++] = '%';
      output[j++] = hexString.charAt(i++);
      output[j++] = hexString.charAt(i++);
    }
    return new String(output);
  }

  static final char[] CHAR_FOR_BYTE = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  };
  /** Encode byte data as a hex string... hex chars are UPPERCASE */
  public static String hexEncode(byte[] data) {
    if (data == null || data.length == 0) {
      return "";
    }
    char[] store = new char[data.length * 2];
    for (int i = 0; i < data.length; i++) {
      final int val = (data[i] & 0xFF);
      final int charLoc = i << 1;
      store[charLoc] = CHAR_FOR_BYTE[val >>> 4];
      store[charLoc + 1] = CHAR_FOR_BYTE[val & 0x0F];
    }
    return new String(store);
  }
}
