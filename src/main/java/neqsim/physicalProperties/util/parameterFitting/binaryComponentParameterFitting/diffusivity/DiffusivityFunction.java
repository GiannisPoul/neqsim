/*
 * RacketFunction.java
 *
 * Created on 24. januar 2001, 21:15
 */

package neqsim.physicalProperties.util.parameterFitting.binaryComponentParameterFitting.diffusivity;

import neqsim.statistics.parameterFitting.nonLinearParameterFitting.LevenbergMarquardtFunction;

/**
 *
 * @author  Even Solbraa
 * @version
 */
public class DiffusivityFunction extends LevenbergMarquardtFunction{

    private static final long serialVersionUID = 1000;
    
    
    public DiffusivityFunction() {
    }
    
    public double calcValue(double[] dependentValues){
        system.init(1);
        system.initPhysicalProperties();
        return system.getPhase(1).getPhysicalProperties().getDiffusionCoeffisient(0,1)*1e9;
    }
    
    public void setFittingParams(int i, double value){
         params[i] = value;
//        system.getPhases()[0].getPhysicalProperties().getMixingRule().setViscosityGij(value, 0, 1);
//        system.getPhases()[0].getPhysicalProperties().getMixingRule().setViscosityGij(value, 1, 0);
    //    system.getPhase(1).getComponent(0).setLiquidViscosityModel(i);//system.getPhases()[1].getComponent(0).setMolarMass(i);//getPhysicalProperties().getMixingRule().setViscosityGij(value, 1, 0);
    //    system.getPhase(0).getComponent(0).setLiquidViscosityModel(i);//MolarMass(i);//system.getPhase(1).getPhysicalProperties().getMixingRule().setViscosityGij(value, 0, 1);
    }
}