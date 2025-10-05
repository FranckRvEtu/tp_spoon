package scanner.util;

import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.CtScanner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CallGraphScanner extends CtScanner {

    private final Map<CtMethod<?>, Set<CtMethod<?>>> callGraph = new HashMap<>();


    @Override
    public <T> void visitCtInvocation(CtInvocation<T> invocation) {
        CtMethod<?> caller = invocation.getParent(CtMethod.class);
        CtExecutable<?> target = invocation.getExecutable().getDeclaration();

        if (caller != null && target instanceof CtMethod<?> callee) {
            callGraph
                    .computeIfAbsent(caller, k -> new HashSet<>())
                    .add(callee);
        }

        super.visitCtInvocation(invocation);
    }

    public Map<CtMethod<?>, Set<CtMethod<?>>> getCallGraph() {
        return callGraph;
    }
}
