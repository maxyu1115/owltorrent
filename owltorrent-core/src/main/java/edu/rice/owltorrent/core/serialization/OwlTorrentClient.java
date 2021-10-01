package edu.rice.owltorrent.core.serialization;

import edu.rice.owltorrent.common.adapters.StorageAdapter;
import edu.rice.owltorrent.common.entity.FilePiece;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.util.Exceptions;
import edu.rice.owltorrent.storage.DiskFile;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class OwlTorrentClient {

  public interface ProgressMeter {
    float getPercentDone();
  }

  public ProgressMeter downloadFile(String torrentFileName)
      throws IOException, Exceptions.FileAlreadyExistsException, Exceptions.IllegalByteOffsets,
          Exceptions.FileCouldNotBeCreatedException {
    File torrentFile = new File(torrentFileName);
    Torrent torrent = TorrentParser.parse(torrentFile);
    StorageAdapter adapter = createStorageAdapter(torrent);
    TorrentManager manager = new TorrentManager(torrent, adapter);
    manager.startDownloadingAsynchronously();
    return () -> manager.getPercentDone();
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
      public FilePiece read(int pieceIndex, int pieceOffset, int length)
          throws Exceptions.IllegalByteOffsets, IOException {
        return new FilePiece(
            pieceIndex, length, diskFile.readBlock(pieceIndex, pieceOffset, length));
      }

      @Override
      public void write(FilePiece filePiece) throws Exceptions.IllegalByteOffsets, IOException {
        diskFile.writeBlock(filePiece.getPieceIndex(), filePiece.getOffset(), filePiece.getData());
      }
    };
  }
}
