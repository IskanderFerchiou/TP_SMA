import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class Timer implements Runnable {

    private final List<Thread> buyersThread;

    private static final DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final AtomicReference<LocalDate> date = new AtomicReference<>(LocalDate.parse("01/01/2023", df));

    Timer(List<Thread> buyersThread) {
        this.buyersThread = buyersThread;
    }

    public static LocalDate getDate() {
        return date.get();
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
                    date.updateAndGet(d -> d.plusDays(1));
                    Utils.lock.notifyAll();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

            }
        }
    }
}
