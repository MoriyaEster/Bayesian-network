import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
                }
            }

            NodeList definitionList = doc.getElementsByTagName("DEFINITION");
            for (int temp = 0; temp < definitionList.getLength(); temp++) {
                Node nNode = definitionList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    String name = eElement.getElementsByTagName("FOR").item(0).getTextContent();
                    NodeList givenNodes = eElement.getElementsByTagName("GIVEN");
                    List<String> given = new ArrayList<>();
                    for (int i = 0; i < givenNodes.getLength(); i++) {
                        given.add(givenNodes.item(i).getTextContent());
                    }
                    for (BayesianNode node : network.getNodes()) {
                        if (node.getName().equals(name)) {
                            node.setGiven(given);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return network;
    }
}
