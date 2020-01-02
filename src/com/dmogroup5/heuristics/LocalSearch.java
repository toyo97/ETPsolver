package com.dmogroup5.heuristics;

import com.dmogroup5.utils.Solution;

import java.util.ArrayList;
import java.util.Random;

public class LocalSearch {

    // neighborhood structures
    public enum NeighStructures {
        N2, // Choose a single course at random and move it to another random feasible timeslot
        N3, // Select two timeslots at random and simply swap all the courses in one timeslot with all the courses in the other timeslot
        N4, // Move random timeslot and shift the others
        N5, // Move the highest penalty course from a random 10% selection of the courses to a random feasible timeslot
        N6, // Same as N5 but with 20%
        N7,
        N8
    }

    // TODO implement both with first improvement and first new solution
    public static Solution genImprovedSolution(Solution oldSolution, NeighStructures neighStruct) {
        Solution newSolution = new Solution(oldSolution);

        switch (neighStruct) {
//            case N2:
//                double r = 1. / oldSolution.getInstance().getExams().length;
//                newSolution = GeneticAlgorithms.mutateSolution(oldSolution, r);
//                break;
            case N3:
                newSolution = swapRandTimeslots(oldSolution);
                break;
            case N4:
                newSolution = moveRandTimeslot(oldSolution);
                break;
            case N5:
                newSolution = moveCriticalExam(oldSolution, 0.1, false);
                break;
            case N6:
                newSolution = moveCriticalExam(oldSolution, 0.2, false);
                break;
            case N7:
                newSolution = moveCriticalExam(oldSolution, 0.1, true);
                break;
            case N8:
                newSolution = moveCriticalExam(oldSolution, 0.2, true);
                break;
        }
        return newSolution;
    }

    /**
     * N3
     */
    // TODO implement steepest descent
    private static Solution swapRandTimeslots(Solution oldSolution) {
        Random rand = new Random();
        int ts1 = rand.nextInt(oldSolution.getTimetable().length);
        int ts2;
        do {
            ts2 = rand.nextInt(oldSolution.getTimetable().length);
        } while (ts1 == ts2);

        Solution newSolution = new Solution(oldSolution);
        newSolution.getTimetable()[ts1] = (ArrayList) oldSolution.getTimetable()[ts2].clone();
        newSolution.getTimetable()[ts2] = (ArrayList) oldSolution.getTimetable()[ts1].clone();
        newSolution.resetAttributes();
        if (!newSolution.isFeasible()) {
            System.err.println("Solution is not feasible!");
        }
        if (newSolution.getFitness() > oldSolution.getFitness()) {
            System.out.println("[IMPROVEMENT N3] Swapped " + ts1 + " with " + ts2);
        }

        return newSolution;
    }

    /**
     * N4
     */
    private static Solution moveRandTimeslot(Solution oldSolution) {
        Random rand = new Random();
        int nTimeslots = oldSolution.getTimetable().length;

//      randomly determine direction of the swap
        boolean forward = rand.nextBoolean();

//      randomly determine first timeslot and the second, whose distance must be greater than 1
        int ts1 = rand.nextInt(nTimeslots);
        int ts2;
        do {
            ts2 = rand.nextInt(nTimeslots);
        } while (Math.abs(ts1 - ts2) < 2);

//      order timeslots so that ts1 < ts2
        int tmpTS;
        if (ts1 > ts2) {
            tmpTS = ts1;
            ts1 = ts2;
            ts2 = tmpTS;
        }

        Solution newSolution = new Solution(oldSolution);

        ArrayList[] timetable = newSolution.getTimetable();
        //  clockwise rotation
        if (forward) {
            ArrayList tmp = timetable[ts1];
            timetable[ts1] = timetable[ts2];
            for (int i = ts2; i > ts1 + 1; i--) {
                timetable[i] = timetable[i-1];
            }
            timetable[ts1+1] = tmp;
        }
        //  anti-clockwise rotation
        else {
            ArrayList tmp = timetable[ts2];
            timetable[ts2] = timetable[ts1];
            for (int i = ts1; i < ts2 - 1; i++) {
                timetable[i] = timetable[i+1];
            }
            timetable[ts2-1] = tmp;
        }

        newSolution.resetAttributes();
        // TODO remove feasibility check
        if (!newSolution.isFeasible()) {
            System.err.println("Solution is not feasible!");
        }

        if (newSolution.getFitness() > oldSolution.getFitness()) {
            System.out.println("[IMPROVEMENT N4] Shift between " + ts1 + " and " + ts2);
        }

        return newSolution;
    }

    /**
     * N5-N6
     * Move highest penalty exam
     *
     * @param ratio     in range (0,1): percentage of the exams to be analysed
     * @param optimize  true if exam is placed in the optimal timeslot
     */
    private static Solution moveCriticalExam(Solution oldSolution, double ratio, boolean optimize) {
        int[] highestPenaltyExam = oldSolution.getHighestPenaltyExam(ratio);
        int exam = highestPenaltyExam[0];
        int examTS = highestPenaltyExam[1];

        Solution newSolution = new Solution(oldSolution);
        newSolution.resetAttributes();
        newSolution.popExam(exam, examTS);

        if (optimize) {
            int nTimeslots = newSolution.getTimetable().length;
            double bestFitness = Double.MAX_VALUE;
            int bestTimeslot = examTS - 1;
            for (int i = 0; i < nTimeslots; i++) {
                if (newSolution.placeExam(exam, i)) {
                    double obj = newSolution.getFitness();
                    if (obj < bestFitness) {
                        bestFitness = obj;
                        bestTimeslot = i;
                    }
                    newSolution.popExam(exam, i);
                    newSolution.resetAttributes();
                }
            }
            newSolution.placeExam(exam, bestTimeslot);
        } else {
            newSolution.placeExam(exam, true);
        }

        if (!newSolution.isFeasible()) {
            System.err.println("Solution is not feasible!");
        }

        if (newSolution.getFitness() > oldSolution.getFitness()) {
            System.out.println("[IMPROVEMENT N5-N6] Moved exam " + exam + " from ts " + examTS + " to ts "
                    + newSolution.findExam(exam));
        }
        return newSolution;
    }
}
