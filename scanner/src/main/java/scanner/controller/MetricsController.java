package scanner.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import scanner.service.MetricsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@Controller
public class MetricsController {

    @Autowired
    private MetricsService service;

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
            Map<String,Object> metrics = service.analyzeProjects(src,x);
            model.addAttribute("metrics", metrics);
            String graphStr = service.buildCallGraph(src);
            model.addAttribute("graph", graphStr);
            //Calcul du couplage
            Map<String, Map<String, Double>> couplings = service.getCouplings(src);
            model.addAttribute("couplings", couplings );
            //Mise en graphe du couplage
            String couplingGraph = service.buildCallGraph(src);
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