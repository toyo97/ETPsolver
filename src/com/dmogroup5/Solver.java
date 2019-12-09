package com.dmogroup5;

import com.dmogroup5.utils.Instance;
import com.dmogroup5.utils.Solution;

import java.util.ArrayList;
import java.util.Arrays;

public class Solver {

    private Instance instance;
    private boolean verbose;

    public Solver(Instance instance, boolean verbose) {
        this.instance = instance;
        this.verbose = verbose;
    }

    public void solve() throws InterruptedException {
        // TODO implement solver
        System.out.println("Solving " + this.instance.getInstanceName());

        // Test reading exams
        System.out.println("Number of exams: " + this.instance.getExams().length);

        // Test P matrix creation
        System.out.println("First row of the P matrix:\n" + Arrays.toString(this.instance.getP()[0]));

        // Test nTimeslot reading
        System.out.println("Number of timeslots in " + this.instance.getInstanceName() +
                ": " + this.instance.getnTimeslots());

        // Test N matrix creation
        System.out.println("First row of the N matrix:\n" +
                Arrays.toString(this.instance.getN()[0]));

        Solution solution = Solution.randomSolution(this.instance);
        for (ArrayList timeslot: solution.getTimetable()) {
            System.out.println(timeslot);
        }

        while (true) {
            Thread.sleep(3000);
            if (writeSolution() && this.verbose) {
                System.out.println("File written successfully");
            }
            System.out.println("3 seconds passed...");
        }
    }

    /**
     * @return  true if successful writing
     */
    private boolean writeSolution(){
        // TODO implement solution output writing in `.sol` file
        return true;
    }
}
