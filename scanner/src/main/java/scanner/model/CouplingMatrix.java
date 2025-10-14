package scanner.model;

import java.util.List;
import java.util.Map;

public class CouplingMatrix {

    private final List<String> rows;
    private final List<String> cols;
    private final Map<String, Map<String, Double>> matrix;

    public CouplingMatrix(List<String> rows, List<String> cols, Map<String, Map<String, Double>> matrix) {
        this.rows = rows;
        this.cols = cols;
        this.matrix = matrix;
    }

    public List<String> getRows() {
        return rows;
    }

    public List<String> getCols() {
        return cols;
    }

    public Map<String, Map<String, Double>> getMatrix() {
        return matrix;
    }
}
