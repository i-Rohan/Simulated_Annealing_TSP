import org.graphstream.algorithm.measure.ChartMeasure;
import org.graphstream.algorithm.measure.ChartSeries2DMeasure;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Rohan Sharma on 08-04-2016.
 */
public class GUISimulatedAnnealingTSP extends SimulatedAnnealingTSP {

    private static ChartSeries2DMeasure m1 = new ChartSeries2DMeasure("Current Solution");
    private static ChartSeries2DMeasure m2 = new ChartSeries2DMeasure("Temperature");
//    private static ChartSeries2DMeasure m3=new ChartSeries2DMeasure("Time");

    private static Graph g = new MultiGraph("Graph");

    //function to read from file and generate graph gui
    private static void read() throws FileNotFoundException, ChartMeasure.PlotException {

        m1.plot();
        m2.plot();
//        m3.plot();

        m1.setWindowSize(Integer.MAX_VALUE);
        m2.setWindowSize(Integer.MAX_VALUE);
//        m3.setWindowSize(Integer.MAX_VALUE);

        Scanner scanner = new Scanner(new File("input.txt"));
        CITY_COUNT = scanner.nextInt();
        X = new float[CITY_COUNT];
        Y = new float[CITY_COUNT];

        String stylesheet = "node{text-size:10px;size:5px;text-color:red;text-style:bold;} " +
                "edge{visibility-mode:hidden;}" +
                "edge.marked{fill-color:green;size:3px;visibility-mode:normal;}";
        g.addAttribute("ui.stylesheet", stylesheet);
        Viewer v = g.display();
        v.disableAutoLayout();

        Node n;
        for (int i = 0; i < CITY_COUNT; i++) {
//            System.out.println("Reading line " + (i + 1) + " out of " + CITY_COUNT + " lines");
            n = g.addNode(Integer.toString(i + 1));
            X[i] = scanner.nextFloat();
            Y[i] = scanner.nextFloat();
            n.setAttribute("xy", X[i], Y[i]);
        }

        n = g.addNode("A");
        n.addAttribute("ui.style", "size:0px;text-size:15px;text-color:black;text-alignment:center;");
        n.setAttribute("xy", (getMin(X, CITY_COUNT) + getMax(X, CITY_COUNT)) / 2,
                getMax(Y, CITY_COUNT) + (getMax(Y, CITY_COUNT) - getMin(Y, CITY_COUNT)) / 1.1);

        n = g.addNode("B");
        n.addAttribute("ui.style", "size:0px;text-size:15px;text-color:black;");
        n.setAttribute("xy", (getMin(X, CITY_COUNT) + getMax(X, CITY_COUNT)) / 2,
                getMax(Y, CITY_COUNT) + (getMax(Y, CITY_COUNT) - getMin(Y, CITY_COUNT)) / 1.25);

        n = g.addNode("C");
        n.addAttribute("ui.style", "size:0px;text-size:15px;text-color:black;");
        n.setAttribute("xy", (getMin(X, CITY_COUNT) + getMax(X, CITY_COUNT)) / 2,
                getMax(Y, CITY_COUNT) + (getMax(Y, CITY_COUNT) - getMin(Y, CITY_COUNT)) / 1.5);

        n = g.addNode("D");
        n.addAttribute("ui.style", "size:0px;text-size:15px;text-color:black;");
        n.setAttribute("xy", (getMin(X, CITY_COUNT) + getMax(X, CITY_COUNT)) / 2,
                getMax(Y, CITY_COUNT) + (getMax(Y, CITY_COUNT) - getMin(Y, CITY_COUNT)) / 1.75);

        while (scanner.hasNext())
            TARGET = scanner.nextDouble();

        int temp = 1;
        for (int i = 0; i < CITY_COUNT; i++)
            for (int j = 0; j < CITY_COUNT; j++) {
//                System.out.println("Adding Edge " + temp++ + " of " + CITY_COUNT * CITY_COUNT);
                g.addEdge(Integer.toString(i + 1) + "_" + Integer.toString(j + 1), Integer.toString(i + 1), Integer.toString(j + 1));
            }
    }

    //function to get min element of array
    private static float getMin(float[] array, int size) {
        float min = array[0];
        for (int i = 1; i < size; i++)
            if (array[i] < min)
                min = array[i];
        return min;
    }

    //function to get maximum element of array
    private static float getMax(float[] array, int size) {
        float max = array[0];
        for (int i = 1; i < size; i++)
            if (array[i] > max)
                max = array[i];
        return max;
    }

