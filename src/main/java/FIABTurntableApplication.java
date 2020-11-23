import c2akka.c2architecture.ApplicationProviderModule;
import c2akka.c2architecture.C2ArchitectureBuilder;
import fiab.core.capabilities.plotting.WellknownPlotterCapability;
import fiab.opcua.server.NonEncryptionBaseOpcUaServer;
import fiab.opcua.server.OPCUABase;
import fiab.opcua.server.PublicNonEncryptionBaseOpcUaServer;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import runner.FIABTurntableApplicationRunner;
import modules.FIABTurntableModule;

import javax.sound.midi.Soundbank;

public class FIABTurntableApplication {

    public static void main(String[] args) {
        ApplicationProviderModule module = new FIABTurntableModule();
        runApplication(module);
    }

    private static void runApplication(ApplicationProviderModule module){
        new C2ArchitectureBuilder(FIABTurntableApplicationRunner.class, module).buildArchitectureAndRun();
    }


}