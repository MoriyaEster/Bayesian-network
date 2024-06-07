import java.util.*;

public class BayesBall {

    private final BayesianNetwork network;
    private final Map<String, BayesianNode> nodeMap;

    public BayesBall(BayesianNetwork network) {
        this.network = network;
        this.nodeMap = new HashMap<>();
        for (BayesianNode node : network.getNodes()) {
            nodeMap.put(node.getName(), node);
        }
    }

    public boolean isConditionallyIndependent(String start, String end, Set<String> evidence) {
        return !bayesBall(start, end, evidence, new HashSet<>(), Direction.CHILD);
    }

    private boolean bayesBall(String current, String target, Set<String> evidence, Set<String> visited, Direction direction) {
        if (current.equals(target)) {
            return true;
        }

        BayesianNode node = nodeMap.get(current);

        // Node is evidence
        if (evidence.contains(current)) {
            if (direction == Direction.CHILD) {
                return false; // Stop traversal
            } else {
                // From above or initial: Go to all parents and children
                return goToParents(node, target, evidence, visited, current) || goToChildren(node, target, evidence, visited, current);
            }
        } else {
            // Node is not evidence
            boolean result = false;
            if (direction == Direction.PARENT || direction == Direction.NONE) {
                // From above or initial: Go to children
                result = goToChildren(node, target, evidence, visited, current);
            }
            if (direction == Direction.CHILD || direction == Direction.NONE) {
                // From below or initial: Go to children and parents
                result = result || goToChildren(node, target, evidence, visited, current) || goToParents(node, target, evidence, visited, current);
            }
            if (result) {
                return true;
            }
        }

        // Add to visited after visiting all parents and children
        visited.add(current + direction);
        return false;
    }

    private boolean goToParents(BayesianNode node, String target, Set<String> evidence, Set<String> visited, String from) {
        for (String parent : node.getGiven()) {
            if (!parent.equals(from)) {
                if (bayesBall(parent, target, evidence, visited, Direction.CHILD)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean goToChildren(BayesianNode node, String target, Set<String> evidence, Set<String> visited, String from) {
        for (BayesianNode child : getChildren(node)) {
            if (!child.getName().equals(from)) {
                if (bayesBall(child.getName(), target, evidence, visited, Direction.PARENT)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<BayesianNode> getChildren(BayesianNode node) {
        List<BayesianNode> children = new ArrayList<>();
        for (BayesianNode potentialChild : network.getNodes()) {
            if (potentialChild.getGiven().contains(node.getName())) {
                children.add(potentialChild);
            }
        }
        return children;
    }

    private enum Direction {
        PARENT, CHILD, NONE
    }
}
