package scanner.util;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.CtScanner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InvocationScanner extends CtScanner {

    private final Map<CtMethod<?>, Set<CtMethod<?>>> callGraph = new HashMap<>();
    // Stocke le nombre d'appel pour chaque couple de Classes au format :
    // {A : { B : 5}, {C : 3}}
    private final Map<String, Map<String, Double>> coupling = new HashMap<>();
    private int totalCall=0;

    @Override
    public <T> void visitCtInvocation(CtInvocation<T> invocation) {
        totalCall++;
        CtMethod<?> caller = invocation.getParent(CtMethod.class);
        CtExecutable<?> target = invocation.getExecutable().getDeclaration();

        if (target instanceof CtMethod<?> callee) {
            callGraph
                    .computeIfAbsent(caller, k -> new HashSet<>())
                    .add(callee);
            String callerClass = caller.getParent(CtClass.class).getSimpleName();
            String targetClass = caller.getParent(CtClass.class).getSimpleName();

            if (callerClass != null && targetClass != null) {
                if(!callerClass.equals(targetClass)) {
                    coupling.computeIfAbsent(callerClass, k -> new HashMap<>()).merge(targetClass, 1.0, Double::sum);
                }
            }
        }

        super.visitCtInvocation(invocation);
    }

    public Map<CtMethod<?>, Set<CtMethod<?>>> getCallGraph() {
        return callGraph;
    }

    public int getTotalCall() {
        return totalCall;
    }

    public Map<String, Map<String, Double>> getCoupling() {
        return coupling;
    }
}
