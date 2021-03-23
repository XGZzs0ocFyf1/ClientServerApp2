package part1;

public class Main {

    private volatile char currentLetter = 'A';


    public char getCurrentLetter() {
        return currentLetter;
    }

    public void setCurrentLetter(char currentLetter) {
        this.currentLetter = currentLetter;
    }

    Thread tA = new MyRunnablePrinter('A', this);
    Thread tB = new MyRunnablePrinter('B', this);
    Thread tC = new MyRunnablePrinter('C', this);


    public void executeThreads(){
        tA.start();
        tB.start();
        tC.start();
    }


    public static void main(String[] args) {
       new Main().executeThreads();

    }


}
