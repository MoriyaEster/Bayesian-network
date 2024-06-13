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

                    List<List<Boolean>> combinations = generateOrderedCombinations(given.size() + 1); // +1 for the node itself

                    int index = 0;
                    for (List<Boolean> combination : combinations) {
                        cpt.put(combination, Double.parseDouble(tableValues[index++]));
                    }
                    node.setCPT(cpt);
                    System.out.println("cpt = " + cpt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return network;
    }

    private static List<List<Boolean>> generateOrderedCombinations(int n) {
        List<List<Boolean>> combinations = new ArrayList<>();
        generateCombinationsHelper(combinations, new Boolean[n], 0);
        return combinations;
    }

    private static void generateCombinationsHelper(List<List<Boolean>> combinations, Boolean[] current, int index) {
        if (index == current.length) {
            combinations.add(new ArrayList<>(Arrays.asList(current)));
            return;
        }

        // Generate with current index as true
        current[index] = true;
        generateCombinationsHelper(combinations, current, index + 1);

        // Generate with current index as false
        current[index] = false;
        generateCombinationsHelper(combinations, current, index + 1);
    }
}
