/*
 * Test.java
 *
 * Created on 22. januar 2001, 22:59
 */

package neqsim.thermo.util.parameterFitting.pureComponentParameterFitting.cpaParam;

/**
 *
 * @author  Even Solbraa
 * @version
 */
public class CPAFunctionDens extends CPAFunction {

    private static final long serialVersionUID = 1000;
    
    int phasetype = 1;
    /** Creates new Test */
    public CPAFunctionDens(){
    }
    
    public CPAFunctionDens(int phase){
        phasetype = phase;
    }
    
    public double calcTrueValue(double val){
        return val;
    }
    
    //    public double calcValue(double[] dependentValues){
    //        system.setTemperature(dependentValues[0]);
    //        system.init(0);
    //        system.init(1);
    //        system.initPhysicalProperties();
    //        return system.getPhase(phasetype).getPhysicalProperties().getDensity();
    //    }
    public double calcValue2(double[] dependentValues){
        system.setTemperature(dependentValues[0]);
        system.setPressure(1.0);//system.getPhases()[0].getComponents()[0].getAntoineVaporPressure(dependentValues[0]));
        try{
            thermoOps.bubblePointPressureFlash(false);
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
        system.initPhysicalProperties();
        // System.out.println("pres: " + system.getPressure());
        return system.getPhase(phasetype).getPhysicalProperties().getDensity();
    }
    
    public double calcValue(double[] dependentValues){
        //system.setTemperature(dependentValues[0]);
        //system.setPressure(system.getPhases()[0].getComponents()[0].getAntoineVaporPressure(dependentValues[0]));
        
        system.init(0);
        system.init(1);
        system.initPhysicalProperties();
        // System.out.println("pres: " + system.getPressure());
        return system.getPhase(phasetype).getPhysicalProperties().getDensity();
    }
}