    //main simulated annealing algorithm function
    private static void simulatedAnnealingAlgorithm() throws ChartMeasure.PlotException {
        boolean solution = false;
        boolean useNew;
        double TEMPERATURE = 100;

        initializeSolution();

        currentSolution.computeEnergy();

        bestSolution.solutionEnergy(currentSolution.solutionEnergy());

        workingSolution.equals(currentSolution);

        while (TEMPERATURE > FINAL_TEMPERATURE) {    //&& bestSolution.solutionEnergy() > TARGET) {
            for (int i = 0; i < ITERATIONS_AT_TEMPERATURE; i++) {
                useNew = false;

                workingSolution.randomChange();

                workingSolution.computeEnergy();

                if (workingSolution.solutionEnergy() <= currentSolution.solutionEnergy())
                    useNew = true;
                else {
                    double test = new Random().nextDouble(); //  Get random value between 0.0 and 1.0
                    double delta = workingSolution.solutionEnergy() - currentSolution.solutionEnergy();
                    double calc = Math.exp(-delta / TEMPERATURE);
                    if (calc > test) {
                        useNew = true;
                    }
                }
                if (useNew) {
                    currentSolution.equals(workingSolution);
                    if (currentSolution.solutionEnergy() < bestSolution.solutionEnergy()) {
                        bestSolution.equals(currentSolution);
                        solution = true;
                    }
                } else
                    workingSolution.equals(currentSolution);

                for (int j = 0; j < CITY_COUNT; j++)
                    for (int k = 0; k < CITY_COUNT; k++) {
                        Edge e;
                        e = g.getEdge(Integer.toString(j + 1) + "_" + Integer.toString(k + 1));
                        e.removeAttribute("ui.class");
                        e.removeAttribute("z");
                    }

                int temp = -1;
                for (int j = 0; j < CITY_COUNT; j++) {
                    if (temp != -1) {
                        Edge e = g.getEdge(Integer.toString(temp + 1) + "_" + Integer.toString(bestSolution.data(j) + 1));
                        e.setAttribute("ui.class", "marked");
                    }
                    temp = bestSolution.data(j);
                }
                Edge e = g.getEdge(Integer.toString(bestSolution.data(0) + 1) + "_" + Integer.toString(bestSolution.data(CITY_COUNT - 1) + 1));
                e.setAttribute("ui.class", "marked");
                e.setAttribute("z", 1);

                Node n = g.getNode("A");
                n.setAttribute("ui.label", "Distance: " + bestSolution.solutionEnergy());
                n = g.getNode("B");
                n.setAttribute("ui.label", "Temperature: " + TEMPERATURE);
                n = g.getNode("C");
                n.setAttribute("ui.label", "Iterations: " + TOTAL_ITERATIONS);
                n = g.getNode("D");
                n.setAttribute("ui.label", "Target: " + TARGET);

                m1.addValue(TOTAL_ITERATIONS, currentSolution.solutionEnergy());
                m2.addValue(TOTAL_ITERATIONS, TEMPERATURE);
//                m3.addValue(TOTAL_ITERATIONS,(double)(System.currentTimeMillis()-INITIAL_TIME)/1000);

                TOTAL_ITERATIONS++;
            }
            TEMPERATURE *= ALPHA;
        }

        if (solution) {
            System.out.println("Best solution: " + bestSolution.solutionEnergy());
            int temp = -1;
            for (int j = 0; j < CITY_COUNT; j++) {
                if (temp != -1) {
                    Edge e = g.getEdge(Integer.toString(temp + 1) + "_" + Integer.toString(bestSolution.data(j) + 1));
                    e.setAttribute("ui.class", "marked");
                    e.setAttribute("z", 1);
                }
                temp = bestSolution.data(j);
            }
            Edge e = g.getEdge(Integer.toString(bestSolution.data(0) + 1) + "_" + Integer.toString(bestSolution.data(CITY_COUNT - 1) + 1));
            e.setAttribute("ui.class", "marked");
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
            System.out.print("Time used: " +(System.currentTimeMillis() - INITIAL_TIME) / 1000 + " secs");
        }
    }

    //main function
    public static void main(String[] args) throws IOException, ChartMeasure.PlotException {
        read();
        currentSolution = new Solution();
        workingSolution = new Solution();
        bestSolution = new Solution();

        initializeCities();

        simulatedAnnealingAlgorithm();
    }
}