package scanner.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import scanner.model.Cluster;
import scanner.model.CouplingMatrix;
import scanner.service.ClusteringService;
import scanner.service.CouplingService;
import scanner.service.MetricsService;
import org.springframework.web.bind.annotation.*;
import scanner.util.GraphExporter;

import java.util.*;


@Controller
public class MetricsController {

    @Autowired
    private MetricsService metricsService;
    @Autowired
    private CouplingService couplingService;
    @Autowired
    private ClusteringService clusteringService;

    @GetMapping("/")
    public String index(){
        return "index";
    }

    //Endpoint pour analyser puis calculer les métriques
    @PostMapping("/analyzer/results")
    public String calculateMetrics(
            @RequestParam("path") String src,
            @RequestParam("x") int x,
            @RequestParam("cp") double cp,
            Model model){
        try{
            if (src == null || src.trim().isEmpty()){
                throw new IllegalArgumentException("Veuillez fournir un chemin");
            }
            //Métriques et graphe du TP1
            Map<String,Object> metrics = metricsService.analyzeProjects(src,x);
            model.addAttribute("metrics", metrics);

            String graphStr = metricsService.buildCallGraph(src);
            model.addAttribute("graph", graphStr);


            //Calcul du couplage
            //System.out.println("Graphe ajouté au model, début du calcul du couplage");
            CouplingMatrix matrixObject = couplingService.getCouplingsMatrix(src);
            //System.err.println("Matrice dans le controller : "+matrixObject.getMatrix().toString());

            model.addAttribute("rows", matrixObject.getRows());
            model.addAttribute("cols", matrixObject.getCols());
            model.addAttribute("matrix", matrixObject.getMatrix());

            //Mise en graphe du couplage
            String couplingGraph = couplingService.getCouplingGraph(matrixObject);
            //DEBUG System.err.println( "Coupling Graph : "+couplingGraph );
            model.addAttribute("couplingGraph", couplingGraph);

            //Calcul du clustering hiérarchique
            Cluster rootCluster = clusteringService.createHierchicalCluster(matrixObject.getMatrix());
            model.addAttribute("rootCluster", rootCluster);
            String dendrogramDot = GraphExporter.clusterToDot(rootCluster);
            model.addAttribute("dendrogramGraph", dendrogramDot);

            //Identification des modules
            List<Cluster> modules = clusteringService.identifyModules(rootCluster,matrixObject.getMatrix(),cp,(int) metrics.get("Nombre de classes"));
            model.addAttribute("modules", modules);
            return "index";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "index"; // recharge la page avec un message d’erreur
        } catch (Exception e) {
            model.addAttribute("error", "Erreur interne : " + e.getMessage());
            return "index";
        }
    }

}