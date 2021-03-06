/*
 * Test.java
 *
 * Created on 22. januar 2001, 22:59
 */

package neqsim.thermo.util.parameterFitting.Procede.WaterMDEA;

import neqsim.statistics.parameterFitting.nonLinearParameterFitting.LevenbergMarquardtFunction;
import neqsim.thermo.mixingRule.HVmixingRuleInterface;
import neqsim.thermo.phase.PhaseEosInterface;
import org.apache.logging.log4j.*;

/**
 *
 * @author  Even Solbraa
 * @version
 */
public class BinaryHVParameterFittingFunction_MDEA extends LevenbergMarquardtFunction {

    private static final long serialVersionUID = 1000;
    int type = 0;
    int phase = 0;
    static Logger logger = LogManager.getLogger(BinaryHVParameterFittingFunction_MDEA.class);
    
    /** Creates new Test */
    public BinaryHVParameterFittingFunction_MDEA() {
    }
    
    public BinaryHVParameterFittingFunction_MDEA(int phase, int type) {
        this.phase = phase;
        this.type = type;
    }
    
    public double calcValue(double[] dependentValues){
        double aMDEAold, aWaterold, aMDEAnew, aWaternew, daMDEAdT, daWaterdT, H;
        
        if(type ==0){
            try{
                thermoOps.bubblePointPressureFlash(false);
            } catch(Exception e){
                logger.error(e.toString());
            }
            return (system.getPressure());//*system.getPhases()[0].getComponent(0).getx());
        }
        
        if (type==1){
            system.init(0);
            system.init(1);
            return system.getPhases()[1].getActivityCoefficient(0);
        }
        
        if (type==2){
            system.init(0);
            system.init(1);
            aMDEAold = system.getPhase(1).getActivityCoefficient(1);
            aWaterold = system.getPhase(1).getActivityCoefficient(0);
            system.setTemperature(system.getTemperature()+0.00001);
            system.init(0);
            system.init(1);
            aMDEAnew = system.getPhase(1).getActivityCoefficient(1);
            aWaternew = system.getPhase(1).getActivityCoefficient(0);
            daMDEAdT = (Math.log(aMDEAnew)-Math.log(aMDEAold))/0.00001;
            daWaterdT = (Math.log(aWaternew)-Math.log(aWaterold))/0.00001;
            system.setTemperature(system.getTemperature()-0.00001);
            H = -8.314*system.getTemperature()*system.getTemperature()*(system.getPhase(1).getComponent(0).getx()*daWaterdT + system.getPhase(1).getComponent(1).getx()*daMDEAdT);
            return H;
        }
        
        if (type==3){
            system.init(0);
            system.init(1);
            return system.getPhase(1).getActivityCoefficient(0);//system.getPhase(0).getComponent(0).getFugasityCoeffisient();
        }
        
        
        if (type==4){
            system.init(0);
            system.init(1);
            return system.getPhase(1).getActivityCoefficient(1);//system.getPhase(0).getComponent(0).getFugasityCoeffisient();
        }
        
        
        return (0);
    }
    
    public double calcTrueValue(double val){
        return (val);
    }
    
    
    public void setFittingParams(int i, double value){
        params[i] = value;
        
        /*((PhaseEosInterface)system.getPhases()[0]).getMixingRule().setBinaryInteractionParameter(0,1, value);
        ((PhaseEosInterface)system.getPhases()[1]).getMixingRule().setBinaryInteractionParameter(0,1, value);*/
        
        if(i==0){
            ((HVmixingRuleInterface) ((PhaseEosInterface)system.getPhases()[0]).getMixingRule()).setHVDijParameter(0,1, value);
            ((HVmixingRuleInterface) ((PhaseEosInterface)system.getPhases()[1]).getMixingRule()).setHVDijParameter(0,1, value);
        }
        if(i==1){
            ((HVmixingRuleInterface) ((PhaseEosInterface)system.getPhases()[0]).getMixingRule()).setHVDijParameter(1,0, value);
            ((HVmixingRuleInterface) ((PhaseEosInterface)system.getPhases()[1]).getMixingRule()).setHVDijParameter(1,0, value);
        }
        
        if(i==4){
            ((HVmixingRuleInterface) ((PhaseEosInterface)system.getPhases()[0]).getMixingRule()).setHValphaParameter(0,1, value);
            ((HVmixingRuleInterface) ((PhaseEosInterface)system.getPhases()[1]).getMixingRule()).setHValphaParameter(0,1, value);
        }
        
        if(i==2){
            ((HVmixingRuleInterface) ((PhaseEosInterface)system.getPhases()[0]).getMixingRule()).setHVDijTParameter(0,1, value);
            ((HVmixingRuleInterface) ((PhaseEosInterface)system.getPhases()[1]).getMixingRule()).setHVDijTParameter(0,1, value);
        }
        if(i==3){
            ((HVmixingRuleInterface) ((PhaseEosInterface)system.getPhases()[0]).getMixingRule()).setHVDijTParameter(1,0, value);
            ((HVmixingRuleInterface) ((PhaseEosInterface)system.getPhases()[1]).getMixingRule()).setHVDijTParameter(1,0, value);
        }
        
        
    }
    
    
}