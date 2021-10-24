package edu.rice.owltorrent.common.entity;

import lombok.Value;

/**
 * Context of variables related to a Torrent
 *
 * @author Max Yu
 */
@Value
public class TorrentContext {
  TwentyByteId ourPeerId;
  short listenerPort;
  Torrent torrent;
}
