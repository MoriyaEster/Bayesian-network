import java.util.List;

public class BayesianNode {
    private String name;
    private List<String> outcomes;
    private List<String> given;

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

    public void setOutcomes(List<String> outcomes) {
        this.outcomes = outcomes;
    }

    public List<String> getGiven() {
        return given;
    }

    public void setGiven(List<String> given) {
        this.given = given;
    }
}
