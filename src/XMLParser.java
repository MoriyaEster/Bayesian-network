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
                    List<List<Boolean>> combinations = generateCombinations(given.size() + 1);
                    Map<List<Boolean>, Double> cpt = new HashMap<>();

                    for (int i = 0; i < combinations.size(); i++) {
                        cpt.put(combinations.get(i), Double.parseDouble(tableValues[i]));
                    }
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
            for (int j = n - 1; j >= 0; j--) {
                combination.add((i / (1 << j)) % 2 == 1);
            }
            combinations.add(combination);
        }
        return combinations;
    }
}
