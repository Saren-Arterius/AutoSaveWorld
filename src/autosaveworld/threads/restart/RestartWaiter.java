package autosaveworld.threads.restart;

public class RestartWaiter {

    private static int waittorestart = 0;

    public static boolean shouldWait() {
        return RestartWaiter.waittorestart != 0;
    }

    public static void incrementWait() {
        RestartWaiter.waittorestart++;
    }

    public static void decrementWait() {
        if (RestartWaiter.waittorestart > 0) {
            RestartWaiter.waittorestart--;
        }
    }

}
