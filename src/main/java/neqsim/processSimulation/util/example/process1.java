package neqsim.processSimulation.util.example;

import neqsim.processSimulation.processEquipment.stream.Stream;

public class process1 {

    private static final long serialVersionUID = 1000;

    /**
     * This method is just meant to test the thermo package.
     */
    public static void main(String args[]) {

        neqsim.thermo.system.SystemInterface testSystem = new neqsim.thermo.system.SystemSrkCPA((273.15 + 25.0), 50.00);
        testSystem.addComponent("methane", 180.00);
        testSystem.addComponent("ethane", 10.00);
        testSystem.addComponent("propane", 1.00);
      //  testSystem.addComponent("n-nonane", 1.00);
      //  testSystem.addComponent("water", 1.00);
        testSystem.createDatabase(true);
        testSystem.setMultiPhaseCheck(true);
        testSystem.setMixingRule(2);
        
        
        Stream stream_1 = new Stream("Stream1", testSystem);

        neqsim.processSimulation.processEquipment.compressor.Compressor compr = new neqsim.processSimulation.processEquipment.compressor.Compressor(stream_1);
        compr.setOutletPressure(80.0);
        compr.setPolytropicEfficiency(0.9);
        compr.setIsentropicEfficiency(0.9);
        compr.setUsePolytropicCalc(true);

        neqsim.processSimulation.processSystem.ProcessSystem operations = new neqsim.processSimulation.processSystem.ProcessSystem();
        operations.add(stream_1);
        operations.add(compr);


        operations.run();
        operations.displayResult();

     //   compr.solvePolytropicEfficiency(compr.getOutStream().getTemperature() + 0.01);
     //   operations.displayResult();
    }
}