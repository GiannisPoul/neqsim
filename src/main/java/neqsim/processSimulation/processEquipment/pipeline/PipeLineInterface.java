/*
 * PipeLineInterface.java
 *
 * Created on 21. august 2001, 20:44
 */

package neqsim.processSimulation.processEquipment.pipeline;

import neqsim.fluidMechanics.flowSystem.FlowSystemInterface;

/**
 *
 * @author  esol
 * @version
 */
public interface PipeLineInterface {
    public void setNumberOfLegs(int number);
    public void setHeightProfile(double[] heights);
    public void setLegPositions(double[] positions);
    public void setPipeDiameters(double[] diameter);
    public void setPipeWallRoughness(double[] rough);
    public void setOuterTemperatures(double[] outerTemp);
    public void setNumberOfNodesInLeg(int number);
    public void setOutputFileName(String name);
    public void setInitialFlowPattern(String flowPattern);
    public FlowSystemInterface getPipe();
    public String getName();
    public void setName(String name);
}

