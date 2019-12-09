package com.dmogroup5.utils;

import java.util.ArrayList;
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
        int nExams = randSolution.instance.getExams().length;

        // Generate a random permutation of exams (note: shuffledExams contain positions, NOT exam IDs)
        List<Integer> shuffledExams = new ArrayList<>();
        for (int i = 0; i < nExams; i++) {
            shuffledExams.add(i);
        }
        java.util.Collections.shuffle(shuffledExams);

        int count = 0; // verify that all exams have been assigned to a timeslot
        for (int i : shuffledExams) {
            for (ArrayList<Integer> timeslot : randSolution.timetable) {
                int l = 0;
                boolean conflictFound = false;
                // Scan for conflict in any exam in the current timeslot
                while (l < timeslot.size() && !conflictFound) {
                    int j = timeslot.get(l);
                    conflictFound = randSolution.instance.getN()[i][j] > 0;
                    l++;
                }

                if (!conflictFound) {
                    timeslot.add(i);
                    count ++;
                    break;
                }
            }
        }

        if (count < nExams) {
            System.out.println("EROOR: partial solution");
        }

        // TODO check feasibility and handle possible non-feasible solution

        return randSolution;
    }

    // TODO implement
    public boolean checkFeasibility() { return false; }

    // TODO implement
    public boolean writeSolution() { return false; }
}
