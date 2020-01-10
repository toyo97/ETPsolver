package com.dmogroup5;

import com.dmogroup5.parser.Params;
import com.dmogroup5.threads.SolverThread;
import com.dmogroup5.utils.*;
import org.apache.commons.cli.ParseException;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        Params params = new Params(args);

        // Read command line parameters and handle exception
        try {
            // Parse the input from command line
            params.parse();

            // Pass the name of the instance and read the files
            Instance instance = Instance.readInstance(params.getInstanceName());

            // Start the execution of the solver in a separate thread
            Solver solver = new Solver(instance, params.isDEBUG(), params.getTLIM());
            SolverThread solverThread = new SolverThread(solver);
            solverThread.start();

            // Wait for the requested timelim (converted in ms) and interrupt the execution of the solver
            Thread.sleep((long) (params.getTLIM() * 1000));
            System.out.println("Time available elapsed!");
            solverThread.interrupt();

        } catch (ParseException | IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
