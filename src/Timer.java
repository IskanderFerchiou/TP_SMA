import java.time.LocalDate;
import java.util.List;

public class Timer implements Runnable {

    private List<Thread> buyersThread;
    private static LocalDate date;

    Timer(LocalDate d) {
        date = d;
    }

    public void setBuyersThread(List<Thread> buyersThread) {
        this.buyersThread = buyersThread;
    }

    public static LocalDate getDate() {
        return date;
    }

    @Override
    public void run() {
        synchronized (Utils.lock) {
            boolean keepRunning;
            while (true) {
                keepRunning = false;
                for (Thread thread : buyersThread) {
                    if (thread.isAlive()) {
                        keepRunning = true;
                    }
                }
                if (!keepRunning) {
                    break;
                }

                // mise à jour de la date toutes les 200 MS
                try {
                    Utils.lock.wait(200);
                    date = date.plusDays(1);
                    Utils.lock.notifyAll();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }
}
