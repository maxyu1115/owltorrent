package edu.rice.owltorrent.network;

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
   * @param torrent the torrent manager
   */
  void registerTorrentManager(TorrentManager torrent);

  /**
   * Unregisters the torrent, when the torrent manager deactivates
   *
   * @param torrent the torrent
   */
  void unregisterTorrent(Torrent torrent);

  /**
   * Retrieves the torrent specified by its info hash
   *
   * @param infoHash the info hash of that torrent
   * @return Optional of the Torrent when found. empty otherwise
   */
  Optional<TorrentManager> retrieveTorrent(TwentyByteId infoHash);
}
