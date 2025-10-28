package scanner.util;

import scanner.model.Cluster;
import spoon.reflect.declaration.CtMethod;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class GraphExporter {

    //Crée la string du graphe au format DOT
    public static String toDot(Map<CtMethod<?>, Set<CtMethod<?>>> callGraph) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph G {\n");

        for (Map.Entry<CtMethod<?>, Set<CtMethod<?>>> entry : callGraph.entrySet()) {
            String caller = entry.getKey().getSimpleName();
            for (CtMethod<?> callee : entry.getValue()) {
                String calleeName = callee.getSimpleName();
                sb.append("  \"").append(caller).append("\" -> \"")
                        .append(calleeName).append("\";\n");
            }
        }

        sb.append("}\n");
        //DEBUG System.out.println(sb.toString());
        return sb.toString();
    }

    public static String buildCouplingGraphDot(Map<String, Map<String, Double>> couplingMap) {
        StringBuilder sb = new StringBuilder();
        sb.append("graph couplingGraph {\n");
        sb.append("  rankdir=LR;\n");
        sb.append("  node [shape=box, style=filled, color=lightblue];\n");

        couplingMap.forEach((classA, targets) -> {
            targets.forEach((classB, weight) -> {
                if (weight > 0) {
                    sb.append(String.format(Locale.US,
                            "  \"%s\" -- \"%s\" [label=\"\\\"%.6f\\\"\", penwidth=%.2f];\n",
                            classA, classB, weight, 1 + weight * 5));// penwidth pour l'épaisseur
                }
            });
        });

        sb.append("}\n");
        //System.err.println("Coupling Graph = "+sb.toString());
        return sb.toString();
    }

    public static String clusterToDot(Cluster root) {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph Dendrogram {\n");
        sb.append("    node [shape=circle, style=filled, fillcolor=lightyellow];\n");

        int[] counter = {0}; // sert à donner des IDs uniques aux nœuds
        exportCluster(root, sb, counter);

        sb.append("}\n");
        return sb.toString();
    }

    private static String exportCluster(Cluster cluster, StringBuilder sb, int[] counter) {
        if (cluster == null) return "";

        String nodeId = "n" + counter[0]++;
        String label;

        if (cluster.isAtomic()) {
            label = cluster.getAtomicClass();
            sb.append(String.format("    %s [label=\"%s\", fillcolor=lightblue];\n", nodeId, label));
        } else {
            label = String.format("%.3f", cluster.getCoupling());
            sb.append(String.format("    %s [label=\"%s\", fillcolor=lightcoral];\n", nodeId, label));
        }

        if (cluster.getLeft() != null) {
            String leftId = exportCluster(cluster.getLeft(), sb, counter);
            sb.append(String.format("    %s -> %s;\n", nodeId, leftId));
        }

        if (cluster.getRight() != null) {
            String rightId = exportCluster(cluster.getRight(), sb, counter);
            sb.append(String.format("    %s -> %s;\n", nodeId, rightId));
        }

        return nodeId;
    }

}
