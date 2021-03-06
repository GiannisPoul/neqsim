/*
 * Conductivity.java
 *
 * Created on 1. november 2000, 19:00
 */

package neqsim.physicalProperties.physicalPropertyMethods.liquidPhysicalProperties.diffusivity;

import neqsim.physicalProperties.physicalPropertyMethods.liquidPhysicalProperties.conductivity.Conductivity;
import org.apache.logging.log4j.*;

/**
 *
 * @author  Even Solbraa
 * @version
 */
abstract class Diffusivity  extends neqsim.physicalProperties.physicalPropertyMethods.liquidPhysicalProperties.LiquidPhysicalPropertyMethod implements neqsim.physicalProperties.physicalPropertyMethods.methodInterface.DiffusivityInterface, Cloneable{

    private static final long serialVersionUID = 1000;
    static Logger logger = LogManager.getLogger(Diffusivity.class);

    
    double[][] binaryDiffusionCoeffisients;
    double[] effectiveDiffusionCoefficient;
    
    /** Creates new Conductivity */
    
    public Diffusivity() {
    }
    
    public Diffusivity(neqsim.physicalProperties.physicalPropertySystem.PhysicalPropertiesInterface liquidPhase) {
        super(liquidPhase);
        binaryDiffusionCoeffisients = new double[liquidPhase.getPhase().getNumberOfComponents()][liquidPhase.getPhase().getNumberOfComponents()];
        effectiveDiffusionCoefficient = new double[liquidPhase.getPhase().getNumberOfComponents()];
        
    }
    
    public Object clone(){
        Diffusivity properties = null;
        
        try{
            properties = (Diffusivity) super.clone();
        }
        catch(Exception e) {
            logger.error("Cloning failed.",e);
        }
        
        properties.binaryDiffusionCoeffisients = this.binaryDiffusionCoeffisients.clone();
        for(int i=0;i<liquidPhase.getPhase().getNumberOfComponents();i++){
            System.arraycopy(this.binaryDiffusionCoeffisients[i], 0, properties.binaryDiffusionCoeffisients[i], 0, liquidPhase.getPhase().getNumberOfComponents());
        }
        return properties;
    }
    
    public double[][] calcDiffusionCoeffisients(int binaryDiffusionCoefficientMethod , int multicomponentDiffusionMethod){
        double tempVar=0 , tempVar2=0;
        
        for(int i = 0; i < liquidPhase.getPhase().getNumberOfComponents(); i++) {
            for(int j = 0; j < liquidPhase.getPhase().getNumberOfComponents(); j++) {
                binaryDiffusionCoeffisients[i][j] =  calcBinaryDiffusionCoefficient(i, j, binaryDiffusionCoefficientMethod);
            }
        }
        
        // Vignes correlation
        for(int i = 0; i < liquidPhase.getPhase().getNumberOfComponents(); i++) {
            for(int j = 0; j < liquidPhase.getPhase().getNumberOfComponents(); j++) {
                if(i!=j) {
                    binaryDiffusionCoeffisients[i][j] = Math.pow(binaryDiffusionCoeffisients[i][j],liquidPhase.getPhase().getComponents()[j].getx())*Math.pow(binaryDiffusionCoeffisients[j][i],liquidPhase.getPhase().getComponents()[i].getx());
                }
                //System.out.println("diff liq " + binaryDiffusionCoeffisients[i][j] );
            }
        }
        return binaryDiffusionCoeffisients;
    }
    
    
    public void calcEffectiveDiffusionCoeffisients(){
        double sum=0;
        
        for(int i = 0; i < liquidPhase.getPhase().getNumberOfComponents(); i++) {
            sum = 0;
            for(int j = 0; j < liquidPhase.getPhase().getNumberOfComponents(); j++) {
                if(i==j){
                }
                else{
                    sum += liquidPhase.getPhase().getComponents()[j].getx()/binaryDiffusionCoeffisients[i][j];
                }
            }
            effectiveDiffusionCoefficient[i] = (1.0-liquidPhase.getPhase().getComponents()[i].getx())/sum;
        }
    }
    
    public double getMaxwellStefanBinaryDiffusionCoefficient(int i, int j){
        return binaryDiffusionCoeffisients[i][j];
    }
    
    public double getEffectiveDiffusionCoefficient(int i){
        return effectiveDiffusionCoefficient[i];
    }
    
    public double getFickBinaryDiffusionCoefficient(int i, int j){
        double temp = (i==j)? 1.0: 0.0;
        double nonIdealCorrection = temp + liquidPhase.getPhase().getComponents()[i].getx() * liquidPhase.getPhase().getComponents()[i].getdfugdn(j) *  liquidPhase.getPhase().getNumberOfMolesInPhase();
        if (Double.isNaN(nonIdealCorrection)) {
            nonIdealCorrection=1.0;
        }
        return binaryDiffusionCoeffisients[i][j]*nonIdealCorrection; // shuld be divided by non ideality factor
    }
}
