import java.util.List;
import java.util.Map;

public class BayesianNode {
    private String name;
    private List<String> outcomes;
    private List<String> given;
    private boolean visited;
    private Map<List<Boolean>, Double> cpt; // New field for CPT

    public BayesianNode(String name, List<String> outcomes, List<String> given) {
        this.name = name;
        this.outcomes = outcomes;
        this.given = given;
        this.visited = false;
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

    public void setOutcomes(List<String> outcomes) {
        this.outcomes = outcomes;
    }

    public List<String> getGiven() {
        return given;
    }

    public void setGiven(List<String> given) {
        this.given = given;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public Map<List<Boolean>, Double> getCPT() {
        return cpt;
    }

    public void setCPT(Map<List<Boolean>, Double> cpt) {
        this.cpt = cpt;
    }
}
