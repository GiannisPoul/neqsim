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
 * TPflash.java
 *
 * Created on 2. oktober 2000, 22:26
 */
package neqsim.thermodynamicOperations.flashOps;

import java.util.*;
import neqsim.thermo.system.SystemInterface;
import org.ejml.simple.SimpleMatrix;

/**
 *
 * @author Even Solbraa
 * @version
 */
public class TPmultiflash extends TPflash implements java.io.Serializable {

    private static final long serialVersionUID = 1000;

    //   SystemInterface clonedSystem;
    boolean multiPhaseTest = false;
    double dQdbeta[][];
    double Qmatrix[][];
    double E[];
    double Q = 0;
    boolean doStabilityAnalysis = true;
    boolean removePhase = false, checkOneRemove = false;
    boolean secondTime = false;

    /**
     * Creates new TPflash
     */
    public TPmultiflash() {
    }

    public TPmultiflash(SystemInterface system) {
        super(system);
    }

    public TPmultiflash(SystemInterface system, boolean check) {
        super(system, check);
    }

    public void calcMultiPhaseBeta() {
    }

    public void setXY() {
        for (int k = 0; k < system.getNumberOfPhases(); k++) {
            for (int i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
                if (system.getPhase(0).getComponent(i).getz() > 1e-100) {
                    system.getPhase(k).getComponents()[i].setx(system.getPhase(0).getComponents()[i].getz() / E[i] / system.getPhase(k).getComponents()[i].getFugasityCoeffisient());
                }
                if (system.getPhase(0).getComponent(i).getIonicCharge() != 0 && !system.getPhase(k).getPhaseTypeName().equals("aqueous")) {
                    system.getPhase(k).getComponents()[i].setx(1e-50);
                }
                if (system.getPhase(0).getComponent(i).getIonicCharge() != 0 && system.getPhase(k).getPhaseTypeName().equals("aqueous")) {
                    system.getPhase(k).getComponents()[i].setx(system.getPhase(k).getComponents()[i].getNumberOfmoles() / system.getPhase(k).getNumberOfMolesInPhase());
                }
            }

            system.getPhase(k).normalize();
        }
    }

    public void calcE() {
        E = new double[system.getPhase(0).getNumberOfComponents()];

        for (int i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
            for (int k = 0; k < system.getNumberOfPhases(); k++) {
                E[i] += system.getPhase(k).getBeta() / system.getPhase(k).getComponents()[i].getFugasityCoeffisient();
            }
        }
    }

    public double calcQ() {
        Q = 0;
        double betaTotal = 0;
        dQdbeta = new double[system.getNumberOfPhases()][1];
        Qmatrix = new double[system.getNumberOfPhases()][system.getNumberOfPhases()];

        for (int k = 0; k < system.getNumberOfPhases(); k++) {
            betaTotal += system.getPhase(k).getBeta();
        }

        Q = betaTotal;
        this.calcE();

        for (int i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
            Q -= Math.log(E[i]) * system.getPhase(0).getComponents()[i].getz();
        }

        for (int k = 0; k < system.getNumberOfPhases(); k++) {
            dQdbeta[k][0] = 1.0;
            for (int i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
                dQdbeta[k][0] -= system.getPhase(0).getComponents()[i].getz() * 1.0 / E[i] / system.getPhase(k).getComponents()[i].getFugasityCoeffisient();
            }
        }

        for (int i = 0; i < system.getNumberOfPhases(); i++) {
            for (int j = 0; j < system.getNumberOfPhases(); j++) {
                Qmatrix[i][j] = 0;
                for (int k = 0; k < system.getPhase(0).getNumberOfComponents(); k++) {
                    Qmatrix[i][j] += system.getPhase(0).getComponents()[k].getz() / (E[k] * E[k] * system.getPhase(j).getComponents()[k].getFugasityCoeffisient() * system.getPhase(i).getComponents()[k].getFugasityCoeffisient());
                }
                if (i == j) {
                    Qmatrix[i][j] += 1e-10;
                }
            }
        }
        return Q;
    }

