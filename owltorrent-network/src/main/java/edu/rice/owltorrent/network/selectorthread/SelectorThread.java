package edu.rice.owltorrent.network.selectorthread;

import java.nio.channels.Selector;

public interface SelectorThread extends Runnable {
    public Selector getSelector();
}
