package scanner.util;

import spoon.Launcher;
import spoon.reflect.CtModel;

public class ModelGetter {
    public static CtModel getNewModel(String path){
        Launcher launcher = new Launcher();
        launcher.addInputResource(path);
        launcher.buildModel();
        return launcher.getModel();
    }
}
