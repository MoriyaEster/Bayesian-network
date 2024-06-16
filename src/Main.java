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

    private static Set<String> parseEvidence(Set<String> evidenceSet) {
        Set<String> evidenceNodes = new HashSet<>();
        for (String ev : evidenceSet) {
            String[] parts = ev.split("=");
            if (parts.length == 2) {
                evidenceNodes.add(parts[0].trim());
            }
        }
        return evidenceNodes;
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
            boolean independent = bayesBall.areIndependent(startNode, endNode, parseEvidence(evidence));

            bw.write(independent ? "yes" : "no");
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processVEQuery(String query, BayesianNetwork network, BufferedWriter bw) {
        try {
            // Example input: P(M=Y|N=T,S=good,F=nice) A-E
            String[] parts = query.split("\\)");

            // Handle case without hidden variables
            String path = parts.length == 2 ? parts[1].trim() : "";

            // Extract the whole_query part and the path part
            String whole_query = parts[0].substring(2); // Remove "P("
            System.out.println("whole_query = " + whole_query);
            String[] queryEvidenceParts = whole_query.split("\\|");
            String queryVarPart = queryEvidenceParts[0].trim();
            System.out.println("queryVarPart = " + queryVarPart);

            // Extract the query variable and its outcome
            String[] queryVariableParts = queryVarPart.split(",");
            Map<String, String> queryVariables = new HashMap<>();
            for (String qv : queryVariableParts) {
                String[] qvParts = qv.split("=");
                if (qvParts.length == 2) {
                    queryVariables.put(qvParts[0].trim(), qvParts[1].trim());
                }
            }

            // Extract evidence if it exists
            Map<String, String> evidence = new HashMap<>();
            if (queryEvidenceParts.length > 1) {
                String evidencePart = queryEvidenceParts[1].trim();
                String[] evidenceVariables = evidencePart.split(",");
                for (String ev : evidenceVariables) {
                    String[] evParts = ev.split("=");
                    if (evParts.length == 2) {
                        evidence.put(evParts[0].trim(), evParts[1].trim());
                    }
                }
            }

            List<String> hiddenVariables = new ArrayList<>();
            if (!path.isEmpty()) {
                String[] pathNodes = path.split("-");
                if (pathNodes.length == 2) {
                    String startNode = pathNodes[0].trim();
                    String endNode = pathNodes[1].trim();
                    hiddenVariables.add(startNode);
                    hiddenVariables.add(endNode);
                }
            }

            // Identify hidden variables
            for (BayesianNode node : network.getNodes()) {
                String nodeName = node.getName();
                if (!evidence.containsKey(nodeName) && !queryVariables.containsKey(nodeName) && !hiddenVariables.contains(nodeName)) {
                    hiddenVariables.add(nodeName);
                }
            }

            // Run the variable elimination algorithm
            System.out.println("evidence = " + evidence + ", queryVariables = " + queryVariables + ", hiddenVariables = " + hiddenVariables);
            VariableElimination ve = new VariableElimination(network, evidence, queryVariables, hiddenVariables);
            double result = ve.run();

            // Write the result to the output file
            bw.write(String.format("%.5f,%d,%d", result, ve.getAdditionCount(), ve.getMultiplicationCount()));
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}