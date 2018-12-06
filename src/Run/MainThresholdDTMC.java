package Run;

import DTMC.DTMCconstructor;
import DTMC.DTMCsimulator;
import Scheme.ServiceCounter;
import Scheme.ServiceDeployScheme;
import Simulation.Heuristic;
import Simulation.Violation;

/**
 *
 * @author Ashkan Y. This main class runs the scheme for different intervals of
 * running the heuristic in the scheme
 */
public class MainThresholdDTMC {

    private static int MAX_THRESHOLD = 80;
    private final static int TOTAL_RUN = 5000;

    private final static int TAU = 30; // time interval between run of the heuristic (s)
    private final static int TRAFFIC_CHANGE_INTERVAL = 10; // time interval between run of the heuristic (s)

    public static void main(String[] args) {

        RunParameters.TAU = TAU;
        RunParameters.TRAFFIC_CHANGE_INTERVAL = TRAFFIC_CHANGE_INTERVAL;
        int q = RunParameters.TAU / RunParameters.TRAFFIC_CHANGE_INTERVAL;
        // the number of times that traffic changes between each run of the heuristic

        DTMCconstructor dtmcConstructor = new DTMCconstructor();
        DTMCsimulator trafficRateSetter = new DTMCsimulator(dtmcConstructor.dtmc);

        Heuristic heuristicAllCloud = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_CLOUD), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicAllFog = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.ALL_FOG), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogStatic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, dtmcConstructor.getAverageTrafficRate()), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogDynamic = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);

        Heuristic heuristicFogStaticViolation = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_STATIC, dtmcConstructor.getAverageTrafficRate()), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);
        Heuristic heuristicFogDynamicViolation = new Heuristic(new ServiceDeployScheme(ServiceDeployScheme.FOG_DYNAMIC), RunParameters.NUM_FOG_NODES, RunParameters.NUM_SERVICES, RunParameters.NUM_CLOUD_SERVERS);

        Heuristic.initializeStaticVariables();

        ServiceCounter containersDeployedAllCloud;
        ServiceCounter containersDeployedAllFog;
        ServiceCounter containersDeployedFogStatic;
        ServiceCounter containersDeployedFogDynamic;
        ServiceCounter containersDeployedFogStaticViolation;
        ServiceCounter containersDeployedFogDynamicViolation;

        // used for getting average
        double fogcontainersDeployedAllCloud = 0;
        double fogcontainersDeployedAllFog = 0;
        double fogcontainersDeployedFogStatic = 0;
        double fogcontainersDeployedFogDynamic = 0;
        double fogcontainersDeployedFogStaticViolation = 0;
        double fogcontainersDeployedFogDynamicViolation = 0;

        // used for getting average
        double cloudcontainersDeployedAllCloud = 0;
        double cloudcontainersDeployedAllFog = 0;
        double cloudcontainersDeployedFogStatic = 0;
        double cloudcontainersDeployedFogDynamic = 0;
        double cloudcontainersDeployedFogStaticViolation = 0;
        double cloudcontainersDeployedFogDynamicViolation = 0;

        double delayAllCloud = 0;
        double delayAllFog = 0;
        double delayFogStatic = 0;
        double delayFogDynamic = 0;
        double delayFogStaticViolation = 0;
        double delayFogDynamicViolation = 0;

        double costAllCloud = 0;
        double costAllFog = 0;
        double costFogStatic = 0;
        double costFogDynamic = 0;
        double costFogStaticViolation = 0;
        double costFogDynamicViolation = 0;

        double violAllCloud = 0;
        double violAllFog = 0;
        double violFogStatic = 0;
        double violFogDynamic = 0;
        double violFogStaticViolation = 0;
        double violFogDynamicViolation = 0;

        double sumTrafficPerNodePerApp = 0; // used for getting average

        double violationSlack = Violation.getViolationSlack();
        double trafficPerNodePerApp;

        System.out.println("Threshold\tTraffic\tD(AC)\tD(AF)\tD(FS)\tD(FD)\tD(FSV)\tD(FDV)\tC(AC)\tC(AF)\tC(FS)\tC(FD)\tC(FSV)\tC(FDV)\tCNT(AC)\tCNT(AF)\tCNT(FS)\tCNT(FD)\tCNT(FSV)\tCNT(FDV)\tV(AC)\tV(AF)\tV(FS)\tV(FD)\tV(FSV)\tV(FDV)\tVS=" + violationSlack);

        for (int threshold = 5; threshold <= MAX_THRESHOLD; threshold = threshold + 1) {

            Heuristic.setThresholds(threshold);
            heuristicFogStatic.unsetFirstTimeBoolean();
            heuristicFogStaticViolation.unsetFirstTimeBoolean();

            for (int i = 0; i < TOTAL_RUN; i++) {

                trafficPerNodePerApp = trafficRateSetter.nextRate();
                Heuristic.distributeTraffic(trafficPerNodePerApp);

                sumTrafficPerNodePerApp += trafficPerNodePerApp;

                heuristicAllCloud.setTrafficToGlobalTraffic();
                containersDeployedAllCloud = heuristicAllCloud.run(Heuristic.COMBINED_APP_REGIONES, false);
                fogcontainersDeployedAllCloud += containersDeployedAllCloud.getDeployedFogServices();
                cloudcontainersDeployedAllCloud += containersDeployedAllCloud.getDeployedCloudServices();
                delayAllCloud += heuristicAllCloud.getAvgServiceDelay();
                costAllCloud += heuristicAllCloud.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
                violAllCloud += Violation.getViolationPercentage(heuristicAllCloud);

                heuristicAllFog.setTrafficToGlobalTraffic();
                containersDeployedAllFog = heuristicAllFog.run(Heuristic.COMBINED_APP_REGIONES, false);
                fogcontainersDeployedAllFog += containersDeployedAllFog.getDeployedFogServices();
                cloudcontainersDeployedAllFog += containersDeployedAllFog.getDeployedCloudServices();
                delayAllFog += heuristicAllFog.getAvgServiceDelay();
                costAllFog += heuristicAllFog.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
                violAllFog += Violation.getViolationPercentage(heuristicAllFog);

                heuristicFogStatic.setTrafficToGlobalTraffic();
                containersDeployedFogStatic = heuristicFogStatic.run(Heuristic.COMBINED_APP_REGIONES, false);
                fogcontainersDeployedFogStatic += containersDeployedFogStatic.getDeployedFogServices();
                cloudcontainersDeployedFogStatic += containersDeployedFogStatic.getDeployedCloudServices();
                delayFogStatic += heuristicFogStatic.getAvgServiceDelay();
                costFogStatic += heuristicFogStatic.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
                violFogStatic += Violation.getViolationPercentage(heuristicFogStatic);

                heuristicFogDynamic.setTrafficToGlobalTraffic();
                if (i % q == 0) {
                    containersDeployedFogDynamic = heuristicFogDynamic.run(Heuristic.COMBINED_APP_REGIONES, false);
                    fogcontainersDeployedFogDynamic += containersDeployedFogDynamic.getDeployedFogServices();
                    cloudcontainersDeployedFogDynamic += containersDeployedFogDynamic.getDeployedCloudServices();
                }
                delayFogDynamic += heuristicFogDynamic.getAvgServiceDelay();
                costFogDynamic += heuristicFogDynamic.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
                violFogDynamic += Violation.getViolationPercentage(heuristicFogDynamic);

                heuristicFogStaticViolation.setTrafficToGlobalTraffic();
                containersDeployedFogStaticViolation = heuristicFogStaticViolation.run(Heuristic.COMBINED_APP_REGIONES, true);
                fogcontainersDeployedFogStaticViolation += containersDeployedFogStaticViolation.getDeployedFogServices();
                cloudcontainersDeployedFogStaticViolation += containersDeployedFogStaticViolation.getDeployedCloudServices();

                delayFogStaticViolation += heuristicFogStaticViolation.getAvgServiceDelay();
                costFogStaticViolation += heuristicFogStaticViolation.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
                violFogStaticViolation += Violation.getViolationPercentage(heuristicFogStaticViolation);

                heuristicFogDynamicViolation.setTrafficToGlobalTraffic();
                if (i % q == 0) {
                    containersDeployedFogDynamicViolation = heuristicFogDynamicViolation.run(Heuristic.COMBINED_APP_REGIONES, true);
                    fogcontainersDeployedFogDynamicViolation += containersDeployedFogDynamicViolation.getDeployedFogServices();
                    cloudcontainersDeployedFogDynamicViolation += containersDeployedFogDynamicViolation.getDeployedCloudServices();
                }
                delayFogDynamicViolation += heuristicFogDynamicViolation.getAvgServiceDelay();
                costFogDynamicViolation += heuristicFogDynamicViolation.getCost(RunParameters.TRAFFIC_CHANGE_INTERVAL);
                violFogDynamicViolation += Violation.getViolationPercentage(heuristicFogDynamicViolation);

            }

            System.out.println(threshold + "\t" + ((sumTrafficPerNodePerApp * RunParameters.NUM_FOG_NODES * RunParameters.NUM_SERVICES) / (TOTAL_RUN))
                    + "\t" + (delayAllCloud / TOTAL_RUN) + "\t" + (delayAllFog / TOTAL_RUN) + "\t" + (delayFogStatic / TOTAL_RUN) + "\t" + (delayFogDynamic / TOTAL_RUN) + "\t" + (delayFogStaticViolation / TOTAL_RUN) + "\t" + (delayFogDynamicViolation / TOTAL_RUN)
                    + "\t" + ((costAllCloud / RunParameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costAllFog / RunParameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costFogStatic / RunParameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costFogDynamic / RunParameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costFogStaticViolation / RunParameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN) + "\t" + ((costFogDynamicViolation / RunParameters.TRAFFIC_CHANGE_INTERVAL) / TOTAL_RUN)
                    + "\t" + (fogcontainersDeployedAllCloud / TOTAL_RUN) + "\t" + (fogcontainersDeployedAllFog / TOTAL_RUN) + "\t" + (fogcontainersDeployedFogStatic / TOTAL_RUN) + "\t" + (fogcontainersDeployedFogDynamic / TOTAL_RUN) + "\t" + (fogcontainersDeployedFogStaticViolation / TOTAL_RUN) + "\t" + (fogcontainersDeployedFogDynamicViolation / TOTAL_RUN)
                    + "\t" + (cloudcontainersDeployedAllCloud / TOTAL_RUN) + "\t" + (cloudcontainersDeployedAllFog / TOTAL_RUN) + "\t" + (cloudcontainersDeployedFogStatic / TOTAL_RUN) + "\t" + (cloudcontainersDeployedFogDynamic / TOTAL_RUN) + "\t" + (cloudcontainersDeployedFogStaticViolation / TOTAL_RUN) + "\t" + (cloudcontainersDeployedFogDynamicViolation / TOTAL_RUN)
                    + "\t" + (violAllCloud / TOTAL_RUN) + "\t" + (violAllFog / TOTAL_RUN) + "\t" + (violFogStatic / TOTAL_RUN) + "\t" + (violFogDynamic / TOTAL_RUN) + "\t" + (violFogStaticViolation / TOTAL_RUN) + "\t" + (violFogDynamicViolation / TOTAL_RUN));

            // reset the average parameters
            fogcontainersDeployedAllCloud = 0;
            fogcontainersDeployedAllFog = 0;
            fogcontainersDeployedFogStatic = 0;
            fogcontainersDeployedFogDynamic = 0;
            fogcontainersDeployedFogStaticViolation = 0;
            fogcontainersDeployedFogDynamicViolation = 0;
            
            cloudcontainersDeployedAllCloud = 0;
            cloudcontainersDeployedAllFog = 0;
            cloudcontainersDeployedFogStatic = 0;
            cloudcontainersDeployedFogDynamic = 0;
            cloudcontainersDeployedFogStaticViolation = 0;
            cloudcontainersDeployedFogDynamicViolation = 0;

            delayAllCloud = 0;
            delayAllFog = 0;
            delayFogStatic = 0;
            delayFogDynamic = 0;
            delayFogStaticViolation = 0;
            delayFogDynamicViolation = 0;

            costAllCloud = 0;
            costAllFog = 0;
            costFogStatic = 0;
            costFogDynamic = 0;
            costFogStaticViolation = 0;
            costFogDynamicViolation = 0;

            violAllCloud = 0;
            violAllFog = 0;
            violFogStatic = 0;
            violFogDynamic = 0;
            violFogStaticViolation = 0;
            violFogDynamicViolation = 0;

            sumTrafficPerNodePerApp = 0;

        }
    }

}
