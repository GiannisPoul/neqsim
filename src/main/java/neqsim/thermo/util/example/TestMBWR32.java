package neqsim.thermo.util.example;

import neqsim.thermo.system.SystemBWRSEos;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

/*
 * TPflash.java
 *
 * Created on 27. september 2001, 09:43
 */

/*
 *
 * @author  esol
 * @version
 */
public class TestMBWR32 {

    private static final long serialVersionUID = 1000;

    /**
     * Creates new TPflash
     */
    public TestMBWR32() {
    }

    public static void main(String args[]) {
        SystemInterface testSystem = new SystemBWRSEos(131.38, 4.31);
        //SystemInterface testSystem = new SystemSrkEos(131.38, 4.31);
        //SystemInterface testSystem = new SystemPrEos(111.0, 1.0523);
        ThermodynamicOperations testOps = new ThermodynamicOperations(testSystem);

        testSystem.addComponent("methane", 1.0);

        testSystem.createDatabase(true);
        testSystem.setMixingRule(1);

        testSystem.init(0);
        testSystem.init(3);
        System.out.println("Z " + testSystem.getLowestGibbsEnergyPhase().getZ());


        try {
            //  testOps.TPflash();
            // testOps.bubblePointTemperatureFlash();
            testOps.bubblePointPressureFlash(false);
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        testSystem.display();
    }
}