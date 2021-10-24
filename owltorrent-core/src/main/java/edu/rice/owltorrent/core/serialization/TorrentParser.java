package edu.rice.owltorrent.core.serialization;

import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.common.util.Bencoder;
import edu.rice.owltorrent.common.util.SHA1Encryptor;
import java.io.File;
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
    String announceURL = new String((byte[]) dict.get(announceField));
    Map<String, Object> infoDict = (Map<String, Object>) dict.get(infoField);

    String name = new String((byte[]) infoDict.get(nameField));
    long pieceLength = (long) infoDict.get(pieceLengthField);
    List<byte[]> pieces = extractPieces(infoDict);
    HashMap<String, Long> fileLengths = new HashMap<>();
    if (infoDict.containsKey(filesField)) { // Multiple files
      fileLengths =
          getFileLengths((ArrayList<LinkedHashMap<String, String>>) infoDict.get(filesField));
    } else { // Single file
      fileLengths.put(name, (Long) infoDict.get(lengthField));
    }

    byte[] infoHashString = bencoder.encodeDict(infoDict);
    byte[] encryptedInfoHashBytes = SHA1Encryptor.encrypt(infoHashString);

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
   * @param infoDict a dictionary matching the info attribute of the torrent file
   * @return list of SHA1 hashes
   */
  public static List<byte[]> extractPieces(@NonNull Map<String, Object> infoDict) {
    List<byte[]> pieces = new ArrayList<>();
    byte[] rawPieces = (byte[]) infoDict.get(piecesField);

    for (int i = 0; i < rawPieces.length; i += 20) {
      byte[] temp = Arrays.copyOfRange(rawPieces, i, i + 20);
      pieces.add(temp);
    }
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
}
