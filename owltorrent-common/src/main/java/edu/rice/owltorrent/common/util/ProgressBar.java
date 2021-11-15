package edu.rice.owltorrent.common.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author shijie
 *     <p>A simple CLI progress bar
 */
public class ProgressBar {
  public int tick;
  private int totalNumberOfTick;
  public String name;

  public ProgressBar(int tick, String name) {
    this.tick = tick;
    this.name = name;
    this.totalNumberOfTick = Math.floorDiv(100, tick);
  }

  public String getProgressBar(float percentage) {
    String bar = "";
    bar += this.name;
    bar += " [";
    int numberOfTicks = Math.round(percentage / tick);
    int remainingTicks = totalNumberOfTick - numberOfTicks;
    bar += StringUtils.repeat('=', numberOfTicks);
    bar += ">";
    bar += StringUtils.repeat(' ', remainingTicks);
    bar += "] ";
    if (percentage == 100.0) {
      bar += "Done! 100%\r";
    } else {
      bar += String.format("%.2f", percentage) + "% \r";
    }
    return bar;
  }
}
