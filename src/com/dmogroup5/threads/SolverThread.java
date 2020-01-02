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
            this.solver.solveILS();
        } catch (InterruptedException e) {
            System.out.println("Solver execution interrupted");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }
}
