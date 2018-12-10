/*
 * bubblePointFlash.java
 *
 * Created on 14. oktober 2000, 16:30
 */
package neqsim.thermodynamicOperations.flashOps.saturationOps;

import neqsim.thermo.component.ComponentHydrate;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

public class HydrateFormationTemperatureFlash extends constantDutyTemperatureFlash {

    private static final long serialVersionUID = 1000;

    /**
     * Creates new bubblePointFlash
     */
    public HydrateFormationTemperatureFlash() {
    }

    public HydrateFormationTemperatureFlash(SystemInterface system) {
        super(system);
    }
    
    public void stop(){
        system = null;
    }

    public void run() {
        double olfFug = 0.0;
        double temp = 0.0, oldTemp = 0.0, oldDiff = 0.0, oldOldDiff = 0.0;
        //system.setHydrateCheck(true);
        ThermodynamicOperations ops = new ThermodynamicOperations(system);
        system.getPhase(4).getComponent("water").setx(1.0);
        int iter = 0;
        double diff = 0;
        system.setTemperature(system.getTemperature() + 0.0001);
        do {
            iter++;
            olfFug = system.getPhase(4).getFugacity("water");
            ops.TPflash();
            setFug();
            system.getPhase(4).getComponent("water").fugcoef(system.getPhase(4));
            system.getPhase(4).getComponent("water").setx(1.0);
            oldDiff = diff;
            diff = 1.0 - (system.getPhase(4).getFugacity("water") / system.getPhase(0).getFugacity("water"));
            oldTemp = temp;
            temp = system.getTemperature();
            double dDiffdT = (diff - oldDiff) / (temp - oldTemp);

            if (iter < 2) {
                system.setTemperature((system.getTemperature() + 0.1));

            } else {
                double dT = (Math.abs(diff / dDiffdT)) > 10 ? Math.signum(diff / dDiffdT) * 10 : diff / dDiffdT;
                if (Double.isNaN(dT)) {
                    dT = 0.1;
                }
                system.setTemperature(system.getTemperature() - dT);
            }
            if (iter > 2 && Math.abs(diff) > Math.abs(oldDiff)) {
                system.setTemperature((oldTemp + system.getTemperature()) / 2.0 );
            }
            System.out.println("diff " + (system.getPhase(4).getFugacity("water") / system.getPhase(0).getFugacity("water")));
            System.out.println("temperature " + system.getTemperature() + " iter " + iter);
            //System.out.println("x water " + system.getPhase(4).getComponent("water").getx());
            try{
                Thread.sleep(100);
            }
            catch (InterruptedException iex) {
            }

        } while (Math.abs((olfFug - system.getPhase(4).getFugacity("water")) / olfFug) > 1e-6 && iter < 100 || iter < 3);
    }

    public void run2() {
        double olfFug = 0.0;
        double oldTemp = 0.0, oldOldTemp = 0.0, oldDiff = 0.0, oldOldDiff = 0.0;
        //system.setHydrateCheck(true);
        ThermodynamicOperations ops = new ThermodynamicOperations(system);
        system.getPhase(4).getComponent("water").setx(1.0);
        int iter = 0;
        do {
            iter++;
            olfFug = system.getPhase(4).getFugacity("water");
            ops.TPflash();
            setFug();
            system.getPhase(4).getComponent("water").fugcoef(system.getPhase(4));
            system.getPhase(4).getComponent("water").setx(1.0);

            if (iter % 4 == 0) {
                //System.out.println("ny temp " +(system.getTemperature() - oldDiff/((oldDiff-oldOldDiff)/(oldTemp-oldOldTemp))));
                double change = -oldDiff / ((oldDiff - oldOldDiff) / (oldTemp - oldOldTemp));
                if (Math.abs(change) > 5.0) {
                    change = Math.abs(change) / change * 5.0;
                }
                system.setTemperature((system.getTemperature() + change));
            } else {
                double change = (1.0 - system.getPhase(4).getFugacity("water") / system.getPhase(0).getFugacity("water"));
                if (Math.abs(change) > 5.0) {
                    change = Math.abs(change) / change * 5.0;
                }
                system.setTemperature(system.getTemperature() + change);
            }

            double diff = 1.0 - (system.getPhase(4).getFugacity("water") / system.getPhase(0).getFugacity("water"));
            System.out.println("iter " + iter + " diff " + (system.getPhase(4).getFugacity("water") / system.getPhase(0).getFugacity("water")));
            oldOldTemp = oldTemp;
            oldTemp = system.getTemperature();

            oldOldDiff = oldDiff;
            oldDiff = diff;

            System.out.println("temperature " + system.getTemperature());
            //System.out.println("x water " + system.getPhase(4).getComponent("water").getx());
        } while (Math.abs((olfFug - system.getPhase(4).getFugacity("water")) / olfFug) > 1e-6 && iter < 100 || iter < 3);
    }

    public void setFug() {
        system.getPhase(4).getComponent("water").setx(1.0);
        for (int i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
            for (int j = 0; j < system.getPhase(0).getNumberOfComponents(); j++) {
                if (system.getPhase(4).getComponent(j).isHydrateFormer() || system.getPhase(4).getComponent(j).getName().equals("water")) {
                    ((ComponentHydrate) system.getPhase(4).getComponent(i)).setRefFug(j, system.getPhase(0).getFugacity(j));
                } else {
                    ((ComponentHydrate) system.getPhase(4).getComponent(i)).setRefFug(j, 0);
                }
            }
        }
        system.getPhase(4).getComponent("water").setx(1.0);
        system.getPhase(4).init();
        system.getPhase(4).getComponent("water").fugcoef(system.getPhase(4));
    }

    public void printToFile(String name) {
    }
}