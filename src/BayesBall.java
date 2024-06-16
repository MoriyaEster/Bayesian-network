import java.util.*;

public class BayesBall {
    private BayesianNetwork network;
    private Set<String> visited = new HashSet<>();

    public BayesBall(BayesianNetwork network) {
        this.network = network;
    }

    public boolean areIndependent(String start, String end, Set<String> evidence) {
        //System.out.println("evidenceSet = "+evidence);
        visited.clear();
        return !travelBayesBall(start, end, evidence, "down");
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

    private boolean travelBayesBall(String current, String target, Set<String> evidence, String direction) {
        //System.out.println("Visiting: " + current + ", Direction: " + direction + ", Evidence: " + evidence);

        if (current.equals(target)) {
            //System.out.println("Reached target: " + target);
            return true;
        }

        List<String> parents = getParents(current);
        List<String> children = getChildren(current);

        if (visited.contains(current + direction)) return false;
        visited.add(current + direction);

        if (!evidence.contains(current)) {
            if (direction.equals("down")) {
                // Traverse to children and parents if current node is not in the evidence and direction is down
                for (String child : children) {
                    if (travelBayesBall(child, target, evidence, "up")) {
                        return true;
                    }
                }
                for (String parent : parents) {
                    if (travelBayesBall(parent, target, evidence, "down")) {
                        return true;
                    }
                }
            }
            else if (direction.equals("up")) {
                // Traverse to children if current node is not in the evidence and direction is up
                for (String child : children) {
                    if (travelBayesBall(child, target, evidence, "up")) {
                        return true;
                    }
                }
            }
        } else {
            // Stop descending if current node is in the evidence and direction is down
            if (direction.equals("down")) {
                return false;
            }
            // Traverse to parents if current node is in the evidence and direction is up
            else if (direction.equals("up")) {
                for (String parent : parents) {
                    if (!current.equals(parent) && travelBayesBall(parent, target, evidence, "down")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}