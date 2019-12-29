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
        N6 // Same as N5 but with 20%
    }

    // TODO implement both with first improvement and first new solution
    public static Solution genImprovedSolution(Solution oldSolution, NeighStructures neighStruct) {
        Solution newSolution = new Solution(oldSolution);

        switch (neighStruct) {
//            case N2:
////                newSolution = new Solution(oldSolution.getInstance());
//                System.out.println("Called neighborhood " + NeighStructures.N2);
            case N3:
                newSolution = swapRandTimeslots(oldSolution);
                break;
            case N4:
                newSolution = moveRandTimeslot(oldSolution);
                break;
            case N5:
                newSolution = moveCriticalExam(oldSolution, 0.1);
                break;
            case N6:
                newSolution = moveCriticalExam(oldSolution, 0.2);
                break;
        }
        return newSolution;
    }

    /**
     * N3
     */
    // TODO implement steepes descent
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
        newSolution.resetFitness();
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

        newSolution.resetFitness();
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
     * @param ratio in range (0,1): percentage of the exams to be analysed
     */
    private static Solution moveCriticalExam(Solution oldSolution, double ratio) {
        int[] highestPenaltyExam = oldSolution.getHighestPenaltyExam(ratio);
        int exam = highestPenaltyExam[0];
        int examTS = highestPenaltyExam[1];

        Solution newSolution = new Solution(oldSolution);
        newSolution.resetFitness();
        newSolution.popExam(exam, examTS - 1);

        newSolution.placeExam(exam, true);

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
