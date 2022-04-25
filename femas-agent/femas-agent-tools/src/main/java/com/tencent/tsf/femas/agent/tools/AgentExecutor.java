package com.tencent.tsf.femas.agent.tools;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;


public class AgentExecutor {
    public static ScheduledThreadPoolExecutor executorService;

    static {
        executorService = new ScheduledThreadPoolExecutor(3, new ThreadPoolExecutor.DiscardPolicy());
        executorService.setMaximumPoolSize(10);
    }


    public static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return executorService.schedule(command, delay, unit);
    }


    public static <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {

        return executorService.schedule(callable, delay, unit);
    }


    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return executorService.scheduleAtFixedRate(command, initialDelay, period, unit);
    }


    public static ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return executorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }


    public static void shutdown() {
        executorService.shutdown();
    }


    public static List<Runnable> shutdownNow() {
        return executorService.shutdownNow();
    }


    public static boolean isShutdown() {
        return executorService.isShutdown();
    }


    public static boolean isTerminated() {
        return executorService.isTerminated();
    }


    public static boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.awaitTermination(timeout, unit);
    }


    public static <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }


    public static <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(task, result);
    }


    public static Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }


    public static <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executorService.invokeAll(tasks);
    }


    public static <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.invokeAll(tasks, timeout, unit);
    }


    public static <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executorService.invokeAny(tasks);
    }


    public static <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executorService.invokeAny(tasks, timeout, unit);
    }


    public static void execute(Runnable command) {
        executorService.execute(command);
    }
}
