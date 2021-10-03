package edu.rice.owltorrent.network;

import com.dampcake.bencode.BencodeInputStream;
import com.google.common.io.ByteStreams;
import edu.rice.owltorrent.common.entity.Torrent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import lombok.NonNull;

/**
 * Tracker connector to fetch peers.
 *
 * @author bhaveshshah
 */
public class PeerLocator {

  public static final String infoHash =
      "%32%31%0b%4d%b8%4d%e5%c0%23%bf%cb%9f%40%64%8d%8c%9e%7c%a1%6e"; // TODO: retrieve from torrent
  public static final String peerID = "owltorrentclientpeer";
  public static final String port = "6991";
  public static final String left = "0";
  public static final String downloaded = "0";
  public static final String uploaded = "0";
  public static final String compact = "1";

  public List<InetSocketAddress> locatePeers(@NonNull Torrent torrent) {
    List<InetSocketAddress> addresses = new ArrayList<>();

    try {
      // Build request
      StringBuilder builder = new StringBuilder("https://torrent.ubuntu.com/announce?");
      builder.append("info_hash=" + infoHash + "&");
      builder.append("peer_id=" + peerID + "&");
      builder.append("port=" + port + "&");
      builder.append("left=" + left + "&");
      builder.append("downloaded=" + downloaded + "&");
      builder.append("uploaded=" + uploaded + "&");
      builder.append("compact=" + compact);

      // Set up connection with tracker
      URL url = new URL(builder.toString());
      URLConnection connection = url.openConnection();

      // Read bytes from tracker
      InputStream is = connection.getInputStream();
      byte[] bytes = ByteStreams.toByteArray(is);

      // Bencode tracker response
      ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
      BencodeInputStream bencodeInputStream = new BencodeInputStream(inputStream);
      var dict = bencodeInputStream.readDictionary();

      // Extract peers from bencoded dictionary.
      String peers = (String) dict.get("peers");
      byte[] peersAsBytes = peers.getBytes();

      // Iterate peers and store IP + port for each peer
      for (int i = 0; i < peersAsBytes.length - 6; i += 6) {
        byte[] ipAsBytes = {
          peersAsBytes[i], peersAsBytes[i + 1], peersAsBytes[i + 2], peersAsBytes[i + 3]
        };
        InetAddress inetAddress = InetAddress.getByAddress(ipAsBytes);

        int peerPort = ((peersAsBytes[i + 5] & 0xFF) << 8) | (peersAsBytes[i + 6] & 0xFF);

        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, peerPort);
        addresses.add(inetSocketAddress);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return addresses;
  }
}
