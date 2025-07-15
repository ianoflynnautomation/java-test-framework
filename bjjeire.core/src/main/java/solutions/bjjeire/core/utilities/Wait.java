package solutions.bjjeire.core.utilities;

import java.time.Duration;
import java.util.Arrays;

public class Wait {
    public static void retry(Runnable action, int timesToRetry, long sleepInterval, Class<? extends Throwable> ... exceptionsToIgnore) {
        Wait.retry(action, timesToRetry, sleepInterval, true, exceptionsToIgnore);
    }

    public static void retry(Runnable action, int timesToRetry, long sleepInterval, boolean shouldThrowException, Class<? extends Throwable> ... exceptionsToIgnore) {
        int repeat = 0;
        while(repeat <= timesToRetry) {
            repeat ++;
            if (retry(action, Duration.ofSeconds(sleepInterval), Duration.ofSeconds(sleepInterval), shouldThrowException, exceptionsToIgnore)) {
                break;
            }
        }
    }

    public static void retry(Runnable action, Class<? extends Throwable> ... exceptionsToIgnore) {
        retry(action, Duration.ofSeconds(30), Duration.ofSeconds(1), exceptionsToIgnore);
    }

    public static boolean retry(Runnable action, Duration timeout, Duration sleepInterval, Boolean shouldThrowException, Class<? extends Throwable> ... exceptionsToIgnore) {

        if(sleepInterval.toMillis() > timeout.toMillis()) {
            throw new IllegalArgumentException("Sleep interval cannot be bigger than Total timeout.");
        }

        long start = System.currentTimeMillis();
        long end = start + timeout.toMillis();
        boolean shouldThrow = false;
        Exception exceptionToThrow = null;

        while(System.currentTimeMillis() < end) {
            try {
                action.run();
                return true;
            } catch(Exception exc) {

                shouldThrow = !(Arrays.stream(exceptionsToIgnore).anyMatch(exception -> exception.isInstance(exc)));
                if (shouldThrow) {
                    exceptionToThrow = exc;
                    if (shouldThrowException) {
                        break;
                    }
                }

                Wait.forMilliseconds(sleepInterval.toMillis());
            }
        }

        if (shouldThrow) {
            if (shouldThrowException) {
                throw new RuntimeException(exceptionToThrow);
            }
        }

        return false;
    }

    public static void retry(Runnable action, Duration timeout, Duration sleepInterval, Class<? extends Throwable> ... exceptionsToIgnore) {
        Wait.retry(action, timeout, sleepInterval, true, exceptionsToIgnore);
    }

    public static void forMilliseconds(long millis) {
        try {
//            Log.info("Waiting for %s milliseconds".formatted(millis));
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean forConditionUntilTimeout(Comparator condition, long timeoutInMilliseconds, long pollingIntervalInMilliseconds) {
        boolean isConditionMet = false;

        long startTime = System.currentTimeMillis();

        while (!isConditionMet && (System.currentTimeMillis() - startTime) <= (timeoutInMilliseconds)) {
            try {
                if (condition.evaluate()) {
                    isConditionMet = true;
                } else {
                    Thread.sleep(pollingIntervalInMilliseconds);
                }
            } catch (Exception ignored) {
            }
        }

        return isConditionMet;
    }

    @FunctionalInterface
    public interface Comparator {
        boolean evaluate();
    }
}
