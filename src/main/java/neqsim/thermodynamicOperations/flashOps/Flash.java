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
 * Flash.java
 *
 * Created on 2. oktober 2000, 22:22
 */
package neqsim.thermodynamicOperations.flashOps;

import Jama.*;
import neqsim.thermo.system.SystemInterface;
import neqsim.thermodynamicOperations.BaseOperation;
import neqsim.thermodynamicOperations.OperationInterface;

/**
 *
 * @author Even Solbraa
 * @version
 */
abstract class Flash extends BaseOperation implements OperationInterface, java.io.Serializable {

    private static final long serialVersionUID = 1000;
    public double[] minGibsPhaseLogZ;
    public double[] minGibsLogFugCoef;

    SystemInterface system;
    int i = 0, j = 0, nummer = 0, iterations = 0, maxNumberOfIterations = 100;
    double gibbsEnergy = 0, gibbsEnergyOld = 0;
    double Kold = 0, deviation = 0, g0 = 0, g1 = 0;
    double lnOldOldOldK[], lnOldOldK[], lnK[];
    double lnOldK[];
    double oldoldDeltalnK[], oldDeltalnK[], deltalnK[];
    double tm[];
    int lowestGibbsEnergyPhase = 0; // lowestGibbsEnergyPhase
    sysNewtonRhapsonTPflash secondOrderSolver;
    SystemInterface minimumGibbsEnergySystem;
    protected boolean solidCheck = false, stabilityCheck = false;
    boolean findLowesGibsPhaseIsChecked = false;

    /**
     * Creates new Flash
     */
    public Flash() {
    }

    public int findLowestGibbsEnergyPhase() {
        if (!findLowesGibsPhaseIsChecked) {
            minimumGibbsEnergySystem = (SystemInterface) system.clone();
            minimumGibbsEnergySystem.init(0);
            minimumGibbsEnergySystem.init(1);
            if ((minimumGibbsEnergySystem.getPhase(0).getGibbsEnergy() * (1.0 - Math.signum(minimumGibbsEnergySystem.getPhase(0).getGibbsEnergy()) * 1e-8)) < minimumGibbsEnergySystem.getPhase(1).getGibbsEnergy()) {
                lowestGibbsEnergyPhase = 0;
            } else {
                lowestGibbsEnergyPhase = 1;
            }
            findLowesGibsPhaseIsChecked = true;
        }
        return lowestGibbsEnergyPhase;
    }

