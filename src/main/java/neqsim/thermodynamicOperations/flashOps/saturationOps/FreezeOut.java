package neqsim.thermodynamicOperations.flashOps.saturationOps;

/*
 * TPflash.java
 *
 * Created on 27. september 2001, 09:43
 */

import java.io.*;
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
public class FreezeOut extends constantDutyTemperatureFlash implements ThermodynamicConstantsInterface {

    private static final long serialVersionUID = 1000;
    public double[] FCompTemp = new double[10];
    public String[] FCompNames = new String[10];
    public boolean noFreezeFlash = true;
    
    /** Creates new FugTest2 */
    public FreezeOut(){
    }
   
    public FreezeOut(SystemInterface system) {
        super(system);
    } 
    
  public void run(){
      SystemInterface testSystem= system;  
      ThermodynamicOperations testOps = new ThermodynamicOperations(testSystem);
        
        double[][] Fug = new double[12][35];
        double[][] Fugrel = new double[2][40];
        int iterations =0;
        double newTemp=0, OldTemp=0;
        double FugRatio; 
        double T2low=0,T2high=0;
        boolean Left=true,half=false; 
        double SolidFug =0.0, FluidFug =0.0, temp=0.0, pres=0.0, Pvapsolid=0.0;
        double solvol=0.0,soldens=0.0, trpTemp=0.0;
        boolean CCequation = true;
        boolean noFreezeliq = true;
        boolean SolidForms = true;      
        double maximum=0;
     
          for(int k=0;k<testSystem.getPhase(0).getNumberOfComponents();k++){    
          FCompNames[k]=testSystem.getPhase(0).getComponent(k).getComponentName();
          if(testSystem.getPhase(0).getComponent(k).doSolidCheck()){
          trpTemp = testSystem.getPhases()[0].getComponents()[k].getTriplePointTemperature();
             if(noFreezeFlash){
                     testSystem.setTemperature(trpTemp);
                     System.out.println("Starting at Triple point temperature " + system.getPhase(0).getComponent(k).getComponentName() );
                    }
             else{
                    testSystem.setTemperature(FCompTemp[k]); //�ker hastigheten n�r det kun sjekkes for en komponent
                    System.out.println("starting at Temperature  " + system.getTemperature());
                    }
          
          SystemInterface testSystem2 = new SystemSrkSchwartzentruberEos(216,1);
          ThermodynamicOperations testOps2 = new ThermodynamicOperations(testSystem2);
          testSystem2.addComponent(testSystem.getPhase(0).getComponent(k).getComponentName(),1);
          testSystem2.setPhaseType(0,1);
          noFreezeliq = true;
          SolidFug=0.0;
          FluidFug=0.0;
          SolidForms = true;
          temp=0.0;
          Pvapsolid=0.0;
          iterations=0;
          half=false;
          T2high=trpTemp+0.1;
          if(Math.abs(testSystem.getPhases()[0].getComponents()[k].getHsub())<1) {
              CCequation=false;
          }
          do{
              iterations++;
              System.out.println("-------------"); 
              temp=testSystem.getTemperature();
              System.out.println("temperature " +temp);
              if(temp>trpTemp+0.01) {
                  temp=trpTemp;
              }
                    if(CCequation){
                        Pvapsolid = testSystem.getPhase(0).getComponent(k).getCCsolidVaporPressure(temp);
                    }
                    else{
                        Pvapsolid = testSystem.getPhase(0).getComponent(k).getSolidVaporPressure(temp);
                    }
                    soldens = testSystem.getPhase(0).getComponent(k).getPureComponentSolidDensity(temp)*1000;
                    if(soldens>2000) {
                        soldens = 1000;
              }
                    solvol = 1.0/soldens*testSystem.getPhase(0).getComponent(k).getMolarMass();
                    
                    System.out.println("solid density "+ soldens);
                    testSystem.setTemperature(temp);                    
                    testSystem2.setTemperature(temp);
                    testSystem2.setPressure(Pvapsolid);
                    testOps.TPflash();
                    testOps2.TPflash();
                    
                    System.out.println("Partial pressure " +testSystem.getPhase(1).getComponent(k).getx()*testSystem.getPressure());
                    
                    
                    SolidFug = Pvapsolid*testSystem2.getPhase(0).getComponent(0).getFugasityCoeffisient()*Math.exp(solvol/(R*temp)*(pres-Pvapsolid));
                    FluidFug = testSystem.getPhase(0).getFugacity(k);
                
               
          FugRatio=SolidFug/FluidFug;
                    
          OldTemp = testSystem.getTemperature();
          System.out.println("Temperature " + OldTemp);
          System.out.println("FugRatio solid/fluidphase " +FugRatio);
          
          
          if(1<(FugRatio)){
              if(OldTemp<trpTemp/3) {
                  SolidForms=false;
              }
              T2high=OldTemp;
              
              if(half){
                  newTemp=0.5*(T2low+T2high);
              }
              else if(1.5>FugRatio){
                  newTemp=OldTemp-trpTemp*0.1;
              }
              else if(1.5<FugRatio){
                  newTemp=OldTemp-trpTemp*0.15;
              }
              else{
              newTemp=OldTemp-trpTemp*0.15;
              }
          Left=false;
          }
          else if(1>(FugRatio)){
              if(Left&&((OldTemp-trpTemp)>0)) {
                  noFreezeliq = false;
              }
              
              T2low=OldTemp;
              Left=true;
              half=true;
          newTemp=0.5*(T2low+T2high);
          }
          
         testSystem.setTemperature(newTemp);
            }//do l�kke
      while(((Math.abs(FugRatio-1)>=0.00001 && iterations<100)) && noFreezeliq && SolidForms);
            System.out.println("noFreezeliq: "+ noFreezeliq+ " SolidForms: " + SolidForms);  
            
            if(noFreezeliq && SolidForms){
            testSystem.setTemperature(OldTemp);
            FCompTemp[k] = OldTemp;
            }
            else if(!noFreezeliq){
            testSystem.setTemperature(OldTemp);
            FCompTemp[k] = OldTemp;
            System.out.println("Freezing Temperature not found");
            }
            else{
             testSystem.setTemperature(1000);
             FCompTemp[k] = OldTemp;
             }
          
          System.out.println("Iterations :" + iterations);
                    
          }//end Ifl�kke    
          }//end for 
        maximum = FCompTemp[0];   // start with the first value
        for (int i=1; i<FCompTemp.length; i++) {
        if (FCompTemp[i] > maximum) {
            maximum = FCompTemp[i];   // new maximum
        }
        }
        
        testSystem.setTemperature(maximum);
  //  this.printToFile("FrzOut");
     } //end Main
    
public void printToFile(String name) {
        
        for(int n=0;n<system.getPhases()[0].getNumberOfComponents();n++){
        name = name+"_"+system.getPhase(0).getComponent(n).getComponentName();
        } 
              
        String myFile = "/java/"+name+".frz";
              
       try {
               FileWriter file_writer = new FileWriter(myFile, true);
               PrintWriter pr_writer = new PrintWriter(file_writer);
               pr_writer.println("name,freezeT,freezeP,z,iterations");
               pr_writer.flush();
               
               for(int k=0;k<system.getPhases()[0].getNumberOfComponents();k++){
     
               // print line to output file
                pr_writer.println(FCompNames[k]+","+java.lang.Double.toString(FCompTemp[k])
                +","+system.getPressure()
                +","+java.lang.Double.toString(system.getPhases()[0].getComponents()[k].getz()));
                pr_writer.flush();
               }                 
                pr_writer.close();       

            }
            catch (SecurityException e) {
            System.out.println("writeFile: caught security exception");
            }
            catch (IOException ioe) {
	    System.out.println("writeFile: caught i/o exception");            
            }   
    
    
    }


}//end Class