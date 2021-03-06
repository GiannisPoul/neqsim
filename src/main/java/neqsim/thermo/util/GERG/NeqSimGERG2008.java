package neqsim.thermo.util.GERG;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author esol
 */

import neqsim.thermo.phase.PhaseInterface;
import neqsim.thermo.system.*;
import neqsim.thermodynamicOperations.ThermodynamicOperations;
import org.netlib.util.StringW;
import org.netlib.util.Util;
import org.netlib.util.doubleW;
import org.netlib.util.intW;

public class NeqSimGERG2008 {

	double[] normalizedGERGComposition = new double[21];
	double[] notNormalizedGERGComposition = new double[21];
	PhaseInterface phase = null;

	public NeqSimGERG2008() {

	}

	public NeqSimGERG2008(PhaseInterface phase) {
		this.setPhase(phase);
	}

	public double getMolarDensity(PhaseInterface phase) {
		this.setPhase(phase);
		return getMolarDensity();
	}

	public double getDensity(PhaseInterface phase) {
		this.setPhase(phase);
		return getMolarDensity() * phase.getMolarMass() * 1000.0;
	}

	public double getDensity() {
		return getMolarDensity() * phase.getMolarMass() * 1000.0;
	}

	public double getPressure() {
		int d = 0;
		double moldens = getMolarDensity();
		doubleW herr = new doubleW(0.0);
		doubleW ierr = new doubleW(0.0);
		Pressuregerg.pressuregerg(phase.getTemperature(), moldens, normalizedGERGComposition, d, ierr, herr);
		return ierr.val;
	}

	public double getMolarDensity() {
		int d = 0;
		int flag = 0;
		intW herr = new intW(0);
		doubleW ierr = new doubleW(0.0);
		StringW strW = new StringW("");
		double pressure = phase.getPressure() * 100.0;
		// Densitygerg dens = Densitygerg();
		// neqsim.thermo.GERG.Densitygerg.densitygerg(0, 0, 0, arg3, 0, arg5, arg6,
		// arg7);
		
		neqsim.thermo.util.GERG.Densitygerg.densitygerg(flag, phase.getTemperature(), pressure,
				normalizedGERGComposition, d, ierr, herr, strW);
		return ierr.val;
	}

	public double[] propertiesGERG(PhaseInterface phase) {
		this.setPhase(phase);
		return propertiesGERG();
	}

	public double[] getProperties(PhaseInterface phase, String[] properties) {
		double molarDens = getMolarDensity(phase);
		double[] allProperties = propertiesGERG();
		double[] returnProperties = new double[properties.length];

		for (int i = 0; i < properties.length; i++) {
			switch (properties[i]) {
			case "density":
				returnProperties[i] = allProperties[0];
				break;
			case "Cp":
				returnProperties[i] = allProperties[1];
				break;
			case "Cv":
				returnProperties[i] = allProperties[2];
				break;
			case "soundSpeed":
				returnProperties[i] = allProperties[3];
				break;
			}
		}
		return returnProperties;
	}

	public double[] propertiesGERG() {
		int _x_offset = 0;
		doubleW p = new doubleW(0.0);
		doubleW z = new doubleW(0.0);
		doubleW dpdd = new doubleW(0.0);
		doubleW d2pdd2 = new doubleW(0.0);
		doubleW d2pdtd = new doubleW(0.0);
		doubleW dpdt = new doubleW(0.0);
		doubleW u = new doubleW(0.0);
		doubleW h = new doubleW(0.0);
		doubleW s = new doubleW(0.0);
		doubleW cv = new doubleW(0.0);
		doubleW cp = new doubleW(0.0);
		doubleW w = new doubleW(0.0);
		doubleW g = new doubleW(0.0);
		doubleW jt = new doubleW(0.0);
		doubleW kappa = new doubleW(0.0);

		double dens = getMolarDensity();
		// neqsim.thermo.GERG.Densitygerg.densitygerg(0, 0, 0, arg3, 0, arg5, arg6,
		// arg7);
		neqsim.thermo.util.GERG.Propertiesgerg.propertiesgerg(phase.getTemperature(), dens, normalizedGERGComposition,
				_x_offset, p, z, dpdd, d2pdd2, d2pdtd, dpdt, u, h, s, cv, cp, w, g, jt, kappa);
		double[] properties = new double[] { p.val, z.val, dpdd.val, d2pdd2.val, d2pdtd.val, dpdt.val, u.val, h.val,
				s.val, cv.val, cp.val, w.val, g.val, jt.val, kappa.val };
		return properties;
	}

