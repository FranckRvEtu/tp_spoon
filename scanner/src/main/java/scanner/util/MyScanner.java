package scanner.util;

import org.springframework.stereotype.Service;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtPackage;
import spoon.reflect.visitor.CtScanner;

import java.util.HashMap;
import java.util.Map;

public class MyScanner extends CtScanner {
    private int nbClasses = 0;
    private int nbMethods=0;
    private int nbPackage=-1; //-1 pour ignorer le package racine
    private long LocTotal;
    private final Map<CtClass<?>, Integer> methodsPerClass = new HashMap<>();
    private final Map<CtClass<?>, Integer> fieldsPerClass = new HashMap<>();
    private final Map<CtClass<?>, Map<CtMethod<?>, Integer>> locPerMethodPerClass  = new HashMap<>(new HashMap<>());
    private int maxParameters = 0;

    public MyScanner() {
        super();
    }

    //Méthode appelé quand le noeud courant est une classe
    @Override
    public <T> void visitCtClass(CtClass<T> ctClass) {
        //On incrémente le nbr de classes totales
        this.nbClasses++;
        this.LocTotal +=  computeLoc(ctClass);
        this.fieldsPerClass.compute(ctClass, (k, v) -> v == null ? 1 : ctClass.getFields().size());
        super.visitCtClass(ctClass);
    }

    //Méthode appelé quand le noeud courant est une méthode
    @Override
    public <T> void visitCtMethod(CtMethod<T> m) {
        this.nbMethods++;

        CtClass<?> c = m.getParent(CtClass.class);

        // Nombre de méthodes par classe
        this.methodsPerClass.compute(c, (k, v) -> v == null ? 1 : v + 1);

        // LOC pour cette méthode
        int loc = computeLoc(m);
        this.locPerMethodPerClass
                .computeIfAbsent(c, k -> new HashMap<>())
                .put(m, loc);

        // Nombre max de paramètres
        this.maxParameters = Math.max(this.maxParameters, m.getParameters().size());

        super.visitCtMethod(m);
    }

    //Méthode appelé quand le noeud courant est un package
    @Override
    public void visitCtPackage(CtPackage ctPackage) {
        //On incrémente le nbr de packages totales
        this.nbPackage++;
        //DEBUG System.err.println(ctPackage.getQualifiedName()+"/");
        super.visitCtPackage(ctPackage);
    }

    //Méthode utilitaire permettant de calculer les LoC d'un noeud de l'arbre
    public int computeLoc(CtElement elem) {
        int loc = 0;
        if (elem.getPosition().isValidPosition()) {
            int start = elem.getPosition().getLine();
            int end = elem.getPosition().getEndLine();
            loc = end - start + 1;
        }
        return loc;
    }

    public int getNbClasses() {
        return nbClasses;
    }

    public int getNbMethods() {
        return nbMethods;
    }

    public int getNbPackage() {
        return nbPackage;
    }

    public long getLocTotal() {
        return LocTotal;
    }

    public Map<CtClass<?>, Integer> getMethodsPerClass() {
        return methodsPerClass;
    }

    public Map<CtClass<?>, Integer> getFieldsPerClass() {
        return fieldsPerClass;
    }

    public Map<CtClass<?>, Map<CtMethod<?>, Integer>> getLocPerMethodPerClass() {
        return locPerMethodPerClass;
    }

    public int getMaxParameters() {
        return maxParameters;
    }
}
