import java.io.*;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Read input file and parse content
        try (BufferedReader br = new BufferedReader(new FileReader("src/input.txt"));
             BufferedWriter bw = new BufferedWriter(new FileWriter("src/output.txt"))) {

            // Read the name of the XML file
            String xmlFileName = br.readLine().trim();
            BayesianNetwork network = XMLParser.parseXML("src/" + xmlFileName);

            // Process the rest of the input
            String line;
            boolean isBBQuery = true;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }

                if (line.startsWith("P(")) {
                    isBBQuery = false; // Switch to VE queries
                }

                if (isBBQuery) {
                    // Process BB Query
                    processBBQuery(line, network, bw);
                } else {
                    // Process VE Query
                    processVEQuery(line, network, bw);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processBBQuery(String query, BayesianNetwork network, BufferedWriter bw) {
        try {
            String[] parts = query.split("\\|");
            String path = parts[0].trim();
            String[] pathNodes = path.split("-");
            String startNode = pathNodes[0].trim();
            String endNode = pathNodes[1].trim();

            Set<String> evidence = new HashSet<>();
            if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                String[] evidenceParts = parts[1].split(",");
                for (String ev : evidenceParts) {
                    evidence.add(ev.trim());
                }
            }

            BayesBall bayesBall = new BayesBall(network);
            boolean independent = bayesBall.isConditionallyIndependent(startNode, endNode, evidence);

            bw.write(independent ? "yes" : "no");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processVEQuery(String query, BayesianNetwork network, BufferedWriter bw) {
        try {
            // Example input: P(B=T|J=T,M=T) A-E
            String[] parts = query.split("\\) ");
            if (parts.length != 2) {
                return; // Skip invalid lines
            }

            String queryPart = parts[0].substring(2); // Remove "P("
            String[] queryEvidenceParts = queryPart.split("\\|");
            String queryVar = queryEvidenceParts[0].trim();
            Map<String, Boolean> evidence = new HashMap<>();

            if (queryEvidenceParts.length > 1) {
                String evidencePart = queryEvidenceParts[1].trim();
                String[] evidenceVariables = evidencePart.split(",");
                for (String ev : evidenceVariables) {
                    String[] evParts = ev.split("=");
                    if (evParts.length == 2) {
                        evidence.put(evParts[0].trim(), evParts[1].trim().equals("T"));
                    }
                }
            }

            String path = parts[1].trim();
            String[] pathNodes = path.split("-");
            if (pathNodes.length < 2) {
                return; // Skip invalid paths
            }

            String startNode = pathNodes[0].trim();
            String endNode = pathNodes[1].trim();
            List<String> queryList = Arrays.asList(startNode, endNode);
            List<String> hiddenVariables = new ArrayList<>();

            // Identify hidden variables
            for (BayesianNode node : network.getNodes()) {
                String nodeName = node.getName();
                if (!evidence.containsKey(nodeName) && !queryList.contains(nodeName)) {
                    hiddenVariables.add(nodeName);
                }
            }

            // Run the variable elimination algorithm
            VariableElimination ve = new VariableElimination(network, evidence, queryList, hiddenVariables);
            double result = ve.run();

            // Write the result to the output file
            bw.write(String.format("%.5f,%d,%d", result, hiddenVariables.size(), ve.getMultiplicationCount()));
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