    public void stabilityAnalysis() throws neqsim.util.exception.IsNaNException, neqsim.util.exception.TooManyIterationsException {
        double[] logWi = new double[system.getPhases()[0].getNumberOfComponents()];
        double[] deltalogWi = new double[system.getPhases()[0].getNumberOfComponents()];
        double[] oldDeltalogWi = new double[system.getPhases()[0].getNumberOfComponents()];
        double[] oldoldDeltalogWi = new double[system.getPhases()[0].getNumberOfComponents()];
        double[][] Wi = new double[2][system.getPhases()[0].getNumberOfComponents()];
        double[] sumw = new double[2];
        boolean secondOrderStabilityAnalysis = false;
        double[] oldlogw = new double[system.getPhases()[0].getNumberOfComponents()];
        double[] oldoldlogw = new double[system.getPhases()[0].getNumberOfComponents()];
        double[] oldoldoldlogw = new double[system.getPhases()[0].getNumberOfComponents()];
        double[] d = new double[system.getPhases()[0].getNumberOfComponents()];
        double[][] x = new double[2][system.getPhases()[0].getNumberOfComponents()];
        double[] error = new double[2];
        tm = new double[system.getPhase(0).getNumberOfComponents()];
        double[] alpha = null;
        Matrix f = new Matrix(system.getPhases()[0].getNumberOfComponents(), 1);
        Matrix df = null;
        Matrix identitytimesConst = null;
        int maxiterations = 50;
        double oldErr = 1.0;

        for (int i = 0; i < system.getPhases()[0].getNumberOfComponents(); i++) {
            d[i] = minGibsPhaseLogZ[i] + minGibsLogFugCoef[i];
        }

        SystemInterface clonedSystem = minimumGibbsEnergySystem;//(SystemInterface) minimumGibbsEnergySystem.clone();
        clonedSystem.setTotalNumberOfMoles(1.0);
        clonedSystem.init(1);
        sumw[1] = 0.0;
        sumw[0] = 0.0;

        for (int i = 0; i < clonedSystem.getPhase(0).getNumberOfComponents(); i++) {
            double xliq = clonedSystem.getPhase(0).getComponent(i).getz() / clonedSystem.getPhase(0).getComponent(i).getK();
            if (xliq > 1) {
                xliq = 1.0;
            }
            double ygas = clonedSystem.getPhase(0).getComponent(i).getK() * clonedSystem.getPhase(0).getComponent(i).getz();
            if (ygas > 1) {
                ygas = 1.0;
            }
            clonedSystem.getPhase(0).getComponent(i).setx(ygas);
            clonedSystem.getPhase(1).getComponent(i).setx(xliq);

            sumw[1] += clonedSystem.getPhase(1).getComponent(i).getx();
            sumw[0] += clonedSystem.getPhase(0).getComponent(i).getx();
        }

        for (int i = 0; i < clonedSystem.getPhase(0).getNumberOfComponents(); i++) {
            clonedSystem.getPhase(1).getComponent(i).setx(clonedSystem.getPhase(1).getComponent(i).getx() / sumw[1]);
            clonedSystem.getPhase(0).getComponent(i).setx(clonedSystem.getPhase(0).getComponent(i).getx() / sumw[0]);
        }

        for (int j = 0; j < clonedSystem.getNumberOfPhases(); j++) {
            for (int i = 0; i < clonedSystem.getPhases()[0].getNumberOfComponents(); i++) {

                Wi[j][i] = clonedSystem.getPhase(j).getComponent(i).getx();
                logWi[i] = Math.log(Wi[j][i]);
            }
            iterations = 0;
            do {
                iterations++;
                oldErr = error[j];
                error[j] = 0.0;

                for (int i = 0; i < clonedSystem.getPhases()[0].getNumberOfComponents(); i++) {
                    oldoldoldlogw[i] = oldoldlogw[i];
                    oldoldlogw[i] = oldlogw[i];
                    oldlogw[i] = logWi[i];

                    oldoldDeltalogWi[i] = oldoldlogw[i] - oldoldoldlogw[i];
                    oldDeltalogWi[i] = oldlogw[i] - oldoldlogw[i];
                }

                if (iterations <= maxiterations - 10 || !system.isImplementedCompositionDeriativesofFugacity()) {

                    clonedSystem.init(1, j);

                    for (int i = 0; i < clonedSystem.getPhases()[0].getNumberOfComponents(); i++) {
                        f.set(i, 0, Math.sqrt(Wi[j][i]) * (Math.log(Wi[j][i]) + clonedSystem.getPhase(j).getComponent(i).getLogFugasityCoeffisient() - d[i]));
                    }
                    if (iterations % 7 == 0) {
                        double vec1 = 0.0, vec2 = 0.0, prod1 = 0.0, prod2 = 0.0;

                        for (i = 0; i < clonedSystem.getPhases()[0].getNumberOfComponents(); i++) {
                            vec1 = oldDeltalogWi[i] * oldoldDeltalogWi[i];
                            vec2 = Math.pow(oldoldDeltalogWi[i], 2.0);
                            prod1 += vec1 * vec2;
                            prod2 += vec2 * vec2;
                        }

                        double lambda = prod1 / prod2;
                        //System.out.println("lambda " + lambda);
                        for (i = 0; i < clonedSystem.getPhases()[0].getNumberOfComponents(); i++) {
                            logWi[i] += lambda / (1.0 - lambda) * deltalogWi[i];
                            error[j] += Math.abs((logWi[i] - oldlogw[i]) / oldlogw[i]);
                            Wi[j][i] = Math.exp(logWi[i]);
                        }
                    } else {
                        // succsessive substitution
                        for (int i = 0; i < clonedSystem.getPhases()[0].getNumberOfComponents(); i++) {
                            logWi[i] = d[i] - clonedSystem.getPhase(j).getComponent(i).getLogFugasityCoeffisient();
                            error[j] += Math.abs((logWi[i] - oldlogw[i]) / oldlogw[i]);
                            Wi[j][i] = Math.exp(logWi[i]);
                            //System.out.println("Wi " + Wi[j][i] + " fugcoef " + clonedSystem.getPhase(j).getComponent(i).getFugasityCoeffisient());
                        }
                        //System.out.println("tmSP " + tmSP);
                    }

                    //System.out.println("err  sucs sub" + error[j]);
                } else {
                    if (!secondOrderStabilityAnalysis) {
                        clonedSystem.init(3, j);
                        alpha = new double[system.getPhases()[0].getNumberOfComponents()];
                        df = new Matrix(system.getPhases()[0].getNumberOfComponents(), system.getPhases()[0].getNumberOfComponents());
                        identitytimesConst = Matrix.identity(system.getPhases()[0].getNumberOfComponents(), system.getPhases()[0].getNumberOfComponents());
                        secondOrderStabilityAnalysis = true;
                    }

                    for (int i = 0; i < clonedSystem.getPhases()[0].getNumberOfComponents(); i++) {
                        alpha[i] = 2.0 * Math.sqrt(Wi[j][i]);
                    }

                    for (int i = 0; i < clonedSystem.getPhases()[0].getNumberOfComponents(); i++) {
                        f.set(i, 0, Math.sqrt(Wi[j][i]) * (Math.log(Wi[j][i]) + clonedSystem.getPhases()[j].getComponents()[i].getLogFugasityCoeffisient() - d[i]));
                        for (int k = 0; k < clonedSystem.getPhases()[0].getNumberOfComponents(); k++) {
                            double kronDelt = (i == k) ? 1.0 : 0.0;
                            df.set(i, k, kronDelt + Math.sqrt(Wi[j][k] * Wi[j][i]) * clonedSystem.getPhases()[j].getComponents()[i].getdfugdn(k));// * clonedSystem.getPhases()[j].getNumberOfMolesInPhase());
                        }
                    }
                    // f.print(10, 10);
                    //  df.print(10, 10);
                    Matrix dx = df.plus(identitytimesConst).solve(f).times(-1.0);
                    //dx.print(10, 10);

                    for (int i = 0; i < clonedSystem.getPhases()[0].getNumberOfComponents(); i++) {
                        double alphaNew = alpha[i] + dx.get(i, 0);
                        Wi[j][i] = Math.pow(alphaNew / 2.0, 2.0);
                        logWi[i] = Math.log(Wi[j][i]);
                        error[j] += Math.abs((logWi[i] - oldlogw[i]) / oldlogw[i]);
                    }

                    //System.out.println("err newton " + error[j]);
                }

                //System.out.println("norm f " + f.norm1());
                //clonedSystem.display();
                sumw[j] = 0.0;
                for (int i = 0; i < clonedSystem.getPhases()[0].getNumberOfComponents(); i++) {
                    sumw[j] += Wi[j][i];
                }

                for (int i = 0; i < clonedSystem.getPhases()[0].getNumberOfComponents(); i++) {
                    deltalogWi[i] = logWi[i] - oldlogw[i];
                    clonedSystem.getPhase(j).getComponent(i).setx(Wi[j][i] / sumw[j]);
                }
               // System.out.println("err " + error[j]);
            } while ((f.norm1() > 1e-6 && iterations < maxiterations && error[j] < oldErr) || (iterations % 7) == 0 || iterations < 3);
            //System.out.println("err " + error[j]);
            //System.out.println("iterations " + iterations);
            //System.out.println("f.norm1() " + f.norm1());
            if (iterations >= maxiterations) {
                System.out.println("err staability check " + error[j]);
                // throw new util.exception.TooManyIterationsException();
            }

            tm[j] = 1.0;
            for (int i = 0; i < clonedSystem.getPhases()[0].getNumberOfComponents(); i++) {
                tm[j] -= Wi[j][i];
                x[j][i] = clonedSystem.getPhase(j).getComponent(i).getx();
            }
            if (tm[j] < -1e-4) {
                break;
            }
        }
        //clonedSystem.display();
        // check for trivial solution
        double diffx = 0.0;
        for (int i = 0; i < clonedSystem.getPhase(0).getNumberOfComponents(); i++) {
            diffx += Math.abs(clonedSystem.getPhase(0).getComponent(i).getx() - clonedSystem.getPhase(1).getComponent(i).getx());
        }
        if (diffx < 1e-10) {
            tm[0] = 0.0;
            tm[1] = 0.0;
        }

        if (((tm[0] < -1e-4) || (tm[1] < -1e-4)) && !(Double.isNaN(tm[0]) || (Double.isNaN(tm[1])))) {
            for (int i = 0; i < clonedSystem.getPhases()[0].getNumberOfComponents(); i++) {
                if (system.getPhases()[1].getComponents()[i].getx() < 1e-100) {
                    continue;
                }
                if (tm[0] < -1e-4) {
                    system.getPhases()[1].getComponents()[i].setK((Wi[0][i] / sumw[0]) / (Wi[1][i] / sumw[1]));
                    system.getPhases()[0].getComponents()[i].setK((Wi[0][i] / sumw[0]) / (Wi[1][i] / sumw[1]));
                } else if (tm[1] < -1e-4) {
                    system.getPhases()[1].getComponents()[i].setK((Wi[0][i] / sumw[0]) / (Wi[1][i] / sumw[1]));
                    system.getPhases()[0].getComponents()[i].setK((Wi[0][i] / sumw[0]) / (Wi[1][i] / sumw[1]));
                } else {
                    System.out.println("error in stability anlysis");
                    system.init(0);
                }

                if (Double.isNaN(tm[j])) {
                    tm[j] = 0;
                }
            }
        }

        //System.out.println("STABILITY ANALYSIS: ");
        //System.out.println("tm1: " + tm[0] + "  tm2: " + tm[1]);
    }

