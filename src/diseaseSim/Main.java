/**
 * CS 351 - Project4 - Disease Simulation Project.
 * Utsav Malla, Aashish Basnet
 */

package diseaseSim;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import static java.lang.Math.random;

/**Contains the Main loop for the simulation.
 * All the objects are accessed through here and updated.
 */


public class Main extends Application {
    private int gridWidth = 100;
    private int gridHeight = 1;
    private double simWidth = 400;
    private double simHeight = 400;
    private int initSick = 1;
    private char gridInitMode = 'r';
    private int randAgents = 0;
    private double probToSpread = .3;
    private double probToGetSick = .75;
    private double probToDie = .25;
    private double daysOfIncubation = 5;
    private double daysOfSickness = 10;
    private double neighborRadius = 20;
    private final FileChooser chooser = new FileChooser();
    private static File config;
    private boolean gridConstructed = false;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Agent.setProbToSpread(probToSpread);
        Agent.setProbToGetSick(probToGetSick);
        Agent.setProbToDie(probToDie);
        Agent.setDaysOfSickness(daysOfSickness);
        Agent.setDaysOfIncubation(daysOfIncubation);

        StatsGraph statsGraph = new StatsGraph();
        HistoryPane historyPane = new HistoryPane();

        ArrayList<Agent> agents = new ArrayList<>();

        if (config != null) {
            Map<String, Double> params = FileIO.loadDiseaseParams(config);
            setParams(params);
        }

        Agent[][] grid = new Agent[gridHeight][gridWidth];
        gridConstructed = true;

        if (gridInitMode == 'h') {
            StatsGraph.setAgentNum(randAgents);
        } else {
            StatsGraph.setAgentNum(gridWidth*gridHeight);
        }

        switch (gridInitMode) {
            case 'g', 'h' -> {
                if (gridWidth*neighborRadius > simWidth) {
                    simWidth = gridWidth*neighborRadius;
                }

                if (gridHeight*neighborRadius > simHeight) {
                    simHeight = gridHeight*neighborRadius;
                }
            }
            case 'r' -> {
                double sqrt = Math.sqrt(gridWidth);

                if (sqrt*neighborRadius > simWidth) {
                    simWidth = sqrt*neighborRadius;
                }

                if (sqrt*neighborRadius > simHeight) {
                    simHeight = sqrt*neighborRadius;
                }
            }
        }

        UI ui = new UI(grid, simWidth, simHeight);

        switch (gridInitMode) {
            case 'g' -> initGrid(agents, grid);
            case 'r' -> initRandom(agents, grid, ui);
            case 'h' -> initHybrid(agents, grid);
        }

        getNeighbors(agents,neighborRadius);

        AnimationTimer timer = new AnimationTimer() {
            private long nextTime = 0;
            private long lastDay = 0;
            private long timeElapsed = 0;

            @Override
            public void handle(long now) {
                if (lastDay == 0) lastDay = now;

                if (Agent.isPaused()) {
                    if (timeElapsed == 0) timeElapsed = now - lastDay;
                    if (lastDay != 0) lastDay = 0;
                } else {
                    if (timeElapsed != 0) {
                        lastDay -= timeElapsed;
                        timeElapsed = 0;
                    }

                    if (now > lastDay + Agent.getDayLength()) {
                        HistoryPane.incDay();
                        lastDay += Agent.getDayLength();
                    }
                }

                if (now > nextTime){
                    getNeighbors(agents, neighborRadius);
                    nextTime = now + 1000000;
                    //printGrid(grid);
                    ui.updateAgents(gridHeight, gridWidth);
                }
            }
        };
        timer.start();

        VBox optionsBox = new VBox(10);
        optionsBox.getChildren().addAll(statsGraph,historyPane);

        VBox controls = new VBox(10);
        controls.setLayoutX(10);
        controls.setLayoutY(50);

        Label simCtrlLabel = new Label("Simulation Controls");

        HBox simCtrlButtons = new HBox(5);

        Button start = new Button("Start");
        start.setTextAlignment(TextAlignment.CENTER);
        start.setOnAction(e -> Agent.setPaused(false));

        Button pause = new Button("Pause");
        pause.setTextAlignment(TextAlignment.CENTER);
        pause.setOnAction(e -> Agent.setPaused(true));

