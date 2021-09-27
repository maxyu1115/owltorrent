package edu.rice.owltorrent.common.interfaces;

import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.util.Optional;

/**
 * Interface for a torrent repository. Such a class would manage all the owned torrents.
 *
 * @author Max Yu
 */
public interface TorrentRepository {
  /**
   * Registers the torrent file
   *
   * @param torrent the torrent file
   */
  void registerTorrent(Torrent torrent);

  /**
   * Retrieves the torrent specified by its info hash
   *
   * @param infoHash the info hash of that torrent
   * @return Optional of the Torrent when found. empty otherwise
   */
  Optional<Torrent> retrieveTorrent(TwentyByteId infoHash);
}