	public void setPhase(PhaseInterface phase) {
		this.phase = phase;
		for (int i = 0; i < phase.getNumberOfComponents(); i++) {

			String componentName = phase.getComponent(i).getComponentName();

			switch (componentName) {
			case "methane":
				notNormalizedGERGComposition[0] = phase.getComponent(i).getx();
				break;
			case "nitrogen":
				notNormalizedGERGComposition[1] = phase.getComponent(i).getx();
				break;
			case "CO2":
				notNormalizedGERGComposition[2] = phase.getComponent(i).getx();
				break;
			case "ethane":
				notNormalizedGERGComposition[3] = phase.getComponent(i).getx();
				break;
			case "propane":
				notNormalizedGERGComposition[4] = phase.getComponent(i).getx();
				break;
			case "i-butane":
				notNormalizedGERGComposition[5] = phase.getComponent(i).getx();
				break;
			case "n-butane":
				notNormalizedGERGComposition[6] = phase.getComponent(i).getx();
				break;
			case "i-pentane":
				notNormalizedGERGComposition[7] = phase.getComponent(i).getx();
				break;
			case "n-pentane":
				notNormalizedGERGComposition[8] = phase.getComponent(i).getx();
				break;
			case "n-hexane":
				notNormalizedGERGComposition[9] = phase.getComponent(i).getx();
				break;
			case "n-heptane":
				notNormalizedGERGComposition[10] = phase.getComponent(i).getx();
				break;
			case "n-octane":
				notNormalizedGERGComposition[11] = phase.getComponent(i).getx();
				break;
			case "n-nonane":
				notNormalizedGERGComposition[12] = phase.getComponent(i).getx();
				break;
			case "nC10":
				notNormalizedGERGComposition[13] = phase.getComponent(i).getx();
				break;
			case "hydrogen":
				notNormalizedGERGComposition[14] = phase.getComponent(i).getx();
				break;
			case "oxygen":
				notNormalizedGERGComposition[15] = phase.getComponent(i).getx();
				break;
			case "CO":
				notNormalizedGERGComposition[16] = phase.getComponent(i).getx();
				break;
			case "water":
				notNormalizedGERGComposition[17] = phase.getComponent(i).getx();
				break;
			case "H2S":
				notNormalizedGERGComposition[18] = phase.getComponent(i).getx();
				break;
			case "helium":
				notNormalizedGERGComposition[19] = phase.getComponent(i).getx();
				break;
			case "argon":
				notNormalizedGERGComposition[20] = phase.getComponent(i).getx();
				break;
			}
			;
		}
		normalizeComposition();
	}

	public void normalizeComposition() {
		double sum = Sum.sum(notNormalizedGERGComposition, 0);
		for (int k = 0; k < normalizedGERGComposition.length; k++) {
			normalizedGERGComposition[k] = notNormalizedGERGComposition[k] / sum;
		}

	}

	public static void main(String[] args) {

		SystemInterface fluid1 = new SystemSrkEos();

		fluid1.addComponent("CO2", 2.0);
		fluid1.addComponent("nitrogen", 1.2);
		fluid1.addComponent("methane", 92.0);
		fluid1.addComponent("ethane", 8.0);
		fluid1.addComponent("propane", 1.0);
		fluid1.setTemperature(298.0);
		fluid1.setPressure(150.00);
		ThermodynamicOperations ops = new ThermodynamicOperations(fluid1);
		ops.TPflash();
		fluid1.display();
		System.out.println("density GERG " +fluid1.getPhase(0).getDensity_GERG2008());
		NeqSimGERG2008 test = new NeqSimGERG2008(fluid1.getPhase("gas"));
		System.out.println("density " + test.getDensity());
		System.out.println("pressure " + test.getPressure());
		// System.out.println("properties " + test.propertiesGERG());
		double[] properties = test.propertiesGERG();
		System.out.println("Pressure [kPa]:            " + properties[0]);
		System.out.println("Compressibility factor:            " + properties[1]);
		System.out.println("d(P)/d(rho) [kPa/(mol/l)]            " + properties[2]);
		System.out.println("d^2(P)/d(rho)^2 [kPa/(mol/l)^2]:            " + properties[3]);
		System.out.println("d(P)/d(T) [kPa/K]:             " + properties[4]);
		System.out.println("Energy [J/mol]:             " + properties[5]);
		System.out.println("Enthalpy [J/mol]:             " + properties[6]);
		System.out.println("Entropy [J/mol-K]:             " + properties[7]);
		System.out.println("Isochoric heat capacity [J/mol-K]:             " + properties[8]);
		System.out.println("Isobaric heat capacity [J/mol-K]:            " + properties[9]);
		System.out.println("Speed of sound [m/s]:            " + properties[10]);
		System.out.println("Gibbs energy [J/mol]:            " + properties[11]);
		System.out.println("Joule-Thomson coefficient [K/kPa]:            " + properties[12]);
		System.out.println("Isentropic exponent:           " + properties[13]);

	}
}
