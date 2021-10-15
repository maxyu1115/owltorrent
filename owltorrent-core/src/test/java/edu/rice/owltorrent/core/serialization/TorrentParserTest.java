package edu.rice.owltorrent.core.serialization;

import static org.junit.Assert.assertEquals;

import edu.rice.owltorrent.common.entity.Torrent;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.Test;

public class TorrentParserTest {
  static String coreRoot = System.getProperty("user.dir");
  static String torrentFileLocation = coreRoot + "/test2.torrent";
  static File file = new File(torrentFileLocation);

  @Test
  public void parse() throws Exception {
    Torrent torrent = TorrentParser.parse(file);

    assertEquals("https://hello", torrent.getAnnounceURL());
    assertEquals("README.md", torrent.getName());
    assertEquals(1, torrent.getPieces().size()); // Number of pieces
    assertEquals(32768, torrent.getPieceLength());
    assertEquals(1, torrent.getFileLengths().size()); // Effectively number of files
    assertEquals((Long) 75L, (Long) torrent.getFileLengths().get("README.md"));
    assertEquals("1dda8ef2e9942969ff1cab038128014a24b09440", torrent.getInfoHash().toString());
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
        new String("6:pieces40:1234567890098765432112345678900987654321")
            .getBytes(StandardCharsets.UTF_8);
    assertEquals(51, testArray.length);

    TorrentParser testParser = new TorrentParser();
    testParser.infoPart = testArray;
    List<byte[]> result = TorrentParser.extractPieces(testArray);
    assertEquals(2, result.size());
    assertEquals("12345678900987654321", new String(result.get(0)));
    assertEquals("12345678900987654321", new String(result.get(1)));
  }

  @Test(expected = NullPointerException.class)
  public void getFileLengths_nullInput() {
    TorrentParser.getFileLengths(null);
  }
}