    public double solveBeta(boolean updateFugacities) {
        double oldBeta[][] = new double[1][system.getNumberOfPhases()];
        double newBeta[][] = new double[system.getNumberOfPhases()][1];

        SimpleMatrix ans = null;// = new SimpleMatrix(system.getNumberOfPhases(), 1);
        double err = 1.0;
        int iter = 1;
        do {
            iter++;
            for (int k = 0; k < system.getNumberOfPhases(); k++) {
                oldBeta[0][k] = system.getPhase(k).getBeta();
            }

            calcQ();
            SimpleMatrix betaMatrix = new SimpleMatrix(oldBeta);
            SimpleMatrix dQM = new SimpleMatrix(dQdbeta);
            SimpleMatrix dQdBM = new SimpleMatrix(Qmatrix);
            try {
                ans = dQdBM.solve(dQM).transpose();
            } catch (Exception e) {
            }
            betaMatrix = betaMatrix.minus(ans.scale(iter / (iter + 3.0)));
            //  ans.print(10,2);
            // betaMatrix.print(10,2);
            removePhase = false;
            for (int k = 0; k < system.getNumberOfPhases(); k++) {
                system.setBeta(k, betaMatrix.get(0, k));
                if (betaMatrix.get(0, k) < 0) {
                    system.setBeta(k, 1.0e-9);
                    if (checkOneRemove) {
                        checkOneRemove = false;
                        removePhase = true;
                    }
                    checkOneRemove = true;
                } else if (betaMatrix.get(0, k) > 1) {
                    system.setBeta(k, 1.0 - 1e-9);
                }
            }
            system.normalizeBeta();

            if (updateFugacities) {
                //       system.init(1);
            }
            calcE();
            setXY();
            if (updateFugacities) {
                system.init(1);
            }
            err = ans.normF();
        } while ((err > 1e-12 && iter < 50) || iter < 3);
        return err;
    }

