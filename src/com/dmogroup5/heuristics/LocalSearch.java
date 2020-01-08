package com.dmogroup5.heuristics;

import com.dmogroup5.utils.Solution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class LocalSearch {

    // neighborhood structures
    public enum NeighStructures {
        N1, // Choose a single course at random and find another exam in another timeslot with which to swap timeslots
        N2, // Choose a single course at random and move it to another random feasible timeslot
        N3, // Select two timeslots at random and simply swap all the courses in one timeslot with all the courses in the other timeslot
        N4, // Move random timeslot and shift the others
        N5, // Move the highest penalty course from a random 10% selection of the courses to a random feasible timeslot
        N6, // Same as N5 but with 20%
        N7, // Move the highest penalty course from a random 10% selection of the courses to a new feasible timeslot that can generate the lowest penalty cost.
        N8, // As N7 but with 20% of the courses.
        N9, // Apply kempe-chain from a random exam
        N10, // Apply kempe-chain taking the highest penalty course from a random 10% selection of the courses
        N11 // Apply kempe-chain taking the highest penalty course from a random 20% selection of the courses
    }

    public static Solution genImprovedSolution(Solution oldSolution, NeighStructures neighStruct) {
        Solution newSolution = new Solution(oldSolution);
        switch (neighStruct) {
            case N1:
                newSolution = swapRandExams(oldSolution);
                newSolution.setNeighborhoodOrigin(1);
                break;
            case N2:
                double r = 1. / oldSolution.getInstance().getExams().length;
                newSolution = GeneticAlgorithms.mutateSolution(oldSolution, r);
                newSolution.setNeighborhoodOrigin(2);
                break;
            case N3:
                newSolution = swapRandTimeslots(oldSolution);
                newSolution.setNeighborhoodOrigin(3);
                break;
            case N4:
                newSolution = moveRandTimeslot(oldSolution);
                newSolution.setNeighborhoodOrigin(4);
                break;
            case N5:
                newSolution = moveCriticalExam(oldSolution, 0.1, false);
                newSolution.setNeighborhoodOrigin(5);
                break;
            case N6:
                newSolution = moveCriticalExam(oldSolution, 0.2, false);
                newSolution.setNeighborhoodOrigin(6);
                break;
            case N7:
                newSolution = moveCriticalExam(oldSolution, 0.1, true);
                newSolution.setNeighborhoodOrigin(7);
                break;
            case N8:
                newSolution = moveCriticalExam(oldSolution, 0.2, true);
                newSolution.setNeighborhoodOrigin(8);
                break;
            case N9:
                newSolution = kempeRandMove(oldSolution);
                newSolution.setNeighborhoodOrigin(9);
                break;
            case N10:
                newSolution = kempeMove(oldSolution, 0.1);
                newSolution.setNeighborhoodOrigin(10);
                break;
            case N11:
                newSolution = kempeMove(oldSolution, 0.2);
                newSolution.setNeighborhoodOrigin(11);
                break;
        }
        return newSolution;
    }

    private static Solution swapRandExams(Solution oldSolution) {
        Solution newSolution = new Solution(oldSolution);

        int nExams = newSolution.getInstance().getExams().length;
        int nTimeslots = newSolution.getTimetable().length;
        Random rand = new Random();
        int ei = rand.nextInt(nExams);
        int ti = newSolution.findExam(ei);

        ArrayList<Integer> candidateTS = new ArrayList<>();
        for (int i = 0; i < nTimeslots; i++) {
            if (i != ti) {
                candidateTS.add(i);
            }
        }

        Collections.shuffle(candidateTS);
        outerFor:
        for (int tj : candidateTS) {
            for (int ej : newSolution.getTimetable()[tj]) {
                if (newSolution.swappable(ei, ti, ej, tj)) {
                    newSolution.popExam(ei, ti);
                    newSolution.popExam(ej, tj);

                    newSolution.placeExam(ei, tj);
                    newSolution.placeExam(ej, ti);
                    break outerFor;
                }
            }
        }

        newSolution.resetAttributes();

        return newSolution;
    }

    /**
     * Random N3
     */
    private static Solution swapTimeslots(Solution oldSolution, int ts1, int ts2) {
        Solution newSolution = new Solution(oldSolution);
        newSolution.getTimetable()[ts1] = (ArrayList) oldSolution.getTimetable()[ts2].clone();
        newSolution.getTimetable()[ts2] = (ArrayList) oldSolution.getTimetable()[ts1].clone();
        newSolution.resetAttributes();

        return newSolution;
    }

    /**
     * Steepest descent version of N3
     */
    private static Solution swapTimeslotsSD(Solution oldSolution) {
        int nTimeslots = oldSolution.getTimetable().length;
        Solution bestSolution = oldSolution;
        for (int i = 0; i < nTimeslots - 1; i++) {
            for (int j = i + 1; j < nTimeslots; j++) {
                Solution newSolution = LocalSearch.swapTimeslots(oldSolution, i, j);
                if (newSolution.getFitness() < bestSolution.getFitness()) {
                    bestSolution = newSolution;
                }
            }
        }
        return bestSolution;
    }

    private static Solution swapRandTimeslots(Solution oldSolution) {
        Random rand = new Random();
        int ts1 = rand.nextInt(oldSolution.getTimetable().length);
        int ts2;
        do {
            ts2 = rand.nextInt(oldSolution.getTimetable().length);
        } while (ts1 == ts2);

        return LocalSearch.swapTimeslots(oldSolution, ts1, ts2);
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
//        if (!newSolution.isFeasible()) {
//            System.err.println("Solution is not feasible!");
//        }
//
//        if (newSolution.getFitness() > oldSolution.getFitness()) {
//            System.out.println("[IMPROVEMENT N4] Shift between " + ts1 + " and " + ts2);
//        }

        return newSolution;
    }

    /**
     * N5-N6-N7-N8
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
            int bestTimeslot = examTS;
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
//
//        if (!newSolution.isFeasible()) {
//            System.err.println("Solution is not feasible!");
//        }

//        if (newSolution.getFitness() > oldSolution.getFitness()) {
//            System.out.println("[IMPROVEMENT N5-N6] Moved exam " + exam + " from ts " + examTS + " to ts "
//                    + newSolution.findExam(exam));
//        }
        return newSolution;
    }

    /**
     * N9
     */
    private static Solution kempeRandMove(Solution oldSolution) {
        Solution newSolution = new Solution(oldSolution);
        newSolution.resetAttributes();

        int nExams = newSolution.getInstance().getExams().length;
        Random rand = new Random();
        int ei = rand.nextInt(nExams);
        int ti = newSolution.findExam(ei);

        return kempeChainAlgorithm(newSolution, ei, ti);
    }

    /**
     * N10-N11
     */
    private static Solution kempeMove(Solution oldSolution, double ratio) {

        int[] highestPenaltyExam = oldSolution.getHighestPenaltyExam(ratio);
        int exam = highestPenaltyExam[0];
        int examTS = highestPenaltyExam[1];
        Solution newSolution = new Solution(oldSolution);
        newSolution.resetAttributes();

        return kempeChainAlgorithm(newSolution, exam, examTS);
    }

    private static Solution kempeChainAlgorithm(Solution newSolution, int exam, int examTS) {
        int nTimeslots = newSolution.getTimetable().length;

        int newTS;
        Random rand = new Random();
        do {
            newTS = rand.nextInt(nTimeslots);
        } while (newTS == examTS);

        ArrayList<Integer> queue1 = new ArrayList<>();
        queue1.add(exam);
        ArrayList<Integer> queue2 = new ArrayList<>();
        ArrayList<Integer> visited1 = new ArrayList<>();
        ArrayList<Integer> visited2 = new ArrayList<>();

        while (!queue1.isEmpty()) {
            while (!queue1.isEmpty()) {
                int currentExam = queue1.remove(0);
                visited1.add(currentExam);
                for (int ej : newSolution.getTimetable()[newTS]) {
                    if (!visited2.contains(ej) && !queue2.contains(ej) && newSolution.getInstance().getNConflicts(currentExam, ej) > 0) {
                        queue2.add(ej);
                    }
                }
            }

            while (!queue2.isEmpty()) {
                int currentExam = queue2.remove(0);
                visited2.add(currentExam);
                for (int ei : newSolution.getTimetable()[examTS]) {
                    if (!visited1.contains(ei) && !queue1.contains(ei) && newSolution.getInstance().getNConflicts(currentExam, ei) > 0) {
                        queue1.add(ei);
                    }
                }
            }
        }

//        System.out.println("Kempe-chain " + examTS + " - " + newTS + "\n" +
//                visited1.toString() + "\n" + visited2.toString());

        // remove kempe-chain exams from both timeslots
        for (int ei : visited1) {
            newSolution.popExam(ei, examTS);
        }
        for (int ej : visited2) {
            newSolution.popExam(ej, newTS);
        }

        // add kempe-chain exams each to its alternative timeslot
        for (int ei : visited1) {
            newSolution.placeExam(ei, newTS);
        }
        for (int ej : visited2) {
            newSolution.placeExam(ej, examTS);
        }
        return newSolution;
    }
}
