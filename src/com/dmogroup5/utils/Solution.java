package com.dmogroup5.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class for a solution: the timetable variable contains every exam assigned for each time-slot (list of list),
 * (actually `array of lists` since the number of time-slots is first defined and does not change)
 * This class also provide useful methods for solution creation, manipulation and feasibility check.
 */
public class Solution {

    // Note: timetable arrays contain the positions of the exams in the array `exams`, NOT the exams ID
    // This is done to avoid search in the `exams` array. Search is done only in input reading and solution writing
    private ArrayList[] timetable;
    private Instance instance;

    /**
     * Generates an empty solution
     *
     * @param instance The instance which can provide the number of time-slots and many useful data
     */
    public Solution(Instance instance) {
        this.instance = instance;
        this.timetable = new ArrayList[instance.getnTimeslots()];
        for (ArrayList slot : this.timetable) {
            slot = new ArrayList<Integer>();
        }
    }

    public static Solution randomSolution(Instance instance) {
        Solution randSolution = new Solution(instance);

        // Generate a random permutation of exams (note: shuffledExams contain positions, NOT exam IDs)
        List<Integer> shuffledExams = new ArrayList<>();
        for (int i = 0; i < randSolution.instance.getExams().length; i++) {
            shuffledExams.add(i);
        }
        java.util.Collections.shuffle(shuffledExams);

        // TODO continue implementation

        return randSolution;
    }

    // TODO implement
    public boolean checkFeasibility() { return false; }

    // TODO implement
    public boolean writeSolution() { return false; }
}
