package edu.rice.owltorrent.core.serialization;

import com.dampcake.bencode.BencodeInputStream;
import edu.rice.owltorrent.core.Torrent;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TempParser {

  public Torrent parse(File file) throws IOException {
    // TODO: revise byte reading method
    ByteArrayInputStream inputStream = new ByteArrayInputStream(Files.readAllBytes(file.toPath()));
    BencodeInputStream bencodeInputStream = new BencodeInputStream(inputStream);
    var dict = bencodeInputStream.readDictionary();
    System.out.println(dict.keySet());
    return new Torrent((String) dict.get("announce"));
  }
}
