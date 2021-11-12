package edu.rice.owltorrent.network.task;


import edu.rice.owltorrent.network.peerconnector.SocketChannelConnector;
import lombok.RequiredArgsConstructor;

/**
 * @author Lorraine Lyu, Max Yu
 */
@RequiredArgsConstructor
public class ParseMsgTask implements Runnable {
    private final SocketChannelConnector connector;

    @Override
    public void run() {
        connector.readIncomingMsg();
    }
}
