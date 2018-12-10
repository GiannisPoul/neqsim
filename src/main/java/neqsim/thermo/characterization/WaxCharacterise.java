/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package neqsim.thermo.characterization;

import neqsim.thermo.system.SystemInterface;

/**
 *
 * @author ESOL
 */
public class WaxCharacterise extends Object implements java.io.Serializable, Cloneable {
    private static final long serialVersionUID = 1000;    
    SystemInterface thermoSystem = null;
    String name = "";
    protected WaxModelInterface model = new PedersenWaxModel();

    public WaxCharacterise(SystemInterface system) {
        thermoSystem = system;
    }

    public Object clone() {
        WaxCharacterise clonedSystem = null;
        try {
            clonedSystem = (WaxCharacterise) super.clone();
            clonedSystem.model = (WaxModelInterface) model.clone();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return clonedSystem;
    }

    public abstract class WaxBaseModel implements WaxModelInterface {

        double[] parameterWax = new double[3];
        double[] parameterWaxHeatOfFusion = new double[1];
        double[] parameterWaxTriplePointTemperature = new double[1];

        public Object clone() {
            WaxBaseModel clonedSystem = null;
            try {
                clonedSystem = (WaxBaseModel) super.clone();
            } catch (Exception e) {
                e.printStackTrace(System.err);
            }
            return clonedSystem;
        }

        public void addTBPWax() {

        }

        public void setWaxParameters(double[] parameters) {
            parameterWax = parameters;
        }

        public void setWaxParameter(int i, double parameters) {
            parameterWax[i] = parameters;
        }

        public void setParameterWaxHeatOfFusion(int i, double parameters) {
            parameterWaxHeatOfFusion[i] = parameters;
        }

        public void setParameterWaxTriplePointTemperature(int i, double parameters) {
            parameterWaxTriplePointTemperature[i] = parameters;
        }

        public double[] getWaxParameters() {
            return parameterWax;
        }

        /**
         * @return the parameterWaxHeatOfFusion
         */
        public double[] getParameterWaxHeatOfFusion() {
            return parameterWaxHeatOfFusion;
        }

        /**
         * @param parameterWaxHeatOfFusion the parameterWaxHeatOfFusion to set
         */
        public void setParameterWaxHeatOfFusion(double[] parameterWaxHeatOfFusion) {
            this.parameterWaxHeatOfFusion = parameterWaxHeatOfFusion;
        }

        /**
         * @return the parameterWaxTriplePointTemperature
         */
        public double[] getParameterWaxTriplePointTemperature() {
            return parameterWaxTriplePointTemperature;
        }

        /**
         * @param parameterWaxTriplePointTemperature the
         * parameterWaxTriplePointTemperature to set
         */
        public void setParameterWaxTriplePointTemperature(double[] parameterWaxTriplePointTemperature) {
            this.parameterWaxTriplePointTemperature = parameterWaxTriplePointTemperature;
        }

    }

    public class PedersenWaxModel extends WaxBaseModel {

        public PedersenWaxModel() {
            parameterWax[0] = 1.074;
            parameterWax[1] = 6.584e-6;
            parameterWax[2] = 0.1915;

            parameterWaxHeatOfFusion[0] = 1.0;
            parameterWaxTriplePointTemperature[0] = 1.0;

        }

        public double calcTriplePointTemperature(int componentNumber) {
            return parameterWaxTriplePointTemperature[0] * (374.5 + (0.02617 * (thermoSystem.getPhase(0).getComponent(componentNumber).getMolarMass() * 1000.0) - 20172.0 / (thermoSystem.getPhase(0).getComponent(componentNumber).getMolarMass() * 1000.0)));
        }

        public double calcHeatOfFusion(int componentNumber) {
            return getParameterWaxHeatOfFusion()[0] * 0.1426 / 0.238845 * thermoSystem.getPhase(0).getComponent(componentNumber).getMolarMass() * 1000.0 * thermoSystem.getPhase(0).getComponent(componentNumber).getTriplePointTemperature();
        }

        public double calcParaffinDensity(int componentNumber) {
            return 0.3915 + 0.0675 * Math.log(thermoSystem.getPhase(0).getComponent(componentNumber).getMolarMass() * 1000.0);
        }

        public double calcPCwax(int componentNumber, String normalComponent) {

            return thermoSystem.getPhase(0).getComponent(normalComponent).getPC() * Math.pow(calcParaffinDensity(componentNumber) / thermoSystem.getPhase(0).getComponent(normalComponent).getNormalLiquidDensity(), 3.46);
        }

        public void addTBPWax() {
            int numberOfCOmponents = thermoSystem.getPhase(0).getNumberOfComponents();
            boolean hasWax = false;
            for (int i = 0; i < numberOfCOmponents; i++) {
                if (thermoSystem.getPhase(0).getComponent(i).getName().startsWith("wax")) {
                    hasWax = true;
                }
            }

            for (int i = 0; i < numberOfCOmponents; i++) {
                if (hasWax && thermoSystem.getPhase(0).getComponent(i).getName().startsWith("wax")) {
                    double A = parameterWax[0], B = parameterWax[1], C = parameterWax[2];
                    String compName = thermoSystem.getPhase(0).getComponent(i).getName().substring(3);

                    double densityLocal = calcParaffinDensity(i);

                    double molesChange = thermoSystem.getPhase(0).getComponent(compName).getNumberOfmoles() * (1.0 - (A + B * thermoSystem.getPhase(0).getComponent(compName).getMolarMass() * 1000.0) * Math.pow((thermoSystem.getPhase(0).getComponent(compName).getNormalLiquidDensity() - densityLocal) / densityLocal, C));

                    if(molesChange<0) {
                        molesChange=0.0;
                    }
                  
                    thermoSystem.addComponent(compName, -molesChange);
                    thermoSystem.addComponent(thermoSystem.getPhase(0).getComponent(i).getName(), molesChange);
                    for (int k = 0; k < thermoSystem.getNumberOfPhases(); k++) {
                        thermoSystem.getPhase(k).getComponent(i).setWaxFormer(true);
                        thermoSystem.getPhase(k).getComponent(i).setHeatOfFusion(calcHeatOfFusion(i));
                        thermoSystem.getPhase(k).getComponent(i).setTriplePointTemperature(calcTriplePointTemperature(i));
                    }
                } else if (!hasWax && (thermoSystem.getPhase(0).getComponent(i).isIsTBPfraction() || thermoSystem.getPhase(0).getComponent(i).isIsPlusFraction())) {
                    //double A = 1.074, B = 6.584e-4, C = 0.1915;
                    double A = parameterWax[0], B = parameterWax[1], C = parameterWax[2];

                    double densityLocal = calcParaffinDensity(i);
                    double molesChange = thermoSystem.getPhase(0).getComponent(i).getNumberOfmoles() * (1.0 - (A + B * thermoSystem.getPhase(0).getComponent(i).getMolarMass() * 1000.0) * Math.pow((thermoSystem.getPhase(0).getComponent(i).getNormalLiquidDensity() - densityLocal) / densityLocal, C));
                   // if(molesChange<0) molesChange=0.0;
                    //System.out.println("moles change " + molesChange);
                    thermoSystem.addComponent(thermoSystem.getPhase(0).getComponent(i).getComponentName(), -molesChange);
                    thermoSystem.addTBPfraction("wax" + thermoSystem.getPhase(0).getComponent(i).getComponentName(), molesChange, thermoSystem.getPhase(0).getComponent(i).getMolarMass(), thermoSystem.getPhase(0).getComponent(i).getNormalLiquidDensity());

                    int cNumb = thermoSystem.getPhase(0).getNumberOfComponents() - 1;
                    double waxPC =  calcPCwax(cNumb,thermoSystem.getPhase(0).getComponent(i).getComponentName());
                    
                    for (int k = 0; k < thermoSystem.getNumberOfPhases(); k++) {
                        thermoSystem.getPhase(k).getComponent(cNumb).setWaxFormer(true);
                        thermoSystem.getPhase(k).getComponent(cNumb).setHeatOfFusion(calcHeatOfFusion(cNumb));
                        thermoSystem.getPhase(k).getComponent(cNumb).setTriplePointTemperature(calcTriplePointTemperature(cNumb));
                        thermoSystem.getPhase(k).getComponent(cNumb).setPC(waxPC);
                    }
                }
            }
        }

        public void removeWax() {
            for (int i = 0; i < thermoSystem.getPhase(0).getNumberOfComponents(); i++) {
                if (thermoSystem.getPhase(0).getComponent(i).getName().startsWith("wax")) {
                    String compName = thermoSystem.getPhase(0).getComponent(i).getName().substring(3);
                    double moles = thermoSystem.getPhase(0).getComponent(i).getNumberOfmoles();
                    thermoSystem.addComponent(thermoSystem.getPhase(0).getComponent(i).getComponentName(), -moles);
                    thermoSystem.addComponent(compName, moles);
                }
            }
        }

    }

    public WaxModelInterface getModel(String name) {
        this.name = name;
        if (name.equals("PedersenWax")) {
            return new PedersenWaxModel();
        }
        return new PedersenWaxModel();
    }

    public void setModel(String name) {
        this.name = name;
        if (name.equals("PedersenWax")) {
            model = new PedersenWaxModel();
        }
        model = new PedersenWaxModel();
    }

    public WaxModelInterface getModel() {
        return model;
    }

    public void setModelName(String name) {
        this.name = name;
    }

}