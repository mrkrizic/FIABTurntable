import c2akka.c2architecture.ApplicationProviderModule;
import c2akka.c2architecture.C2ArchitectureBuilder;
import modules.FIABModule;
import runner.FIABTurntableApplicationRunner;
import modules.FIABTurntableModule;

public class FIABTurntableApplication {

    public static void main(String[] args) {
        ApplicationProviderModule module = new FIABTurntableModule(new FIABModule());
        runApplication(module);
    }

    private static void runApplication(ApplicationProviderModule module){
        new C2ArchitectureBuilder(FIABTurntableApplicationRunner.class, module).buildArchitectureAndRun();
    }


}