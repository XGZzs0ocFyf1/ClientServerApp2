
import org.junit.jupiter.api.Test;
import part1.Java3Task7;

import static org.junit.jupiter.api.Assertions.*;


/**
 * 
 * Here we tests another method that do the same thing as Java3Task7.handleArray(int[] input)
 * This method uses System.arrayCopy() to get result
 */

public class Java3Task7Test2 {

    Java3Task7 classToTest = new Java3Task7();


    @Test
    public void sendArrayWithoutFourAndGetException() {
        int[] input = new int[]{0, 0, 0, 5, 0, 5, 6, 7};
        Exception exception = assertThrows(RuntimeException.class, () -> classToTest.getPartAfterLastFour(input));
        String message = "no number '4' in input array";
        assertEquals(message, exception.getMessage());
    }

    @Test
    public void sendEmptyArrayAndGetException() {
        int[] input = new int[]{};
        Exception exception = assertThrows(RuntimeException.class, () -> classToTest.getPartAfterLastFour(input));
        String message = "no number '4' in input array";
        assertEquals(message, exception.getMessage());
    }

    @Test
    public void sendArraysAndGet567() {
        int[] input = new int[]{0, 1, 2, 3, 0, 4, 5, 6, 7};
        int[] desiredOutput = {5, 6, 7};
        int[] output = classToTest.getPartAfterLastFour(input);
        assertArrayEquals(desiredOutput, output);
    }

    @Test
    public void sendArraysAndGet0101() {
        int[] input = new int[]{0, 1, 2, 4, 0, 1, 0, 1};
        int[] desiredOutput = {0, 1, 0, 1};
        int[] output = classToTest.getPartAfterLastFour(input);
        assertArrayEquals(desiredOutput, output);
    }


}
