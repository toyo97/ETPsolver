package com.dmogroup5.heuristics;

import com.dmogroup5.utils.Solution;

public class Neighborhood {

    public static Solution genImprovedSolution(Solution oldSolution, int neighStruct) {
        Solution newSolution;

        switch (neighStruct) {
            case 0:
                newSolution = new Solution(oldSolution.getInstance());
            case 1:
            case 2:
            case 3:
        }
        return new Solution(oldSolution.getInstance());
    }
}
