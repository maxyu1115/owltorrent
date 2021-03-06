package edu.rice.owltorrent.network;

import com.dampcake.bencode.BencodeInputStream;
import com.google.common.io.ByteStreams;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TorrentContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;

/**
 * HTTP tracker connector to fetch peers.
 *
 * @author bhaveshshah
 */
public class HttpTrackerConnector {

  public static final String compact = "1";

  /**
   * Retrieve peers from HTTP tracker
   *
   * @param torrentContext Torrent Context object
   * @return list of peers
   */
  public List<Peer> locateWithHTTPTracker(
      @NonNull TorrentContext torrentContext,
      long downloaded,
      long left,
      long uploaded,
      Event event,
      String announceURL)
      throws Exception {
    Torrent torrent = torrentContext.getTorrent();

    String request =
        announceURL
            + "info_hash="
            + torrent.getInfoHash().hexEncodeURL()
            + "&peer_id="
            + new String(torrentContext.getOurPeerId().getBytes())
            + "&port="
            + torrentContext.getListenerPort()
            + "&left="
            + left
            + "&downloaded="
            + downloaded
            + "&uploaded="
            + uploaded
            + "&compact="
            + compact;

    // Set up connection with tracker
    URL url = new URL(request);
    URLConnection connection = url.openConnection();

    // Read bytes from tracker
    InputStream is = connection.getInputStream();
    byte[] bytes = ByteStreams.toByteArray(is);

    // Bencode tracker response
    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
    BencodeInputStream bencodeInputStream = new BencodeInputStream(inputStream);
    var dict = bencodeInputStream.readDictionary();

    // Retrieve peers from bencoded dictionary and then extract addresses
    String peersField = (String) dict.get("peers");
    byte[] addresses = peersField.getBytes();

    // Create peers
    return createPeers(addresses, torrent);
  }

  /**
   * Create peers from address array
   *
   * @param addresses Peer addresses byte array
   * @return list of IP addresses
   */
  public List<Peer> createPeers(@NonNull byte[] addresses, @NonNull Torrent torrent)
      throws UnknownHostException {
    List<Peer> peers = new ArrayList<>();

    // Iterate peers and store IP + port for each peer
    for (int i = 0; i <= addresses.length - 6; i += 6) {
      // Create Inet Socket Address
      byte[] ipAsBytes = Arrays.copyOfRange(addresses, i, i + 4);
      InetAddress inetAddress = InetAddress.getByAddress(ipAsBytes);
      int peerPort = ((addresses[i + 4] & 0xFF) << 8) | (addresses[i + 5] & 0xFF);
      InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, peerPort);

      if (inetSocketAddress.toString().equals("/0.0.0.0:0")) {
        break;
      }
      System.out.println(inetSocketAddress);

      // Create peer
      Peer peer = new Peer(inetSocketAddress, torrent);
      peers.add(peer);
    }

    return peers;
  }
}
