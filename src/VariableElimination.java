import java.util.*;

public class VariableElimination {
    private final BayesianNetwork network;
    private final Map<String, String> evidence;
    private final Map<String, String> queryVariables;
    private final List<String> hiddenVariables;
    private final List<Factor> factors;
    private int multiplicationCount;
    private int additionCount;

    public VariableElimination(BayesianNetwork network, Map<String, String> evidence, Map<String, String> queryVariables, List<String> hiddenVariables) {
        this.network = network;
        this.evidence = evidence;
        this.queryVariables = queryVariables;
        this.hiddenVariables = hiddenVariables;
        this.factors = new ArrayList<>();
        this.multiplicationCount = 0;
        this.additionCount = 0;
        initializeFactors();
        applyEvidence();
        filterIrrelevantVariables();
    }


    // Initialize factors from the Bayesian network
    private void initializeFactors() {
        for (BayesianNode node : network.getNodes()) {
            List<String> variables = new ArrayList<>(node.getGiven());
            variables.add(node.getName());
            Factor factor = new Factor(variables);
            populateFactor(factor, node);
            factors.add(factor);
        }
    }

    private void populateFactor(Factor factor, BayesianNode node) {
        Map<List<String>, Double> cpt = node.getCPT();
        for (Map.Entry<List<String>, Double> entry : cpt.entrySet()) {
            factor.setProbability(entry.getKey(), entry.getValue());
        }
    }

    // Apply evidence to the factors
    private void applyEvidence() {
        for (Factor factor : factors) {
            for (Map.Entry<String, String> entry : evidence.entrySet()) {
                if (factor.getVariables().contains(entry.getKey())) {
                    reduceFactor(factor, entry.getKey(), entry.getValue());
                }
            }
        }
    }

    // Reduce factor based on evidence
    private void reduceFactor(Factor factor, String variable, String value) {
        Map<List<String>, Double> newTable = new HashMap<>();
        int index = factor.getVariables().indexOf(variable);
        for (Map.Entry<List<String>, Double> entry : factor.getTable().entrySet()) {
            if (entry.getKey().get(index).equals(value)) {
                List<String> newOutcomes = new ArrayList<>(entry.getKey());
                newOutcomes.remove(index);
                newTable.put(newOutcomes, entry.getValue());
            }
        }
        factor.getVariables().remove(variable);
        factor.getTable().clear();
        factor.getTable().putAll(newTable);
    }

    // Filter irrelevant variables and factors
    private void filterIrrelevantVariables() {
        BayesBall bayesBall = new BayesBall(network);
        Set<String> relevantVariables = new HashSet<>(queryVariables.keySet());
        relevantVariables.addAll(evidence.keySet());

        Set<String> toRemove = new HashSet<>();
        for (String hiddenVar : hiddenVariables) {
            boolean isAncestor = relevantVariables.stream().anyMatch(var -> isAncestor(hiddenVar, var, network));
            boolean isIndependent = bayesBall.areIndependents(hiddenVar, queryVariables.keySet().iterator().next(), evidence.keySet());

            if (!isAncestor || isIndependent) {
                toRemove.add(hiddenVar);
            }
        }

        // Remove irrelevant hidden variables
        hiddenVariables.removeAll(toRemove);
        factors.removeIf(factor -> !Collections.disjoint(factor.getVariables(), toRemove));

        // Check evidence variables for relevance
        Set<String> evidenceToRemove = new HashSet<>();
        for (String evidenceVar : evidence.keySet()) {
            Set<String> remainingEvidence = new HashSet<>(evidence.keySet());
            remainingEvidence.remove(evidenceVar);
            boolean isAncestor = relevantVariables.stream().anyMatch(var -> isAncestor(evidenceVar, var, network));
            boolean isIndependent = bayesBall.areIndependents(evidenceVar, queryVariables.keySet().iterator().next(), remainingEvidence);

            if (!isAncestor || isIndependent) {
                evidenceToRemove.add(evidenceVar);
            }
        }

        // Remove irrelevant evidence variables
        for (String evidenceVar : evidenceToRemove) {
            evidence.remove(evidenceVar);
        }
    }


