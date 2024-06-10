import java.util.*;

public class BayesBall {
    private BayesianNetwork network;

    public BayesBall(BayesianNetwork network) {
        this.network = network;
    }

    public boolean isConditionallyIndependent(String start, String end, Set<String> evidenceSet) {
        Set<String> evidence = parseEvidence(evidenceSet);
        Set<String> visited = new HashSet<>();
        return !canReach(start, end, evidence, visited, "down", null);
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

    private boolean canReach(String current, String target, Set<String> evidence, Set<String> visited, String direction, String cameFrom) {
        System.out.println("Visiting: " + current + ", Direction: " + direction + ", Came from: " + cameFrom + ", Evidence: " + evidence);

        List<String> parents = getParents(current);
        List<String> children = getChildren(current);
        for(String visit : visited){
            System.out.print(" visited: " + visit);
        }

        if (visited.contains(current)){
            for (String child : children){
                if (visited.contains(child)){
                    for (String parent : parents) {
                        if (visited.contains(parent)) {
                            return false;
                        }
                    }
                }
            }
        }
        System.out.println();
        visited.add(current);

        if (current.equals(target)) {
            System.out.println("Reached target: " + target);
            return true;
        }

        if (direction.equals("down")) {
            if (evidence.contains(current)){
                return false;
            }
            else if (!evidence.contains(current)){
                for (String child : children) {
                    if (canReach(child, target, evidence, visited, "up", current)) {
                        return true;
                    }
                }
                for (String parent : parents) {
                    if (canReach(parent, target, evidence, visited, "down", current)) {
                        return true;
                    }
                }
            }
        }
        else if (direction.equals("up")) {
            if (!evidence.contains(current)) {
                for (String child : children) {
                    if (canReach(child, target, evidence, visited, "down", current)) {
                        return true;
                    }
                }
            }
            else if (evidence.contains(current)) {
                for (String parent : parents) {
                    if (!cameFrom.equals(current) && canReach(parent, target, evidence, visited, "down", current)) {
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
