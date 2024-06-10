import java.util.*;

public class BayesBall {
    private BayesianNetwork network;
    private Set<String> visited = new HashSet<>();

    public BayesBall(BayesianNetwork network) {
        this.network = network;
    }

    public boolean isConditionallyIndependent(String start, String end, Set<String> evidenceSet) {
        Set<String> evidence = parseEvidence(evidenceSet);
        visited.clear();
        return !traverseBayesBall(start, end, evidence, "down");
    }

    private Set<String> parseEvidence(Set<String> evidenceSet) {
        Set<String> evidenceNodes = new HashSet<>();
        for (String ev : evidenceSet) {
            String[] parts = ev.split("=");
            if (parts.length == 2) {
                evidenceNodes.add(parts[0].trim());
            }
        }
        return evidenceNodes;
    }

    private boolean traverseBayesBall(String current, String target, Set<String> evidence, String direction) {
        System.out.println("Visiting: " + current + ", Direction: " + direction + ", Evidence: " + evidence);

        List<String> parents = getParents(current);
        List<String> children = getChildren(current);

        if (visited.contains(current) && children.stream().anyMatch(visited::contains) && parents.stream().anyMatch(visited::contains)) {
            return false;
        }
        visited.add(current);

        if (current.equals(target)) {
            System.out.println("Reached target: " + target);
            return true;
        }

        if (!evidence.contains(current)) {
            if (direction.equals("down")) {
                // Traverse to children and parents if current node is not in the evidence and direction is down
                for (String child : children) {
                    if (traverseBayesBall(child, target, evidence, "up")) {
                        return true;
                    }
                }
                for (String parent : parents) {
                    if (traverseBayesBall(parent, target, evidence, "down")) {
                        return true;
                    }
                }
            } else if (direction.equals("up")) {
                // Traverse to children if current node is not in the evidence and direction is up
                for (String child : children) {
                    if (traverseBayesBall(child, target, evidence, "up")) {
                        return true;
                    }
                }
            }
        } else {
            if (direction.equals("down")) {
                return false; // Stop descending if current node is in the evidence and direction is down
            } else if (direction.equals("up")) {
                // Traverse to parents if current node is in the evidence and direction is up
                for (String parent : parents) {
                    if (!current.equals(parent) && traverseBayesBall(parent, target, evidence, "down")) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private List<String> getParents(String nodeName) {
        List<String> parents = new ArrayList<>();
        for (BayesianNode node : network.getNodes()) {
            if (node.getName().equals(nodeName)) {
                parents.addAll(node.getGiven());
            }
        }
        System.out.println("Parents of " + nodeName + ": " + parents);
        return parents;
    }

    private List<String> getChildren(String nodeName) {
        List<String> children = new ArrayList<>();
        for (BayesianNode node : network.getNodes()) {
            if (node.getGiven().contains(nodeName)) {
                children.add(node.getName());
            }
        }
        System.out.println("Children of " + nodeName + ": " + children);
        return children;
    }
}
