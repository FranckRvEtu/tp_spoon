package scanner.service;


import org.springframework.stereotype.Service;
import scanner.model.Cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ClusteringService {

    private Cluster createHierchicalCluster(Map<String, Map<String, Double>> couplingMatrix) {
        List<Cluster> clusters = new ArrayList<>();

        //Initialisation de la liste des clusters initiaux
        for (String cls : couplingMatrix.keySet()) {
            clusters.add(new Cluster(cls));
        }

        while (clusters.size() > 1){
            Cluster bestA = null,  bestB = null;
            double maxCoupling = -1.0;

            for (int i = 0; i < clusters.size(); i++){
                for (int j = i + 1; j < clusters.size(); j++){
                    //TODO
                }
            }
        }
        }

}
