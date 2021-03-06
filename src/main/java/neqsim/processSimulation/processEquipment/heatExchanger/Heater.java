/*
 * Heater.java
 *
 * Created on 15. mars 2001, 14:17
 */
package neqsim.processSimulation.processEquipment.heatExchanger;

import neqsim.processSimulation.processEquipment.ProcessEquipmentBaseClass;
import neqsim.processSimulation.processEquipment.ProcessEquipmentInterface;
import neqsim.processSimulation.processEquipment.stream.Stream;
import neqsim.processSimulation.processEquipment.stream.StreamInterface;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermodynamicOperations.ThermodynamicOperations;

/**
 *
 * @author Even Solbraa
 * @version
 */
public class Heater extends ProcessEquipmentBaseClass implements ProcessEquipmentInterface, HeaterInterface {

    private static final long serialVersionUID = 1000;

    ThermodynamicOperations testOps;
    boolean setTemperature = false, setOutPressure = false;
    private StreamInterface outStream;
    StreamInterface inStream;
    SystemInterface system;
    protected double temperatureOut = 0, dT = 0.0, pressureOut = 0;
    private boolean setEnergyInput = false;
    private double energyInput = 0.0;
    private double pressureDrop = 0.0;

    /**
     * Creates new Heater
     */
    public Heater() {
    }

    public Heater(StreamInterface inStream) {
        this.inStream = inStream;
        system = (SystemInterface) inStream.getThermoSystem().clone();
        outStream = new Stream(system);
    }

    public Heater(String name, StreamInterface inStream) {
        super(name);
        this.inStream = inStream;
        system = (SystemInterface) inStream.getThermoSystem().clone();
        outStream = new Stream(system);
    }

    public StreamInterface getInStream() {
        return inStream;
    }

    public void setdT(double dT) {
        this.dT = dT;
    }

    public StreamInterface getOutStream() {
        return outStream;
    }

    public void setOutPressure(double pressure) {
        setOutPressure = true;
        this.pressureOut = pressure;
    }

    public void setOutTemperature(double temperature) {
        setTemperature = true;
        setEnergyInput = false;
        this.temperatureOut = temperature;
    }

    public void setOutTP(double temperature, double pressure) {
        setTemperature = true;
        setEnergyInput = false;
        this.temperatureOut = temperature;
        setOutPressure = true;
        this.pressureOut = pressure;
    }

    public void run() {
        system = (SystemInterface) inStream.getThermoSystem().clone();
        system.init(3);
        double oldH = system.getEnthalpy();
        double newEnthalpy = energyInput + oldH;
        system.setPressure(system.getPressure() - pressureDrop);
        if (setOutPressure) {
            system.setPressure(pressureOut);
        }
        testOps = new ThermodynamicOperations(system);
        if (specification.equals("out stream")) {
            getOutStream().run();
            temperatureOut = getOutStream().getTemperature();
            system = (SystemInterface) getOutStream().getThermoSystem().clone();
        } else if (setTemperature) {
            system.setTemperature(temperatureOut);
            testOps.TPflash();
        } else if (setEnergyInput) {
            testOps.PHflash(newEnthalpy, 0);
        } else {
           // System.out.println("temperaturee out " + inStream.getTemperature());
            system.setTemperature(inStream.getTemperature() + dT);
            testOps.TPflash();
        }

        //system.setTemperature(temperatureOut);
        system.init(3);
        double newH = system.getEnthalpy();
        energyInput = newH - oldH;
        // system.setTemperature(temperatureOut);
        //  testOps.TPflash();
        //    system.setTemperature(temperatureOut);
        getOutStream().setThermoSystem(system);
    }

    public void displayResult() {
       // System.out.println("heater dH: " + energyInput);
        getOutStream().displayResult();
    }

    public String getName() {
        return name;
    }

    public void runTransient() {
        run();
    }

    public double getEnergyInput() {
        return energyInput;
    }
    
    public double getDuty() {
        return energyInput;
    }

    public void setEnergyInput(double energyInput) {
        this.energyInput = energyInput;
        setTemperature = false;
        setEnergyInput = true;
    }

    public boolean isSetEnergyInput() {
        return setEnergyInput;
    }

    public void setSetEnergyInput(boolean setEnergyInput) {
        this.setEnergyInput = setEnergyInput;
    }

    /**
     * @return the pressureDrop
     */
    public double getPressureDrop() {
        return pressureDrop;
    }

    /**
     * @param pressureDrop the pressureDrop to set
     */
    public void setPressureDrop(double pressureDrop) {
        this.pressureDrop = pressureDrop;
    }

    /**
     * @param outStream the outStream to set
     */
    public void setOutStream(Stream outStream) {
        this.outStream = outStream;
    }
}
