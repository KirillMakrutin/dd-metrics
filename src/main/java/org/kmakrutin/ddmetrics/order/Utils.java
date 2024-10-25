package org.kmakrutin.ddmetrics.order;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class Utils {
    private final static Random RANDOM = new Random();

    public static int nextInt(int min, int max) {
        return RANDOM.nextInt(max - min) + min;
    }
}
