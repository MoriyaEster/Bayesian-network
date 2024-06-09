import java.util.*;

public class BayesBall {
    private BayesianNetwork network;

    public BayesBall(BayesianNetwork network) {
        this.network = network;
    }

    public boolean isConditionallyIndependent(String start, String end, Set<String> evidenceSet) {
        Set<String> evidence = parseEvidence(evidenceSet);
        Map<String, Set<String>> visitedFrom = new HashMap<>();
        return !canReach(start, end, evidence, visitedFrom, "down", null);
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

    private boolean canReach(String current, String target, Set<String> evidence, Map<String, Set<String>> visitedFrom, String direction, String cameFrom) {
        System.out.println("Visiting: " + current + ", Direction: " + direction + ", Came from: " + cameFrom + ", Evidence: " + evidence);

        if (current.equals(target)) {
            System.out.println("Reached target: " + target);
            return true;
        }

        if (evidence.contains(current) && direction.equals("down")) {
            System.out.println("Stopping at evidence node: " + current + " from below");
            return false;
        }

        if (!visitedFrom.containsKey(current)) {
            visitedFrom.put(current, new HashSet<>());
        }
        visitedFrom.get(current).add(direction);

        List<String> parents = getParents(current);
        List<String> children = getChildren(current);

        boolean canContinue = direction.equals("up") || !evidence.contains(current);
        if (canContinue) {
            if (visitedFrom.get(current).contains("up") && visitedFrom.get(current).contains("down")) {
                if (visitedFrom.get(current).containsAll(parents) && visitedFrom.get(current).containsAll(children)) {
                    visitedFrom.put(current, Collections.singleton("visited"));
                    System.out.println("Added to visited: " + current);
                }
            }
        }

        if (direction.equals("up")) {
            for (String child : children) {
                if (child.equals(cameFrom) || visitedFrom.getOrDefault(child, Collections.emptySet()).contains("visited")) continue;
                if (canReach(child, target, evidence, visitedFrom, "up", current)) {
                    return true;
                }
            }
        }

        if (direction.equals("down") || evidence.contains(current)) {
            for (String child : children) {
                if (child.equals(cameFrom) || visitedFrom.getOrDefault(child, Collections.emptySet()).contains("visited")) continue;
                if (canReach(child, target, evidence, visitedFrom, "up", current)) {
                    return true;
                }
            }
            if (direction.equals("up") && evidence.contains(current)) {
                for (String parent : parents) {
                    if (visitedFrom.getOrDefault(parent, Collections.emptySet()).contains("visited")) continue;
                    if (canReach(parent, target, evidence, visitedFrom, "down", current)) {
                        return true;
                    }
                }
            }
            if (direction.equals("down") && !evidence.contains(current)) {
                for (String parent : parents) {
                    if (visitedFrom.getOrDefault(parent, Collections.emptySet()).contains("visited")) continue;
                    if (canReach(parent, target, evidence, visitedFrom, "down", current)) {
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
