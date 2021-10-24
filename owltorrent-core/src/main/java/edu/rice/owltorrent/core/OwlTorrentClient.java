package edu.rice.owltorrent.core;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.FileBlock;
import edu.rice.owltorrent.common.entity.FileBlockInfo;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.common.util.Exceptions;
import edu.rice.owltorrent.core.serialization.TorrentParser;
import edu.rice.owltorrent.network.HandShakeListener;
import edu.rice.owltorrent.network.TorrentManager;
import edu.rice.owltorrent.network.TorrentRepository;
import edu.rice.owltorrent.network.TorrentRepositoryImpl;
import edu.rice.owltorrent.storage.DiskFile;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "general")
public class OwlTorrentClient {

  public interface ProgressMeter {
    float getPercentDone();
  }

  private static final String OWL_TORRENT_ID_PREFIX = "OwlTorrent";

  private TorrentRepository torrentRepository = new TorrentRepositoryImpl();
  private HandShakeListener handShakeListener;
  private int listenerPort = 6881;

  private final TwentyByteId ourPeerId;

  public OwlTorrentClient() {
    // TODO: generate an actual id
    ourPeerId = TwentyByteId.fromString(OWL_TORRENT_ID_PREFIX + "1234567890");
  }

  void startSeeding() {
    this.handShakeListener = new HandShakeListener(torrentRepository, listenerPort);
    new Thread(this.handShakeListener).start();
  }

  public ProgressMeter downloadFile(String torrentFileName)
      throws Exceptions.FileAlreadyExistsException, Exceptions.IllegalByteOffsets,
          Exceptions.FileCouldNotBeCreatedException, Exceptions.ParsingTorrentFileFailedException {
    Torrent torrent = findTorrent(torrentFileName);
    StorageAdapter adapter = createDownloadingStorageAdapter(torrent);
    TorrentManager manager = TorrentManager.makeDownloader(ourPeerId, torrent, adapter);
    torrentRepository.registerTorrentManager(manager);
    manager.startDownloadingAsynchronously();
    return manager::getProgressPercent;
  }

  public void seedFile(String torrentFileName, String fileName)
      throws Exceptions.ParsingTorrentFileFailedException, FileNotFoundException {
    Torrent torrent = findTorrent(torrentFileName);
    StorageAdapter adapter = createSeedingStorageAdapter(fileName);
    TorrentManager manager = TorrentManager.makeSeeder(ourPeerId, torrent, adapter);
    torrentRepository.registerTorrentManager(manager);
  }

  private Torrent findTorrent(String torrentFileName)
      throws Exceptions.ParsingTorrentFileFailedException {
    File torrentFile = new File(torrentFileName);
    Torrent torrent;
    try {
      torrent = TorrentParser.parse(torrentFile);
    } catch (Exception e) {
      throw new Exceptions.ParsingTorrentFileFailedException();
    }
    return torrent;
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
            blockInfo.getPieceIndex(), blockInfo.getLength(), diskFile.readBlock(blockInfo));
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

  private StorageAdapter createSeedingStorageAdapter(String fileName) throws FileNotFoundException {
    DiskFile diskFile = new DiskFile(fileName);
    return new StorageAdapter() {

      @Override
      public FileBlock read(FileBlockInfo blockInfo)
          throws Exceptions.IllegalByteOffsets, IOException {
        return new FileBlock(
            blockInfo.getPieceIndex(), blockInfo.getLength(), diskFile.readBlock(blockInfo));
      }

      @Override
      public void write(FileBlock fileBlock) {
        log.error("Illegal action, should not write to a seeding file");
      }

      @Override
      public boolean verify(int pieceIndex, byte[] sha1Hash)
          throws Exceptions.IllegalByteOffsets, IOException {
        return diskFile.pieceHashCorrect(pieceIndex, sha1Hash);
      }
    };
  }
}
