package scanner.util;

import spoon.reflect.declaration.CtMethod;

import java.util.Map;
import java.util.Set;

public class CallGraphExporter {

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

}
