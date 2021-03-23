package part1;

import java.util.Arrays;


public class Java3Task7 {


    /**
     * обычный вариант
     */
    public int[] handleArray(int[] inputArray) {
        int[] temp = new int[inputArray.length];
        int outputCounter = 0;
        for (int i = 0; i < inputArray.length; i++) {
            int currentValue = inputArray[inputArray.length - i - 1];
            if (currentValue == 4) {
                int[] output = Arrays.copyOf(temp, outputCounter);
                return reverse(output);
            }
            temp[outputCounter] = currentValue;
            outputCounter++;
        }

        throw new RuntimeException("no number '4' in input array");
    }

    //вспомогательный метод
    private int[] reverse(int[] input) {
        int[] output = new int[input.length];
        int counter = 0;
        while (counter < input.length) {
            output[counter] = input[input.length - counter - 1];
            counter++;
        }
        return output;
    }

    //вариант с использованием   System .arraycopy()
    public int[] getPartAfterLastFour(int[] input) {
        int counter = 0;
        int lastIndexOfFour = -1;
        while (counter != input.length) {
            if (input[counter] == 4) {
                lastIndexOfFour = counter;
            }
            counter++;
        }
        if (lastIndexOfFour == -1) {
            throw new RuntimeException("no number '4' in input array");
        }
        int numberOfElementsToCopy = input.length - lastIndexOfFour - 1;
        int[] output = new int[numberOfElementsToCopy];
        System.arraycopy(input, lastIndexOfFour + 1, output, 0, numberOfElementsToCopy);

        return output;
    }

    public boolean hasFour(int[] input) {
        for (int e : input) {
            if (e == 4) {
                return true;
            }
        }
        return false;
    }

}