        Button reset = new Button("Reset");
        reset.setTextAlignment(TextAlignment.CENTER);
        reset.setOnAction(e -> {
            Agent.setPaused(true);
            HistoryPane.reset();
            StatsGraph.reset();

            switch (gridInitMode) {
                case 'g' -> resetGrid(grid);
                case 'r' -> resetRandom(agents, grid, ui);
                case 'h' -> resetHybrid(agents, grid);
            }

            getNeighbors(agents, neighborRadius);
        });

        simCtrlButtons.getChildren().addAll(start, pause, reset);

        Label paramsLabel = new Label("Disease Parameters");

        HBox infRateBox = new HBox(10);
        Label infRateLabel = new Label("Infection Rate:");
        TextField infRateTF = new TextField(Double.toString(probToSpread));
        infRateBox.getChildren().addAll(infRateLabel, infRateTF);

        HBox asymRateBox = new HBox(10);
        Label asymRateLabel = new Label("Asymptomatic Rate:");
        TextField asymRateTF = new TextField(Double.toString(1-probToGetSick));
        asymRateBox.getChildren().addAll(asymRateLabel, asymRateTF);

        HBox ftlRateBox = new HBox(10);
        Label ftlRateLabel = new Label("Fatality Rate:");
        TextField ftlRateTF = new TextField(Double.toString(probToDie));
        ftlRateBox.getChildren().addAll(ftlRateLabel, ftlRateTF);

        HBox incPerBox = new HBox(10);
        Label incPerLabel = new Label("Incubation Period:");
        TextField incPerTF = new TextField(Double.toString(daysOfIncubation));
        incPerBox.getChildren().addAll(incPerLabel, incPerTF);

        HBox illPerBox = new HBox(10);
        Label illPerLabel = new Label("Illness Period:");
        TextField illPerTF = new TextField(Double.toString(daysOfSickness));
        illPerBox.getChildren().addAll(illPerLabel, illPerTF);

        Button confirmParams = new Button("Confirm");
        confirmParams.setTextAlignment(TextAlignment.CENTER);
        confirmParams.setOnAction(e -> {
            if (!infRateTF.getText().isEmpty()) {
                try {
                    double infRate = Double.parseDouble(infRateTF.getText());

                    if (infRate < 0 || infRate > 1) {
                        throw new NumberFormatException();
                    } else {
                        probToSpread = infRate;
                        Agent.setProbToSpread(infRate);
                    }
                } catch (NumberFormatException exc) {
                    infRateTF.setText(Double.toString(probToSpread));
                }
            }

            if (!asymRateTF.getText().isEmpty()) {
                try {
                    double asymRate = Double.parseDouble(asymRateTF
                            .getText());

                    if (asymRate < 0 || asymRate > 1) {
                        throw new NumberFormatException();
                    } else {
                        Agent.setProbToGetSick(1 - asymRate);
                        probToGetSick = 1 - asymRate;
                    }
                } catch (NumberFormatException exc) {
                    asymRateTF.setText(Double.toString(1 - probToGetSick));
                }
            }

            if (!ftlRateTF.getText().isEmpty()) {
                try {
                    double ftlRate = Double.parseDouble(ftlRateTF.getText());

                    if (ftlRate < 0 || ftlRate > 1) {
                        throw new NumberFormatException();
                    } else {
                        probToDie = ftlRate;
                        Agent.setProbToDie(ftlRate);
                    }
                } catch (NumberFormatException exc) {
                    ftlRateTF.setText(Double.toString(probToDie));
                }
            }

            if (!incPerTF.getText().isEmpty()) {
                try {
                    double incPeriod = Double.parseDouble(incPerTF.getText());

                    if (incPeriod < 0) {
                        throw new NumberFormatException();
                    } else {
                        daysOfIncubation = incPeriod;
                        Agent.setDaysOfIncubation(incPeriod);
                    }
                } catch (NumberFormatException exc) {
                    incPerTF.setText(Double.toString(daysOfIncubation));
                }
            }

            if (!illPerTF.getText().isEmpty()) {
                try {
                    double illPeriod = Double.parseDouble(illPerTF.getText());

                    if (illPeriod < 0) {
                        throw new NumberFormatException();
                    } else {
                        daysOfSickness = illPeriod;
                        Agent.setDaysOfSickness(illPeriod);
                    }
                } catch (NumberFormatException exc) {
                    illPerTF.setText(Double.toString(daysOfSickness));
                }
            }
        });


