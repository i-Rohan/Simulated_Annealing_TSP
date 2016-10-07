import org.graphstream.algorithm.measure.ChartMeasure;
import org.graphstream.algorithm.measure.ChartSeries2DMeasure;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Rohan Sharma on 04-04-2016.
 */
public class SimulatedAnnealingTSP{
    static final long INITIAL_TIME=System.currentTimeMillis();
    private static final double INITIAL_TEMPERATURE = 100.0;
    static final double FINAL_TEMPERATURE = 0.5;
    static final double ALPHA = 0.99;
    static double TARGET;
    static int CITY_COUNT;
    static float X[];
    static float Y[];
    static int ITERATIONS_AT_TEMPERATURE=100;
    static long TOTAL_ITERATIONS=0;

    static Solution currentSolution;
    static Solution workingSolution;
    static Solution bestSolution;

    private static ArrayList<City> cities = new ArrayList<>();

    private static ChartSeries2DMeasure m=new ChartSeries2DMeasure("Acceptance Probability");
//    private static  ChartSeries2DMeasure m2 =new ChartSeries2DMeasure("Random number between 0 and 1");

    //main method
    public static void main(String[] args) throws IOException, ChartMeasure.PlotException {
        read();
        currentSolution = new Solution();
        workingSolution = new Solution();
        bestSolution = new Solution();

        initializeCities();

        simulatedAnnealingAlgorithm();
        //new SimulatedAnnealingTSP();
    }
    
    //method to read node data from file
    private static void read() throws FileNotFoundException, ChartMeasure.PlotException {

        m.plot();
        m.setWindowSize(Integer.MAX_VALUE);
//        m2.plot();
//        m2.setWindowSize(Integer.MAX_VALUE);

        Scanner scanner = new Scanner(new File("input.txt"));
        CITY_COUNT = scanner.nextInt();
        X = new float[CITY_COUNT];
        Y = new float[CITY_COUNT];

        for (int i = 0; i < CITY_COUNT; i++) {
//            scanner.nextInt();
            //System.out.println("Reading line " + (i + 1) + " out of " + CITY_COUNT + " lines");
            X[i] = scanner.nextFloat();
            Y[i] = scanner.nextFloat();
        }

        while (scanner.hasNext())
            TARGET = scanner.nextDouble();
    }

    //main simulated annealing algorithm method
    private static void simulatedAnnealingAlgorithm() {
        boolean solution = false;
        boolean useNew;
        double TEMPERATURE = INITIAL_TEMPERATURE;

        initializeSolution();

        currentSolution.computeEnergy();
//        System.out.println("Distance: " + currentSolution.solutionEnergy());

        bestSolution.solutionEnergy(currentSolution.solutionEnergy());

        workingSolution.equals(currentSolution);

//        m.addValue(0,0);

//        long INITIAL_TIME=System.currentTimeMillis();

        while (TEMPERATURE > FINAL_TEMPERATURE) {

            for (int i = 0; i < ITERATIONS_AT_TEMPERATURE; i++) {
                useNew = false;

                workingSolution.randomChange();

                workingSolution.computeEnergy();
//                System.out.println("Distance: " + workingSolution.solutionEnergy());

                if (workingSolution.solutionEnergy() <= currentSolution.solutionEnergy()) {
                    useNew = true;
                } else {
                    double test = new Random().nextDouble(); //  Get random value between 0.0 and 1.0
                    double delta = workingSolution.solutionEnergy() - currentSolution.solutionEnergy();
                    double calc = Math.exp(-delta / TEMPERATURE);
                    if (calc > test) {
                        useNew = true;
                    }
                    m.addValue(TOTAL_ITERATIONS,calc);
//                    m2.addValue(TOTAL_ITERATIONS,test);
                }

                if (useNew) {
                    currentSolution.equals(workingSolution);
                    if (currentSolution.solutionEnergy() < bestSolution.solutionEnergy()) {
                        bestSolution.equals(currentSolution);
                        solution = true;
                    }
                } else {
                    workingSolution.equals(currentSolution);
                }

//                System.out.println("Current Solution Energy: " + currentSolution.solutionEnergy());
//                System.out.println("Working Solution Energy: " + workingSolution.solutionEnergy());
//                System.out.println("Best Solution Energy: " + bestSolution.solutionEnergy());
//
                TOTAL_ITERATIONS++;

//                m.addValue(TOTAL_ITERATIONS,(double)(System.currentTimeMillis()-INITIAL_TIME)/1000);
            }
            TEMPERATURE *= ALPHA;
            //System.out.println(bestSolution.solutionEnergy()+"\nTemperature: " + TEMPERATURE);
        }

        if (solution) {
            System.out.println("Best solution: " + bestSolution.solutionEnergy());
            if (bestSolution.solutionEnergy() <= TARGET) {
                //System.out.println("Best solution is: Correct");
                if (bestSolution.solutionEnergy() < TARGET) {
                    TARGET = bestSolution.solutionEnergy();
                    try {
                        Files.write(Paths.get("input.txt"), ("\n" + Double.toString(TARGET)).getBytes(), StandardOpenOption.APPEND);
                    } catch (IOException e3) {
                        e3.printStackTrace();
                    }
                }
            }
            System.out.println("Finished");
            System.out.println("Time used: "+(double)(System.currentTimeMillis()-INITIAL_TIME)/1000+" secs");
            System.out.println("Total Iterations: "+TOTAL_ITERATIONS);
        }
    }

