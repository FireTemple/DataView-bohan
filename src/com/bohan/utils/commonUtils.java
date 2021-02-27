package com.bohan.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * @ClassName commonUtils
 * @Description TODO
 * @Author bohanxiao
 * @Data 2/27/21 12:56 AM
 * @Version 1.0
 **/
public class commonUtils {

    /**
     * create 6 digit numbers
     * @param min
     * @param max
     * @return
     */
    public static Integer getRandNum(int min, int max) {
        int[] array = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
        Random rand = new Random();
        for (int i = 10; i > 1; i--) {
            int index = rand.nextInt(i);
            int tmp = array[index];
            array[index] = array[i - 1];
            array[i - 1] = tmp;
        }
        int result = 0;
        for (int i = 0; i < 6; i++) {
            result = result * 10 + array[i];
        }
        return result;
    }

    public static void main(String[] args) {
        int randNum = getRandNum(0, 9);
        System.out.println(randNum);
    }
}
