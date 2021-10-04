package edu.rice.owltorrent.core;

import edu.rice.owltorrent.common.util.Exceptions;
import java.util.concurrent.Callable;
import lombok.extern.log4j.Log4j2;
import picocli.CommandLine;

@Log4j2(topic = "general")
@CommandLine.Command(
    name = "owltorrent",
    mixinStandardHelpOptions = true,
    version = "owltorrent 0.0",
    description = "Downloads a torrent file through BitTorrent.")
public class ClientCLI implements Callable<Integer> {

  @CommandLine.Parameters(index = "0", description = "Path to the torrent file to download.")
  private String torrentFileName;

  @Override
  public Integer call() throws Exception {
    OwlTorrentClient client = new OwlTorrentClient();
    OwlTorrentClient.ProgressMeter meter;
    try {
      meter = client.downloadFile(torrentFileName);
    } catch (Exceptions.FileAlreadyExistsException e) {
      e.printStackTrace(System.err);
      log.error(e);
      System.out.println(
          "Sorry, the file you are trying to download already exists in this location on your local machine!");
      return 1;
    } catch (Exceptions.IllegalByteOffsets illegalByteOffsets) {
      // TODO: This should never happen because we should be parsing the 32 bits as unsigned
      log.error(illegalByteOffsets);
      System.out.println(
          "Sorry, the torrent file you are trying to download has an illegal byte offset!");
      return 1;
    } catch (Exceptions.FileCouldNotBeCreatedException e) {
      log.error(e);
      System.out.println("Sorry, the target file could not be created!!");
      return 1;
    } catch (Exceptions.ParsingTorrentFileFailedException e) {
      log.error(e);
      System.out.println("Sorry, the torrent file could not be parsed!!");
      return 1;
    }

    while (meter.getPercentDone() < 1) {
      System.out.printf("Download is %s%% done!\n", meter.getPercentDone());
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // NOOP
      }
    }

    return 0;
  }

  // this example implements Callable, so parsing, error handling and handling user
  // requests for usage help or version help can be done with one line of code.
  public static void main(String... args) {
    int exitCode = new CommandLine(new ClientCLI()).execute(args);
    System.exit(exitCode);
  }
}