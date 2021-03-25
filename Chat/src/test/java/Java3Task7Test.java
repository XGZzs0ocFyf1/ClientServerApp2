import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import part1.Java3Task7;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Java3Task7Test {

    private final static Java3Task7 task = new Java3Task7();


    @Test
    public void testShouldReturn321() {
        int[] input = {6, 5, 4, 4, 3, 2, 1};
        int[] result = task.handleArray(input);
        System.out.println(Arrays.toString(result));
        //desired {3,2,1}
        assertEquals(1, result[2]);
        assertEquals(2, result[1]);
        assertEquals(3, result[0]);
    }

    @Test
    public void testShouldGenerateException() {
        int[] input = new int[]{0, 0, 0, 5, 0, 5, 6, 7};
        Exception exception = assertThrows(RuntimeException.class, () -> task.handleArray(input));
        String message = "no number '4' in input array";
        Assertions.assertEquals(message, exception.getMessage());

    }

    @Test
    public void testGetZeroLengthInputShouldGenerateException() {
        int[] input = {};
        Exception exception = assertThrows(RuntimeException.class, () -> task.handleArray(input));
        String message = "no number '4' in input array";
        Assertions.assertEquals(message, exception.getMessage());

    }

    @Test
    public void testShouldReturnZeroLengthArray() {
        int[] input = {4, 4};
        int[] result = task.handleArray(input);
        assertEquals(0, result.length);
    }



}
