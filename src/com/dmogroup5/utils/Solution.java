package com.dmogroup5.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

/**
 * Class for a solution: the timetable variable contains every exam assigned for each time-slot (list of list),
 * (actually `array of lists` since the number of time-slots is first defined and does not change)
 * This class also provide useful methods for solution creation, manipulation and feasibility check.
 */
public class Solution {

    // Note: timetable arrays contain the positions of the exams in the array `exams`, NOT the exams ID
    // This is done to avoid search in the `exams` array. Search is done only in input reading and solution writing
    private ArrayList<Integer>[] timetable;
    // T array stores the solution in a different encoding: for each exam, its timeslot
    private int[] T;
    private Instance instance;
    private double fitness;
    private int neighborhoodOrigin = 0;

    /**
     * Generates an empty solution
     *
     * @param instance The instance which can provide the number of time-slots and many useful data
     */
    private Solution(Instance instance) {
        this.fitness = Double.MAX_VALUE;
        this.T = null;
        this.instance = instance;
        this.timetable = new ArrayList[instance.getnTimeslots()];
        for (int i = 0; i < instance.getnTimeslots(); i++) {
            this.timetable[i] = new ArrayList<>();
        }
        this.neighborhoodOrigin = 0;
    }

    /**
     * Deep-copy constructor
     *
     * @param original solution to be cloned
     */
    public Solution(Solution original) {
        this.fitness = original.fitness;
        this.T = original.T;
        this.instance = original.getInstance();
        this.timetable = new ArrayList[this.instance.getnTimeslots()];
        for (int i = 0; i < this.instance.getnTimeslots(); i++) {
            this.timetable[i] = new ArrayList<>();
            for (int exam: original.timetable[i]) {
                this.timetable[i].add(exam);
            }
        }
    }
    
    
    /**
     * Generate a feasible initial solution using the saturation degree ordering
     * (or any order provided by the getNextExam() function)
     *
     * @param instance      current instance
     * @param randTimetable if true, the order of the examined timeslots for assignment is randomized
     *                      (produces more valuable solutions but `miss` is more frequent)
     * @return              feasible complete solution
     */
    public static Solution weightedSolution(Instance instance, boolean randTimetable) {
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

                examAssigned = weightedSolution.placeExam(i, randTimetable);

//                if (!examAssigned) {
//                    System.out.println("ERROR: exam not assigned to any timeslot");
//                    break;
//                }
            }
        }

        return weightedSolution;
    }

    public static Solution weightedSolution(Instance instance) {
        return weightedSolution(instance, false);
    }


    /**
     * Saturation degree ordering: exams with less available time-slots are assigned first.
     * Ties are resolved with random picking.
     *
     * @param candidateExams    list of exams remained to be assigned
     * @return                  index of the chosen exam
     */
    private int getNextExam(ArrayList<Integer> candidateExams) {
        int[] degree = new int[candidateExams.size()];
        int max = 0;
        for (int i = 0; i < candidateExams.size(); i++) {
            int ei = candidateExams.get(i);
            degree[i] = getSaturationDegree(ei);
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

    /**
     * Saturation degree value for a given exam
     *
     * @param exam    exam to be checked
     * @return      number of timeslots which are NOT available for the exam
     */
    private int getSaturationDegree(int exam) {
        int deg = 0;
        for (ArrayList<Integer> timeslot : this.timetable) {
            for (int ej: timeslot) {
                if (this.instance.getNConflicts(exam, ej) > 0) {
                    deg += 1;
                    break;
                }
            }
        }
        return deg;
    }

    /**
     * Place an exam in the timetable
     *
     * @param exam          index of the exam to be placed in the timetable
     * @param randTimetable if true, the time-slots of the timetable are shuffled before assignment
     * @return              true if the exam can be placed without conflicts
     */
    public boolean placeExam(int exam, boolean randTimetable) {
        List<ArrayList<Integer>> timetableList = Arrays.asList(this.timetable);
        if (randTimetable) {
            Collections.shuffle(timetableList);
        }
        boolean examAssigned = false;
        for (ArrayList<Integer> timeslot : timetableList) {
            int l = 0;
            boolean conflictFound = false;
            // Scan for conflict in any exam in the current timeslot
            while (l < timeslot.size() && !conflictFound) {
                int j = timeslot.get(l);
                conflictFound = this.instance.getNConflicts(exam, j) > 0;
                l++;
            }

            if (!conflictFound) {
                timeslot.add(exam);
                examAssigned = true;
                break;
            }
        }
        return examAssigned;
    }

    public boolean placeExam(int exam, int timeslot) {
        for (int ej : this.timetable[timeslot]) {
            if (this.getInstance().getNConflicts(exam, ej) > 0) {
                return false;
            }
        }
        this.timetable[timeslot].add(exam);
        return true;
    }

    public boolean popExam(int exam) {
        int ts = findExam(exam);
        return popExam(exam, ts);
    }

    public int findExam(int exam) {
        int ts = -1;
        for (int i = 0; i < this.timetable.length; i++) {
            if (timetable[i].contains(exam)) {
                ts = i;
                break;
            }
        }
        if (ts == -1) {
            System.err.println("Exam " + exam + " expected to be in timetable cannot be found in any timeslot");
        }
        return ts;
    }

    public boolean popExam(int exam, int ts) {
        return this.timetable[ts].remove(Integer.valueOf(exam));
    }

    public int popRandExam() {
        int tsPick = new Random().nextInt(this.timetable.length);
        int examPick = new Random().nextInt(this.timetable[tsPick].size());

        return this.timetable[tsPick].remove(examPick);
    }

    public void writeSolution() throws IOException {
        int[] T = this.getT();
        String line;
        // TODO extract number from instance name and add it to solution name
//        String instanceName = this.instance.getInstanceName();
//        int instanceNumber = instanceName
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
    public double getFitness(){

        if (this.fitness == Double.MAX_VALUE) {
            this.fitness = this.computeObj();
        }
        return this.fitness;
    }

    public void resetAttributes() {
        this.fitness = Double.MAX_VALUE;
        this.T = null;
    }

    private double computeObj() {
        double obj = 0;

        int nExams = this.instance.getExams().length;
        int[] T = this.getT();

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
     * Compute the penalty caused by each exam among a portion (given by the ratio) of all the exams and take the
     * most critical one
     *
     * @param ratio percentage of the exams to be analysed
     * @return      highest penalty exam and its timeslot (from 0 to nTimeslots-1)
     */
    public int[] getHighestPenaltyExam(double ratio) {
        // pick nExams * ratio exams at random
        int totNExams = this.instance.getExams().length;
        
        int[] T = this.getT();
        double[] penalties = new double[totNExams];
        
        for (int i = 0; i < totNExams; i++) {
            penalties[i] += this.getPenalty(i);
        }
        
        int[] selectedExams = Selection.pickRandPortion(IntStream.range(0, totNExams).toArray(), ratio);
        int resIdx = selectedExams[0];
        double maxPenalty = penalties[selectedExams[0]];
        for (int i = 0; i < selectedExams.length; i++) {
            if (penalties[selectedExams[i]] > maxPenalty) {
                resIdx = selectedExams[i];
                maxPenalty = penalties[selectedExams[i]];
            }
        }
        
        return new int[] {resIdx, T[resIdx]-1};
    }

    public double getPenalty(int exam) {
        int nExams = this.instance.getExams().length;
        double penalty = 0;
        int[] T = this.getT();
        for (int j = 0; j < nExams; j++) {
            if (exam != j && this.instance.getNConflicts(exam,j) > 0) {
                int dist = Math.abs(T[exam] - T[j]);
                if (dist <= 5) {
                    penalty += Math.pow(2, 5 - dist) * this.instance.getNConflicts(exam,j) / this.instance.getnStudents();
                }
            }
        }
        return penalty;
    }

    public boolean isFeasible() {
        for (ArrayList timeslot : this.timetable) {
            for (int i = 0; i < timeslot.size() - 1; i++) {
                for (int j = i + 1; j < timeslot.size(); j++) {
                    if (this.instance.getNConflicts((int) timeslot.get(i), (int) timeslot.get(j)) > 0){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * @return array of length nExams, time-slot assigned to each exam
     */
    public int[] getT() {
        if (this.T == null) {
            int nExams = this.instance.getExams().length;
            int[] T = new int[nExams];

            for (int i = 0; i < nExams; i++) {
                for (int j = 0; j < this.timetable.length; j++) {
                    if (timetable[j].contains(i)) {
                        T[i] = j + 1;  // assumed timeslot starting from 1
                        break;
                    }
                }
            }
            this.T = T;
        }
        return this.T;
    }

    public ArrayList<Integer>[] getTimetable() {
        return this.timetable;
    }

    public Instance getInstance() {
        return instance;
    }

    public boolean swappable(int ei, int ti, int ej, int tj) {
        boolean conflict1 = false;
        boolean conflict2 = false;
        for (int i = 0; i < this.timetable[tj].size(); i++) {
            int ek = this.timetable[tj].get(i);
            if (ej != ek && this.instance.getNConflicts(ei, ek) > 0) {
                conflict1 = true;
                break;
            }
        }
        if (!conflict1) {
            for (int i = 0; i < this.timetable[ti].size(); i++) {
                int ek = this.timetable[ti].get(i);
                if (ei != ek && this.instance.getNConflicts(ej, ek) > 0) {
                    conflict2 = true;
                    break;
                }
            }
        }

        return !conflict1 && !conflict2;
    }

    public int getNeighborhoodOrigin() {
        return this.neighborhoodOrigin;
    }

    public void setNeighborhoodOrigin(int no) {
        this.neighborhoodOrigin = no;
    }
}
