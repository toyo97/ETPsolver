package com.dmogroup5;

import com.dmogroup5.heuristics.GeneticAlgorithms;
import com.dmogroup5.heuristics.LocalSearch;
import com.dmogroup5.utils.Instance;
import com.dmogroup5.utils.Logger;
import com.dmogroup5.utils.Solution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Solver {

    private Instance instance;
    private boolean verbose;

    public Solver(Instance instance, boolean verbose) {
        this.instance = instance;
        this.verbose = verbose;
    }


    /**
     * @throws Exception caused mainly if an exam is compared to itself in the N matrix
     */
    public void solveGA() throws Exception {
        System.out.println("Solving " + this.instance.getInstanceName());

        // Test reading exams
        System.out.println("Number of exams: " + this.instance.getExams().length);

        // Test P matrix creation
//        System.out.println("First row of the P matrix:\n" + Arrays.toString(this.instance.getP()[0]));

        // Test nTimeslot reading
        System.out.println("Number of timeslots in " + this.instance.getInstanceName() +
                ": " + this.instance.getnTimeslots());

        // Test N matrix creation
        System.out.println("Num of conflicts between exams 1,6 (IDs): " + this.instance.getNConflicts(5,0));

        Solution solution = null;
        int count = 0;
        while (solution == null) {
            count += 1;
            solution = Solution.weightedSolution(this.instance, true);
        }
        System.out.println("Number of attempts before feasible solution: " + count);
        for (ArrayList timeslot: solution.getTimetable()) {
            System.out.println(timeslot);
        }
        System.out.println("OBJ VALUE: " + solution.getFitness());

        Logger logger = new Logger();

        // *********** PARAMETERS ***********
        // size of the population
        // TODO use a pop size which is a function of the density of the instance
        int POP_SIZE = 30;
        // how many individuals (in percentage) are chosen from the population for generating children
        double SEL_RATIO = 0.2;
        // how many different neighborhood structures we want to explore for each parent
        int N_NEIGH_STR = LocalSearch.NeighStructures.values().length;
        // genetic mutation ratio (see class GeneticAlgorithms for more details)
        double MUTATION_RATIO = 0.2;

        // ************ POPULATION GENERATION ************
        Solution[] population = new Solution[POP_SIZE];

        // generate the initial population and already write the best solution among all the individuals
        for (int i = 0; i < population.length; i++) {
            population[i] = Solution.weightedSolution(this.instance, true);
        }

        // ************ MAIN LOOP ***********
        int it = 0;
        while (!Thread.currentThread().isInterrupted()) {
            // ****** BEST SOLUTION PRINTOUT ******
            int bestIdx = 0;
            for (int i = 0; i < population.length; i++) {
                if (population[bestIdx].getFitness() > population[i].getFitness()) {
                    bestIdx = i;
                }
            }
            System.out.println("Best solution at it " + it + ": " + population[bestIdx].getFitness());

            // take the best and output this first result
            Solution bestSol = population[bestIdx];
            try {
                bestSol.writeSolution();
                logger.appendCurrentBest(bestSol.getFitness());
                if (this.verbose) {
                    System.out.println("File written successfully");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // ****** CANDIDATES SELECTION ******
            // select 20% (selection percentage/ratio) of the population individuals
            int nSelected = (int) (POP_SIZE * SEL_RATIO);
            // TODO see what's better
//            int[] parentsIdx = new Random().ints(nSelected, 0, POP_SIZE).toArray();
            Solution[] parents = GeneticAlgorithms.rouletteWheelSelection(Arrays.asList(population), nSelected);

            Solution[] children = new Solution[nSelected];
            for (int i = 0; i < nSelected; i++) {

                // ****** MUTATION ******
                children[i] = GeneticAlgorithms.mutateSolution(parents[i], MUTATION_RATIO);

                // ****** LOCAL SEARCH ******
                // Find the first improvement or just one new
                // solution in the neighborhood for every structure (called twins)
                // Then put the best between the `k` twins in the pool (as a child)
                Solution[] twins = new Solution[N_NEIGH_STR];

                double twinsBestF = Double.MAX_VALUE;
                int bestTwinIdx = 0;

                for (LocalSearch.NeighStructures k: LocalSearch.NeighStructures.values()) {

                    twins[k.ordinal()] = LocalSearch.genImprovedSolution(children[i], k);
                    double tmpScore = twins[k.ordinal()].getFitness();
                    if (tmpScore < twinsBestF) {
                        twinsBestF = tmpScore;
                        bestTwinIdx = k.ordinal();
                    }
                }

                children[i] = twins[bestTwinIdx];
            }

            // ****** POOL SELECTION ******
            // use roulette-wheel selection to pick the next generation population
            List<Solution> pool = new ArrayList<>(Arrays.asList(population));
            pool.addAll(Arrays.asList(children));

//            population = GeneticAlgorithms.rouletteWheelSelection(pool, POP_SIZE);
            population = GeneticAlgorithms.bestFirstSelection(pool, POP_SIZE);

            it++;
        }
    }

    public void solveILS() throws Exception {
        Solution current = Solution.weightedSolution(this.instance, true);
        Logger logger = new Logger();
        while (!Thread.currentThread().isInterrupted()) {
            try {
                current.writeSolution();
                logger.appendCurrentBest(current.getFitness());

                if (this.verbose) {
                    System.out.println("File written successfully");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            current = iterativeImprovement(current);

        }
    }

    private Solution iterativeImprovement(Solution parent) {
        int N_NEIGH_STR = LocalSearch.NeighStructures.values().length;
        Solution[] twins = new Solution[N_NEIGH_STR];

        double twinsBestF = Double.MAX_VALUE;
        int bestTwinIdx = 0;
        for (LocalSearch.NeighStructures k: LocalSearch.NeighStructures.values()) {

            twins[k.ordinal()] = LocalSearch.genImprovedSolution(parent, k);
            double tmpScore = twins[k.ordinal()].getFitness();
            if (tmpScore < twinsBestF) {
                twinsBestF = tmpScore;
                bestTwinIdx = k.ordinal();
            }
        }

        Solution bestSolution;
        if (twinsBestF < parent.getFitness()) {
            bestSolution = twins[bestTwinIdx];
            System.out.println("better N" + (bestTwinIdx+2));
        } else {
            double delta = twinsBestF - parent.getFitness();
            double pick = new Random().nextDouble();
            if (pick < Math.exp(-delta)) {
                bestSolution = twins[bestTwinIdx];
                System.out.println("worse N" + (bestTwinIdx+2) + " delta: " + delta);
            } else {
                bestSolution = parent;
                System.out.println("nochange");
            }
        }

        return bestSolution;
    }
}
