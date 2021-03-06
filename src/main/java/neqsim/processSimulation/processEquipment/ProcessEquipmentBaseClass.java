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
 * ProcessEquipmentBaseClass.java
 *
 * Created on 6. juni 2006, 15:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package neqsim.processSimulation.processEquipment;

import neqsim.processSimulation.controllerDevice.ControllerDeviceInterface;
import neqsim.processSimulation.mechanicalDesign.MechanicalDesign;

/**
 *
 * @author ESOL
 */
public abstract class ProcessEquipmentBaseClass implements ProcessEquipmentInterface {

    private static final long serialVersionUID = 1000;

    private ControllerDeviceInterface controller = null;
    ControllerDeviceInterface flowValveController = null;
    public boolean hasController = false;
    public String name = new String();
    public MechanicalDesign mechanicalDesign = new MechanicalDesign(this);
    public String specification = "TP";
    public String[][] report = new String[0][0];

    /**
     * Creates a new instance of ProcessEquipmentBaseClass
     */
    public ProcessEquipmentBaseClass() {
        mechanicalDesign = new MechanicalDesign(this);
    }

    public ProcessEquipmentBaseClass(String name) {
        this();
        this.name = name;
    }

    public void run() {
    }

    ;
    public void runTransient(double dt) {
        run();
    }

    ;
    public void displayResult() {
    }

    ;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRegulatorOutSignal(double signal) {
    }

    public void setController(ControllerDeviceInterface controller) {
        this.controller = controller;
        hasController = true;
    }

    public void setFlowValveController(ControllerDeviceInterface controller) {
        this.flowValveController = controller;
    }

    public ControllerDeviceInterface getController() {
        return controller;
    }

    /**
     * @return the mechanicalDesign
     */
    public MechanicalDesign getMechanicalDesign() {
        return mechanicalDesign;
    }

    /**
     * @param mechanicalDesign the mechanicalDesign to set
     */
    public void setMechanicalDesign(MechanicalDesign mechanicalDesign) {
        this.mechanicalDesign = mechanicalDesign;
    }

    /**
     * @return the specification
     */
    public String getSpecification() {
        return specification;
    }

    /**
     * @param specification the specification to set
     */
    public void setSpecification(String specification) {
        this.specification = specification;
    }

    public String[][] reportResults() {
        return report;
    }
    
    public boolean solved() {
    	return true;
    }

}