    public boolean stabilityCheck() {
        boolean stable = false;
        //System.out.println("starting stability analysis....");
        lowestGibbsEnergyPhase = findLowestGibbsEnergyPhase();
        if (system.getPhase(lowestGibbsEnergyPhase).getNumberOfComponents() > 1) {
            try {
                stabilityAnalysis();
            } catch (Exception e) {
                System.out.println("error ");
                e.printStackTrace();
            }
        }
        if (!(tm[0] < -1e-4) && !(tm[1] < -1e-4) || system.getPhase(0).getNumberOfComponents() == 1) {
            stable = true;
            system.init(0);
            //System.out.println("system is stable");
            //System.out.println("Stable phase is : " + lowestGibbsEnergyPhase);
            system.setNumberOfPhases(1);

            if (lowestGibbsEnergyPhase == 0) {
                system.setPhaseType(0, 1);
            } else {
                system.setPhaseType(0, 0);
            }
            system.init(1);
            if (solidCheck) {
                this.solidPhaseFlash();
            }

        } else {
            try {
                system.calcBeta();
            } catch (Exception e) {
                e.printStackTrace();
            }
            system.calc_x_y();
            system.init(1);
        }

        return stable;
    }

    public void displayResult() {
        system.display();
    }

    public void solidPhaseFlash() {
        boolean solidPhase = false;
        double frac = 0;
        int solid = 0;
        double[] tempVar = new double[system.getPhases()[0].getNumberOfComponents()];

        if (!system.hasSolidPhase()) {
            system.setNumberOfPhases(system.getNumberOfPhases() + 1);
            system.setPhaseIndex(system.getNumberOfPhases() - 1, 3);
        }
        //System.out.println("numb " + system.getNumberOfPhases());
        system.init(1);

        for (int k = 0; k < system.getPhase(0).getNumberOfComponents(); k++) {
            if (system.getPhase(0).getComponent(k).doSolidCheck()) {
                tempVar[k] = system.getPhase(0).getComponents()[k].getz();
                for (int i = 0; i < system.getNumberOfPhases() - 1; i++) {
                    tempVar[k] -= system.getBeta(i) * system.getPhases()[3].getComponent(k).getFugasityCoeffisient() / system.getPhase(i).getComponent(k).getFugasityCoeffisient();
                }

                if (tempVar[k] > 0.0 && tempVar[k] > frac) {
                    solidPhase = true;
                    solid = k;
                    frac = tempVar[k];
                    for (int p = 0; p < system.getPhases()[0].getNumberOfComponents(); p++) {
                        system.getPhases()[3].getComponents()[p].setx(1.0e-20);
                    }
                    system.getPhases()[3].getComponents()[solid].setx(1.0);
                }
                System.out.println("tempVar: " + tempVar[k]);
            }
        }

        if (solidPhase) {
            if (frac < system.getPhases()[0].getComponents()[solid].getz() + 1e10) {
                for (int i = 0; i < system.getNumberOfPhases() - 1; i++) {
                    // system.getPhases()[i].getComponents()[solid].setx(1.0e-10);
                }
                system.init(1);
                //System.out.println("solid phase will form..." + system.getNumberOfPhases());
                //System.out.println("freezing component " + solid);
                system.setBeta(system.getNumberOfPhases() - 1, frac);
                system.initBeta();
                system.setBeta(system.getNumberOfPhases() - 1, system.getPhases()[3].getComponent(solid).getNumberOfmoles() / system.getNumberOfMoles());
                //                double phasetot=0.0;
                //                for(int ph=0;ph<system.getNumberOfPhases();ph++){
                //                    phasetot += system.getPhase(ph).getBeta();
                //                }
                //                for(int ph=0;ph<system.getNumberOfPhases();ph++){
                //                    system.setBeta(ph, system.getPhase(ph).getBeta()/phasetot);
                //                }
                system.init(1);
                //                for(int ph=0;ph<system.getNumberOfPhases();ph++){
                //                    System.out.println("beta " + system.getPhase(ph).getBeta());
                //                }
                //                TPmultiflash operation = new TPmultiflash(system, true);
                //                operation.run();
                SolidFlash solflash = new SolidFlash(system);
                solflash.setSolidComponent(solid);
                solflash.run();
            } else {
                //System.out.println("all liquid will freeze out - removing liquid phase..");
                //int phasesNow = system.getNumberOfPhases()-1;
                //                system.init(0);
                //system.setNumberOfPhases(phasesNow);
                //system.setNumberOfPhases(system.getNumberOfPhases()-1);
                //system.setPhaseIndex(system.getNumberOfPhases()-1, 3);
                // system.setBeta(1-system.getPhases()[0].getComponents()[solid].getz());
                // system.init(1);
                // system.init_x_y();
                // system.init(1);
                //                solidPhaseFlash();
                // solid-vapor flash
            }

        } else {
            //system.setPhaseIndex(system.getNumberOfPhases() - 1, system.getNumberOfPhases() - 1);
            system.setNumberOfPhases(system.getNumberOfPhases() - 1);
            //System.out.println("no solid phase will form..");
        }

    }

    public void printToFile(String name) {
    }

    public void createNetCdfFile(String name) {
    }

    public double[][] getPoints(int i) {
        return null;
    }

    public String[][] getResultTable() {
        return null;
    }

    public void addData(String name, double[][] data) {

    }
}