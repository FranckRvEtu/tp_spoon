package scanner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import spoon.Launcher;
import spoon.reflect.CtModel;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class ScannerApplication implements CommandLineRunner {

    @Autowired
    Environment env;

    public ScannerApplication(Environment environment) {
    }

    public static void main(String[] args) {
		SpringApplication.run(ScannerApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        System.err.println("L'application est dispobible à l'adresse localhost://"+env.getProperty("local.server.port"));
    }
}
