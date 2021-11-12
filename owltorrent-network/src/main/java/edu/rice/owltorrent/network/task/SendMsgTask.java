package edu.rice.owltorrent.network.task;

import edu.rice.owltorrent.network.peerconnector.SocketChannelConnector;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
/**
 * @author Lorraine Lyu, Max Yu
 */
@RequiredArgsConstructor
public class SendMsgTask implements Runnable {
    private final SocketChannelConnector connector;

    @Override
    public void run() {
        try {
            connector.processOutgoingMsg();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
