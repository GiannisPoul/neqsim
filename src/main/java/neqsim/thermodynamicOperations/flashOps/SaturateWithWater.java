/*
 * Copyright 2018 ESOL.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * PHflash.java
 *
 * Created on 8. mars 2001, 10:56
 */
package neqsim.thermodynamicOperations.flashOps;

import neqsim.thermo.system.SystemInterface;
import neqsim.thermo.system.SystemSrkCPAstatoil;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

/**
 *
 * @author even solbraa
 * @version
 */
public class SaturateWithWater extends QfuncFlash implements java.io.Serializable {

    private static final long serialVersionUID = 1000;

    Flash tpFlash;

    /**
     * Creates new PHflash
     */
    public SaturateWithWater() {
    }

    public SaturateWithWater(SystemInterface system) {
        this.system = system;
        this.tpFlash = new TPflash(system);
    }

    public void run() {

        if (!system.getPhase(0).hasComponent("water")) {
            system.addComponent("water", system.getTotalNumberOfMoles());
            system.createDatabase(true);
            system.setMixingRule(system.getMixingRule());
            if (system.doMultiPhaseCheck()) {
                system.setMultiPhaseCheck(true);
            }
            system.init(0);
        }
        double dn = 1.0;
        int i = 0;

        this.tpFlash = new TPflash(system);
        tpFlash.run();
        boolean hasAq = false;
        if (system.hasPhaseType("aqueous")) {
            hasAq = true;
        }
        double lastdn = 0.0;
        if (system.hasPhaseType("aqueous")) {
            lastdn = system.getPhaseOfType("aqueous").getComponent("water").getNumberOfMolesInPhase();
        } else {
            lastdn = system.getPhase(0).getNumberOfMolesInPhase()/ 10.0;
        }

        do {
            i++;

            if (!hasAq) {
                system.addComponent("water", lastdn * 0.5);
                lastdn *= 0.8;

            } else {
                dn = system.getPhaseOfType("aqueous").getComponent("water").getNumberOfMolesInPhase() / system.getNumberOfMoles();
                lastdn = system.getPhaseOfType("aqueous").getComponent("water").getNumberOfMolesInPhase();
                system.addComponent("water", -lastdn);
            }
            tpFlash.run();
            // system.display();
            hasAq = system.hasPhaseType("aqueous");
        } while ((i < 50 && Math.abs(dn) > 1e-6) || !hasAq && i<50);
        if(i==50) {
            System.out.println("could not find solution - in water sturate : dn  " + dn);
        }
        //System.out.println("i " + i + " dn " + dn);
        // system.display();
        system.removePhase(system.getNumberOfPhases() - 1);
        tpFlash.run();
    }

    public static void main(String[] args) {
        SystemInterface testSystem = new SystemSrkCPAstatoil(273.15 + 120.0, 210.0);

        testSystem.addComponent("methane", 19.90);
    //    testSystem.addComponent("ethane", 0.06);
    //    testSystem.addComponent("propane", 0.02);
     //   testSystem.addComponent("n-heptane", 2.02);
        //       testSystem.addTBPfraction("C7", 0.10, 100.0 / 1000.0, 0.73);
        //       testSystem.addTBPfraction("C8", 0.20, 130.0 / 1000.0, 0.773);
        //     testSystem.addComponent("CO2", 0.02);
              testSystem.addComponent("water", 1);
        testSystem.createDatabase(true);
        testSystem.setMixingRule(9);
        testSystem.setMultiPhaseCheck(true);
        testSystem.init(0);

        ThermodynamicOperations testOps = new ThermodynamicOperations(testSystem);
        try {
            testOps.TPflash();
            testSystem.display();
            testOps.saturateWithWater();
            testSystem.display();
            //   testSystem.addComponent("water", 1);
            testOps.saturateWithWater();
            testSystem.display();
          //  testOps.TPflash();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        testSystem.display();
    }
}