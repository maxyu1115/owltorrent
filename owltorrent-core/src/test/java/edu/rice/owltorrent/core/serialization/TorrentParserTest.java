package edu.rice.owltorrent.core.serialization;

import static org.junit.Assert.assertEquals;

import edu.rice.owltorrent.common.entity.Torrent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

public class TorrentParserTest {
  static String coreRoot = System.getProperty("user.dir");
  static String torrentFileLocation = coreRoot + "/test.torrent";
  static File file = new File(torrentFileLocation);

  static String torrentFileLocation2 = coreRoot + "/CourseraDSFundamentals.torrent";
  static File file2 = new File(torrentFileLocation2);

  static String torrentFileLocation3 = coreRoot + "/presentation.torrent";
  static File file3 = new File(torrentFileLocation3);

  @Test
  public void parse() throws Exception {
    Torrent torrent = TorrentParser.parse(file);

    assertEquals("https://hello", torrent.getAnnounceURL());
    assertEquals("README.md", torrent.getName());
    assertEquals(1, torrent.getPieceHashes().size()); // Number of pieces
    assertEquals(32768, torrent.getPieceLength());
    assertEquals(1, torrent.getFileLengths().size()); // Effectively number of files
    assertEquals((Long) 75L, (Long) torrent.getFileLengths().get("README.md"));
    assertEquals("1dda8ef2e9942969ff1cab038128014a24b09440", torrent.getInfoHash().toString());
  }

  @Test
  public void parse2() throws Exception {
    Torrent torrent = TorrentParser.parse(file2);

    assertEquals("udp://tracker.leechers-paradise.org:6969/announce", torrent.getAnnounceURL());
    assertEquals("Data Science Fundamentals with Python and SQL", torrent.getName());
    assertEquals(1542, torrent.getPieceHashes().size()); // Number of pieces
    assertEquals(524288, torrent.getPieceLength());
    assertEquals(415, torrent.getFileLengths().size()); // Effectively number of files
    assertEquals("bdc0bb1499b1992a5488b4bbcfc9288c30793c08", torrent.getInfoHash().toString());
  }

  @Test
  public void parse3() throws Exception {
    Torrent torrent = TorrentParser.parse(file3);

    assertEquals("udp://tracker.openbittorrent.com:80/announce", torrent.getAnnounceURL());
    assertEquals("OwlTorrentRiggedDemoPresentation.pdf", torrent.getName());
    assertEquals(16, torrent.getPieceHashes().size()); // Number of pieces
    assertEquals(16384, torrent.getPieceLength());
    assertEquals(1, torrent.getFileLengths().size()); // Effectively number of files
    assertEquals(
        (Long) 255972L,
        (Long) torrent.getFileLengths().get("OwlTorrentRiggedDemoPresentation.pdf"));
    assertEquals("2b692a9c1aff75c54729ba129a3c94d2ea5d2b8c", torrent.getInfoHash().toString());
  }

  @Test(expected = NullPointerException.class)
  public void parse_nullInput() throws Exception {
    TorrentParser.parse(null);
  }

  @Test(expected = NullPointerException.class)
  public void bencode_nullInput() throws Exception {
    TorrentParser.bencode(null);
  }

  @Test(expected = NullPointerException.class)
  public void extractAttributes_nullInput() throws Exception {
    TorrentParser.extractAttributes(null);
  }

  @Test(expected = NullPointerException.class)
  public void extractPieces_nullInput() {
    TorrentParser.extractPieces(null);
  }

  @Test
  public void extractPieces_nonNullInput() {
    byte[] testArray =
        new String("1234567890098765432112345678900987654321").getBytes(StandardCharsets.UTF_8);
    assertEquals(40, testArray.length);

    TorrentParser testParser = new TorrentParser();
    HashMap<String, Object> testMap = new HashMap<>();
    testMap.put("pieces", testArray);
    List<byte[]> result = TorrentParser.extractPieces(testMap);
    assertEquals(2, result.size());
    assertEquals("12345678900987654321", new String(result.get(0)));
    assertEquals("12345678900987654321", new String(result.get(1)));
  }

  @Test(expected = NullPointerException.class)
  public void getFileLengths_nullInput() {
    TorrentParser.getFileLengths(null);
  }
}
