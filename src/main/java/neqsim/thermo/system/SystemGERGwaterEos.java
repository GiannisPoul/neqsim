/*
 * System_SRK_EOS.java
 *
 * Created on 8. april 2000, 23:05
 */

package neqsim.thermo.system;

import neqsim.thermo.phase.PhaseHydrate;
import neqsim.thermo.phase.PhasePrEos;
import neqsim.thermo.phase.PhasePureComponentSolid;

/**
 *
 * @author  Even Solbraa
 * @version
 */

/** This class defines a thermodynamic system using the SRK equation of state
 */
public class SystemGERGwaterEos extends SystemPrEos {

    private static final long serialVersionUID = 1000;
    
    //  SystemPrEos clonedSystem;
    public SystemGERGwaterEos(){
        super();
        modelName = "GERG-water-EOS";
        attractiveTermNumber = 10;
        for (int i=0;i<numberOfPhases;i++){
            phaseArray[i] = new PhasePrEos();
            phaseArray[i].setTemperature(298.15);
            phaseArray[i].setPressure(1.0);
        }
    }
    
    public SystemGERGwaterEos(double T, double P) {
        super(T,P);
        modelName = "GERG-water-EOS";
        attractiveTermNumber = 10;
        for (int i=0;i<numberOfPhases;i++){
            phaseArray[i] = new PhasePrEos();
            phaseArray[i].setTemperature(T);
            phaseArray[i].setPressure(P);
        }
    }
    
    public SystemGERGwaterEos(double T, double P, boolean solidCheck) {
        this(T,P);
        modelName = "GERG-water-EOS";
        attractiveTermNumber = 10;
        numberOfPhases = 5;
        maxNumberOfPhases = 5;
        solidPhaseCheck = solidCheck;
        
        for (int i=0;i<numberOfPhases;i++){
            phaseArray[i] = new PhasePrEos();
            phaseArray[i].setTemperature(T);
            phaseArray[i].setPressure(P);
        }
        
        if(solidPhaseCheck){
            //System.out.println("here first");
            phaseArray[numberOfPhases-1] = new PhasePureComponentSolid();
            phaseArray[numberOfPhases-1].setTemperature(T);
            phaseArray[numberOfPhases-1].setPressure(P);
            phaseArray[numberOfPhases-1].setRefPhase(phaseArray[1].getRefPhase());
        }
        
        if(hydrateCheck){
            //System.out.println("here first");
            phaseArray[numberOfPhases-1] = new PhaseHydrate();
            phaseArray[numberOfPhases-1].setTemperature(T);
            phaseArray[numberOfPhases-1].setPressure(P);
            phaseArray[numberOfPhases-1].setRefPhase(phaseArray[1].getRefPhase());
        }
    }
    
    public Object clone(){
        SystemGERGwaterEos clonedSystem = null;
        try{
            clonedSystem = (SystemGERGwaterEos) super.clone();
        }
        catch(Exception e) {
            e.printStackTrace(System.err);
        }
        
        //        clonedSystem.phaseArray = (PhaseInterface[]) phaseArray.clone();
        //        for(int i = 0; i < numberOfPhases; i++) {
        //            clonedSystem.phaseArray[i] = (PhaseInterface) phaseArray[i].clone();
        //        }
        
        return clonedSystem;
    }
    
    
}