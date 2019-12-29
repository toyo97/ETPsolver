package com.dmogroup5.utils;

import java.util.ArrayList;
import java.util.Collections;

public class Selection {
    public static int[] pickRandPortion(int[] total, double ratio) {
        int n = (int) (total.length * ratio);
        int[] res = new int[n];

        ArrayList<Integer> totalList = new ArrayList<>();
        for (int i = 0; i < total.length; i++) {
            totalList.add(total[i]);
        }

        Collections.shuffle(totalList);

        for (int i = 0; i < n; i++) {
            res[i] = totalList.get(i);
        }
        return res;
    }
}
