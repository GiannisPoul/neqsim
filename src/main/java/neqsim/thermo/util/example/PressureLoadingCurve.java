package neqsim.thermo.util.example;

import neqsim.thermo.system.SystemFurstElectrolyteEos;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermodynamicOperations.ThermodynamicOperations;
import org.apache.logging.log4j.*;

public class PressureLoadingCurve{

    private static final long serialVersionUID = 1000;
    static Logger logger = LogManager.getLogger(PressureLoadingCurve.class);
    
    /** This method is just meant to test the thermo package.
     */
    public static void main(String args[]){
        double[][] points;
        SystemInterface testSystem = new SystemFurstElectrolyteEos((273.15+75.0),1.3);
        
        double loading =  0.65;
        double molProsMDEA = 11.21;
        testSystem.addComponent("CO2", loading*molProsMDEA);
        testSystem.addComponent("water", 100.0-molProsMDEA-loading*molProsMDEA);             // legger til komponenter til systemet
        testSystem.addComponent("MDEA", molProsMDEA);             // legger til komponenter til systemet
        //testSystem.addComponent("Piperazine", loading*molProsMDEA*0.1);
        testSystem.chemicalReactionInit();
        testSystem.createDatabase(true);
        testSystem.setMixingRule(4);
        testSystem.init(0);
        ThermodynamicOperations testOps = new ThermodynamicOperations(testSystem);
        //        testOps.calcPloadingCurve();
        long time = System.currentTimeMillis();
        
        try{
            testOps.bubblePointPressureFlash(true);
        }
        catch(Exception e){
            logger.error(e.toString());
        }
        logger.info("Time taken for benchmark flash = " +(System.currentTimeMillis()-time));
        testSystem.display();
        logger.info("pressure " + testSystem.getPressure());
        int reactionNumber = 0;
        logger.info("K " + testSystem.getChemicalReactionOperations().getReactionList().getReaction(reactionNumber).calcK(testSystem,1));
        logger.info("Kx " + testSystem.getChemicalReactionOperations().getReactionList().getReaction(reactionNumber).calcKx(testSystem,1));
        logger.info("Kgamma " + testSystem.getChemicalReactionOperations().getReactionList().getReaction(reactionNumber).calcKgamma(testSystem,1));
        testSystem.setPressure(100.0);
        testSystem.getChemicalReactionOperations().solveChemEq(1);
        logger.info("K " + testSystem.getChemicalReactionOperations().getReactionList().getReaction(reactionNumber).calcK(testSystem,1));
        logger.info("Kx " + testSystem.getChemicalReactionOperations().getReactionList().getReaction(reactionNumber).calcKx(testSystem,1));
        logger.info("Kgamma " + testSystem.getChemicalReactionOperations().getReactionList().getReaction(reactionNumber).calcKgamma(testSystem,1));
        
        testSystem.display();
        //        thermo.ThermodynamicModelTest testModel = new thermo.ThermodynamicModelTest(testSystem);
        //        testModel.runTest();
        // write data to netcdf
        //testOps.writeNetCDF("c:/testloading.nc");
    }
}