        Slider simSpeedSldr = new Slider(5,300, Agent.getSimSpeed());
        simSpeedSldr.setMajorTickUnit(50);
        simSpeedSldr.setShowTickMarks(true);
        simSpeedSldr.showTickLabelsProperty().setValue(true);
        simSpeedSldr.setOnMouseReleased(e ->
                Agent.setSimSpeed((long)simSpeedSldr.getValue())
        );

        Label fileOptLabel = new Label("File Options");

        HBox fileOptButtons = new HBox(5);

        Button load = new Button("Load File");
        load.setTextAlignment(TextAlignment.CENTER);
        load.setOnAction(e -> {
            chooser.setTitle("Choose Load File");

            try {
                File loadFile = chooser.showOpenDialog(primaryStage);

                if (loadFile != null) {
                    Map<String, Double> params = FileIO
                            .loadDiseaseParams(loadFile);

                    setParams(params);

                    infRateTF.setText(Double.toString(probToSpread));
                    asymRateTF.setText(Double.toString(1 - probToGetSick));
                    ftlRateTF.setText(Double.toString(probToDie));
                    incPerTF.setText(Double.toString(daysOfIncubation));
                    illPerTF.setText(Double.toString(daysOfSickness));
                    simSpeedSldr.setValue(Agent.getSimSpeed());
                }
            } catch (IOException exc) {
                //do nothing
            }
        });

        Button save = new Button("Save File");
        save.setTextAlignment(TextAlignment.CENTER);
        save.setOnAction(e -> {
            chooser.setTitle("Choose Save File");

            try {
                File saveFile = chooser.showSaveDialog(primaryStage);

                if (saveFile != null) {
                    List<String> params = new ArrayList<>();

                    if (neighborRadius != 20)
                        params.add("exposuredistance " + neighborRadius);

                    params.add("infection " + probToSpread);
                    params.add("asymptomatic " + (1 - probToGetSick));
                    params.add("fatality " + probToDie);
                    params.add("incubation " + daysOfIncubation);
                    params.add("sickness " + daysOfSickness);
                    params.add("move " + Agent.getSimSpeed());

                    FileIO.saveDiseaseParams(params, saveFile);
                }
            } catch (IOException exc) {
                System.err.println("Something happened");
            }
        });

        fileOptButtons.getChildren().addAll(load, save);

        controls.getChildren().addAll(simCtrlLabel, simSpeedSldr,
                simCtrlButtons, paramsLabel, infRateBox, asymRateBox,
                ftlRateBox, incPerBox, illPerBox, confirmParams,
                fileOptLabel, fileOptButtons);

        primaryStage.setOnCloseRequest(event -> System.exit(0));

        HBox root = new HBox(10, controls, ui, optionsBox);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    /**
     * This initializes the simulation program and
     * takes the config file from the command line.
     * @param args Command line arguments from the user
     */

    public static void main(String[] args) {
        try {
            File config = new File(args[0]);

            if (config != null) {
                Main.config = config;
            }
        } catch (Exception exc) {
            //do nothing
        }

        launch();
    }

    /**
     * Checks t=he neighbours of each agent who are later checked with other agents
     * and their distance is found using their coordinates and the distance
     * formula.
     * @param agents the agents in the simulation
     * @param neighborRadius neighbor radius
     */


    private synchronized static void getNeighbors(ArrayList<Agent> agents,
                                                  double neighborRadius){
        for (Agent agent : agents) {
            ArrayList<Agent> neighbors = new ArrayList<>();
            for (Agent neighbor :
                    agents) {
                double distance  = Math.pow(agent.getX()-neighbor.getX(),2);
                distance += Math.pow(agent.getY()-neighbor.getY(),2);
                distance = Math.sqrt(distance);
                if(Math.abs(distance) < neighborRadius) neighbors.add(neighbor);
            }
            agent.updateNeighbors(neighbors);
        }
    }

    /**
     * Sets and updates new simulation settings and disease parameters
     */

