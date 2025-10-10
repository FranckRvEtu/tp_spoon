package scanner.util;

import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.util.*;
import java.util.stream.Collectors;

public class MetricsCalculator {

    public static double avgMethodsPerClass(int classes, int methods){
        return (double)methods/classes;
    }

    public static double avgLocPerMethod(Map<CtClass<?>, Map<CtMethod<?>, Integer>> loc){
        return loc.values().stream()
                .flatMap(m -> m.values().stream())
                .mapToInt(Integer::intValue)
                .sum();
    }

    public static double avgFieldsPerClass(Map<CtClass<?>, Integer> fieldsPerClass){
        int sum = fieldsPerClass.values().stream()
                .mapToInt(Integer::intValue).sum();
        return (double)sum/fieldsPerClass.size();
    }

    //Calcule les métriques 8 et 9
    public static Map<String, Integer> topPerClass(Map<CtClass<?>, Integer> map) {
        int resSize = map.size() / 10;
        resSize = resSize == 0 ? 1 : resSize;

        return map.entrySet().stream()
                .peek(entry -> {
                    if (entry.getKey() == null) {
                        System.err.println("[WARN] Entrée avec clé null rencontrée (valeur=" + entry.getValue() + ")");
                    }
                })
                .filter(entry -> entry.getKey() != null && entry.getValue() != null)
                .sorted(Map.Entry.<CtClass<?>, Integer>comparingByValue().reversed())
                .limit(resSize)
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getSimpleName(),
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }



    public static Map<String, Map<String, Integer>> topLocMethodsPerClass(
            Map<CtClass<?>, Map<CtMethod<?>, Integer>> loc) {

        Map<String, Map<String, Integer>> topMethodsPerClass = new HashMap<>();

        for (Map.Entry<CtClass<?>, Map<CtMethod<?>, Integer>> classEntry : loc.entrySet()) {
            String className = classEntry.getKey().getSimpleName(); // nom de la classe
            Map<CtMethod<?>, Integer> methodsMap = classEntry.getValue();

            int topCount = Math.max(1, (int) Math.ceil(methodsMap.size() * 0.10));

            Map<String, Integer> topMethods = methodsMap.entrySet()
                    .stream()
                    .sorted(Map.Entry.<CtMethod<?>, Integer>comparingByValue().reversed())
                    .limit(topCount)
                    .collect(Collectors.toMap(
                            e -> e.getKey().getSimpleName(), // nom de la méthode
                            Map.Entry::getValue,
                            (e1, e2) -> e1,
                            LinkedHashMap::new
                    ));

            topMethodsPerClass.put(className, topMethods);
        }

        return topMethodsPerClass;
    }


    public static List<String> moreThanXMethods(Map<CtClass<?>,Integer> classes, int x){
        return classes.entrySet().stream()
                .filter(e->e.getValue() > x)
                .map(e -> e.getKey().getSimpleName())
                .collect(Collectors.toList());
    }

    public static List<String> mostMethodsAndFieldsClasses(Map<String, Integer> set1,  Map<String, Integer> set2){
        List<String> res = new ArrayList<>();
        Set<String> keySet1 = set1.keySet();
        Set<String> keySet2 = set2.keySet();
        keySet2.forEach(k->{
            if (keySet1.contains(k)){
                res.add(k);
            }
        });
        return res;
    }

    public static Map<String, Map<String, Double>> computeCoupling(Map<String, Map<String, Double>> allPairs,int allCall){
        //TO TEST
        System.err.println("Compute Coupling :\nAllcall = "+allCall+"\nAllPairs = "+allPairs);
        Map<String, Map<String, Double>> res = new LinkedHashMap<>();

        if (allCall==0) return allPairs;

       allPairs.forEach((classA, aRelations)->{
           Map<String, Double> pairsRes = new LinkedHashMap<>();
           aRelations.forEach((classB, calls)->{
               double ratio = calls/allCall;
               pairsRes.put(classB,ratio);
           });
           res.put(classA, pairsRes);
       });
       return res;
    }

    public static Map<String,Object> compute(MyScanner scanner, int x){
        System.out.println("Calcul en cours...");
        Map<String,Object> metrics = new LinkedHashMap<>();
        //Pour éviter le boilerplate
        int classes = scanner.getNbClasses();
        int methods = scanner.getNbMethods();
        System.out.println("Classes: "+classes);

        //Métrique n°1
        metrics.put("Nombre de classes",classes);
        //Métrique n°2
        metrics.put("Lignes de code totale \n(sans compter les imports)",scanner.getLocTotal());
        //Métrique n°3
        metrics.put("Nombres de méthodes",methods);
        //Métrique n°4
        metrics.put("Nombre de packages",scanner.getNbPackage());
        //Métrique n°5
        metrics.put("Moyenne de méthode par classe",avgMethodsPerClass(classes, methods));
        //Métrique n°6
        metrics.put("Moyenne de lignes de code par méthode",avgLocPerMethod(scanner.getLocPerMethodPerClass()));
        //Métrique n°7
        metrics.put("Moyenne d'attributs par classe",avgFieldsPerClass(scanner.getFieldsPerClass()));
        System.out.println("Moitié du calcul fini");
        //Métrique n°8
        Map<String, Integer> topMethodsClasses = topPerClass(scanner.getMethodsPerClass());
        System.out.println("Métrique 8 calculé...");
        metrics.put("10% des classes avec le plus de méthodes",topMethodsClasses);
        //Métrique n°9
        Map<String, Integer> topFieldsClasses = topPerClass(scanner.getFieldsPerClass());
        System.out.println("Métrique 9 calculé...");
        metrics.put("10% des classes avec le plus d'attributs",topFieldsClasses);
        //Métrique n°10
        metrics.put("Classes appartenant aux deux ensembles précédents",mostMethodsAndFieldsClasses(topMethodsClasses, topFieldsClasses));
        System.out.println("10eme métrique calculé...");
        //Métrique n°11
        metrics.put("Classes avec plus de X méthodes",moreThanXMethods(scanner.getMethodsPerClass(), x));
        //Métrique n°12
        metrics.put("Pour chaque classe, le 10% des méthodes avec le plus de lignes de code",topLocMethodsPerClass(scanner.getLocPerMethodPerClass()));
        //Métrique n°13
        metrics.put("Le plus grand nombre de paramètres qu'une méthode possède",scanner.getMaxParameters());
        System.err.println("Calcul du TP1 fini\nDébut du calcul du couplage");

        return metrics;
    }
}
