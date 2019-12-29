package com.dmogroup5.heuristics;

import com.dmogroup5.utils.Solution;
import java.util.List;
import java.util.Random;

public class GeneticAlgorithms {

    /**
     * A percentage of the total courses are chosen at random from any point in the timetable and are reallocated
     * to the earliest (after that point) feasible time slots
     *
     * @param parent    starting solution
     * @param ratio     percentage of the course to be reassigned
     * @return          mutated solution
     */
    public static Solution mutateSolution(Solution parent, double ratio) throws Exception {
        int totExams = parent.getInstance().getExams().length;
        int nCandidates = (int) (totExams * ratio);
        Solution mutatedSol = new Solution(parent);

        for (int i = 0; i < nCandidates; i++) {
            int exam = mutatedSol.popRandExam();
            boolean examAssigned;
            do {
                examAssigned = mutatedSol.placeExam(exam, true);
            } while (!examAssigned);
        }
        mutatedSol.resetFitness();
        return mutatedSol;
    }

    /**
     * Selection of the new (fixed-sized) generation individuals from the current pool with the new children.
     * Solutions are selected randomly with a probability which is proportional to their fitness score.
     *
     * @param pool      set of total current solutions: previous generation individuals plus new generated children
     * @param popSize   size of the population to be maintained
     * @return          array of the solutions of the new generation population
     */
    public static Solution[] rouletteWheelSelection(List<Solution> pool, int popSize) throws Exception {
        double max = 0;
        for (Solution individual : pool) {
            max += individual.getFitness();
        }

        Solution[] newPopulation = new Solution[popSize];
        double[] randPoints = new Random().doubles(popSize, 0, max).toArray();
        for (int i = 0; i < popSize; i++) {
            int pickIdx = 0;
            double current = pool.get(pickIdx).getFitness();
            while (current < randPoints[i]) {
                pickIdx += 1;
                current += pool.get(pickIdx).getFitness();
            }

            newPopulation[i] = pool.get(pickIdx);
        }

        return newPopulation;
    }

    public static Solution[] bestFirstSelection(List<Solution> pool, int popSize) {
        pool.sort((solA, solB) -> {
                double x = solA.getFitness();
                double y = solB.getFitness();
                return Double.compare(x, y);
        });

        Solution[] bestFirst = new Solution[popSize];
        for (int i = 0; i < popSize; i++) {
            bestFirst[i] = pool.get(i);
        }
        return bestFirst;
    }
}
