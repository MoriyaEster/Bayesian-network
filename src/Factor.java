import java.util.*;

public class Factor implements Comparable<Factor> {
    private List<String> variables;
    private Map<List<Boolean>, Double> table;

    public Factor(List<String> variables) {
        this.variables = new ArrayList<>(variables);
        this.table = new LinkedHashMap<>(); // Use LinkedHashMap to maintain order
    }

    // Set the probability value for a given combination of variable outcomes
    public void setProbability(List<Boolean> outcomes, Double probability) {
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

    // Compare two factors based on the number of variables
    @Override
    public int compareTo(Factor other) {
        int variableCountComparison = Integer.compare(this.variables.size(), other.variables.size());
        if (variableCountComparison != 0) {
            return variableCountComparison;
        }

        // If the number of variables is the same, compare based on the sum of ASCII values of the variable names
        int thisAsciiSum = this.variables.stream().mapToInt(this::asciiSum).sum();
        int otherAsciiSum = other.variables.stream().mapToInt(this::asciiSum).sum();
        return Integer.compare(thisAsciiSum, otherAsciiSum);
    }

    // Helper method to calculate the sum of ASCII values of characters in a string
    private int asciiSum(String s) {
        return s.chars().sum();
    }

    @Override
    public String toString() {
        return "Factor{" +
                "variables=" + variables +
                ", table=" + table +
                '}';
    }
}
