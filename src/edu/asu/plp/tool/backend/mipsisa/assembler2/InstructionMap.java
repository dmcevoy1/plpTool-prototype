package edu.asu.plp.tool.backend.mipsisa.assembler2;

import java.util.HashMap;

import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.ITypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.JRRTypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.RITypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.RIUTypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.RJTypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.RLTypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.RMTypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.RTypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.RVTypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.AccRTypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.BRTypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.BTypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.JTypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.RCCTypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.RCTypeInstruction;
import edu.asu.plp.tool.backend.mipsisa.assembler2.instructions.RIBTypeInstruction;

public class InstructionMap extends HashMap<String, MIPSInstruction>
{
	public void addRTypeInstruction(String name, int functCode)
	{
		RTypeInstruction instruction = new RTypeInstruction(functCode);
		this.put(name, instruction);
	}
	
	public void addRITypeInstruction(String name, int functCode, int r_value)
	{
		RITypeInstruction instruction = new RITypeInstruction(functCode, r_value);
		this.put(name, instruction);
	}
	
	public void addRJTypeInstruction(String name, int functCode)
	{
		RJTypeInstruction instruction = new RJTypeInstruction(functCode);
		this.put(name, instruction);
	}
	
	public void addAccRTypeInstruction(String name, int opcode, int functCode)
	{
		AccRTypeInstruction instruction = new AccRTypeInstruction(opcode, functCode);
		this.put(name, instruction);
	}
	
	public void addRMTypeInstruction(String name, int functCode)
	{
		RMTypeInstruction instruction = new RMTypeInstruction(functCode);
		this.put(name, instruction);
	}
	
	public void addITypeInstruction(String name, int opCode)
	{
		ITypeInstruction instruction = new ITypeInstruction(opCode);
		this.put(name, instruction);
	}
	
	public void addRLTypeInstruction(String name, int opCode)
	{
		RLTypeInstruction instruction = new RLTypeInstruction(opCode);
		this.put(name, instruction);
	}
	
	public void addJTypeInstruction(String name, int opCode)
	{
		JTypeInstruction instruction = new JTypeInstruction(opCode);
		this.put(name, instruction);
	}
	
	public void addBTypeInstruction(String name, int opCode)
	{
		BTypeInstruction instruction = new BTypeInstruction(opCode);
		this.put(name, instruction);
	}
	
	public void addRIUTypeInstruction(String name, int opCode)
	{
		RIUTypeInstruction instruction = new RIUTypeInstruction(opCode);
		this.put(name, instruction);
	}
	
	public void addRVTypeInstruction(String name, int functCode, int r_value) {
		RVTypeInstruction instruction = new RVTypeInstruction(functCode, r_value);
		this.put(name, instruction);
	}
	
	public void addRCTypeInstruction(String name, int opCode, int functCode)
	{
		RCTypeInstruction instruction = new RCTypeInstruction(opCode, functCode);
		this.put(name, instruction);
	}
	
	public void addRCCTypeInstruction(String name, int opCode, int functCode)
	{
		RCCTypeInstruction instruction = new RCCTypeInstruction(opCode, functCode);
		this.put(name, instruction);
	}
	
	public void addRIBTypeInstruction(String name, int opCode, int functCode)
	{
		RIBTypeInstruction instruction = new RIBTypeInstruction(opCode, functCode);
		this.put(name, instruction);
	}
	
	public void addJRRTypeInstruction(String name, int opCode)
	{
		JRRTypeInstruction instruction = new JRRTypeInstruction(opCode);
		this.put(name, instruction);
	}
	
	public void addBRTypeInstruction(String name, int opCode, int branchCode)
	{
		BRTypeInstruction instruction = new BRTypeInstruction(opCode, branchCode);
		this.put(name, instruction);
	}
	
	@Override
	public MIPSInstruction put(String name, MIPSInstruction instruction)
	{
		validateKey(name);
		return super.put(name, instruction);
	}
	
	private void validateKey(String name)
	{
		if (this.containsKey(name))
		{
			String message = "MIPSInstructionMap cannot contain multiple instructions "
					+ "with the same name (" + name + ")";
			throw new IllegalArgumentException(message);
		}
	}
}
