package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.TwentyByteId;
import lombok.extern.log4j.Log4j2;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2(topic="general")
public class TorrentRepositoryImpl implements TorrentRepository {
    private ConcurrentHashMap<TwentyByteId, TorrentManager> infoHashToTorrentManager;

    public TorrentRepositoryImpl() {
        infoHashToTorrentManager = new ConcurrentHashMap<>();
    }

    @Override
    public void registerTorrentManager(TorrentManager manager) {
        TwentyByteId infoHash = manager.getTorrent().getInfoHash();
        /* This conditional should not be triggered in any case. */
        if (infoHashToTorrentManager.containsKey(infoHash)) {
            log.error("Torrent manager for torrent {} already exist.", manager.getTorrent().getName());
            return;
        }
        this.infoHashToTorrentManager.put(infoHash, manager);
    }

    /**
     * Usage: once a new handshake is received, the client queries with this method
     * for the TorrentManager. If nothing found, the client should ignore the handshake.
     * @param infoHash The info_hash parsed from the remote peer's handshake
     */
    @Override
    public Optional<TorrentManager> retrieveTorrent(TwentyByteId infoHash) {
        return Optional.of(this.infoHashToTorrentManager.get(infoHash));
    }
}
