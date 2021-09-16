package edu.rice.owltorrent.core;

import edu.rice.owltorrent.core.entity.Torrent;
import java.io.File;
import java.io.IOException;

public interface TorrentParser {
  Torrent parse(File file) throws IOException;
}