    public void stabilityAnalysis() {
        double[] logWi = new double[system.getPhase(0).getNumberOfComponents()];
        double[][] Wi = new double[system.getPhase(0).getNumberOfComponents()][system.getPhase(0).getNumberOfComponents()];

        double[] deltalogWi = new double[system.getPhases()[0].getNumberOfComponents()];
        double[] oldDeltalogWi = new double[system.getPhases()[0].getNumberOfComponents()];
        double[] oldoldDeltalogWi = new double[system.getPhases()[0].getNumberOfComponents()];
        double[] sumw = new double[system.getPhase(0).getNumberOfComponents()];
        double sumz = 0, err = 0;
        double[] oldlogw = new double[system.getPhase(0).getNumberOfComponents()];
        double[] oldoldlogw = new double[system.getPhases()[0].getNumberOfComponents()];
        double[] oldoldoldlogw = new double[system.getPhases()[0].getNumberOfComponents()];
        double[] d = new double[system.getPhase(0).getNumberOfComponents()];
        double[][] x = new double[system.getPhase(0).getNumberOfComponents()][system.getPhase(0).getNumberOfComponents()];
        tm = new double[system.getPhase(0).getNumberOfComponents()];

        double[] alpha = null;
        SystemInterface minimumGibbsEnergySystem;
        ArrayList clonedSystem = new ArrayList(1);

        minimumGibbsEnergySystem = (SystemInterface) system.clone();
        //        minimumGibbsEnergySystem.init(1);
        for (int i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
            if (system.getPhase(0).getComponent(i).getx() < 1e-100) {
                clonedSystem.add(null);
                continue;
            }
            double numb = 0;
            clonedSystem.add(system.clone());
//            ((SystemInterface)clonedSystem.get(i)).init(0); commented out sept 2005, Even S.
            for (int j = 0; j < system.getPhase(0).getNumberOfComponents(); j++) {
                numb = i == j ? 1.0 : 1.0e-12; // set to 0 by Even Solbraa 23.01.2013 - chaged back to 1.0e-12 27.04.13
                if (system.getPhase(0).getComponent(j).getz() < 1e-100) {
                    numb = 0;
                }
                ((SystemInterface) clonedSystem.get(i)).getPhase(1).getComponents()[j].setx(numb);
            }
            if (system.getPhase(0).getComponent(i).getIonicCharge() == 0) {
                ((SystemInterface) clonedSystem.get(i)).init(1);
            }
        }

        lowestGibbsEnergyPhase = 0;

        // System.out.println("low gibbs phase " + lowestGibbsEnergyPhase);
        for (int k = 0; k < minimumGibbsEnergySystem.getPhase(0).getNumberOfComponents(); k++) {
            sumz += minimumGibbsEnergySystem.getPhase(0).getComponents()[k].getz();
            for (int i = 0; i < minimumGibbsEnergySystem.getPhase(0).getNumberOfComponents(); i++) {
                if (!(((SystemInterface) clonedSystem.get(k)) == null)) {
                    sumw[k] += ((SystemInterface) clonedSystem.get(k)).getPhase(1).getComponents()[i].getx();
                }
            }
        }

        for (int k = 0; k < minimumGibbsEnergySystem.getPhase(0).getNumberOfComponents(); k++) {
            for (int i = 0; i < minimumGibbsEnergySystem.getPhase(0).getNumberOfComponents(); i++) {
                if (!(((SystemInterface) clonedSystem.get(k)) == null) && system.getPhase(0).getComponent(k).getx() > 1e-100) {
                    ((SystemInterface) clonedSystem.get(k)).getPhase(1).getComponents()[i].setx(((SystemInterface) clonedSystem.get(k)).getPhase(1).getComponents()[i].getx() / sumw[0]);
                }
                //  System.out.println("x: " +  ((SystemInterface) clonedSystem.get(k)).getPhase(0).getComponents()[i].getx());
            }
            if (system.getPhase(0).getComponent(k).getx() > 1e-100) {
                d[k] = Math.log(minimumGibbsEnergySystem.getPhases()[lowestGibbsEnergyPhase].getComponents()[k].getx()) + minimumGibbsEnergySystem.getPhases()[lowestGibbsEnergyPhase].getComponents()[k].getLogFugasityCoeffisient();
                // if(minimumGibbsEnergySystem.getPhases()[lowestGibbsEnergyPhase].getComponents()[k].getIonicCharge()!=0) d[k]=0;
            }
            //System.out.println("dk: " + d[k]);
        }

        for (int j = 0; j < minimumGibbsEnergySystem.getPhase(0).getNumberOfComponents(); j++) {
            if (system.getPhase(0).getComponent(j).getz() > 1e-100) {
                logWi[j] = 1.0;
            } else {
                logWi[j] = -10000.0;
            }
        }

        int hydrocarbonTestCompNumb = 0, lightTestCompNumb = 0;
        double Mmax = 0, Mmin = 1e10;
        for (int i = 0; i < minimumGibbsEnergySystem.getPhase(0).getNumberOfComponents(); i++) {
            if (minimumGibbsEnergySystem.getPhase(0).getComponent(i).isHydrocarbon()) {
                if ((minimumGibbsEnergySystem.getPhase(0).getComponent(i).getMolarMass()) > Mmax) {
                    Mmax = minimumGibbsEnergySystem.getPhase(0).getComponent(i).getMolarMass();
                }
                if ((minimumGibbsEnergySystem.getPhase(0).getComponent(i).getMolarMass()) < Mmin) {
                    Mmin = minimumGibbsEnergySystem.getPhase(0).getComponent(i).getMolarMass();
                }
            }
        }
        for (int i = 0; i < minimumGibbsEnergySystem.getPhase(0).getNumberOfComponents(); i++) {
            if (minimumGibbsEnergySystem.getPhase(0).getComponent(i).isHydrocarbon() && minimumGibbsEnergySystem.getPhase(0).getComponent(i).getz() > 1e-50) {
                if (Math.abs((minimumGibbsEnergySystem.getPhase(0).getComponent(i).getMolarMass()) - Mmax) < 1e-5) {
                    hydrocarbonTestCompNumb = i;
                    //System.out.println("CHECKING heavy component " + hydrocarbonTestCompNumb);
                }
            }

            if (minimumGibbsEnergySystem.getPhase(0).getComponent(i).isHydrocarbon() && minimumGibbsEnergySystem.getPhase(0).getComponent(i).getz() > 1e-50) {
                if (Math.abs((minimumGibbsEnergySystem.getPhase(0).getComponent(i).getMolarMass()) - Mmin) < 1e-5) {
                    lightTestCompNumb = i;
                    //System.out.println("CHECKING light component " + lightTestCompNumb);
                }
            }
        }

        for (int j = system.getPhase(0).getNumberOfComponents() - 1; j >= 0; j--) {
            if (minimumGibbsEnergySystem.getPhase(0).getComponent(j).getx() < 1e-100 || (minimumGibbsEnergySystem.getPhase(0).getComponent(j).getIonicCharge() != 0) || (minimumGibbsEnergySystem.getPhase(0).getComponent(j).isHydrocarbon() && j != hydrocarbonTestCompNumb && j != lightTestCompNumb)) {
                continue;
            }
            // if(minimumGibbsEnergySystem.getPhase(0).getComponent(j).getName().equals("water") && minimumGibbsEnergySystem.isChemicalSystem()) continue;
            //System.out.println("STAB CHECK COMP " + system.getPhase(0).getComponent(j).getComponentName());
            //if(minimumGibbsEnergySystem.getPhase(0).getComponent(j).isInert()) break;
            int iter = 0;
            double errOld = 1.0e100;
            do {
                errOld = err;
                iter++;
                err = 0;

                if (iter <= 20 || !system.isImplementedCompositionDeriativesofFugacity()) {

                    if (iter % 7 == 0) {
                        double vec1 = 0.0, vec2 = 0.0, prod1 = 0.0, prod2 = 0.0;

                        for (i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
                            vec1 = oldDeltalogWi[i] * oldoldDeltalogWi[i];
                            vec2 = Math.pow(oldoldDeltalogWi[i], 2.0);
                            prod1 += vec1 * vec2;
                            prod2 += vec2 * vec2;
                        }

                        double lambda = prod1 / prod2;
                        //System.out.println("lambda " + lambda);
                        for (i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
                            logWi[i] += lambda / (1.0 - lambda) * deltalogWi[i];
                            err += Math.abs((logWi[i] - oldlogw[i]) / oldlogw[i]);
                            Wi[j][i] = Math.exp(logWi[i]);
                        }
                    } else {
                        for (int i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
                            oldoldoldlogw[i] = oldoldlogw[i];
                            oldoldlogw[i] = oldlogw[i];
                            oldlogw[i] = logWi[i];
                            oldoldDeltalogWi[i] = oldoldlogw[i] - oldoldoldlogw[i];
                            oldDeltalogWi[i] = oldlogw[i] - oldoldlogw[i];
                        }
                        ((SystemInterface) clonedSystem.get(j)).init(1, 1);
                        for (int i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
                            //oldlogw[i] = logWi[i];
                            if (!Double.isInfinite(((SystemInterface) clonedSystem.get(j)).getPhase(1).getComponents()[i].getLogFugasityCoeffisient()) && system.getPhase(0).getComponent(i).getx() > 1e-100) {
                                logWi[i] = d[i] - ((SystemInterface) clonedSystem.get(j)).getPhase(1).getComponents()[i].getLogFugasityCoeffisient();
                                if (((SystemInterface) clonedSystem.get(j)).getPhase(1).getComponents()[i].getIonicCharge() != 0) {
                                    logWi[i] = -1000.0;
                                }
                            }
                            deltalogWi[i] = logWi[i] - oldlogw[i];
                            err += Math.abs(logWi[i] - oldlogw[i]);
                            Wi[j][i] = Math.exp(logWi[i]);
                        }
                    }
                } else {
                    SimpleMatrix f = new SimpleMatrix(system.getPhases()[0].getNumberOfComponents(), 1);
                    SimpleMatrix df = null;
                    SimpleMatrix identitytimesConst = null;
                    // if (!secondOrderStabilityAnalysis) {
                    for (int i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
                        oldoldoldlogw[i] = oldoldlogw[i];
                        oldoldlogw[i] = oldlogw[i];
                        oldlogw[i] = logWi[i];
                        oldoldDeltalogWi[i] = oldoldlogw[i] - oldoldoldlogw[i];
                        oldDeltalogWi[i] = oldlogw[i] - oldoldlogw[i];
                    }
                    ((SystemInterface) clonedSystem.get(j)).init(3, 1);
                    alpha = new double[((SystemInterface) clonedSystem.get(j)).getPhases()[0].getNumberOfComponents()];
                    df = new SimpleMatrix(system.getPhases()[0].getNumberOfComponents(), system.getPhases()[0].getNumberOfComponents());
                    identitytimesConst = SimpleMatrix.identity(system.getPhases()[0].getNumberOfComponents());//, system.getPhases()[0].getNumberOfComponents());
                    //       secondOrderStabilityAnalysis = true;
                    //    }

                    for (int i = 0; i < ((SystemInterface) clonedSystem.get(j)).getPhases()[0].getNumberOfComponents(); i++) {
                        alpha[i] = 2.0 * Math.sqrt(Wi[j][i]);
                    }

                    for (int i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
                        if (system.getPhase(0).getComponent(i).getz() > 1e-100) {
                            f.set(i, 0, Math.sqrt(Wi[j][i]) * (Math.log(Wi[j][i]) + ((SystemInterface) clonedSystem.get(j)).getPhases()[1].getComponents()[i].getLogFugasityCoeffisient() - d[i]));
                        }
                        for (int k = 0; k < ((SystemInterface) clonedSystem.get(j)).getPhases()[0].getNumberOfComponents(); k++) {
                            double kronDelt = (i == k) ? 1.0 : 0.0;
                            if (system.getPhase(0).getComponent(i).getz() > 1e-100) {
                                df.set(i, k, kronDelt + Math.sqrt(Wi[j][k] * Wi[j][i]) * ((SystemInterface) clonedSystem.get(j)).getPhases()[1].getComponents()[i].getdfugdn(k));// * clonedSystem.getPhases()[j].getNumberOfMolesInPhase());
                            } else {
                                df.set(i, k, 0);// * clonedSystem.getPhases()[j].getNumberOfMolesInPhase());
                            }
                        }
                    }
                    // f.print(10, 10);
                    //  df.print(10, 10);
                    SimpleMatrix dx = df.plus(identitytimesConst).solve(f).negative();
                    //dx.print(10, 10);

                    for (int i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
                        double alphaNew = alpha[i] + dx.get(i, 0);
                        Wi[j][i] = Math.pow(alphaNew / 2.0, 2.0);
                        if (system.getPhase(0).getComponent(i).getz() > 1e-100) {
                            logWi[i] = Math.log(Wi[j][i]);
                        }
                        if (system.getPhase(0).getComponent(i).getIonicCharge() != 0) {
                            logWi[i] = -1000.0;
                        }
                        err += Math.abs((logWi[i] - oldlogw[i]) / oldlogw[i]);
                    }

                    //   System.out.println("err newton " + err);
                }
                // System.out.println("err: " + err);
                sumw[j] = 0;

                for (int i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
                    sumw[j] += Math.exp(logWi[i]);
                }

                for (int i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
                    if (system.getPhase(0).getComponent(i).getx() > 1e-100) {
                        ((SystemInterface) clonedSystem.get(j)).getPhase(1).getComponents()[i].setx(Math.exp(logWi[i]) / sumw[j]);
                    }
                    if (system.getPhase(0).getComponent(i).getIonicCharge() != 0) {
                        ((SystemInterface) clonedSystem.get(j)).getPhase(1).getComponents()[i].setx(1e-50);
                    }
                }
            } while ((Math.abs(err) > 1e-9 || err > errOld) && iter < 200);
            // System.out.println("err: " + err + " ITER " + iter);
            double xTrivialCheck0 = 0.0;
            double xTrivialCheck1 = 0.0;

            tm[j] = 1.0;

            for (int i = 0; i < system.getPhase(1).getNumberOfComponents(); i++) {
                if (system.getPhase(0).getComponent(i).getx() > 1e-100) {
                    tm[j] -= Math.exp(logWi[i]);
                }
                x[j][i] = ((SystemInterface) clonedSystem.get(j)).getPhase(1).getComponents()[i].getx();
                //  System.out.println("txji: " + x[j][i]);

                xTrivialCheck0 += Math.abs(x[j][i] - system.getPhase(0).getComponent(i).getx());
                xTrivialCheck1 += Math.abs(x[j][i] - system.getPhase(1).getComponent(i).getx());
            }
            if (iter >= 199) {
                System.out.println("iter > maxiter multiphase stability ");
                System.out.println("error " + Math.abs(err));
                System.out.println("tm: " + tm[j]);
            }

            if (Math.abs(xTrivialCheck0) < 1e-6 || Math.abs(xTrivialCheck1) < 1e-6) {
                tm[j] = 10.0;
            }

            if (tm[j] < -1e-8) {
                break;
            }
        }

        int unstabcomp = 0;
        for (int k = system.getPhase(0).getNumberOfComponents() - 1; k >= 0; k--) {
            if (tm[k] < -1e-8 && !(Double.isNaN(tm[k]))) {
                system.addPhase();
                unstabcomp = k;
                for (int i = 0; i < system.getPhase(1).getNumberOfComponents(); i++) {
                    system.getPhase(system.getNumberOfPhases() - 1).getComponents()[i].setx(x[k][i]);
                }
                system.getPhases()[system.getNumberOfPhases() - 1].normalize();
                multiPhaseTest = true;
                system.setBeta(system.getNumberOfPhases() - 1, system.getPhase(0).getComponent(unstabcomp).getz());
                system.init(1);
                system.normalizeBeta();
                //system.display();
                return;
            }
        }
        system.normalizeBeta();
        //System.out.println("STABILITY ANALYSIS: ");
        //  System.out.println("tm1: " + tm[0] + "  tm2: " + tm[1]);
        //system.display();
    }

