package scanner.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import scanner.model.CouplingMatrix;
import scanner.service.CouplingService;
import scanner.service.MetricsService;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@Controller
public class MetricsController {

    @Autowired
    private MetricsService metricsService;
    @Autowired
    private CouplingService couplingService;

    @GetMapping("/")
    public String index(){
        return "index";
    }

    //Endpoint pour analyser puis calculer les métriques
    @PostMapping("/analyzer/results")
    public String calculateMetrics(
            @RequestParam("path") String src,
            @RequestParam("x") int x,
            Model model){
        try{
            if (src == null || src.trim().isEmpty()){
                throw new IllegalArgumentException("Veuillez fournir un chemin");
            }
            Map<String,Object> metrics = metricsService.analyzeProjects(src,x);
            model.addAttribute("metrics", metrics);
            String graphStr = metricsService.buildCallGraph(src);
            model.addAttribute("graph", graphStr);


            //Calcul du couplage
            CouplingMatrix matrixObject = couplingService.getCouplingsMatrix(src);
            System.err.println("Matrice dans le controller : "+matrixObject.getMatrix().toString());

            model.addAttribute("rows", matrixObject.getRows());
            model.addAttribute("cols", matrixObject.getCols());
            model.addAttribute("matrix", matrixObject.getMatrix());

            //Mise en graphe du couplage
            String couplingGraph = couplingService.getCouplingGraph(matrixObject);
            //System.err.println( "Coupling Graph : "+couplingGraph );
            model.addAttribute("couplingGraph", couplingGraph);

            return "index";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "index"; // recharge la page avec un message d’erreur
        } catch (Exception e) {
            model.addAttribute("error", "Erreur interne : " + e.getMessage());
            return "index";
        }
    }

    /*@GetMapping("/analyzer/callgraph")
    public String calculateCallGraph(@RequestParam("path") String path, Model model){
        Map<CtMethod<?>, Set<CtMethod<?>>> graph = service.buildCallGraph(path);
        String graphStr = CallGraphExporter.toDot(graph);
        model.addAttribute("graph", graphStr);
        return "index";
    }*/


}