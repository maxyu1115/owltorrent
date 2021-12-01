package edu.rice.owltorrent.common.adapters;

import java.util.concurrent.Future;

/** @author yunlyu */
public interface TaskExecutor {
  Future<Void> submitTask(Task task);

  Future<Void> submitLongRunningTask(LongRunningTask task);

  /** Type narrowed interface */
  interface Task extends Runnable {}

  /** Type narrowed interface */
  interface LongRunningTask extends Runnable {}
}
