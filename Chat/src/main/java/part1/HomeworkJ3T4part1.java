package part1;

public class HomeworkJ3T4part1 {
    private final Object mon = new Object();
    private volatile char currentLetter = 'A';


    public static void main(String[] args) {
        HomeworkJ3T4part1 w = new HomeworkJ3T4part1();
        Thread t1 = new Thread(() -> w.printA());
        Thread t2 = new Thread(() -> w.printB());

         Thread t3 = new Thread(() -> w.printC());
        t1.start();
        t2.start(); //c 2 потоками ABABABAB... выводится
        t3.start();// с добавлением 3 потока все ломается
    }

    public void printA() {
        synchronized (mon) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (currentLetter != 'A') {
                        mon.wait();
                    }
                    System.out.print("A");
                    currentLetter = 'B';
                    mon.notify();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    public void printB() {
        synchronized (mon) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (currentLetter != 'B') {
                        mon.wait();
                    }
                    System.out.print("B");
                  //  currentLetter = 'A';
                    currentLetter = 'C';
                    mon.notify();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    public void printC() {
        synchronized (mon) {
            try {
                for (int i = 0; i < 5; i++) {
                    while (currentLetter != 'C') {
                        mon.wait();
                    }
                    System.out.print("C");
                    currentLetter = 'A';
                    mon.notify();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
}
