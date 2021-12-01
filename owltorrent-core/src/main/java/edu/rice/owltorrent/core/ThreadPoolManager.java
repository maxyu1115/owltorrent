package edu.rice.owltorrent.core;

import com.google.common.util.concurrent.MoreExecutors;
import edu.rice.owltorrent.common.adapters.TaskExecutor;
import edu.rice.owltorrent.common.adapters.TaskExecutor.Task;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.Getter;

/**
 * Thread pool manager
 *
 * @author Lorraine Lyu, Max Yu
 */
class ThreadPoolManager implements TaskExecutor {
  @Getter private final ExecutorService longRunningThreads;
  @Getter private final ExecutorService taskThreads;
  private final int LONG_RUNNING_THREADS_NUM = 5;
  private final int TASK_THREADS_LOWER_BOUND = 5;
  private final int TASK_THREADS_UPPER_BOUND = 15;

  ThreadPoolManager() {
    longRunningThreads =
        MoreExecutors.getExitingExecutorService(
            new ThreadPoolExecutor(
                LONG_RUNNING_THREADS_NUM,
                LONG_RUNNING_THREADS_NUM,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()),
            1000,
            TimeUnit.MILLISECONDS);
    taskThreads =
        MoreExecutors.getExitingExecutorService(
            new ThreadPoolExecutor(
                TASK_THREADS_LOWER_BOUND,
                TASK_THREADS_UPPER_BOUND,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()),
            1000,
            TimeUnit.MILLISECONDS);
  }

  @Override
  public Future<Void> submitTask(Task task) {
    return taskThreads.submit(task, null);
  }

  @Override
  public Future<Void> submitLongRunningTask(LongRunningTask task) {
    return longRunningThreads.submit(task, null);
  }
}