    public void run() {
        int aqueousPhaseNumber = 0;
        // System.out.println("Starting multiphase-flash....");

        // system.setNumberOfPhases(system.getNumberOfPhases()+1);
        //  system.display();
        if (doStabilityAnalysis) {
            stabilityAnalysis();
        }
        //  system.orderByDensity();
        doStabilityAnalysis = true;
        //system.init(1);
        //system.display();
        aqueousPhaseNumber = system.getPhaseNumberOfPhase("aqueous");
        if (system.isChemicalSystem()) {
            system.getChemicalReactionOperations().solveChemEq(system.getPhaseNumberOfPhase("aqueous"), 0);
            system.getChemicalReactionOperations().solveChemEq(system.getPhaseNumberOfPhase("aqueous"), 1);
        }

        int iterations = 0;
        if (multiPhaseTest) {// && !system.isChemicalSystem()) {
            double oldBeta = 1.0;
            double diff = 1.0e10, oldDiff = 1.0e10;

            double chemdev = 0;
            int iterOut = 0;
            do {
                iterOut++;
                if (system.isChemicalSystem()) {
                    if (system.getPhaseNumberOfPhase("aqueous") != aqueousPhaseNumber) {
                        aqueousPhaseNumber = system.getPhaseNumberOfPhase("aqueous");
                        system.getChemicalReactionOperations().solveChemEq(system.getPhaseNumberOfPhase("aqueous"), 0);
                        // system.getChemicalReactionOperations().solveChemEq(system.getPhaseNumberOfPhase("aqueous"), 1);
                    }

                    for (int phase = system.getPhaseNumberOfPhase("aqueous"); phase < system.getPhaseNumberOfPhase("aqueous") + 1; phase++) {
                        chemdev = 0.0;
                        double xchem[] = new double[system.getPhase(phase).getNumberOfComponents()];

                        for (i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
                            xchem[i] = system.getPhase(phase).getComponents()[i].getx();
                        }

                        system.init(1);
                        system.getChemicalReactionOperations().solveChemEq(system.getPhaseNumberOfPhase("aqueous"), 1);

                        for (i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
                            chemdev += Math.abs(xchem[i] - system.getPhase(phase).getComponents()[i].getx());
                        }
                        System.out.println("chemdev: " + chemdev);
                    }
                }

                do {
                    iterations++;
                    oldBeta = system.getBeta(system.getNumberOfPhases() - 1);
                    //system.init(1);
                    oldDiff = diff;
                    diff = this.solveBeta(true);
                    // diff = Math.abs((system.getBeta(system.getNumberOfPhases() - 1) - oldBeta) / oldBeta);
                    //System.out.println("diff multiphase " + diff);
                } while (diff > 1e-12 && !removePhase && (diff < oldDiff || iterations < 50) && iterations < 500);
                //  this.solveBeta(true);
                if (iterations >= 499) {
                    System.out.println("error in multiphase flash..did not solve in 50 iterations");
                }

            } while ((Math.abs(chemdev) > 1e-10 && iterOut < 100) || (iterOut < 3 && system.isChemicalSystem()));

            boolean hasRemovedPhase = false;
            for (int i = 0; i < system.getNumberOfPhases(); i++) {
                if (system.getBeta(i) < 1.1e-9) {
                    system.removePhaseKeepTotalComposition(i);
                    doStabilityAnalysis = false;
                    hasRemovedPhase = true;
                }
            }

            boolean trivialSolution = false;
            for (int i = 0; i < system.getNumberOfPhases() - 1; i++) {
                for (int j = 0; j < system.getPhase(i).getNumberOfComponents(); j++) {
                    if (Math.abs(system.getPhase(i).getDensity() - system.getPhase(i + 1).getDensity()) < 1.1e-5) {
                        trivialSolution = true;
                    }
                }
            }

            if (trivialSolution && !hasRemovedPhase) {
                for (int i = 0; i < system.getNumberOfPhases() - 1; i++) {
                    if (Math.abs(system.getPhase(i).getDensity() - system.getPhase(i + 1).getDensity()) < 1.1e-5) {
                        system.removePhaseKeepTotalComposition(i + 1);
                        doStabilityAnalysis = false;
                        hasRemovedPhase = true;
                    }
                }
            }

            /*
             * for (int i = 0; i < system.getNumberOfPhases()-1; i++) { if
             * (Math.abs(system.getPhase(i).getDensity()-system.getPhase(i+1).getDensity())<1e-6
             * && !hasRemovedPhase) { system.removePhase(i+1);
             * doStabilityAnalysis=false; hasRemovedPhase = true; } }
             */
            if (hasRemovedPhase && !secondTime) {
                secondTime = true;
                run();
            }
            /*
            if (!secondTime) {
                secondTime = true;
                doStabilityAnalysis = false;
                run();
            }
             */
        }
    }
}