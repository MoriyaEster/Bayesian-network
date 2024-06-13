import java.util.*;

public class VariableElimination {
    private BayesianNetwork network;
    private Map<String, Boolean> evidence;
    private List<String> queryVariables;
    private List<String> hiddenVariables;
    private List<Factor> factors;
    private int multiplicationCount;
    private int additionCount;

    public VariableElimination(BayesianNetwork network, Map<String, Boolean> evidence, List<String> queryVariables, List<String> hiddenVariables) {
        this.network = network;
        this.evidence = evidence;
        this.queryVariables = queryVariables;
        this.hiddenVariables = hiddenVariables;
        this.factors = new ArrayList<>();
        this.multiplicationCount = 0;
        this.additionCount = 0;
        initializeFactors();
        applyEvidence();
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
        Map<List<Boolean>, Double> cpt = node.getCPT();
        for (Map.Entry<List<Boolean>, Double> entry : cpt.entrySet()) {
            factor.setProbability(entry.getKey(), entry.getValue());
        }
    }

    // Apply evidence to the factors
    private void applyEvidence() {
        for (Factor factor : factors) {
            for (Map.Entry<String, Boolean> entry : evidence.entrySet()) {
                if (factor.getVariables().contains(entry.getKey())) {
                    reduceFactor(factor, entry.getKey(), entry.getValue());
                }
            }
        }
    }

    // Reduce factor based on evidence
    private void reduceFactor(Factor factor, String variable, Boolean value) {
        Map<List<Boolean>, Double> newTable = new HashMap<>();
        int index = factor.getVariables().indexOf(variable);
        for (Map.Entry<List<Boolean>, Double> entry : factor.getTable().entrySet()) {
            if (entry.getKey().get(index).equals(value)) {
                List<Boolean> newOutcomes = new ArrayList<>(entry.getKey());
                newOutcomes.remove(index);
                newTable.put(newOutcomes, entry.getValue());
            }
        }
        factor.getVariables().remove(variable);
        factor.getTable().clear();
        factor.getTable().putAll(newTable);
    }

    // Perform the variable elimination algorithm
    public double run() {
        System.out.println("Initial Factors:");
        printFactors();

        int connections = 0;
        for (String hidden : hiddenVariables) {
            List<Factor> factorsWithHidden = new ArrayList<>();
            for (Factor factor : factors) {
                if (factor.getVariables().contains(hidden)) {
                    factorsWithHidden.add(factor);
                }
            }

            connections += factorsWithHidden.size();
            Factor joinedFactor = joinFactors(factorsWithHidden);
            Factor reducedFactor = eliminateVariable(joinedFactor, hidden);

            factors.removeAll(factorsWithHidden);
            factors.add(reducedFactor);
        }

        Factor finalFactor = joinFactors(factors);
        normalize(finalFactor);

        System.out.println("Final Factor:");
        printFactor(finalFactor);

        // Extract the probability for the query variable from the final factor
        double result = finalFactor.getProbability(getQueryOutcomes());
        System.out.printf("Probability: %.5f, Connections: %d, Multiplications: %d, Additions: %d%n", result, connections, multiplicationCount, additionCount);
        return result;
    }

    // Join a list of factors
    private Factor joinFactors(List<Factor> factorsToJoin) {
        Factor result = factorsToJoin.get(0);
        for (int i = 1; i < factorsToJoin.size(); i++) {
            result = multiply(result, factorsToJoin.get(i));
            multiplicationCount++;
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

    private void generateOutcomes(int size, List<Boolean> currentOutcomes, Factor result, Factor f1, Factor f2) {
        if (currentOutcomes.size() == size) {
            List<Boolean> f1Outcomes = new ArrayList<>();
            List<Boolean> f2Outcomes = new ArrayList<>();

            for (String var : f1.getVariables()) {
                int index = result.getVariables().indexOf(var);
                f1Outcomes.add(currentOutcomes.get(index));
            }

            for (String var : f2.getVariables()) {
                int index = result.getVariables().indexOf(var);
                f2Outcomes.add(currentOutcomes.get(index));
            }

            double probability = f1.getProbability(f1Outcomes) * f2.getProbability(f2Outcomes);
            result.setProbability(currentOutcomes, probability);
            return;
        }

        for (Boolean outcome : Arrays.asList(true, false)) {
            currentOutcomes.add(outcome);
            generateOutcomes(size, currentOutcomes, result, f1, f2);
            currentOutcomes.remove(currentOutcomes.size() - 1);
        }
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
        Map<List<Boolean>, Double> newTable = new HashMap<>();

        for (Map.Entry<List<Boolean>, Double> entry : factor.getTable().entrySet()) {
            List<Boolean> outcomes = new ArrayList<>(entry.getKey());
            outcomes.remove(index);

            newTable.putIfAbsent(outcomes, 0.0);
            newTable.put(outcomes, newTable.get(outcomes) + entry.getValue());
            additionCount++;
        }

        result.getTable().putAll(newTable);
        return result;
    }

    // Normalize the final factor
    private void normalize(Factor factor) {
        factor.normalize();
    }

    // Get the outcomes for the query variables
    private List<Boolean> getQueryOutcomes() {
        List<Boolean> outcomes = new ArrayList<>();
        for (String var : queryVariables) {
            outcomes.add(evidence.get(var));
        }
        return outcomes;
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
        for (Map.Entry<List<Boolean>, Double> entry : factor.getTable().entrySet()) {
            System.out.println("Outcomes: " + entry.getKey() + " -> Probability: " + entry.getValue());
        }
    }
}