    protected synchronized void setParams(Map<String, Double> params) {
        if (!gridConstructed) {
            try {
                double mode = params.get("initmode");

                switch ((int) mode) {
                    case 0 -> {
                        gridInitMode = 'g';
                        try {
                            double w = params.get("width");
                            if (w > 0) gridWidth = (int) w;
                            double h = params.get("height");
                            if (h > 0) gridHeight = (int) h;
                        } catch (NullPointerException exc) {
                            //do nothing
                        }
                    }
                    case 1 -> {
                        gridInitMode = 'r';
                        try {
                            double agents = params.get("random");
                            if (agents > 0) {
                                gridWidth = (int) agents;
                                gridHeight = 1;
                            }
                        } catch (NullPointerException exc) {
                            //do nothing
                        }
                    }
                    case 2 -> {
                        gridInitMode = 'h';
                        try {
                            double w = params.get("width");
                            if (w > 0) gridWidth = (int) w;

                            double h = params.get("height");
                            if (h > 0) gridHeight = (int) h;

                            double numAgents = params.get("random");
                            if (numAgents > 0) {
                                if (numAgents <= h * w) {
                                    randAgents = (int) numAgents;
                                } else {
                                    randAgents = (int) Math.sqrt(h * w);
                                    System.err.println("WARNING: Cannot place " +
                                            numAgents + " agents into a " +
                                            gridHeight + "x" + gridWidth +
                                            " grid. Defaulting to " + randAgents +
                                            " agents.");
                                }
                            }
                        } catch (NullPointerException exc) {
                            //do nothing
                        }
                    }
                    default -> {
                        gridInitMode = 'r';
                        gridWidth = 100;
                        gridHeight = 1;
                    }
                }
            } catch (NullPointerException exc) {
                //do nothing
            }
        }

        try {
            double numSick = params.get("initialsick");
            if (numSick > 0) initSick = (int)numSick;
        } catch (NullPointerException exc) {
            //do nothing
        }

        try {
            double expDist = params.get("exposuredistance");
            if (expDist > 0) neighborRadius = expDist;
        } catch (NullPointerException exc) {
            //do nothing
        }

        try {
            double infRate = params.get("infection");

            if (infRate >= 0 && infRate <= 1) {
                probToSpread = infRate;
                Agent.setProbToSpread(infRate);
            }
        } catch (NullPointerException exc) {
            //do nothing
        }

        try {
            double asymRate = params.get("asymptomatic");

            if (asymRate >= 0 && asymRate <= 1) {
                probToGetSick = 1 - asymRate;
                Agent.setProbToGetSick(1 - asymRate);
            }
        } catch (NullPointerException exc) {
            //do nothing
        }

        try {
            double ftlRate = params.get("fatality");

            if (ftlRate >= 0 && ftlRate <= 1) {
                probToDie = ftlRate;
                Agent.setProbToDie(ftlRate);
            }
        } catch (NullPointerException exc) {
            //do nothing
        }

        try {
            double incPer = params.get("incubation");

            if (incPer > 0) {
                daysOfIncubation = incPer;
                Agent.setDaysOfIncubation(incPer);
            }
        } catch (NullPointerException exc) {
            //do nothing
        }

        try {
            double illPer = params.get("sickness");

            if (illPer > 0) {
                daysOfSickness = illPer;
                Agent.setDaysOfSickness(illPer);
            }
        } catch (NullPointerException exc) {
            //do nothing
        }

        try {
            long simDelay = params.get("move").longValue();

            if (simDelay >= 5 && simDelay <= 300) {
                Agent.setSimSpeed(simDelay);
                Agent.setMovement(true);
            } else if (simDelay < 0)
                Agent.setMovement(false);
        } catch (NullPointerException exc) {

        }
    }
    /**
     * Initializes all the agenets into a grid of rows and columns.
     * @param agents List containing all agents in the simulation
     * @param grid 2-D array of agents present in the simulation
     */


    private synchronized void initGrid(List<Agent> agents,
                                       Agent[][] grid) {
        for (int i = 0; i < gridHeight; i++) {
            for (int j = 0; j < gridWidth; j++) {
                double x = j*neighborRadius;
                double y = i*neighborRadius;
                int id = j + i*gridHeight;

                Agent a = new Agent(false, x, y, id);
                grid[i][j] = a;
                agents.add(a);
                Thread myThread = new Thread(a);
                myThread.start();
            }
        }

        int numSick = 0;

        while (numSick < initSick) {
            int row = (int)(random()*gridHeight);
            int col = (int)(random()*gridWidth);

            if (grid[row][col].getStateColor() != Color.SALMON) {
                grid[row][col].resetState(true);
                numSick++;
            }
        }
    }


