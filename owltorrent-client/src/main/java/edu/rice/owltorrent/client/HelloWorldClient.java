package edu.rice.owltorrent.client;

import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.core.Peer;
import edu.rice.owltorrent.core.serialization.TorrentParser;
import edu.rice.owltorrent.network.ClientHandler;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import edu.rice.owltorrent.network.HandShakeListener;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

@NoArgsConstructor
@Log4j2(topic = "general")
public class HelloWorldClient {
  private String torrentFileLocation;

  private TorrentParser parser = new TorrentParser();

  public HelloWorldClient(String torrentFileLocation) {
    this.torrentFileLocation = torrentFileLocation;
  }

  private void talkToPort(String hostName, int portNumber) {
    try (Peer peer = new Peer(hostName, portNumber);
    //  PrintWriter out = new PrintWriter(peer.getOutputStream(), true);
    //  BufferedReader in = new BufferedReader(
    //      new InputStreamReader(peer.getInputStream()));
    ) {
      // Write 1 to the peer
      peer.getOutputStream().write(1);
      //  out.println("test");
      //  BufferedReader stdIn =
      //      new BufferedReader(new InputStreamReader(System.in));
      //  String fromServer;
      //  String fromUser;
      //
      //  while ((fromServer = in.readLine()) != null) {
      //    System.out.println("Server: " + fromServer);
      //    if (fromServer.equals("Bye."))
      //      break;
      //
      //    fromUser = stdIn.readLine();
      //    if (fromUser != null) {
      //      System.out.println("Client: " + fromUser);
      //      out.println(fromUser);
      //    }
      //  }
    } catch (UnknownHostException e) {
      log.error("Don't know about host " + hostName);
      System.exit(1);
    } catch (IOException e) {
      log.error("Couldn't get I/O for the connection to " + hostName);
      System.exit(1);
    }
  }

  public void run() throws IOException {
    if (torrentFileLocation == null) {
      HandShakeListener.listenOnPort(8080);
    } else {
      File file = new File(torrentFileLocation);
      Torrent torrent = parser.parse(file);
      log.info(torrent);
      // System.out.println(torrent);
      talkToPort("127.0.0.1", 8080);
    }
  }

  public static void main(String[] args) throws IOException {
    HelloWorldClient client;
    if (args.length > 0) {
      client = new HelloWorldClient(args[0]);
    } else {
      client = new HelloWorldClient();
    }

    try {
      client.run();
    } catch (Exception e) {
      log.error(e);
    }
  }
}
