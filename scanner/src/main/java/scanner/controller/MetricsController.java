package scanner.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import scanner.service.MetricsService;
import scanner.util.CallGraphExporter;
import scanner.util.MyScanner;
import org.springframework.web.bind.annotation.*;
import spoon.reflect.declaration.CtMethod;

import java.util.List;
import java.util.Map;
import java.util.Set;


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
            //TODO Lancer le calcul de couplage
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