package com.dmogroup5.heuristics;

import com.dmogroup5.utils.Solution;

public class GeneticAlgorithms {

    /**
     * Courses are chosen at random from any point in the timetable and are reallocated
     * to the earliest (after that point) feasible timeslots
     *
     * @param parent    starting solution
     * @param ratio     percentage of the course to be reassigned
     * @return          mutated solution
     */
    public static Solution mutate(Solution parent, double ratio) {
        Solution mutatedSol = parent;
        // TODO implement
        return mutatedSol;
    }
}
