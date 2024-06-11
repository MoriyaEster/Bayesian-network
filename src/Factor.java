import java.util.*;

public class Factor {
    private List<String> variables;
    private Map<List<Boolean>, Double> table;

    public Factor(List<String> variables) {
        this.variables = new ArrayList<>(variables);
        this.table = new HashMap<>();
    }

    // Set the probability value for a given combination of variable outcomes
    public void setProbability(List<Boolean> outcomes, Double probability) {
        if (outcomes.size() != variables.size()) {
            throw new IllegalArgumentException("Outcomes size must match the number of variables.");
        }
        table.put(new ArrayList<>(outcomes), probability);
    }

    // Get the probability value for a given combination of variable outcomes
    public Double getProbability(List<Boolean> outcomes) {
        if (outcomes.size() != variables.size()) {
            throw new IllegalArgumentException("Outcomes size must match the number of variables.");
        }
        return table.getOrDefault(outcomes, 0.0);
    }

    // Getters for variables and table
    public List<String> getVariables() {
        return variables;
    }

    public Map<List<Boolean>, Double> getTable() {
        return table;
    }

    // Multiply this factor with another factor
    public Factor multiply(Factor other) {
        List<String> newVariables = new ArrayList<>(this.variables);
        for (String var : other.getVariables()) {
            if (!newVariables.contains(var)) {
                newVariables.add(var);
            }
        }

        Factor result = new Factor(newVariables);
        generateOutcomes(newVariables.size(), new ArrayList<>(), result, other);

        return result;
    }

    private void generateOutcomes(int size, List<Boolean> currentOutcomes, Factor result, Factor other) {
        if (currentOutcomes.size() == size) {
            List<Boolean> thisOutcomes = new ArrayList<>();
            List<Boolean> otherOutcomes = new ArrayList<>();

            for (String var : this.variables) {
                int index = result.getVariables().indexOf(var);
                thisOutcomes.add(currentOutcomes.get(index));
            }

            for (String var : other.getVariables()) {
                int index = result.getVariables().indexOf(var);
                otherOutcomes.add(currentOutcomes.get(index));
            }

            double probability = this.getProbability(thisOutcomes) * other.getProbability(otherOutcomes);
            result.setProbability(currentOutcomes, probability);
            return;
        }

        for (Boolean outcome : Arrays.asList(true, false)) {
            currentOutcomes.add(outcome);
            generateOutcomes(size, currentOutcomes, result, other);
            currentOutcomes.remove(currentOutcomes.size() - 1);
        }
    }

    // Eliminate a variable from this factor by summing out the variable
    public Factor eliminateVariable(String variable) {
        List<String> newVariables = new ArrayList<>(variables);
        int index = newVariables.indexOf(variable);
        if (index == -1) {
            throw new IllegalArgumentException("Variable to eliminate not found in factor.");
        }
        newVariables.remove(index);

        Factor result = new Factor(newVariables);
        Map<List<Boolean>, Double> newTable = new HashMap<>();

        for (Map.Entry<List<Boolean>, Double> entry : table.entrySet()) {
            List<Boolean> outcomes = new ArrayList<>(entry.getKey());
            outcomes.remove(index);

            newTable.putIfAbsent(outcomes, 0.0);
            newTable.put(outcomes, newTable.get(outcomes) + entry.getValue());
        }

        result.getTable().putAll(newTable);
        return result;
    }

    // Normalize the factor
    public void normalize() {
        double sum = 0;
        for (double value : table.values()) {
            sum += value;
        }
        for (Map.Entry<List<Boolean>, Double> entry : table.entrySet()) {
            table.put(entry.getKey(), entry.getValue() / sum);
        }
    }
}
