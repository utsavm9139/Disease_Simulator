/**
 * CS 351 - Project4 - Disease Simulation Project.
 * Utsav Malla, Aashish Basnet
 */

package diseaseSim;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FileIO {

    public static void saveDiseaseParams(List<String> diseaseParams,
                                         File saveFile) throws IOException {
        FileWriter fw = new FileWriter(saveFile);

        for (String line : diseaseParams) {
            fw.append(line);
            fw.append("\n");
        }

        fw.close();
    }


    public static Map<String, Double> loadDiseaseParams(File loadFile)
            throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(loadFile));
        Map<String, Double> diseaseParams = new HashMap<>();
        String line;

        while ((line = br.readLine()) != null) {
            String[] arr = line.split(" ");

            if (arr.length == 2) {
                if (arr[0].length() > arr[1].length()) {
                    try {
                        double value = Double.parseDouble(arr[1]);
                        diseaseParams.put(arr[0], value);

                        if (arr[0].equalsIgnoreCase("random")) {
                            diseaseParams.put("initmode", (double)1);
                        }
                    } catch (NumberFormatException exc) {
                        if (arr[0].equalsIgnoreCase("move") &&
                                arr[1].equalsIgnoreCase("off")) {
                            diseaseParams.put(arr[1], (double)-1);
                        }
                    }
                }
            } else if (arr.length == 3 &&
                    arr[0].equalsIgnoreCase("grid")) {
                diseaseParams.put("initmode", (double)0);
                try {
                    double rows = Double.parseDouble(arr[1]);
                    diseaseParams.put("height", rows);
                    double cols = Double.parseDouble(arr[2]);
                    diseaseParams.put("width", cols);
                } catch (NumberFormatException exc) {
                    //do nothing
                }
            } else if (arr.length == 4 &&
                    arr[0].equalsIgnoreCase("randomgrid")) {
                diseaseParams.put("initmode", (double)2);
                try {
                    double rows = Double.parseDouble(arr[1]);
                    diseaseParams.put("height", rows);
                    double cols = Double.parseDouble(arr[2]);
                    diseaseParams.put("width", cols);
                    double agents = Double.parseDouble(arr[3]);
                    diseaseParams.put("random", agents);
                } catch (NumberFormatException exc) {
                    //do nothing
                }
            }
        }
        br.close();

        return diseaseParams;
    }
}

//    public static void saveGameState(Agent[][] grid, String fileName) throws IOException {
//        StringBuilder gridState = new StringBuilder();
//        for (int i = 0; i < grid.length; i++) {
//            for (int j = 0; j < grid.length; j++) {
//                gridState.append(grid[i][j].toString());
//                if(j != grid.length-1) gridState.append(",");
//            }
//            gridState.append("\n");
//        }
//        File saveFile = new File(fileName);
//        FileWriter fw = new FileWriter(saveFile);
//
//        fw.append(gridState);
//        fw.close();
//    }
