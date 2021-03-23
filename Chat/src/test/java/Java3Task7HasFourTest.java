import org.junit.jupiter.api.Test;
import part1.Java3Task7;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

//tests for hasFour method
public class Java3Task7HasFourTest {

    Java3Task7 instance = new Java3Task7();

    @Test
    public void testThatFalse(){
        int[] input = {1,2,3};
        boolean output = instance.hasFour(input);
        assertFalse(output);
    }

    @Test
    public void testThatTrue(){
        int[] input = {1,2,3, 4};
        boolean output = instance.hasFour(input);
        assertTrue(output);
    }

    @Test
    public void testGetEmptyAndReturnFalse(){
        int[] input = {};
        boolean output = instance.hasFour(input);
        assertFalse(output);
    }

}
