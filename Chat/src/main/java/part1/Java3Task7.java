package part1;

import java.util.Arrays;

/**
 * 2/ Написать метод, которому в качестве аргумента передается не пустой одномерный целочисленный массив. Метод должен
 * вернуть новый массив, который получен путем вытаскивания из исходного массива элементов, идущих после последней
 * четверки. Входной массив должен содержать хотя бы одну четверку, иначе в методе необходимо выбросить RuntimeException.
 * Написать набор тестов для этого метода (по 3-4 варианта входных данных). Вх: [ 1 2 4 4 2 3 4 1 7 ] -> вых: [ 1 7 ].
 * 3. Написать метод, который проверяет состав массива из чисел 1 и 4. Если в нем нет хоть одной четверки или единицы,
 * то метод вернет false; Написать набор тестов для этого метода (по 3-4 варианта входных данных).
 */
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
