package edu.rice.owltorrent.core;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.FileBlock;
import edu.rice.owltorrent.common.entity.FileBlockInfo;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TorrentContext;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.common.util.Exceptions;
import edu.rice.owltorrent.core.serialization.TorrentParser;
import edu.rice.owltorrent.network.*;
import edu.rice.owltorrent.storage.DiskFile;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "general")
public class OwlTorrentClient {

  public interface ProgressMeter {
    float getPercentDone();
  }

  private static final String OWL_TORRENT_ID_PREFIX = "OwlTorrent";

  private final TorrentRepository torrentRepository = new TorrentRepositoryImpl();
  private final HandShakeListener handShakeListener;
  private final Thread listenerThread;
  private final PeerLocator locator;
  private final int listenerPort;

  private final TwentyByteId ourPeerId;

  public static TwentyByteId generateRandomPeerId() {
    int bytesToGenerate = 20 - OWL_TORRENT_ID_PREFIX.length();
    String random_uuid =
        UUID.randomUUID().toString().replace("-", "").substring(0, bytesToGenerate);
    return TwentyByteId.fromString(OWL_TORRENT_ID_PREFIX + random_uuid);
  }

  public OwlTorrentClient() {
    listenerPort = 57600;
    ourPeerId = generateRandomPeerId();
    handShakeListener = new HandShakeListener(torrentRepository, listenerPort);
    listenerThread = new Thread(this.handShakeListener);
    locator = new MultipleTrackerConnector();
  }

  public OwlTorrentClient(int port, PeerLocator locator, TwentyByteId peerId) {
    listenerPort = port;
    ourPeerId = peerId;
    handShakeListener = new HandShakeListener(torrentRepository, listenerPort);
    listenerThread = new Thread(this.handShakeListener);
    this.locator = locator;
  }

  void startSeeding() {
    listenerThread.start();
  }

  public ProgressMeter downloadFile(String torrentFileName)
      throws Exceptions.FileAlreadyExistsException, Exceptions.IllegalByteOffsets,
          Exceptions.FileCouldNotBeCreatedException, Exceptions.ParsingTorrentFileFailedException {
    TorrentContext torrentContext = findTorrent(torrentFileName);
    StorageAdapter adapter = createDownloadingStorageAdapter(torrentContext.getTorrent());
    TorrentManager manager = TorrentManager.makeDownloader(torrentContext, adapter, locator);
    torrentRepository.registerTorrentManager(manager);
    manager.startDownloadingAsynchronously();
    return manager::getProgressPercent;
  }

  public void seedFile(String torrentFileName, String fileName)
      throws Exceptions.ParsingTorrentFileFailedException, IOException,
          Exceptions.FileNotMatchingTorrentException {
    TorrentContext torrentContext = findTorrent(torrentFileName);
    StorageAdapter adapter = createSeedingStorageAdapter(torrentContext.getTorrent(), fileName);
    TorrentManager manager = TorrentManager.makeSeeder(torrentContext, adapter, locator);
    torrentRepository.registerTorrentManager(manager);
    try {
      listenerThread.join();
    } catch (InterruptedException e) {
      log.info("Listener Thread interrupted");
    }
  }

  private TorrentContext findTorrent(String torrentFileName)
      throws Exceptions.ParsingTorrentFileFailedException {
    File torrentFile = new File(torrentFileName);
    Torrent torrent;
    try {
      torrent = TorrentParser.parse(torrentFile);
    } catch (Exception e) {
      throw new Exceptions.ParsingTorrentFileFailedException();
    }
    return new TorrentContext(ourPeerId, (short) listenerPort, torrent);
  }

  private StorageAdapter createDownloadingStorageAdapter(Torrent torrent)
      throws Exceptions.IllegalByteOffsets, Exceptions.FileAlreadyExistsException,
          Exceptions.FileCouldNotBeCreatedException {
    // For now we just support one file. TODO(josh): Add support for multiple files
    assert (torrent.getFileLengths().size() == 1);
    Map.Entry<String, Long> singleFile = torrent.getFileLengths().entrySet().iterator().next();
    log.info(singleFile);
    DiskFile diskFile =
        new DiskFile(singleFile.getKey(), singleFile.getValue(), torrent.getPieceLength());
    return new StorageAdapter() {

      @Override
      public FileBlock read(FileBlockInfo blockInfo)
          throws Exceptions.IllegalByteOffsets, IOException {
        return new FileBlock(
            blockInfo.getPieceIndex(),
            blockInfo.getOffsetWithinPiece(),
            diskFile.readBlock(blockInfo));
      }

      @Override
      public void write(FileBlock fileBlock) throws Exceptions.IllegalByteOffsets, IOException {
        diskFile.writeBlock(fileBlock);
      }

      @Override
      public boolean verify(int pieceIndex, byte[] sha1Hash)
          throws Exceptions.IllegalByteOffsets, IOException {
        return diskFile.pieceHashCorrect(pieceIndex, sha1Hash);
      }
    };
  }

  private StorageAdapter createSeedingStorageAdapter(Torrent torrent, String fileName)
      throws IOException, Exceptions.FileNotMatchingTorrentException {
    DiskFile diskFile = new DiskFile(torrent, fileName);
    return new StorageAdapter() {

      @Override
      public FileBlock read(FileBlockInfo blockInfo)
          throws Exceptions.IllegalByteOffsets, IOException {
        return new FileBlock(
            blockInfo.getPieceIndex(),
            blockInfo.getOffsetWithinPiece(),
            diskFile.readBlock(blockInfo));
      }

      @Override
      public void write(FileBlock fileBlock) {
        log.error("Illegal action, should not write to a seeding file");
        throw new IllegalArgumentException("Illegal action, should not write to a seeding file");
      }

      @Override
      public boolean verify(int pieceIndex, byte[] sha1Hash) {
        log.error("Illegal action, seeder should not be verifying");
        throw new IllegalArgumentException("Illegal action, seeder should not be verifying");
      }
    };
  }
}
