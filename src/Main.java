import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        // Parse the XML to create the Bayesian Network
        BayesianNetwork network = XMLParser.parseXML("src/alarm_net.xml");

        // Read input from input.txt and write output to output.txt
        try (BufferedReader br = new BufferedReader(new FileReader("src/input.txt"));
             BufferedWriter bw = new BufferedWriter(new FileWriter("src/output.txt"))) {

            BayesBall bayesBall = new BayesBall(network);

            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue; // Skip empty lines
                }

                String[] parts = line.split("\\|");
                if (parts.length < 1) {
                    continue; // Skip invalid lines
                }

                String query = parts[0].trim();
                String[] queryParts = query.split("-");
                if (queryParts.length < 2) {
                    continue; // Skip invalid queries
                }

                String startNode = queryParts[0].trim();
                String endNode = queryParts[1].trim();

                Set<String> evidence = new HashSet<>();
                if (parts.length > 1 && !parts[1].trim().isEmpty()) {
                    String[] evidenceParts = parts[1].split(",");
                    for (String ev : evidenceParts) {
                        evidence.add(ev.trim());
                    }
                }

                boolean independent = bayesBall.isConditionallyIndependent(startNode, endNode, evidence);
                bw.write(independent ? "yes" : "no");
                bw.newLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
