import java.util.ArrayList;
import java.util.List;

public class BayesianNetwork {
    private final List<BayesianNode> nodes;

    public BayesianNetwork() {
        this.nodes = new ArrayList<>();
    }

    public void addNode(BayesianNode node) {
        nodes.add(node);
    }

    public List<BayesianNode> getNodes() {
        return nodes;
    }
}
