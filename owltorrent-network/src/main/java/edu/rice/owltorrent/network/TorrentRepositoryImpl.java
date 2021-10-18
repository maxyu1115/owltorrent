package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.TwentyByteId;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.log4j.Log4j2;

/**
 * The TorrentRepository Implementation.
 *
 * @author Lorraine Lyu
 */
@Log4j2(topic = "general")
public class TorrentRepositoryImpl implements TorrentRepository {
  private final ConcurrentHashMap<TwentyByteId, TorrentManager> infoHashToTorrentManager =
      new ConcurrentHashMap<>();

  @Override
  public void registerTorrentManager(TorrentManager manager) {
    TwentyByteId infoHash = manager.getTorrent().getInfoHash();
    this.infoHashToTorrentManager.putIfAbsent(infoHash, manager);
  }

  /**
   * Usage: once a new handshake is received, the client queries with this method for the
   * TorrentManager. If nothing found, the client should ignore the handshake.
   *
   * @param infoHash The info_hash parsed from the remote peer's handshake
   */
  @Override
  public Optional<TorrentManager> retrieveTorrent(TwentyByteId infoHash) {
    return infoHashToTorrentManager.containsKey(infoHash)
        ? Optional.of(infoHashToTorrentManager.get(infoHash))
        : Optional.empty();
  }
}