    static void initializeCities() {
        City city;

        for (int i = 0; i < CITY_COUNT; i++) {
            city = new City();
            city.x(X[i]);
            city.y(Y[i]);
            cities.add(city);
        }
    }

    static void initializeSolution() {
        // Initial setup of the solution.
        for (int i = 0; i < CITY_COUNT; i++) {
            currentSolution.data(i, i);
        }

        // Randomly perturb the solution.
        for (int i = 0; i < CITY_COUNT; i++) {
            currentSolution.randomChange();
        }
    }

    private static double getDistance(final int FirstCity, final int SecondCity) {
        City CityA;
        City CityB;
        double A2;
        double B2;
        CityA = cities.get(FirstCity);
        CityB = cities.get(SecondCity);
        A2 = Math.pow(Math.abs(CityA.x() - CityB.x()), 2);
        B2 = Math.pow(Math.abs(CityA.y() - CityB.y()), 2);

        return Math.sqrt(A2 + B2);
    }

    private static int getExclusiveRandomNumber(final int high, final int except) {
        boolean done = false;
        int getRand = 0;

        while (!done) {
            getRand = new Random().nextInt(high);
            if (getRand != except) {
                done = true;
            }
        }

        return getRand;
    }

    static class Solution {
        private double SolutionEnergy = 0.0;
        private int Data[] = null;

        Solution() {
            Data = new int[CITY_COUNT];
        }

        void equals(Solution that) {
            for (int i = 0; i < CITY_COUNT; i++) {
                this.Data[i] = that.data(i);
            }
            this.SolutionEnergy = that.SolutionEnergy;
        }

        void data(int index, int value) {
            this.Data[index] = value;
        }

        int data(int index) {
            return this.Data[index];
        }

        void solutionEnergy(double value) {
            this.SolutionEnergy = value;
        }

        double solutionEnergy() {
            return this.SolutionEnergy;
        }

        void randomChange() {
            int temp;
            int x;
            int y;

            // Get two different random numbers.
            x = new Random().nextInt(CITY_COUNT);
            y = getExclusiveRandomNumber(CITY_COUNT, x);

            temp = this.Data[x];
            this.Data[x] = this.Data[y];
            this.Data[y] = temp;
        }

        void computeEnergy() {
            this.SolutionEnergy = 0.0;
            // Find the round-trip distance.
            for (int i = 0; i < CITY_COUNT; i++) {
                if (i == CITY_COUNT - 1) {
                    this.SolutionEnergy += getDistance(this.Data[CITY_COUNT - 1], this.Data[0]); // Complete trip.
                } else {
                    this.SolutionEnergy += getDistance(this.Data[i], this.Data[i + 1]);
                }
            }
        }

    } // Solution class

    private static class City {
        private float mX = 0;
        private float mY = 0;

        float x() {
            return mX;
        }

        void x(final float xCoordinate) {
            mX = xCoordinate;
        }

        float y() {
            return mY;
        }

        void y(final float yCoordinate) {
            mY = yCoordinate;
        }
    } // City class
}