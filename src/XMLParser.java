import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

public class XMLParser {

    public static BayesianNetwork parseXML(String filePath) {
        BayesianNetwork network = new BayesianNetwork();
        try {
            File inputFile = new File(filePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList variableList = doc.getElementsByTagName("VARIABLE");
            Map<String, BayesianNode> nodeMap = new HashMap<>();
            Map<String, List<String>> outcomeMap = new HashMap<>(); // To store outcomes for each variable

            for (int temp = 0; temp < variableList.getLength(); temp++) {
                Node nNode = variableList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String name = eElement.getElementsByTagName("NAME").item(0).getTextContent();
                    NodeList outcomeNodes = eElement.getElementsByTagName("OUTCOME");
                    List<String> outcomes = new ArrayList<>();
                    for (int i = 0; i < outcomeNodes.getLength(); i++) {
                        outcomes.add(outcomeNodes.item(i).getTextContent());
                    }
                    BayesianNode node = new BayesianNode(name, outcomes, new ArrayList<>());
                    network.addNode(node);
                    nodeMap.put(name, node);
                    outcomeMap.put(name, outcomes); // Store outcomes for the variable
                }
            }

            NodeList definitionList = doc.getElementsByTagName("DEFINITION");
            for (int temp = 0; temp < definitionList.getLength(); temp++) {
                Node nNode = definitionList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String name = eElement.getElementsByTagName("FOR").item(0).getTextContent();
                    BayesianNode node = nodeMap.get(name);

                    NodeList givenNodes = eElement.getElementsByTagName("GIVEN");
                    List<String> given = new ArrayList<>();
                    for (int i = 0; i < givenNodes.getLength(); i++) {
                        given.add(givenNodes.item(i).getTextContent());
                    }
                    node.setGiven(given);

                    NodeList tableNodes = eElement.getElementsByTagName("TABLE");
                    String[] tableValues = tableNodes.item(0).getTextContent().trim().split("\\s+");
                    Map<List<String>, Double> cpt = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order

                    // Collect all outcomes for the given variables and the current node
                    List<List<String>> givenOutcomes = new ArrayList<>();
                    for (String givenVar : given) {
                        givenOutcomes.add(outcomeMap.get(givenVar));
                    }
                    givenOutcomes.add(outcomeMap.get(name)); // Add outcomes for the current node

                    List<List<String>> combinations = generateOrderedCombinations(givenOutcomes);

                    int index = 0;
                    for (List<String> combination : combinations) {
                        cpt.put(combination, Double.parseDouble(tableValues[index++]));
                    }
                    node.setCPT(cpt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return network;
    }

    private static List<List<String>> generateOrderedCombinations(List<List<String>> givenOutcomes) {
        List<List<String>> combinations = new ArrayList<>();
        generateCombinationsHelper(combinations, givenOutcomes, new ArrayList<>(), 0);
        return combinations;
    }

    private static void generateCombinationsHelper(List<List<String>> combinations, List<List<String>> givenOutcomes, List<String> current, int index) {
        if (index == givenOutcomes.size()) {
            combinations.add(new ArrayList<>(current));
            return;
        }

        for (String outcome : givenOutcomes.get(index)) {
            current.add(outcome);
            generateCombinationsHelper(combinations, givenOutcomes, current, index + 1);
            current.remove(current.size() - 1);
        }
    }
}
