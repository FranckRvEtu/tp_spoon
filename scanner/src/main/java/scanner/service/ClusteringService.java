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

        //System.out.println("initClusters : classes uniques trouvées = " + classnames.size());
        if (!probableDuplicates.isEmpty()) {
            System.out.println("Possibles doublons (même nom diff. casse) : " + probableDuplicates);
        }

        // Construction des clusters atomiques
        ArrayList<Cluster> clusters = classnames.stream()
                .map(Cluster::new)
                .collect(Collectors.toCollection(ArrayList::new));

        //Debug
        //System.out.println("Clusters initialisés (" + clusters.size() + "): " + classnames);

        return clusters;
    }


    public Double computeCoupling(Cluster a, Cluster b, Map<String, Map<String, Double>> couplingMatrix) {
        //Cas de base de la récursion
        if (a.isAtomic() && b.isAtomic()) {
            String classA = a.getAtomicClass();
            String classB = b.getAtomicClass();
            Set<String> rows = couplingMatrix.keySet();

            //Matrice non symétrique donc il faut chercher correctement pour éviter les NPE
            if(rows.contains(classA)){
                return couplingMatrix.get(classA).get(classB);
            }
            else {
                //Si a n'est pas une ligne de la matrice, il faut fouiller chaque entrée
                for (Map.Entry<String, Map<String, Double>> entry : couplingMatrix.entrySet()) {
                    Map<String, Double> row = entry.getValue();
                    if (row.containsKey(classA) && entry.getKey().equals(classB)) {
                        return row.get(classA);
                    }
                }
            }
            return 0.0;
        }
        //Cas récursif sur b
        if (a.isAtomic()) {
            return (computeCoupling(a, b.getLeft(), couplingMatrix) + computeCoupling(a, b.getRight(), couplingMatrix)) / 2.0;
        }
        //Cas récursif sur a
        if (b.isAtomic()) {
            return (computeCoupling(a.getLeft(), b, couplingMatrix) + computeCoupling(a.getRight(), b, couplingMatrix)) / 2.0;
        }
        //Cas récursif sur a et b
        double sum = computeCoupling(a.getLeft(), b.getLeft(), couplingMatrix);
        sum += computeCoupling(a.getLeft(), b.getRight(), couplingMatrix);
        sum += computeCoupling(a.getRight(), b.getLeft(), couplingMatrix);
        sum += computeCoupling(a.getRight(), b.getRight(), couplingMatrix);

        return sum / 4.0;
    }

    public Cluster createHierchicalCluster(Map<String, Map<String, Double>> couplingMatrix) {
        try {
            ArrayList<Cluster> clusters = initClusters(couplingMatrix);
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
                //DEBUG
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

    public List<Cluster> identifyModules(Cluster root, Map<String, Map<String, Double>> matrix, double CP, int totalClasses) {
        List<Cluster> modules = new ArrayList<>();

        // Compter M = nombre total de classes atomiques
        //int totalClasses = countAtomic(root);
        int maxModules = Math.max(1, totalClasses / 2);

        exploreCluster(root, matrix, CP, modules, maxModules);

        return modules;
    }

    private void exploreCluster(Cluster cluster, Map<String, Map<String, Double>> matrix,
                                double CP, List<Cluster> modules, int maxModules) {
        if (modules.size() >= maxModules) return;

        if (cluster.isAtomic()) {
            modules.add(cluster);
            return;
        }

        // On récupère toutes les classes atomiques sous ce cluster
        List<String> elements = getAllAtomicClasses(cluster);
        double avgCoupling = computeAverageCoupling(elements, matrix);

        if (avgCoupling >= CP) {
            modules.add(cluster);
        } else {
            // sinon, descente récursive
            if (cluster.getLeft() != null)
                exploreCluster(cluster.getLeft(), matrix, CP, modules, maxModules);
            if (cluster.getRight() != null)
                exploreCluster(cluster.getRight(), matrix, CP, modules, maxModules);
        }
    }

    private List<String> getAllAtomicClasses(Cluster cluster) {
        List<String> result = new ArrayList<>();
        collectAtomicClasses(cluster, result);
        return result;
    }

    private void collectAtomicClasses(Cluster cluster, List<String> list) {
        if (cluster == null) return;
        if (cluster.isAtomic()) {
            list.add(cluster.getAtomicClass());
        } else {
            collectAtomicClasses(cluster.getLeft(), list);
            collectAtomicClasses(cluster.getRight(), list);
        }
    }

    private double computeAverageCoupling(List<String> classes, Map<String, Map<String, Double>> matrix) {
        if (classes.size() < 2) return 0.0;
        double sum = 0.0;
        int count = 0;

        for (int i = 0; i < classes.size(); i++) {
            for (int j = i + 1; j < classes.size(); j++) {
                String a = classes.get(i);
                String b = classes.get(j);

                double value = 0.0;
                if (matrix.containsKey(a) && matrix.get(a).containsKey(b)) {
                    value = matrix.get(a).get(b);
                } else if (matrix.containsKey(b) && matrix.get(b).containsKey(a)) {
                    value = matrix.get(b).get(a);
                }
                sum += value;
                count++;
            }
        }
        return count > 0 ? sum / count : 0.0;
    }


}
