package neqsim.thermodynamicOperations.flashOps.saturationOps;

/*
 * TPflash.java
 *
 * Created on 27. september 2001, 09:43
 */

import neqsim.dataPresentation.JFreeChart.graph2b;
import neqsim.thermo.ThermodynamicConstantsInterface;
import static neqsim.thermo.ThermodynamicConstantsInterface.R;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermo.system.SystemSrkSchwartzentruberEos;
import neqsim.thermodynamicOperations.ThermodynamicOperations;


//import dataPresentation.
/**
 *
 * @author  esol
 * @version
 */
public class FugTestConstP extends constantDutyTemperatureFlash implements ThermodynamicConstantsInterface {

    private static final long serialVersionUID = 1000;
    public double temp=0.0, pres=0.0;
    public SystemInterface testSystem, testSystem2;
    public ThermodynamicOperations testOps, testOps2;
    public int compNumber=0;
    public String compName;
    public boolean compNameGiven=false;
    
    /** Creates new FugTestdT */
    public FugTestConstP() {
    }
    
    public FugTestConstP(SystemInterface system) {
    this.testSystem=system;
    this.testOps = new ThermodynamicOperations(testSystem);
    this.pres= testSystem.getPressure();
    }
    
    public FugTestConstP(SystemInterface system,double pres) {
    this.testSystem=system;
    this.testOps = new ThermodynamicOperations(testSystem);
    this.pres= pres;
    }
    
    // initializing reference system for pure vapor fugacity
    public void initTestSystem2(int K){
         this.testSystem2 = new SystemSrkSchwartzentruberEos(temp,pres);
         this.testSystem2.addComponent(compName,1);
         this.testSystem2.setPhaseType(0,1);
         this.testOps2 = new ThermodynamicOperations(testSystem2);
    }
    
    public void run(){
        double SolidFug =0.0, Pvapsolid=0.0, SolVapFugCoeff=0.0, dfugdt=0.0;
        double solvol=0.0, soldens=0.0, trpTemp=0.0;
        boolean CCequation = true;
        double[][] Fug = new double[4][20];
        double[][] Fugrel = new double[2][20];
        int vapPhase=0; 
        double Xivapor;
      
        for(int k=0;k<testSystem.getPhases()[0].getNumberOfComponents();k++){
            if(testSystem.getPhase(0).getComponent(k).doSolidCheck()){
               this.compName = testSystem.getPhase(0).getComponent(k).getComponentName();
               trpTemp = testSystem.getPhases()[0].getComponents()[k].getTriplePointTemperature();
               this.initTestSystem2(k); 
               if(Math.abs(testSystem.getPhases()[0].getComponents()[k].getHsub())<0.000001) {
                   CCequation=false;
               } 
                    for(int i=0;i<20;i++){
                    System.out.println("--- calculating --- "+compName+" at "); 
                    temp=trpTemp+2-i*trpTemp/40;
                    testSystem.setTemperature(temp);
                    testSystem2.setTemperature(temp);
                    System.out.println("temperature " + temp);
                    if(temp>trpTemp+0.1) {
                        temp=trpTemp;
                    }
                    //Vapor pressure estiamtion
                    if(CCequation){
                        Pvapsolid = testSystem.getPhase(0).getComponent(k).getCCsolidVaporPressure(temp);
                        System.out.println("pvap solid CC " +Pvapsolid);
                        }
                    else{
                        Pvapsolid = testSystem.getPhase(0).getComponent(k).getSolidVaporPressure(temp);
                        System.out.println("pvap solid Antonie " +Pvapsolid);
                        }
                    soldens = testSystem.getPhase(0).getComponent(k).getPureComponentSolidDensity(temp)*1000;
                    if(soldens>2000) {
                        soldens = 1000;
                    }
                    System.out.println("Solid_vapour_____solid density"+ soldens);
                    solvol = 1.0/soldens*testSystem.getPhase(0).getComponent(k).getMolarMass();
                    SolidFug = Pvapsolid*Math.exp(solvol/(R*temp)*(pres-Pvapsolid));
                    
                   // testSystem2.setTemperature(temp);
                    testSystem2.setPressure(Pvapsolid);
                    testOps.TPflash();
                    testOps2.TPflash();
                    
                    SolVapFugCoeff = testSystem2.getPhase(0).getComponent(0).getFugasityCoeffisient(); 
                   
                    Fug[3][i]= testSystem.getPhase(0).getFugacity(k);
                    Fug[1][i]= SolidFug*SolVapFugCoeff;
                    Fug[0][i]= testSystem.getTemperature();
                    Fug[2][i]= testSystem.getTemperature();
               
              
                    Fugrel[0][i]=testSystem.getTemperature();
                    Fugrel[1][i]=Fug[1][i]/Fug[3][i];
                    //lagre data i fil
           
                   // if(Math.abs(Fugrel[1][i]-1)<0.01)testSystem.display();
                  
            }//end iterasjons l�kke
            
            String[] title = new String[2];
            title[0]="Solid Fugacity";
            title[1]="Fluid Fugacity";
            
            graph2b graffug = new graph2b(Fug,title,compName+" Fugacity  VS T, constant P= "+ pres, "Temperature [K]","Fugacity [bar]");
            graffug.setVisible(true);
            String[] title2 = new String[1];
            title2[0] = "Solid/Fluid";
            
            graph2b grafvapor = new graph2b(Fugrel,title2,compName+" Fugacity Ratio", "Temperature [K]","Fsolid/Ffluid");
            grafvapor.setVisible(true);
        }//end solidcheck l�kke
      }//end komponent l�kke
    } //end run
    
    public void PrintToFile(String FileName){
    String myFile = "/java/fugcoeff_C02_ N2.dat";
                                   
//           try {
//               FileWriter file_writer = new FileWriter(myFile, true);
//               PrintWriter pr_writer = new PrintWriter(file_writer);
//
//                // print line to output file
//                pr_writer.println( testSystem.getPhases()[0].getComponents()[k].getComponentName()+"  " + java.lang.Double.toString(Fug[0+k*6][i])+"    "+java.lang.Double.toString(Fug[1+k*6][i])+"   "+java.lang.Double.toString(Fug[3+k*6][i]) +"   "+ java.lang.Double.toString(Fug[5+k*6][i]) );
//                pr_writer.flush(); 
//                pr_writer.close();
//         
//            System.out.println("Successful attempt to write to " + myFile);
//            }
//            catch (SecurityException e) {
//            System.out.println("writeFile: caught security exception");
//            }
//            catch (IOException ioe) {
//	    System.out.println("writeFile: caught i/o exception");            
//            }
//              
    
    }
    
}//end Class