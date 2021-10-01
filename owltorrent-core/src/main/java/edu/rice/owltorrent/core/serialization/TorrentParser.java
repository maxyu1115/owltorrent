package edu.rice.owltorrent.core.serialization;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.BencodeInputStream;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.util.SHA1Encryptor;
import lombok.NonNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

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
    if (infoDict.containsKey(filesField)) { // Multiple files
      fileLengths =
          getFileLengths((ArrayList<LinkedHashMap<String, String>>) infoDict.get(filesField));
    } else { // Single file
      fileLengths.put(name, (long) infoDict.get(lengthField));
    }

    System.out.println(dict.keySet());
    System.out.println(infoDict.keySet());
    System.out.println(announceURL);

    Bencode bencode = new Bencode();
    byte[] infoHashBytes = bencode.encode(infoDict);
    byte[] encryptedInfoHashBytes = SHA1Encryptor.encrypt(infoHashBytes);

    System.out.println("encrypted bytes: " + encryptedInfoHashBytes);
    System.out.println("bytes -> hex: " + hexEncode(encryptedInfoHashBytes));
    System.out.println("bytes -> urlencode: " + byteUrlEncode(encryptedInfoHashBytes));

    // TODO: fix info hash
    String infoHash = byteUrlEncode(encryptedInfoHashBytes);

    try {
      String str = hexEncodeURL("8835f0ca640ca9405f32c7d00c140cc16fcfa078");
      System.out.println("hexed hash -> urlencode: " + str);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("---");

//    System.out.println(name);
//    System.out.println(pieceLength);

    return new Torrent(announceURL, name, pieceLength, pieces, fileLengths, infoHash);
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
    if(hexString==null || hexString.isEmpty()){
      return "";
    }
    if(hexString.length()%2 != 0){
      throw new Exception("String is not hex, length NOT divisible by 2: "+hexString);
    }
    int len = hexString.length();
    char[] output = new char[len+len/2];
    int i=0;
    int j=0;
    while(i<len){
      output[j++]='%';
      output[j++]=hexString.charAt(i++);
      output[j++]=hexString.charAt(i++);
    }
    return new String(output);
  }

  static final char[] CHAR_FOR_BYTE = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
  /** Encode byte data as a hex string... hex chars are UPPERCASE*/
  public static String hexEncode(byte[] data){
    if(data == null || data.length==0){
      return "";
    }
    char[] store = new char[data.length*2];
    for(int i=0; i<data.length; i++){
      final int val = (data[i]&0xFF);
      final int charLoc=i<<1;
      store[charLoc]=CHAR_FOR_BYTE[val>>>4];
      store[charLoc+1]=CHAR_FOR_BYTE[val&0x0F];
    }
    return new String(store);
  }

}
