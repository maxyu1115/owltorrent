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
        if (infoHashToTorrentManager.containsKey(infoHash)) {
            log.error("Torrent manager for torrent {} already exist.", manager.getTorrent().getName());
        }
        this.infoHashToTorrentManager.put(infoHash, manager);
    }

    @Override
    public Optional<TorrentManager> retrieveTorrent(TwentyByteId infoHash) {
        return Optional.of(this.infoHashToTorrentManager.get(infoHash));
    }
}
