package scanner.service;


import org.springframework.stereotype.Service;
import scanner.model.Cluster;
import scanner.util.MetricsCalculator;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class ClusteringService {

    //Créer la liste de clusters atomiques
    public ArrayList<Cluster> initClusters(Map<String, Map<String, Double>> couplingMatrix) {
        // Set ordonné et dé-dupliqué
        LinkedHashSet<String> classnames = new LinkedHashSet<>();

        // Remplissage / normalisation
        couplingMatrix.forEach((k, v) -> {
            if (k != null) {
                classnames.add(k.trim());
            }
            if (v != null) {
                v.keySet().stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .forEach(classnames::add);
            }
        });

        // Diagnostic : vérifier doublons "visuels" (même lower-case)
        Map<String, Integer> lcCounts = new HashMap<>();
        for (String n : classnames) {
            lcCounts.merge(n.toLowerCase(Locale.ROOT), 1, Integer::sum);
        }
        List<String> probableDuplicates = lcCounts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        System.out.println("initClusters : classes uniques trouvées = " + classnames.size());
        if (!probableDuplicates.isEmpty()) {
            System.out.println("Possibles doublons (même nom diff. casse) : " + probableDuplicates);
        }

        // Construction des clusters atomiques
        ArrayList<Cluster> clusters = classnames.stream()
                .map(Cluster::new)
                .collect(Collectors.toCollection(ArrayList::new));

        // debug print
        //System.out.println("Clusters initialisés (" + clusters.size() + "): " + classnames);

        return clusters;
    }


    public Double computeCoupling(Cluster a, Cluster b, Map<String, Map<String, Double>> couplingMatrix) {
        if (a.isAtomic() && b.isAtomic()) {
            String classA = a.getAtomicClass();
            String classB = b.getAtomicClass();
            Set<String> rows = couplingMatrix.keySet();

            if(rows.contains(classA)){
                return couplingMatrix.get(classA).get(classB);
            }
            else {
                for (Map.Entry<String, Map<String, Double>> entry : couplingMatrix.entrySet()) {
                    Map<String, Double> row = entry.getValue();
                    if (row.containsKey(classA) && entry.getKey().equals(classB)) {
                        return row.get(classA);
                    }
                }
            }

            return 0.0;
        }

        if (a.isAtomic()) {
            return (computeCoupling(a, b.getLeft(), couplingMatrix) + computeCoupling(a, b.getRight(), couplingMatrix)) / 2.0;
        }
        if (b.isAtomic()) {
            return (computeCoupling(a.getLeft(), b, couplingMatrix) + computeCoupling(a.getRight(), b, couplingMatrix)) / 2.0;
        }

        double sum = computeCoupling(a.getLeft(), b.getLeft(), couplingMatrix);
        sum += computeCoupling(a.getLeft(), b.getRight(), couplingMatrix);
        sum += computeCoupling(a.getRight(), b.getLeft(), couplingMatrix);
        sum += computeCoupling(a.getRight(), b.getRight(), couplingMatrix);

        return sum / 4.0;
    }

    public Cluster createHierchicalCluster(Map<String, Map<String, Double>> couplingMatrix) {
        try {
            ArrayList<Cluster> clusters = initClusters(couplingMatrix);
//            for (String i : couplingMatrix.keySet()) {
//                Map<String, Double> row = couplingMatrix.get(i);
//                for (String j : row.keySet()) {
//                    couplingMatrix.putIfAbsent(j, new HashMap<>());
//                    couplingMatrix.get(i).putIfAbsent(j, couplingMatrix.get(j).get(i));
//                }
//            }
            //System.out.println("couplingMatrix : " + couplingMatrix);
            while (clusters.size() > 1) {
                double maxCoupling = -1.0;
                Cluster bestCluster = null;
                for (int i = 0; i < clusters.size() - 1; i++) {
                    Cluster a = clusters.get(i);
                    for (int j = i + 1; j < clusters.size(); j++) {
                        Cluster b = clusters.get(j);
                        double coupling = computeCoupling(a, b, couplingMatrix);
                        if (coupling > maxCoupling) {
                            bestCluster = new Cluster(a, b, coupling);
                        }
                        maxCoupling = Math.max(maxCoupling, coupling);
                    }
                }

                clusters.add(bestCluster);
                clusters.remove(bestCluster.getLeft());
                clusters.remove(bestCluster.getRight());
//                System.out.println("Fin de tour : \n"+
//                        "\tClusters restants :"+clusters.size()+"\n"+
//                        "\tMeilleur cluster trouvé :"+ bestCluster +"avec un couplage de "+maxCoupling+"\n"+
//                        "\tOn retire"+bestCluster.getLeft()+" et "+bestCluster.getRight());
            }
            return clusters.getFirst();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
