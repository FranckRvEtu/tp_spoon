package scanner.service;

import org.springframework.stereotype.Service;
import scanner.util.CallGraphExporter;
import scanner.util.CallGraphScanner;
import scanner.util.MetricsCalculator;
import scanner.util.MyScanner;
import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtType;

import java.util.*;

@Service
public class MetricsService {

    //Méthode pour éviter le boilerplate entre les deux autres méthodes
    private CtModel getNewModel(String path){
        Launcher launcher = new Launcher();
        launcher.addInputResource(path);
        launcher.buildModel();
        return launcher.getModel();
    }

    //Analyse le projets
    public Map<String, Object> analyzeProjects(String path, int x){

        CtModel model = getNewModel(path);
        System.out.println("model : "+model.getAllTypes().size());

        MyScanner myScanner = new MyScanner();
        if (model.getRootPackage() == null) {
            throw new IllegalStateException("Aucun package racine trouvé pour le chemin : " + path);
        }
        System.out.println("Scanner instancié");
        try {
            model.getRootPackage().accept(myScanner);
            //DEBUG Collection<CtType<?>> types = model.getAllTypes();
            /*Debug model.getAllTypes().forEach(type -> {
                System.err.println(type.getSimpleName()+" - "+(type.getPosition().getEndLine()-type.getPosition().getLine()));
            });*/
            //DEBUG System.out.println("Toutes les classes : "+model.getAllTypes());
            System.out.println("Scanner appliqué");
        } catch (Exception e) {
            System.err.println("Erreur pendant le scan Spoon : " + e.getMessage());
            e.printStackTrace();
        }

        return MetricsCalculator.compute(myScanner, x);
    }

    public String buildCallGraph(String path) {
        System.out.println("Création du graphe");
        CtModel model = getNewModel(path);
        CallGraphScanner scanner = new CallGraphScanner();
        model.getRootPackage().accept(scanner);
        Map<CtMethod<?>, Set<CtMethod<?>>> graph = scanner.getCallGraph();
        return CallGraphExporter.toDot(graph);
    }
}