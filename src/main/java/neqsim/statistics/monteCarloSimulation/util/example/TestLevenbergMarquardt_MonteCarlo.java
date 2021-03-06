/*
 * TestAcentric.java
 *
 * Created on 23. januar 2001, 22:08
 */

package neqsim.statistics.monteCarloSimulation.util.example;

import java.util.*;
import neqsim.statistics.parameterFitting.SampleSet;
import neqsim.statistics.parameterFitting.SampleValue;
import neqsim.statistics.parameterFitting.nonLinearParameterFitting.LevenbergMarquardt;
/**
 *
 * @author  Even Solbraa
 * @version
 */
public class TestLevenbergMarquardt_MonteCarlo extends java.lang.Object {

    private static final long serialVersionUID = 1000;
    
    /** Creates new TestAcentric */
    public TestLevenbergMarquardt_MonteCarlo() {
    }
    
    
    public static void main(String[] args){
        ArrayList sampleList = new ArrayList();
        
        TestFunction function = new  TestFunction();
        
        double sample1[] = {0.1};  // temperature
        double standardDeviation1[] = {0.1}; // 
        SampleValue sample_01 = new SampleValue(0.5, 0.05, sample1, standardDeviation1);
        sample_01.setFunction(function);
        sampleList.add(sample_01);
        
        double sample2[] = {0.2};  // temperature
        double standardDeviation2[] = {0.2}; // std.dev temperature    // presure std.dev pressure
        SampleValue sample_02 = new SampleValue(0.3, 0.03, sample2, standardDeviation2);
        sample_02.setFunction(function);
        sampleList.add(sample_02);
        
        double sample3[] = {0.3};  // temperature
        double standardDeviation3[] = {0.3}; // std.dev temperature    // presure std.dev pressure
        SampleValue sample_03 = new SampleValue(0.1, 0.01, sample3, standardDeviation3);
        sample_03.setFunction(function);
        sampleList.add(sample_03);
        
       
        double guess[] = {0.311, 1.0};
        function.setInitialGuess(guess);
        
        LevenbergMarquardt optim = new LevenbergMarquardt();
        SampleSet sampleSet = new SampleSet(sampleList);
        optim.setSampleSet(sampleSet);
        
        optim.solve();
        optim.runMonteCarloSimulation();
        optim.displayCurveFit();
    }
}
