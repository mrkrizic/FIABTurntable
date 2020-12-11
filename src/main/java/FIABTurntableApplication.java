import c2akka.c2architecture.ApplicationProviderModule;
import c2akka.c2architecture.C2ArchitectureBuilder;
import com.google.inject.AbstractModule;
import configurations.ServerConfig;
import modules.FIABModule;
import runner.FIABTurntableApplicationRunner;
import modules.FIABTurntableModule;

public class FIABTurntableApplication {

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException("Machine needs a name in order to load a config");
        } else if (args.length > 1) {
            throw new IllegalArgumentException("Machine needs exactly one name in order to load a config");
        }
        ServerConfig.setMachineName(args[0]);
        ApplicationProviderModule module = new FIABTurntableModule(new FIABModule());
        runApplication(module);
    }

    private static void runApplication(ApplicationProviderModule module) {
        new C2ArchitectureBuilder(FIABTurntableApplicationRunner.class, module).buildArchitectureAndRun();
    }

}