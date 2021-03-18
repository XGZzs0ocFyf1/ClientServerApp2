package part1;

public class MyRunnablePrinter extends Thread{

    private final Main monitor;
    private final char letter;

    public   MyRunnablePrinter(char letter, Main monitor) {
        this.letter = letter;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        synchronized (monitor) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (letter != monitor.getCurrentLetter()) {
                        monitor.wait();
                    }
                    System.out.print(letter);

                    monitor.setCurrentLetter(getNextLetter(letter));
                    monitor.notifyAll();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private char getNextLetter(char currentLetter){
        switch (currentLetter){
            case 'A': return 'B';
            case 'B': return 'C';
            default: return 'A';
        }

    }
}
