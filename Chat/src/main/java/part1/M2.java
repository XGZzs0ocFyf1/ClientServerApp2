package part1;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class M2 {


    public int[] twoSum(int[] nums, int target) {
        int[] result = new int[2];
        int i = 0;
        while (i < nums.length) {
            int j = i + 1;
            while (j < nums.length) {
                int a = nums[i];
                int b = nums[j];


                int sum = a + b;
                if (sum == target) {
                    result[0] = i;
                    result[1] = j;
                    return new int[]{i, j};
                }
                j++;
            }


            i++;
        }
        return result;
    }

    public int[] sumOf2(int[] nums, int target){
        int[] result = new int[2];
        TreeMap<Integer, Integer> map = new TreeMap<>();
        int i = 0;
        for (int e : nums) {
            map.put(e, i);
            if (map.containsKey(target-e)){
                System.out.println("i = "+i);
                System.out.println("target-e = "+(target-e));
                System.out.println("j = "+map.get(target-e));
                result[0] = i;
                result[1] = map.get(target-e);

            }
            i++;
        }
        return result;
    }


    public static void main(String[] args) {

        System.out.println(-(-1));
        Thread.yield();
    }

    @Test
    public void test1() {
        int[] inputs = {3, 2, 4};
        int[] result = new M2().twoSum(inputs, 6);
        int[] desired = new int[]{1, 2};

        assertTrue(Arrays.equals(desired, result));
    }

    @Test
    public void test2() {
        int[] inputs = {3, 2, 95, 4, -3};
        int[] result = new M2().twoSum(inputs, 92);
        int[] desired = new int[]{2, 4};
        assertEquals(desired[0] + "" + desired[1], result[0] + "" + result[1]);


    }


    private static void printArray(int[] arr) {
        for (int e : arr) {
            System.out.print(e + " ");
        }

    }

}
