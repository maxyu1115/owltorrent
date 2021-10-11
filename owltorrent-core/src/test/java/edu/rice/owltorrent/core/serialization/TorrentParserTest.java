package edu.rice.owltorrent.core.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import edu.rice.owltorrent.common.entity.Torrent;
import java.io.File;
import java.io.IOException;
import org.junit.Test;

public class TorrentParserTest {
  static String coreRoot = System.getProperty("user.dir");
  static String torrentFileLocation = coreRoot + "/test.torrent";
//static String torrentFileLocation = coreRoot + "/cs124.torrent";
  static File file = new File(torrentFileLocation);

  @Test
  public void parse() throws IOException {
    Torrent torrent = TorrentParser.parse(file);

    assertEquals("udp://tracker.leechers-paradise.org:6969/announce", torrent.getAnnounceURL());
    assertEquals("Data Science Fundamentals with Python and SQL", torrent.getName());
    assertEquals(1464, torrent.getPieces().size()); // Number of pieces
    assertEquals(524288, torrent.getPieceLength());
    assertEquals(415, torrent.getFileLengths().size()); // Effectively number of files
    System.out.println(torrent.getInfoHash());
  }

  @Test
  public void parse2() throws IOException {
    Torrent torrent = TorrentParser.parse(file);

    System.out.println(torrent.getInfoHash());
  }

  @Test(expected = NullPointerException.class)
  public void parse_nullInput() throws IOException {
    TorrentParser.parse(null);
  }

  @Test(expected = NullPointerException.class)
  public void bencode_nullInput() throws IOException {
    TorrentParser.bencode(null);
  }

  @Test(expected = NullPointerException.class)
  public void extractAttributes_nullInput() {
    TorrentParser.extractAttributes(null);
  }

  @Test(expected = NullPointerException.class)
  public void extractPieces_nullInput() {
    TorrentParser.extractPieces(null);
  }

  @Test(expected = NullPointerException.class)
  public void getFileLengths_nullInput() {
    TorrentParser.getFileLengths(null);
  }
}