    // Breadth-first search to check if one variable is an ancestor of another
    private static boolean isAncestor(String hidden, String target, BayesianNetwork bn) {
        Queue<String> queue = new LinkedList<>();
        Set<String> visited = new HashSet<>();
        queue.add(target);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(hidden)) {
                return true;
            }
            if (visited.add(current)) {
                BayesianNode currentNode = bn.getNode(current);
                if (currentNode != null) {
                    queue.addAll(currentNode.getGiven());
                }
            }
        }
        return false;
    }

    // Perform the variable elimination algorithm
    public double run() {
        System.out.println("Initial Factors:");
        printFactors();

        factors.removeIf(factor -> factor.size() == 0);

        System.out.println("numbers of factors = " + factors.size());
        if(factors.size() == 1){
            Factor factor = factors.getFirst();
            System.out.println("Factor with all query and evidence variables found:");
            printFactor(factor);
            double result = factor.getProbability(getQueryOutcomes());
            System.out.printf("Probability: %.5f, Multiplications: %d, Additions: %d%n", result, multiplicationCount, additionCount);
            return result;
        }
        // Check if there's a factor that contains all query variables and evidence variables
        Set<String> allVars = new HashSet<>(queryVariables.keySet());
        allVars.addAll(evidence.keySet());
        System.out.println("evidence.keySet() = " + evidence.keySet());

        for (Factor factor : factors) {
            if (new HashSet<>(factor.getVariables()).containsAll(allVars) && factor.size() == allVars.size()) {
                System.out.println("Factor with all query and evidence variables found:");
                printFactor(factor);
                double result = factor.getProbability(getQueryOutcomes());
                System.out.printf("Probability: %.5f, Multiplications: %d, Additions: %d%n", result, multiplicationCount, additionCount);
                return result;
            }
        }

        for (String hidden : hiddenVariables) {
            List<Factor> factorsWithHidden = new ArrayList<>();
            for (Factor factor : factors) {
                if (factor.getVariables().contains(hidden)) {
                    factorsWithHidden.add(factor);
                }
            }

            if (factorsWithHidden.isEmpty()) {
                continue; // Skip if no factors contain the hidden variable
            }

            // Sort the factors by size before joining them
            Collections.sort(factorsWithHidden);

            // Ensure correct order of joining factors
            Factor joinedFactor = factorsWithHidden.get(0);
            for (int i = 1; i < factorsWithHidden.size(); i++) {
                System.out.println("----------------------------------------------------------------");
                System.out.println("hidden = " + factorsWithHidden.get(i).getVariables());
                System.out.println("----------------------------------------------------------------");
                joinedFactor = multiply(joinedFactor, factorsWithHidden.get(i));
            }

            Factor reducedFactor = eliminateVariable(joinedFactor, hidden);

            factors.removeAll(factorsWithHidden);
            factors.add(reducedFactor);
        }

        Factor finalFactor = joinFactors(factors);
        if (getMultiplicationCount() > 0 ){
            normalize(finalFactor);
        }

        System.out.println("Final Factor:");
        printFactor(finalFactor);

        // Extract the probability for the query variable from the final factor
        double result = finalFactor.getProbability(getQueryOutcomes());
        System.out.printf("Probability: %.5f, Multiplications: %d, Additions: %d%n", result, multiplicationCount, additionCount);
        return result;
    }

    // Get the outcomes for the query variables
    private List<String> getQueryOutcomes() {
        List<String> outcomes = new ArrayList<>();
        for (String var : queryVariables.keySet()) {
            String outcome = queryVariables.get(var); // Get the outcome from the query variables map
            if (outcome == null) {
                System.err.println("Outcome missing for variable: " + var);
            }
            outcomes.add(outcome);
        }
        System.out.println("Generated Query Outcomes: " + outcomes);
        return outcomes;
    }

    // Join a list of factors
    private Factor joinFactors(List<Factor> factorsToJoin) {
        if (factorsToJoin.isEmpty()) {
            throw new IllegalStateException("No factors to join.");
        }
        Factor result = factorsToJoin.get(0);
        for (int i = 1; i < factorsToJoin.size(); i++) {
            result = multiply(result, factorsToJoin.get(i));
        }
        return result;
    }

    // Multiply two factors
    private Factor multiply(Factor f1, Factor f2) {
        List<String> newVariables = new ArrayList<>(f1.getVariables());
        for (String var : f2.getVariables()) {
            if (!newVariables.contains(var)) {
                newVariables.add(var);
            }
        }

        Factor result = new Factor(newVariables);
        generateOutcomes(newVariables.size(), new ArrayList<>(), result, f1, f2);
        return result;
    }

    private void generateOutcomes(int size, List<String> currentOutcomes, Factor result, Factor f1, Factor f2) {
        if (currentOutcomes.size() == size) {
            List<String> f1Outcomes = new ArrayList<>();
            List<String> f2Outcomes = new ArrayList<>();

            for (String var : f1.getVariables()) {
                int index = result.getVariables().indexOf(var);
                f1Outcomes.add(currentOutcomes.get(index));
            }

            for (String var : f2.getVariables()) {
                int index = result.getVariables().indexOf(var);
                f2Outcomes.add(currentOutcomes.get(index));
            }

            double probability = f1.getProbability(f1Outcomes) * f2.getProbability(f2Outcomes);
            multiplicationCount++;
            result.setProbability(currentOutcomes, probability);
            return;
        }

        String currentVariable = result.getVariables().get(currentOutcomes.size());
        for (String outcome : getPossibleOutcomes(currentVariable)) {
            currentOutcomes.add(outcome);
            generateOutcomes(size, currentOutcomes, result, f1, f2);
            currentOutcomes.removeLast();
        }
    }

    // Helper method to generate possible outcomes for a variable
    private List<String> getPossibleOutcomes(String variable) {
        for (BayesianNode node : network.getNodes()) {
            if (node.getName().equals(variable)) {
                return node.getOutcomes();
            }
        }
        throw new RuntimeException("No outcomes found for variable: " + variable);
    }

    // Eliminate a variable from a factor
    private Factor eliminateVariable(Factor factor, String variable) {
        List<String> newVariables = new ArrayList<>(factor.getVariables());
        int index = newVariables.indexOf(variable);
        if (index == -1) {
            throw new IllegalArgumentException("Variable to eliminate not found in factor.");
        }
        newVariables.remove(index);

        Factor result = new Factor(newVariables);
        Map<List<String>, Double> newTable = new HashMap<>();

        // Determine the number of outcomes for the variable to be eliminated
        Set<String> outcomesSet = new HashSet<>();
        for (List<String> outcomes : factor.getTable().keySet()) {
            outcomesSet.add(outcomes.get(index));
        }
        int numOutcomes = outcomesSet.size(); // Number of unique outcomes for the eliminated variable

        for (Map.Entry<List<String>, Double> entry : factor.getTable().entrySet()) {
            List<String> outcomes = new ArrayList<>(entry.getKey());
            outcomes.remove(index);

            newTable.putIfAbsent(outcomes, 0.0);
            newTable.put(outcomes, newTable.get(outcomes) + entry.getValue());
        }

        result.getTable().putAll(newTable);
        additionCount += result.tableSize() * (numOutcomes - 1); // Correct calculation for additionCount
        System.out.println("-----------------------------> result size = " + result.tableSize());

        return result;
    }



    // Normalize the final factor
    private void normalize(Factor factor) {
        double sum = 0;
        for (double value : factor.getTable().values()) {
            sum += value;
            if (sum > 0) additionCount++;
        }
        for (Map.Entry<List<String>, Double> entry : factor.getTable().entrySet()) {
            factor.getTable().put(entry.getKey(), entry.getValue() / sum);
        }
        additionCount--;
    }

    // Get the multiplication count
    public int getMultiplicationCount() {
        return multiplicationCount;
    }

    // Get the addition count
    public int getAdditionCount() {
        return additionCount;
    }

    // Print factors for debugging
    private void printFactors() {
        for (Factor factor : factors) {
            printFactor(factor);
        }
    }

    // Print a single factor for debugging
    private void printFactor(Factor factor) {
        System.out.println("Variables: " + factor.getVariables());
        for (Map.Entry<List<String>, Double> entry : factor.getTable().entrySet()) {
            System.out.println("Outcomes: " + entry.getKey() + " -> Probability: " + entry.getValue());
        }
    }
}