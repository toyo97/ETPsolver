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
    private ArrayList<Integer>[] timetable;
    private Instance instance;

    /**
     * Generates an empty solution
     *
     * @param instance The instance which can provide the number of time-slots and many useful data
     */
    public Solution(Instance instance) {
        this.instance = instance;
        this.timetable = new ArrayList[instance.getnTimeslots()];
        for (int i = 0; i < instance.getnTimeslots(); i++) {
            this.timetable[i] = new ArrayList<Integer>();
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
            System.out.println("ERROR: partial solution");
            return null;
        }

        // TODO check feasibility and handle possible non-feasible solution

        return randSolution;
    }

    public static Solution weightedSolution(Instance instance) {
        Solution randSolution = new Solution(instance);
        int nExams = randSolution.instance.getExams().length;

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
            System.out.println("ERROR: partial solution");
        }

        // TODO check feasibility and handle possible non-feasible solution

        return randSolution;
    }

    // TODO implement
    public boolean checkFeasibility() { return false; }

    // TODO implement
    public boolean writeSolution() { return false; }

    /**
     * @return objective function value of the solution
     */
    public double computeObj() {
        double obj = 0;

        int nExams = this.instance.getExams().length;
        int[] T = this.computeT();

        for (int i = 0; i < nExams; i++) {
            for (int j = i; j < nExams; j++) {
                if (this.instance.getN()[i][j] > 0) {
                    int dist = Math.abs(T[i] - T[j]);
                    if (dist <= 5) {
                       obj += Math.pow(2, 5 - dist) * this.instance.getN()[i][j] / this.instance.getnStudents();
                    }
                }
            }
        }
        return obj;
    }

    /**
     * @return array of length nExams, time-slot assigned to each exam
     */
    private int[] computeT() {
        int nExams = this.instance.getExams().length;
        int[] T = new int[nExams];

        for (int i = 0; i < nExams; i++) {
            for (int j = 0; j < this.timetable.length; j++) {
                if (timetable[j].contains(i)) {
                    // TODO check if time-slot index starts from 1 or from 0
                    T[i] = j + 1;  // assumed timeslot startingo from 0
                    break;
                }
            }
        }
        return T;
    }

    public ArrayList[] getTimetable() {
        return timetable;
    }
}
