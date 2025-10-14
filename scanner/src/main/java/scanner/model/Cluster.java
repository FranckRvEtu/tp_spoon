package scanner.model;

import java.util.HashSet;
import java.util.Set;

public class Cluster {
    private final Set<String> elements = new HashSet<>();
    private final Cluster left;
    private final Cluster right;
    private final double coupling;

    public Cluster(String element) {
        this.left = null;
        this.right = null;
        this.coupling = 0.0;
        this.elements.add(element);
    }

    public Cluster(Cluster a, Cluster b, double coupling) {
        this.left = a;
        this.right = b;
        this.coupling = coupling;
        this.elements.addAll(a.getElements());
        this.elements.addAll(b.getElements());
    }

    public Set<String> getElements() { return elements; }
    public double getCoupling() { return coupling; }

    @Override
    public String toString() {
        return elements.toString() + " (c=" + coupling + ")";
    }
}
