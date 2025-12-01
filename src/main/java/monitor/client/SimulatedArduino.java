package monitor.client;

import java.util.Random;
import java.util.function.Consumer;

/**
 * Hilo que simula un Arduino enviando una línea cada segundo.
 * Formato: "x:10,y:20,z:30"
 */
public class SimulatedArduino implements Runnable {

    private volatile boolean running = false;
    private Thread thread;
    private final Consumer<String> onLine;

    public SimulatedArduino(Consumer<String> onLine) {
        this.onLine = onLine;
    }

    public void start() {
        if (running) return;
        running = true;
        thread = new Thread(this, "SimulatedArduino");
        thread.start();
    }

    public void stop() {
        running = false;
        if (thread != null) thread.interrupt();
    }

    @Override
    public void run() {
        Random random = new Random();
        while (running) {
            int x = random.nextInt(101);
            int y = random.nextInt(101);
            int z = random.nextInt(101);

            String dataString = "x:" + x + ",y:" + y + ",z:" + z;
            onLine.accept(dataString);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
