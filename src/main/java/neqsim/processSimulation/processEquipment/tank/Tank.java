/*
 * Separator.java
 *
 * Created on 12. mars 2001, 19:48
 */
package neqsim.processSimulation.processEquipment.tank;

import neqsim.processSimulation.processEquipment.ProcessEquipmentBaseClass;
import neqsim.processSimulation.processEquipment.ProcessEquipmentInterface;
import neqsim.processSimulation.processEquipment.mixer.Mixer;
import neqsim.processSimulation.processEquipment.stream.Stream;
import neqsim.processSimulation.processEquipment.stream.StreamInterface;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

/**
 *
 * @author  Even Solbraa
 * @version
 */
public class Tank extends ProcessEquipmentBaseClass implements ProcessEquipmentInterface {

    private static final long serialVersionUID = 1000;

    SystemInterface thermoSystem, gasSystem, waterSystem, liquidSystem, thermoSystemCloned;
    ThermodynamicOperations thermoOps;
    Stream gasOutStream;
    Stream liquidOutStream;
    private int numberOfInputStreams = 0;
    Mixer inletStreamMixer = new Mixer("Separator Inlet Stream Mixer");
    String name = new String();
    private double efficiency = 1.0;
    private double liquidCarryoverFraction = 0.0;
    private double gasCarryunderFraction = 0.0;
    private double volume = 136000.0;
    double steelWallTemperature = 298.15, steelWallMass = 1840.0 * 1000.0, steelWallArea = 15613.0, heatTransferNumber = 5.0, steelCp = 450.0;
    double separatorLength = 40.0, separatorDiameter = 60.0;
    double liquidVolume = 235.0, gasVolume = 15.0;
    private double liquidLevel = liquidVolume / (liquidVolume + gasVolume);

    /** Creates new Separator */
    public Tank() {
    }

    public Tank(Stream inletStream) {
        addStream(inletStream);
    }

