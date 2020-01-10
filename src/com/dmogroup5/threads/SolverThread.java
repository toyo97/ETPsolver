package com.dmogroup5.threads;

import com.dmogroup5.Solver;
import com.dmogroup5.utils.Instance;

public class SolverThread extends Thread {

    private Solver solver;

    public SolverThread(Solver solver){
        this.solver = solver;
    }

    @Override
    public void run() {
        try {
            // The temperature depends on how much time is dedicated to the solving algorithm
            // From a time in range 180-300 sec, temp will be around 900-1200 (empirically found to
            // be appropriate)
            double temp = 2.5 * this.solver.getSolvingTime();
            this.solver.solveILS(temp);
        } catch (InterruptedException e) {
            System.out.println("Solver execution interrupted");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
