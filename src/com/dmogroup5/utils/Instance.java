package com.dmogroup5.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class Instance {

    private String instanceName;
    private String directory;

    private int nStudents;
    private int[] exams;
    private int nTimeslots;

    // Conflicts matrix: N[i][j] = # of conflicts between exam in position i and exam in position j
    // shape: (nExams, nExams)
    private int[][] N;

    /**
     * Class with reading/writing (I/O) utility functions
     *
     * @param instanceName  Name of the instance without extension, e.g. 'instance01'
     */
    private Instance(String instanceName, String directory) {
        this.instanceName = instanceName;

        if (directory.endsWith("/")) {
            this.directory = directory;
        } else {
            this.directory = directory + "/";
        }
    }


    /**
     * Read all 3 instance files `.exm`, `.stu` and `.slo` and handle possible exceptions during reading
     * Run validate on the read instance to check if data is consistent. If it's not, print the error
     * and returns an empty instance
     *
     * @return the Instance object with all parameters set up if no errors are found in reading the files
 * @param instanceName
     */
    public static Instance readInstance(String instanceName) throws IOException {
        return readInstance(instanceName, "");
    }

    /**
     * Read all 3 instance files `.exm`, `.stu` and `.slo` and handle possible exceptions during reading
     * Run validate on the read instance to check if data is consistent. If it's not, print the error
     * and returns an empty instance
     * 
     * @return the Instance object with all parameters set up if no errors are found in reading the files
     */
    public static Instance readInstance(String instanceName, String directory) throws IOException {
        Instance instance = new Instance(instanceName, directory);

        instance.readExams();
        instance.readStudents();
        instance.readNTimeslots();

        return instance;
    }

    /**
     * Store the exams IDs in the `exams` array. Note: exams IDs must be integer but do not need to be
     * from 1 to nExams. This is for a more general input reading.
     * It also stores the number of subscriptions for each exams. These values are only used in the
     * `validate()` method.
     */
    private void readExams() throws IOException {
        List<Integer> examsList = new ArrayList<>();
        List<Integer> subscriptionsList = new ArrayList<>();

        File instanceFile = new File( this.directory + this.instanceName + ".exm");

        for (String line : Files.readAllLines(instanceFile.toPath())) {
            if (!line.equals("")) {
                String[] lineString = line.trim().split(" ");
                examsList.add(Integer.parseInt(lineString[0]));
                subscriptionsList.add(Integer.parseInt(lineString[1]));
            }
        }

        int nExams = examsList.size();
        this.exams = new int[nExams];

        for (int i = 0; i < nExams; i++) {
            this.exams[i] = examsList.get(i);
        }

        Arrays.sort(this.exams);
    }

    /**
     * Creates matrix of shape (nStudents, nExams). Element i,j is true if student i subscribed to exam j
     * otherwise is false.
     *
     * NOTE: this implementation is NOT under the assumptions that every input element is of incremental
     * value (even if the test instances seem to be like that). For this reason the algorithm has to search
     * through the array `exams` for matching with IDs.
     */
    // New implementation
    private void readStudents() throws IOException {

        File instanceFile = new File(this.directory + this.instanceName + ".stu");

        Map<Integer, ArrayList<Integer>> studentSubscriptions = new HashMap<>();

        for (String line : Files.readAllLines(instanceFile.toPath())) {
            if (!line.equals("")) {
                String[] lineValues = line.trim().split(" ");
                int sID = Integer.parseInt(lineValues[0].substring(1));
                int eID = Integer.parseInt(lineValues[1]);
                if (studentSubscriptions.containsKey(sID)) {
                    studentSubscriptions.get(sID).add(eID);
                }
                else {
                    studentSubscriptions.put(sID, new ArrayList<>());
                    studentSubscriptions.get(sID).add(eID);
                }
            }
        }

        this.nStudents = studentSubscriptions.size();

        // N matrix generation
        int nExams = this.exams.length;
        this.N = new int[nExams][nExams];

        for (ArrayList<Integer> subscriptions : studentSubscriptions.values()) {
            for (int i = 0; i < subscriptions.size() - 1; i++) {
                for (int j = i + 1; j < subscriptions.size(); j++) {
                    int ei = Arrays.binarySearch(this.exams, subscriptions.get(i));
                    int ej = Arrays.binarySearch(this.exams, subscriptions.get(j));
                    if (ei < ej) {
                        this.N[ei][ej] += 1;
                    } else {
                        this.N[ej][ei] += 1;
                    }
                }
            }
        }

    }

    private void readNTimeslots() throws IOException {
        File instanceFile = new File(this.directory + this.instanceName + ".slo");
        this.nTimeslots = Integer.parseInt(Files.readAllLines(instanceFile.toPath()).get(0).trim());
    }

    public String getInstanceName() {
        return instanceName;
    }

    public int[] getExams() {
        return exams;
    }

    public int getnTimeslots() {
        return nTimeslots;
    }

    /**
     * Method to manage the reading of an upper triangular matrix.
     *
     * @param i first exam index
     * @param j second exam index
     * @return  the number of students subscribed both to exams in position i and j
     * @throws Exception    if i equals j, the algorithm is probably wrong. An exam should not be compared with itself
     */
    public int getNConflicts(int i, int j) {
        if (i > j)
            return this.N[j][i];
        else if (i < j)
            return this.N[i][j];
        else {
            System.out.println("WARNING: an exam has been compared to itself, " +
                    "you might have put it twice in the timetable");
            return Integer.MAX_VALUE;
        }
//            throw new Exception("Exam " + i + " has been compared with itself!");
    }

    public int getnStudents() {
        return nStudents;
    }
}
