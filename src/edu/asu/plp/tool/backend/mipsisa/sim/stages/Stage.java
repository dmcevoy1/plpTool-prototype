package edu.asu.plp.tool.backend.mipsisa.sim.stages;

import edu.asu.plp.tool.backend.mipsisa.sim.stages.state.CpuState;

public interface Stage
{
	void evaluate();
	
	void clock();
	
	void printVariables();
	
	void printNextVariables();
	
	String printInstruction();
	
	void reset();
	
	boolean isHot();
	
	edu.asu.plp.tool.backend.mipsisa.sim.stages.state.CpuState getState();
}
