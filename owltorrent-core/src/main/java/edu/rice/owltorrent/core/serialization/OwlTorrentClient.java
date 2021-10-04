package edu.rice.owltorrent.core.serialization;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.FileBlock;
import edu.rice.owltorrent.common.entity.FileBlockInfo;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.util.Exceptions;
import edu.rice.owltorrent.network.TorrentManager;
import edu.rice.owltorrent.storage.DiskFile;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class OwlTorrentClient {

  public interface ProgressMeter {
    float getPercentDone();
  }

  public ProgressMeter downloadFile(String torrentFileName)
      throws Exceptions.FileAlreadyExistsException, Exceptions.IllegalByteOffsets,
          Exceptions.FileCouldNotBeCreatedException, Exceptions.ParsingTorrentFileFailedException {
    File torrentFile = new File(torrentFileName);
    Torrent torrent;
    try {
      torrent = TorrentParser.parse(torrentFile);
    } catch (IOException e) {
      throw new Exceptions.ParsingTorrentFileFailedException();
    }
    StorageAdapter adapter = createStorageAdapter(torrent);
    TorrentManager manager = new TorrentManager(torrent, adapter);
    manager.startDownloadingAsynchronously();
    return manager::getProgressPercent;
  }

  private StorageAdapter createStorageAdapter(Torrent torrent)
      throws Exceptions.IllegalByteOffsets, Exceptions.FileAlreadyExistsException,
          Exceptions.FileCouldNotBeCreatedException {
    // For now we just support one file. TODO(josh): Add support for multiple files
    assert (torrent.getFileLengths().size() == 1);
    Map.Entry<String, Long> singleFile = torrent.getFileLengths().entrySet().iterator().next();
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
    };
  }
}