    public Tank(String name, Stream inletStream) {
        this.name = name;
        addStream(inletStream);
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setInletStream(Stream inletStream) {
        inletStreamMixer.addStream(inletStream);
        thermoSystem = (SystemInterface) inletStream.getThermoSystem().clone();
        gasSystem = thermoSystem.phaseToSystem(thermoSystem.getPhases()[0]);
        gasOutStream = new Stream(gasSystem);

        thermoSystem = (SystemInterface) inletStream.getThermoSystem().clone();
        liquidSystem = thermoSystem.phaseToSystem(thermoSystem.getPhases()[1]);
        liquidOutStream = new Stream(liquidSystem);

    }

    public void addStream(StreamInterface newStream) {
        if (numberOfInputStreams == 0) {
            setInletStream((Stream) newStream);
        } else {
            inletStreamMixer.addStream(newStream);
        }
        numberOfInputStreams++;
    }

    public Stream getLiquidOutStream() {
        return liquidOutStream;
    }

    public Stream getGasOutStream() {
        return gasOutStream;
    }

    public Stream getGas() {
        return getGasOutStream();
    }

    public Stream getLiquid() {
        return getLiquidOutStream();
    }

    public void run() {
        inletStreamMixer.run();
        SystemInterface thermoSystem2 = (SystemInterface) inletStreamMixer.getOutStream().getThermoSystem().clone();
        ThermodynamicOperations ops = new ThermodynamicOperations(thermoSystem2);
        ops.VUflash(thermoSystem2.getVolume(), thermoSystem2.getInternalEnergy());
         System.out.println("Volume " + thermoSystem2.getVolume() + " internalEnergy " + thermoSystem2.getInternalEnergy());
        steelWallTemperature = thermoSystem2.getTemperature();
        if (thermoSystem2.hasPhaseType("gas")) {
            gasOutStream.setThermoSystemFromPhase(thermoSystem2, "gas");
        } else {
            gasOutStream.setThermoSystemFromPhase(thermoSystem2.getEmptySystemClone(), "gas");
        }
        if (thermoSystem2.hasPhaseType("oil")) {
            liquidOutStream.setThermoSystemFromPhase(thermoSystem2, "oil");
        } else {
            gasOutStream.setThermoSystemFromPhase(thermoSystem2.getEmptySystemClone(), "oil");
        }

        thermoSystem = (SystemInterface) thermoSystem2.clone();
        thermoSystem.setTotalNumberOfMoles(1.0e-10);
        thermoSystem.init(1);
        System.out.println("number of phases " + thermoSystem.getNumberOfPhases());
        for (int j = 0; j < thermoSystem.getNumberOfPhases(); j++) {
            double relFact = gasVolume / (thermoSystem.getPhase(j).getVolume() * 1.0e-5);
            if (j == 1) {
                relFact = liquidVolume / (thermoSystem.getPhase(j).getVolume() * 1.0e-5);
            }
            for (int i = 0; i < thermoSystem.getPhase(j).getNumberOfComponents(); i++) {
                thermoSystem.addComponent(thermoSystem.getPhase(j).getComponent(i).getComponentName(), relFact * thermoSystem.getPhase(j).getComponent(i).getNumberOfMolesInPhase(), j);
            }
        }
        if (thermoSystem2.getNumberOfPhases() == 2) {
            thermoSystem.setBeta(gasVolume / thermoSystem2.getPhase(0).getMolarVolume() / (gasVolume / thermoSystem2.getPhase(0).getMolarVolume() + liquidVolume / thermoSystem2.getPhase(1).getMolarVolume()));
        } else {
            thermoSystem.setBeta(1.0 - 1e-10);
        }
        thermoSystem.init(3);
        System.out.println("moles in separator " + thermoSystem.getNumberOfMoles());
        double volume1 = thermoSystem.getVolume();
        System.out.println("volume1 bef " + volume1);
        System.out.println("beta " + thermoSystem.getBeta());

        if (thermoSystem2.getNumberOfPhases() == 2) {
            liquidLevel = thermoSystem.getPhase(1).getVolume() * 1e-5 / (liquidVolume + gasVolume);
        } else {
            liquidLevel = 1e-10;
        }
        liquidVolume = getLiquidLevel() * 3.14 / 4.0 * separatorDiameter * separatorDiameter * separatorLength;
        gasVolume = (1.0 - getLiquidLevel()) * 3.14 / 4.0 * separatorDiameter * separatorDiameter * separatorLength;
        System.out.println("moles out" + liquidOutStream.getThermoSystem().getTotalNumberOfMoles());

    }

    public void displayResult() {
        thermoSystem.display();
    }

    public String getName() {
        return name;
    }

    public void runTransient(double dt) {
        inletStreamMixer.run();


        System.out.println("moles out" + liquidOutStream.getThermoSystem().getTotalNumberOfMoles());
        double inMoles = inletStreamMixer.getOutStream().getThermoSystem().getTotalNumberOfMoles();
        double gasoutMoles = gasOutStream.getThermoSystem().getNumberOfMoles();
        double liqoutMoles = liquidOutStream.getThermoSystem().getNumberOfMoles();
        thermoSystem.init(3);
        gasOutStream.getThermoSystem().init(3);
        liquidOutStream.getThermoSystem().init(3);
        inletStreamMixer.getOutStream().getThermoSystem().init(3);
        double volume1 = thermoSystem.getVolume();
        System.out.println("volume1 " + volume1);
        double deltaEnergy = inletStreamMixer.getOutStream().getThermoSystem().getEnthalpy() - gasOutStream.getThermoSystem().getEnthalpy() - liquidOutStream.getThermoSystem().getEnthalpy();
        System.out.println("enthalph delta " + deltaEnergy);
        double wallHeatTransfer = heatTransferNumber * steelWallArea * (steelWallTemperature - thermoSystem.getTemperature()) * dt;
        System.out.println("delta temp " + (steelWallTemperature - thermoSystem.getTemperature()));
        steelWallTemperature -= wallHeatTransfer / (steelCp * steelWallMass);
        System.out.println("wall Temperature " + steelWallTemperature);

        double newEnergy = thermoSystem.getInternalEnergy() + dt * deltaEnergy + wallHeatTransfer;

        System.out.println("energy cooling " + dt * deltaEnergy);
        System.out.println("energy heating " + wallHeatTransfer / dt + " kW");

        for (int i = 0; i < thermoSystem.getPhase(0).getNumberOfComponents(); i++) {
            double dn = 0.0;
            for (int k = 0; k < inletStreamMixer.getOutStream().getThermoSystem().getNumberOfPhases(); k++) {
                dn += inletStreamMixer.getOutStream().getThermoSystem().getPhase(k).getComponent(i).getNumberOfMolesInPhase();
            }
            dn = dn - gasOutStream.getThermoSystem().getPhase(0).getComponent(i).getNumberOfMolesInPhase()
                    - liquidOutStream.getThermoSystem().getPhase(0).getComponent(i).getNumberOfMolesInPhase();
            System.out.println("dn " + dn);
            thermoSystem.addComponent(inletStreamMixer.getOutStream().getThermoSystem().getPhase(0).getComponent(i).getComponentName(), dn * dt);
        }

        System.out.println("total moles " + thermoSystem.getTotalNumberOfMoles());
        thermoOps = new ThermodynamicOperations(thermoSystem);
        thermoOps.VUflash(volume1,newEnergy);

        setOutComposition(thermoSystem);
        setTempPres(thermoSystem.getTemperature(), thermoSystem.getPressure());

        if (thermoSystem.hasPhaseType("oil")) {
            liquidLevel = thermoSystem.getPhase(1).getVolume() * 1e-5 / (liquidVolume + gasVolume);
        } else {
            liquidLevel = 1e-10;
        }
        System.out.println("liquid level " + liquidLevel);
        liquidVolume = getLiquidLevel() * 3.14 / 4.0 * separatorDiameter * separatorDiameter * separatorLength;
        gasVolume = (1.0 - getLiquidLevel()) * 3.14 / 4.0 * separatorDiameter * separatorDiameter * separatorLength;


    }

    public void setOutComposition(SystemInterface thermoSystem) {
        for (int i = 0; i < thermoSystem.getPhase(0).getNumberOfComponents(); i++) {
            if (thermoSystem.hasPhaseType("gas")) {
                getGasOutStream().getThermoSystem().getPhase(0).getComponent(i).setx(thermoSystem.getPhase(thermoSystem.getPhaseNumberOfPhase("gas")).getComponent(i).getx());
            }
            if (thermoSystem.hasPhaseType("oil")) {
                getLiquidOutStream().getThermoSystem().getPhase(0).getComponent(i).setx(thermoSystem.getPhase(thermoSystem.getPhaseNumberOfPhase("oil")).getComponent(i).getx());
            }
        }
    }

    public void setTempPres(double temp, double pres) {
        gasOutStream.getThermoSystem().setTemperature(temp);
        liquidOutStream.getThermoSystem().setTemperature(temp);


        inletStreamMixer.setPressure(pres);
        gasOutStream.getThermoSystem().setPressure(pres);
        liquidOutStream.getThermoSystem().setPressure(pres);

        inletStreamMixer.run();
        gasOutStream.run();
        liquidOutStream.run();
    }

    public double getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(double efficiency) {
        this.efficiency = efficiency;
    }

    public double getLiquidCarryoverFraction() {
        return liquidCarryoverFraction;
    }

    public void setLiquidCarryoverFraction(double liquidCarryoverFraction) {
        this.liquidCarryoverFraction = liquidCarryoverFraction;
    }

    public double getGasCarryunderFraction() {
        return gasCarryunderFraction;
    }

    public void setGasCarryunderFraction(double gasCarryunderFraction) {
        this.gasCarryunderFraction = gasCarryunderFraction;
    }

    public double getLiquidLevel() {
        return liquidLevel;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }
}
