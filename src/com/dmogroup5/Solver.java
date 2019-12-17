package com.dmogroup5;

import com.dmogroup5.heuristics.GeneticAlgorithms;
import com.dmogroup5.heuristics.LocalSearch;
import com.dmogroup5.utils.Instance;
import com.dmogroup5.utils.Solution;

import java.io.IOException;
import java.util.ArrayList;

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
    public void solve() throws Exception {
        // TODO implement solver
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
            solution = Solution.weightedSolution(this.instance);
        }
        System.out.println("Number of attempts before feasible solution: " + count);
        for (ArrayList timeslot: solution.getTimetable()) {
            System.out.println(timeslot);
        }
        System.out.println("OBJ VALUE: " + solution.computeObj());

        // *********** PARAMETERS ***********
        // size of the population
        // TODO use a pop size which is a function of the density of the instance
        int POP_SIZE = 50;
        // how many individuals (in percentage) are chosen from the population for generating children
        double SEL_RATIO = 0.2;
        // how many different neighborhood structures we want to explore for each parent
        int N_NEIGH_STR = 4;
        // genetic mutation ratio (see class GeneticAlgorithms for more details)
        double MUTATION_RATIO = 0.2;

        // ************ POPULATION GENERATION ************
        Solution[] population = new Solution[POP_SIZE];

        // generate the initial population and already write the best solution among all the individuals
        int bestIdx = 0;
        double bestF = Double.MAX_VALUE;
        for (int i = 0; i < population.length; i++) {
            population[i] = Solution.weightedSolution(this.instance);
            double tempF = population[i].computeObj();
            if (bestF > tempF) {
                bestF = tempF;
                bestIdx = i;
            }
        }
        System.out.println("Best solution: " + bestF);

        // take the best and output this first result
        Solution bestSol = population[bestIdx];
        try {
            bestSol.writeSolution();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // ************ MAIN LOOP ***********
        while (true) {
            // select 20% (selection percentage/ratio) of the population individuals
            int nSelected = (int) (POP_SIZE * SEL_RATIO);
            int[] parentsIdx = getSelIndividuals(nSelected);

            Solution[] children = new Solution[nSelected];
            for (int idx: parentsIdx){
                // ****** MUTATION ******
                children[idx] = GeneticAlgorithms.mutate(population[idx], MUTATION_RATIO);

                // ****** LOCAL SEARCH ******
                // find the first improvement in the neighborhood for every structure
                for (LocalSearch.NeighStructures k: LocalSearch.NeighStructures.values()){
                    children[k.ordinal()] = LocalSearch.genImprovedSolution(population[idx], k);
                }
            }
            // write the current solution
            try {
                solution.writeSolution();
            } catch (IOException e) {
                e.printStackTrace();
            }



            Thread.sleep(3000);
            if (this.verbose) {
                System.out.println("File written successfully");
            }
            System.out.println("3 seconds passed...");
        }
    }
    
    private int[] getSelIndividuals(int nSelected) {
        int[] selected = new int[nSelected];
        
        // TODO implement
        
        return selected;
    }

}
