package com.dmogroup5.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Instance {

    private String instanceName;
    private String path;

    private int[] students;
    private int[] exams;
    private int nTimeslots;
    private boolean[][] P;
    
    private int[] currentSol;

    /**
     * Class with reading/writing (I/O) utility functions
     *
     * @param instanceName  Name of the instance without extension, e.g. 'instance01'
     * @param path          Path of the folder containing the instance files, default: current path
     */
    public Instance(String instanceName, String path){
        this.path = path;
        this.instanceName = instanceName;
    }

    public Instance(String instanceName) {
        this(instanceName, "absolute_path");
    }


    /**
     * Read all 3 instance files `.exm`, `.stu` and `.slo` and handle possible exceptions during reading
     * 
     * @return false if something went wrong, true otherwise
     */
    public boolean readInstance(){
        try {
            this.readExams();
            this.readStudents();
            this.readNTimeslots();
        } catch (IOException e) {
            return false;
        }
        return true;
    }


    private void readExams() throws IOException {
        List<Integer> numbers = new ArrayList<>();
        File instanceFile = new File("instances/" + this.instanceName + ".exm");

        for (String line : Files.readAllLines(instanceFile.toPath())) {
            if (!line.equals("")) {
                numbers.add(Integer.valueOf(line.trim().split(" ")[0]));
            }
        }
        int nExams = numbers.size();
        this.exams = new int[nExams];

        for (int i = 0; i < nExams; i++) {
            this.exams[i] = numbers.get(i);
        }
    }

    /**
     * @return  Matrix of shape (nStudents, nExams). Element i,j is true if student i subscribed to exam j
     *          otherwise is false
     */
    private boolean[][] readStudents(){
        // TODO implement reading of `.stu` file
        return this.P;
    }

    /**
     * @return  Max number of timeslots
     */
    private int readNTimeslots(){
        // TODO implement reading of `.slo` file
        return this.nTimeslots;
    }

    /**
     * @param solution  Vector of size nExams containing timeslot ID for each exam
     */
    public void setCurrentSol(int[] solution) {
        this.currentSol = solution;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public int[] getExams() {
        return exams;
    }
}
