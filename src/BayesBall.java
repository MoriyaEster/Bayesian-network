import java.util.*;

public class BayesBall {
    private BayesianNetwork network;
    private Set<String> visitedNodes;

    public BayesBall(BayesianNetwork network) {
        this.network = network;
        this.visitedNodes = new HashSet<>();
    }

    public boolean areIndependents(String start, String end, Set<String> evidence) {
        this.visitedNodes.clear();
        return !BBTravel(start, end, evidence, Direction.DOWN);
    }

    private List<String> getParents(String nodeName) {
        List<String> parents = new ArrayList<>();
        for (BayesianNode node : network.getNodes()) {
            if (node.getName().equals(nodeName)) {
                parents.addAll(node.getGiven());
            }
        }
        //System.out.println("Parents of " + nodeName + ": " + parents);
        return parents;
    }

    private List<String> getChildren(String nodeName) {
        List<String> children = new ArrayList<>();
        for (BayesianNode node : network.getNodes()) {
            if (node.getGiven().contains(nodeName)) {
                children.add(node.getName());
            }
        }
        //System.out.println("Children of " + nodeName + ": " + children);
        return children;
    }

    private boolean BBTravel(String current, String target, Set<String> evidence, Direction direction) {
        if (current.equals(target)) {
            return true;
        }

        List<String> parents = getParents(current);
        List<String> children = getChildren(current);

        if (!visitedNodes.add(current + direction)) {
            return false;
        }

        if (!evidence.contains(current)) {
            if (direction == Direction.DOWN) {
                return traverseChildrenFirst(children, target, evidence) || traverseParentsLater(parents, target, evidence);
            } else if (direction == Direction.UP) {
                return traverseChildrenFirst(children, target, evidence);
            }
        } else {
            if (direction == Direction.DOWN) {
                return false;
            } else if (direction == Direction.UP) {
                return traverseParentsFirst(parents, current, target, evidence);
            }
        }
        return false;
    }

    private boolean traverseChildrenFirst(List<String> children, String target, Set<String> evidence) {
        for (String child : children) {
            if (BBTravel(child, target, evidence, Direction.UP)) {
                return true;
            }
        }
        return false;
    }

    private boolean traverseParentsLater(List<String> parents, String target, Set<String> evidence) {
        for (String parent : parents) {
            if (BBTravel(parent, target, evidence, Direction.DOWN)) {
                return true;
            }
        }
        return false;
    }

    private boolean traverseParentsFirst(List<String> parents, String current, String target, Set<String> evidence) {
        for (String parent : parents) {
            if (!current.equals(parent) && BBTravel(parent, target, evidence, Direction.DOWN)) {
                return true;
            }
        }
        return false;
    }

    private enum Direction {
        UP, DOWN
    }
}
