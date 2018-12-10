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

package neqsim.thermo.util.benchmark;

import neqsim.thermo.system.SystemInterface;
import neqsim.thermo.system.SystemSrkCPAstatoil;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

public class TPflash_benchmark_fullcomp {

    private static final long serialVersionUID = 1000;

    /** This method is just meant to test the thermo package.
     */
    public static void main(String args[]) {

        double[][] points;

        SystemInterface testSystem = new SystemSrkCPAstatoil(273.15 - 5.0, 10.0);
        //SystemInterface testSystem = new SystemSrkSchwartzentruberEos(298.15, 1.01325);
        ThermodynamicOperations testOps = new ThermodynamicOperations(testSystem);

 //       testSystem.addComponent("CO2", 2.1);
  //      testSystem.addComponent("nitrogen", 1.16);
        testSystem.addComponent("methane", 26.19);
        testSystem.addComponent("propane", 8.27);
        
        testSystem.addComponent("propane", 7.5);
        testSystem.addComponent("i-butane", 1.83);
        testSystem.addComponent("n-butane", 4.05);
        testSystem.addComponent("iC5", 1.85);
        testSystem.addComponent("n-pentane", 2.45);
        testSystem.addComponent("n-hexane", 40.6);
        

        testSystem.addTBPfraction("C6", 1.49985, 86.3 / 1000.0, 0.7232);
        testSystem.addTBPfraction("C7", 0.0359864, 96.0 / 1000.0, 0.738);
        testSystem.addTBPfraction("C8", 0.939906, 107.0 / 1000.0, 0.765);
        testSystem.addTBPfraction("C9", 0.879912, 121.0 / 1000.0, 0.781);
        testSystem.addTBPfraction("C10", 0.45, 134.0 / 1000.0, 0.792);

//        testSystem.addComponent("methanol", 1.0);
  //      testSystem.addComponent("MEG", 11.0);
  //      testSystem.addComponent("water", 84.35);
       // testSystem.addComponent("methanol", 15.65);
        testSystem.setMultiPhaseCheck(true);
        testSystem.setHydrateCheck(true);
        testSystem.createDatabase(true);
        testSystem.setMixingRule(9);
        System.out.println("start benchmark TPflash......");
        long time = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            testOps.TPflash();
            try {
            //    testOps.hydrateFormationTemperature();
            //    testOps.calcTOLHydrateFormationTemperature();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("Time taken for benchmark flash = " + (System.currentTimeMillis() - time));
        testOps.displayResult();

        // time for 5000 flash calculations
        // Results Dell Portable PIII 750 MHz - JDK 1.3.1:
        //  mixrule 1 (Classic - no interaction):    6.719 sec
        //  mixrule 2 (Classic):    6.029 sec ny PC 1.498 sec
        // mixrule 4 (Huron-Vidal2):    17.545 sec
        //  mixrule 6 (Wong-Sandler):    12.859 sec

        //        // system:
        //        SystemSrkEos testSystem = new SystemSrkEos(303.15, 10.01325);
        //        ThermodynamicOperations testOps = new ThermodynamicOperations(testSystem);
        //        testSystem.addComponent("methane", 100.0);
        //        testSystem.addComponent("water", 100.0);
        //        testSystem.setMixingRule(1);
    }
}