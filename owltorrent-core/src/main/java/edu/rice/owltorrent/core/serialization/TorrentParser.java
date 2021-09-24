package edu.rice.owltorrent.core.serialization;

import com.dampcake.bencode.BencodeInputStream;
import edu.rice.owltorrent.common.entity.Torrent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import lombok.NonNull;

/**
 * Torrent file parser.
 *
 * @author bhaveshshah
 */
public class TorrentParser {

  public static final String infoField = "info";
  public static final String announceField = "announce";
  public static final String nameField = "name";
  public static final String pieceLengthField = "piece length";
  public static final String piecesField = "pieces";
  public static final String filesField = "files";
  public static final String lengthField = "length";
  public static final String pathField = "path";

  /**
   * Bencode a Torrent file and extract important attributes
   *
   * @param file Torrent file path
   * @return pruned Torrent object
   */
  public static Torrent parse(@NonNull File file) throws IOException {
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
  public static Map<String, Object> bencode(@NonNull File file) throws IOException {
    ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(file.toPath()));
    BencodeInputStream bencodeInputStream = new BencodeInputStream(inputStream);
    var dict = bencodeInputStream.readDictionary();

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

    var infoDict = (Map<String, Object>) dict.get(infoField);
    String name = (String) infoDict.get(nameField);
    long pieceLength = (long) infoDict.get(pieceLengthField);
    List<String> pieces = extractPieces((String) infoDict.get(piecesField));

    HashMap<String, Long> fileLengths = new HashMap<>();
    if (infoDict.containsKey(filesField)) // Multiple files
    fileLengths =
          getFileLengths((ArrayList<LinkedHashMap<String, String>>) infoDict.get(filesField));
    else // Single file
    fileLengths.put(name, (long) infoDict.get(lengthField));

    return new Torrent(announceURL, name, pieceLength, pieces, fileLengths);
  }

  /**
   * Extract SHA1 hashes of all the pieces
   *
   * @param rawPieceData String containing all the piece hashes
   * @return list of SHA1 hashes
   */
  public static List<String> extractPieces(@NonNull String rawPieceData) {
    List<String> pieces = new ArrayList<>();

    // Take chunks of size 20 at a time.
    for (int i = 0; i < rawPieceData.length(); i += 20) {
      int startIndex = i;
      int endIndex = Math.min(rawPieceData.length(), i + 20);

      pieces.add(rawPieceData.substring(startIndex, endIndex));
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
