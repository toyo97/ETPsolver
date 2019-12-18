package com.dmogroup5.heuristics;

import com.dmogroup5.utils.Solution;

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
        Solution newSolution;

        switch (neighStruct) {
            case N2:
//                newSolution = new Solution(oldSolution.getInstance());
                System.out.println("Called neighborhood " + NeighStructures.N2);
            case N3:
            case N4:
            case N5:
            case N6:
        }
        return new Solution(oldSolution.getInstance());
    }
}
