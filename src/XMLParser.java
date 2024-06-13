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
                    Map<List<Boolean>, Double> cpt = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order

                    int numGiven = given.size();
                    int numOutcomes = node.getOutcomes().size();
                    List<List<Boolean>> combinations = generateCombinations(numGiven);

                    int index = 0;
                    for (List<Boolean> combination : combinations) {
                        for (int outcomeIndex = 0; outcomeIndex < numOutcomes; outcomeIndex++) {
                            List<Boolean> key = new ArrayList<>(combination);
                            key.add(outcomeIndex == 0); // Add true/false for the node's outcome
                            cpt.put(key, Double.parseDouble(tableValues[index++]));
                        }
                    }
                    System.out.println("cpt = "+ cpt);
                    node.setCPT(cpt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return network;
    }

    private static List<List<Boolean>> generateCombinations(int n) {
        List<List<Boolean>> combinations = new ArrayList<>();
        int size = (int) Math.pow(2, n);
        for (int i = 0; i < size; i++) {
            List<Boolean> combination = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                combination.add((i & (1 << j)) != 0);
            }
            combinations.add(combination);
        }
        return combinations;
    }
}