    /**
     * Initializes the simulation with n agents placed at random locations
     * in the simulation space
     * @param agents List containing all agents in the simulation
     * @param grid Array of agents in the simulation
     * @param ui UI object used to display the simulation
     */
    private synchronized void initRandom(List<Agent> agents,
                                         Agent[][] grid, UI ui) {
        for (int i = 0; i < gridHeight; i++) {
            for (int j = 0; j < gridWidth; j++) {
                double x = random()*ui.getWIDTH();
                double y = random()*ui.getHEIGHT();
                int id = j + i*gridHeight;

                Agent a = new Agent(false, x, y, id);
                grid[i][j] = a;
                agents.add(a);
                Thread myThread = new Thread(a);
                myThread.start();
            }
        }

        int numSick = 0;

        while (numSick < initSick) {
            int index = (int)(random()*(agents.size() - 1));
            Agent agent = agents.get(index);

            if (agent.getStateColor() != Color.SALMON) {
                agent.resetState(true);
                numSick++;
            }
        }
    }


    /**
     * Initializes the simulation with n agents placed at random locations
     * within a grid of r rows and c columns
     * @param agents List containing all agents in the simulation
     * @param grid Array of agents in the simulation
     */
    private synchronized void initHybrid(List<Agent> agents,
                                         Agent[][] grid) {
        while (agents.size() < randAgents) {
            Agent a = new Agent(agents.size() < initSick,0,0,agents.size());
            agents.add(a);
            Thread myThread = new Thread(a);
            myThread.start();
        }

        Collections.shuffle(agents);
        int numPlaced = 0;

        while (numPlaced < randAgents) {
            int i = (int)(random()*gridHeight);
            int j = (int)(random()*gridWidth);

            if (grid[i][j] == null) {
                grid[i][j] = agents.get(numPlaced);
                grid[i][j].relocate(j*neighborRadius,i*neighborRadius);
                numPlaced++;
            }
        }
    }

    /**
     * Resets the simulation agents by returning the agents to a grid of r rows
     * and c columns, clearing all agent inboxes, and selecting new initially
     * infected agents
     * @param grid Array of agents in the simulation
     */


    private synchronized void resetGrid(Agent[][] grid) {
        for (int i = 0; i < gridHeight; i++) {
            for (int j = 0; j < gridWidth; j++) {
                grid[i][j].relocate(j*neighborRadius,i*neighborRadius);
                grid[i][j].resetState(false);
                grid[i][j].clearMessages();
            }
        }

        int numSick = 0;

        while (numSick < initSick) {
            int row = (int)(random()*gridHeight);
            int col = (int)(random()*gridWidth);

            if (grid[row][col].getStateColor() != Color.SALMON) {
                grid[row][col].resetState(true);
                numSick++;
            }
        }
    }

    /**
     * Resets the simulation agents by relocating them to random locations,
     * clearing all agent inboxes, and selecting new initially infected agents
     * @param agents List containing all agents in the simulation
     * @param grid Array of agents in the simulation
     * @param ui UI object used to display the simulation
     */


    private synchronized void resetRandom(List<Agent> agents,
                                          Agent[][] grid, UI ui) {
        for (int i = 0; i < gridHeight; i++) {
            for (int j = 0; j < gridWidth; j++) {
                double x = random()*ui.getWIDTH();
                double y = random()*ui.getHEIGHT();
                grid[i][j].relocate(x, y);
                grid[i][j].resetState(false);
                grid[i][j].clearMessages();
            }
        }

        int numSick = 0;

        while (numSick < initSick) {
            int index = (int)(random()*(agents.size() - 1));
            Agent agent = agents.get(index);

            if (agent.getStateColor() != Color.SALMON) {
                agent.resetState(true);
                numSick++;
            }
        }
    }

    /**
     * Relocates the simulation agents and relocates them to different locations
     *  and resets the simulation agents within the gird of columns and rows.
     * @param agents List containing all agents in the simulation
     * @param grid Array of agents in the simulation
     */

    private synchronized void resetHybrid(List<Agent> agents,
                                          Agent[][] grid) {
        for (int i = 0; i < gridHeight; i++) {
            for (int j = 0; j < gridWidth; j++) {
                grid[i][j] = null;
            }
        }

        for (int index = 0; index < agents.size(); index++) {
            agents.get(index).resetState(index < initSick);
            agents.get(index).clearMessages();
        }

        Collections.shuffle(agents);
        int numPlaced = 0;

        while (numPlaced < randAgents) {
            int i = (int)(random()*gridHeight);
            int j = (int)(random()*gridWidth);

            if (grid[i][j] == null) {
                grid[i][j] = agents.get(numPlaced);
                grid[i][j].relocate(j*neighborRadius,i*neighborRadius);
                numPlaced++;
            }
        }
    }
}


