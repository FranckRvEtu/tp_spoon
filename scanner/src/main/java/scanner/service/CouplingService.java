package scanner.service;

import org.springframework.stereotype.Service;
import scanner.model.CouplingMatrix;
import scanner.util.GraphExporter;
import scanner.util.InvocationScanner;
import scanner.util.MetricsCalculator;
import spoon.reflect.CtModel;

import java.util.*;

import static scanner.util.ModelGetter.getNewModel;

@Service
public class CouplingService {

    public CouplingMatrix getCouplingsMatrix(String path){
        System.out.println("Calcul du couplage");
        CtModel model = getNewModel(path);
        InvocationScanner scanner = new InvocationScanner();
        model.getRootPackage().accept(scanner);
        Map<String, Map<String, Double>>  couplings = MetricsCalculator.computeCoupling(scanner.getCoupling(), scanner.getTotalCall());
        LinkedHashSet<String> colsSet = new LinkedHashSet<>();

        // inclure les classes "appelantes"
        colsSet.addAll(couplings.keySet());
        // inclure les classes "appelées"
        couplings.values().forEach(inner -> colsSet.addAll(inner.keySet()));

        // Liste triée/ordonnée des colonnes
        List<String> cols = new ArrayList<>(colsSet);
        // Pour les lignes on peut choisir l'ordre voulu (ici on prend les appelantes)
        List<String> rows = new ArrayList<>(couplings.keySet());

        // 2) Construire la matrice complète (String formaté à 5 décimales)
        Map<String, Map<String, Double>> matrix = new LinkedHashMap<>();

        for (String r : rows) {
            Map<String, Double> rowMap = new LinkedHashMap<>();
            Map<String, Double> inner = couplings.getOrDefault(r, Collections.emptyMap());
            for (String c : cols) {
                double v = inner.getOrDefault(c, 0.0);
                rowMap.put(c, v);
            }
            matrix.put(r, rowMap);
        }
        CouplingMatrix res = new CouplingMatrix(rows,cols,matrix);
        System.err.println("Matrice en sorti du service : "+ res.getMatrix().toString());
        return new CouplingMatrix(rows,cols,matrix);
    }

    public String getCouplingGraph(CouplingMatrix matrix) {
        System.out.println("Crétion du graphe de couplage pondéré");
        return GraphExporter.buildCouplingGraphDot(matrix.getMatrix());
    }
}
