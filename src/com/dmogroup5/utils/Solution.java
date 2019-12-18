package com.dmogroup5.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

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
    private double fitness;

    /**
     * Generates an empty solution
     *
     * @param instance The instance which can provide the number of time-slots and many useful data
     */
    public Solution(Instance instance) {
        this.fitness = Double.MAX_VALUE;
        this.instance = instance;
        this.timetable = new ArrayList[instance.getnTimeslots()];
        for (int i = 0; i < instance.getnTimeslots(); i++) {
            this.timetable[i] = new ArrayList<Integer>();
        }
    }

//    public static Solution randomSolution(Instance instance) {
//        Solution randSolution = new Solution(instance);
//        int nExams = randSolution.instance.getExams().length;
//
//        // Generate a random permutation of exams (note: shuffledExams contain positions, NOT exam IDs)
//        List<Integer> shuffledExams = new ArrayList<>();
//        for (int i = 0; i < nExams; i++) {
//            shuffledExams.add(i);
//        }
//        java.util.Collections.shuffle(shuffledExams);
//
//        for (int i : shuffledExams) {
//            boolean examAssigned = false;
//            for (ArrayList<Integer> timeslot : randSolution.timetable) {
//                int l = 0;
//                boolean conflictFound = false;
//                // Scan for conflict in any exam in the current timeslot
//                while (l < timeslot.size() && !conflictFound) {
//                    int j = timeslot.get(l);
//                    conflictFound = randSolution.instance.getNConflicts(i, j) > 0;
//                    l++;
//                }
//
//                if (!conflictFound) {
//                    timeslot.add(i);
//                    examAssigned = true;
//                    break;
//                }
//            }
//
//            if (!examAssigned) {
//                System.out.println("ERROR: exam not assigned to any timeslot");
//                return null;
//            }
//        }
//
//        // TODO check feasibility and handle possible non-feasible solution
//
//        return randSolution;
//    }

    /**
     * Generate a feasible initial solution using the saturation degree ordering
     * (or any order provided by the getNextExam() function)
     *
     * @param instance      current instance
     * @param randTimetable if true, the order of the examined timeslots for assignment is randomized
     *                      (produces more valuable solutions but `miss` is more frequent)
     * @return              feasible complete solution
     */
    public static Solution weightedSolution(Instance instance, boolean randTimetable) throws Exception {
        Solution weightedSolution = null;
        ArrayList<Integer> candidateExams;

        boolean examAssigned = false;
        // Outer loop goes until a complete solution is found (i.e. all exams assigned to exactly one time-slot
        // If the randomness takes to a partial solution, restart the algorithm
        while (!examAssigned) {

            weightedSolution = new Solution(instance);
            int nExams = weightedSolution.instance.getExams().length;
            candidateExams = new ArrayList<>(nExams);
            for (int i = 0; i < nExams; i++) {
                candidateExams.add(i);
            }

            while (!candidateExams.isEmpty()) {
                int i = weightedSolution.getNextExam(candidateExams);

                examAssigned = false;
                // TODO try with random ordering of timeslot and test the `miss` frequency
                List<ArrayList<Integer>> timetableList = Arrays.asList(weightedSolution.timetable);
                if (randTimetable) {
                    Collections.shuffle(timetableList);
                }
                for (ArrayList<Integer> timeslot : timetableList) {
                    int l = 0;
                    boolean conflictFound = false;
                    // Scan for conflict in any exam in the current timeslot
                    while (l < timeslot.size() && !conflictFound) {
                        int j = timeslot.get(l);
                        conflictFound = weightedSolution.instance.getNConflicts(i, j) > 0;
                        l++;
                    }

                    if (!conflictFound) {
                        timeslot.add(i);
                        examAssigned = true;
                        break;
                    }
                }

                if (!examAssigned) {
                    System.out.println("ERROR: exam not assigned to any timeslot");
                    break;
                }
            }
        }

        // TODO check feasibility and handle possible non-feasible solution

        return weightedSolution;
    }

    public static Solution weightedSolution(Instance instance) throws Exception {
        return weightedSolution(instance, false);
    }


    /**
     * Saturation degree ordering: exams with less available time-slots are assigned first.
     * Ties are resolved with random picking.
     *
     * @param candidateExams    list of exams remained to be assigned
     * @return                  index of the chosen exam
     */
    private int getNextExam(ArrayList<Integer> candidateExams) throws Exception {
        int[] degree = new int[candidateExams.size()];
        int max = 0;
        for (int i = 0; i < candidateExams.size(); i++) {
            for (ArrayList<Integer> timeslot : this.timetable) {
                // TODO extract function
                for (int j: timeslot) {
                    if (this.instance.getNConflicts(candidateExams.get(i), j) > 0) {
                        degree[i] += 1;
                        break;
                    }
                }
            }
            if (degree[i] > max) {
                max = degree[i];
            }
        }

        ArrayList<Integer> maxIdxList = new ArrayList<>();
        for (int i = 0; i < degree.length; i++) {
            if (degree[i] == max)
                maxIdxList.add(i);
        }

        int e = new Random().nextInt(maxIdxList.size());
        return candidateExams.remove((int) maxIdxList.get(e));
    }

    // TODO implement
    public boolean checkFeasibility() { return false; }

    // TODO implement
    public void writeSolution() throws IOException {
        int[] T = this.computeT();
        String line;
        FileWriter fw = new FileWriter("solution.sol");

        for (int i = 0; i < T.length; i++) {
            line = this.instance.getExams()[i] + " " + T[i] + "\n";
            fw.write(line);
        }

        fw.close();
    }

    /**
     * @return objective function value of the solution (lazy-load implementation)
     */
    public double getFitness() throws Exception {

        if (this.fitness == Double.MAX_VALUE) {
            this.fitness = this.computeObj();
        }
        return this.fitness;
    }

    private double computeObj() throws Exception {
        double obj = 0;

        int nExams = this.instance.getExams().length;
        int[] T = this.computeT();

        for (int i = 0; i < nExams - 1; i++) {
            for (int j = i + 1; j < nExams; j++) {
                if (this.instance.getNConflicts(i,j) > 0) {
                    int dist = Math.abs(T[i] - T[j]);
                    if (dist <= 5) {
                        obj += Math.pow(2, 5 - dist) * this.instance.getNConflicts(i,j) / this.instance.getnStudents();
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

    public Instance getInstance() {
        return instance;
    }
}
