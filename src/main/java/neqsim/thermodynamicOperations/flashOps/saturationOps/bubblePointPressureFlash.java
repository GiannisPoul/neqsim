/*
 * bubblePointFlash.java
 *
 * Created on 14. oktober 2000, 16:30
 */

package neqsim.thermodynamicOperations.flashOps.saturationOps;

import neqsim.thermo.system.SystemInterface;

public class bubblePointPressureFlash extends constantDutyPressureFlash{

    private static final long serialVersionUID = 1000;
    
    /** Creates new bubblePointFlash */
    public bubblePointPressureFlash() {
    }
    
    public bubblePointPressureFlash(SystemInterface system) {
        super(system);
    }
    
    public void run() {
        if(system.getPhase(0).getNumberOfComponents()==1 && system.getTemperature()>system.getPhase(0).getComponent(0).getTC()) {
            setSuperCritical(true);
        }
        
        int iterations=0, maxNumberOfIterations = 500;
        double yold=0, ytotal=1, deriv=0, funk=0;
        boolean chemSolved = true;
        //System.out.println("starting");
        //system.setPressure(1.0);
        system.init(0);
        system.setNumberOfPhases(2);
        system.setBeta(1, 1.0-1e-10);
        system.setBeta(0, 1e-10);
        
        double oldPres=0, oldChemPres=1;
        if(system.isChemicalSystem()){
            system.getChemicalReactionOperations().solveChemEq(1,0);
            system.getChemicalReactionOperations().solveChemEq(1,1);
        }
        
        for (int i=0;i<system.getPhases()[1].getNumberOfComponents();i++){
            system.getPhases()[1].getComponents()[i].setx(system.getPhases()[0].getComponents()[i].getz());
            if(system.getPhases()[0].getComponents()[i].getIonicCharge()!=0) {
                system.getPhases()[0].getComponents()[i].setx(1e-40);
            } else {
                system.getPhases()[0].getComponents()[i].setx(system.getPhases()[0].getComponents()[i].getK()*system.getPhases()[1].getComponents()[i].getz());
            }
        }
        
        ytotal = 0.0;
        for (int i=0;i<system.getPhases()[0].getNumberOfComponents();i++){
            ytotal += system.getPhases()[0].getComponents()[i].getx();
        }
        
        double ktot=0.0;
        int chemIter=0;
        
        do{
            chemIter++;
            oldChemPres =  system.getPressure();
            iterations=0;
            do{
                iterations++;
                for (int i=0;i<system.getPhases()[0].getNumberOfComponents();i++){
                    system.getPhases()[0].getComponents()[i].setx(system.getPhases()[0].getComponents()[i].getx()/ytotal);
                }
                system.init(3);
                oldPres =  system.getPressure();
                ktot = 0.0;
                for (int i=0;i<system.getPhases()[1].getNumberOfComponents();i++){
                    do{
                        yold = system.getPhases()[0].getComponents()[i].getx();
                        if(!Double.isNaN(Math.exp(Math.log(system.getPhases()[1].getComponents()[i].getFugasityCoeffisient()) - Math.log(system.getPhases()[0].getComponents()[i].getFugasityCoeffisient())))){
                            if(system.getPhase(0).getComponent(i).getIonicCharge()!=0) {
                                system.getPhases()[0].getComponents()[i].setK(1e-40);
                            } else {
                                system.getPhases()[0].getComponents()[i].setK(Math.exp(Math.log(system.getPhases()[1].getComponents()[i].getFugasityCoeffisient()) - Math.log(system.getPhases()[0].getComponents()[i].getFugasityCoeffisient())));
                            }
                        }
                        system.getPhases()[1].getComponents()[i].setK(system.getPhases()[0].getComponents()[i].getK());
                        system.getPhases()[0].getComponents()[i].setx(system.getPhases()[0].getComponents()[i].getK()*system.getPhases()[1].getComponents()[i].getz());
                        //System.out.println("y err " + Math.abs(system.getPhases()[0].getComponents()[i].getx()-yold));
                    }
                    while(Math.abs(system.getPhases()[0].getComponents()[i].getx()-yold)/yold>1e-8);
                    ktot += Math.abs(system.getPhases()[1].getComponents()[i].getK()-1.0);
                }
                for (int i=0;i<system.getPhases()[0].getNumberOfComponents();i++){
                    if(!Double.isNaN(system.getPhases()[0].getComponents()[i].getK())) {
                        system.getPhases()[0].getComponents()[i].setx(system.getPhases()[0].getComponents()[i].getK()*system.getPhases()[1].getComponents()[i].getz());
                    } else{
                        system.init(0);
                        System.out.println("k err. : nan" );
                    }
                }
                
                ytotal = 0.0;
                for (int i=0;i<system.getPhases()[0].getNumberOfComponents();i++){
                    ytotal += system.getPhases()[0].getComponents()[i].getx();
                }
                //zSystem.out.println("ytot " + ytotal + "  pres " + system.getPressure());
                
                //system.getPhase(0).normalize();
                
                if(ytotal>1.5) {
                    ytotal=1.5;
                }
                if(ytotal<0.5) {
                    ytotal=0.5;
                }
                system.setPressure(system.getPressure()*ytotal);// + 0.5*(ytotal*system.getPressure()-system.getPressure()));
                if(system.getPressure()<0){
                    system.setPressure(oldChemPres/2.0);
                    run();
                    return;
                }
                if(system.getPressure()>5*oldChemPres){
                    system.setPressure(oldChemPres*5);
                    run();
                    return;
                }
                //System.out.println("iter in bub calc " + iterations + " pres " + system.getPressure()+ " ytot " + ytotal + "  chem iter " + chemIter);
                
            }
            while(((((Math.abs(ytotal-1.0)) > 1e-7) || Math.abs(oldPres-system.getPressure())/oldPres>1e-6) && (iterations < maxNumberOfIterations)) || iterations<5);
            
            if(system.isChemicalSystem()){// && (iterations%3)==0 && iterations<50){
                chemSolved = system.getChemicalReactionOperations().solveChemEq(1,1);
                system.setBeta(1, 1.0-1e-10);
                system.setBeta(0, 1e-10);
            }
            //System.out.println("iter in bub calc " + iterations + " pres " + system.getPressure()+ "  chem iter " + chemIter);
            
        }
        while((Math.abs(oldChemPres - system.getPressure())/oldChemPres>1e-6 || chemIter<2 || !chemSolved) && chemIter<20);
        // if(system.getPressure()>300) system.setPressure(300.0);
        //System.out.println("iter in bub calc " + iterations + " pres " + system.getPressure()+ "  chem iter " + chemIter);
        //        System.out.println("iter " + iterations + " XTOT " +ytotal + " ktot " +ktot);
        system.init(1);
        if(Math.abs(ytotal-1.0)>1e-4 || ktot<1e-3 && system.getPhase(0).getNumberOfComponents()>1){
            System.out.println("ytot " + Math.abs(ytotal-1.0));
            setSuperCritical(true);
        }
    }
    
    public void printToFile(String name) {}
    
    
    
}