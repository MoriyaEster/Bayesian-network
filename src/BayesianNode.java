import java.util.List;
import java.util.Map;

public class BayesianNode {
    private String name;
    private List<String> outcomes;
    private List<String> given;
    private Map<List<String>, Double> cpt; // New field for CPT

    public BayesianNode(String name, List<String> outcomes, List<String> given) {
        this.name = name;
        this.outcomes = outcomes;
        this.given = given;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getOutcomes() {
        return outcomes;
    }

    public List<String> getGiven() {
        return given;
    }

    public void setGiven(List<String> given) {
        this.given = given;
    }

    public Map<List<String>, Double> getCPT() {
        return cpt;
    }

    public void setCPT(Map<List<String>, Double> cpt) {
        this.cpt = cpt;
    }
}
