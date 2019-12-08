package com.dmogroup5.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Instance {

    private String instanceName;
    private String path;

    private int[] students;
    private int[] exams;
    private int nTimeslots;

    // `subscriptions` variable is optional, it's used just to check the reading of the instance files
    // See `validate()` method
    private int[] subscriptions;

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
     * Run validate on just read instance to check if data is inconsistent
     * 
     * @return false if something went wrong, true otherwise
     */
    public boolean readInstance(){
        try {
            this.readExams();
            this.readStudents();
            this.readNTimeslots();

            if (!this.validate()) {
                throw new IOException("Subscriptions to exams do not match, data may be wrong." +
                        " Please check the instance files.");
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
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

        File instanceFile = new File("instances/" + this.instanceName + ".exm");

        for (String line : Files.readAllLines(instanceFile.toPath())) {
            if (!line.equals("")) {
                String[] lineString = line.trim().split(" ");
                examsList.add(Integer.valueOf(lineString[0]));
                subscriptionsList.add(Integer.parseInt(lineString[1]));
            }
        }

        int nExams = examsList.size();
        this.exams = new int[nExams];
        this.subscriptions = new int[nExams];

        for (int i = 0; i < nExams; i++) {
            this.exams[i] = examsList.get(i);
            this.subscriptions[i] = subscriptionsList.get(i);
        }

        Arrays.sort(this.exams);
    }

    /**
     * Creates matrix of shape (nStudents, nExams). Element i,j is true if student i subscribed to exam j
     * otherwise is false
     *
     * NOTE: this implementation is NOT under the assumptions that every input element is of incremental
     * value (even if the test instances seem to be like that). For this reason the algorithm has to search
     * through arrays `students` and `exams` for matching with IDs.
     * If we can make the assumption that studID is in position studID-1 in the array,
     * this implementation could be re-writed for more performance in input reading.
     */
    private void readStudents() throws IOException {
        List<Integer> inputStudents = new ArrayList<>();
        List<Integer> inputExams = new ArrayList<>();

        File instanceFile = new File("instances/" + this.instanceName + ".stu");

        for (String line : Files.readAllLines(instanceFile.toPath())) {
            if (!line.equals("")) {
                String[] lineValues = line.trim().split(" ");
                inputStudents.add(Integer.valueOf(lineValues[0].substring(1)));
                inputExams.add(Integer.valueOf(lineValues[1]));
            }
        }

        // Create student set (to avoid duplicates from input)
        Set<Integer> noDuplicates = new HashSet<>(inputStudents);
        int nStudents = noDuplicates.size();
        int nExams = this.exams.length;

        // Create students array
        this.students = new int[nStudents];
        int k = 0;
        for (Integer sID : noDuplicates) {
            this.students[k++] = sID;
        }

        // Sort students array for better performance
        Arrays.sort(this.students);

        // Instantiate P matrix with input dimensions
        // All elements are initialized to `false`
        this.P = new boolean[nStudents][nExams];

        // Create P matrix scanning exams array for matching
        for (int i = 0; i < inputStudents.size(); i++) {
            // binary search require additional time (maybe not relevant)
            int studidx = Arrays.binarySearch(students, inputStudents.get(i));
            int examidx = Arrays.binarySearch(exams, inputExams.get(i));

            P[studidx][examidx] = true;
        }
    }

    private void readNTimeslots() throws IOException {
        File instanceFile = new File("instances/" + this.instanceName + ".slo");
        this.nTimeslots = Integer.parseInt(Files.readAllLines(instanceFile.toPath()).get(0).trim());
    }

    /**
     * Subscriptions written in the `.exm` file must be equal to the sum of the rows in the P matrix
     * which is obtained only from the `.stu` file with `readStudents()` method.
     * See method `validate()`
     *
     * @return true if input data is consistent
     */
    private boolean validate() {
        int[] a = this.subscriptions;
        // b elements are all initialized to 0 by default
        int[] b = new int[a.length];

        // sum rows of P, each column j contains (in total) the number of subscriptions for exam `exams[j]`
        for (int j = 0; j < this.P[0].length; j++) {
            for (int i = 0; i <this.P.length; i++) {
                // sum 1 if true, 0 otherwise (see `ternary operator` in Java, same as C/C++)
                b[j] += P[i][j] ? 1 : 0;
            }
        }

        boolean errorFound = false;
        int i = 0;
        while (!errorFound && i < a.length) {
            errorFound = a[i] != b[i];
            i++;
        }

        return !errorFound;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public int[] getExams() {
        return exams;
    }

    public boolean[][] getP() {
        return P;
    }

    public int getnTimeslots() {
        return nTimeslots;
    }
}
