package edu.asu.plp.tool.backend.plpisa.assembler2.instructions;

import static edu.asu.plp.tool.backend.plpisa.assembler2.arguments.ArgumentType.LABEL_LITERAL;

import java.text.ParseException;

import edu.asu.plp.tool.backend.plpisa.assembler2.Argument;
import edu.asu.plp.tool.backend.plpisa.assembler2.arguments.ArgumentType;
import edu.asu.plp.tool.backend.plpisa.assembler2.instructions.AbstractInstruction;

public class JTypeInstruction extends AbstractInstruction
{
	
	int opCode;
	

	public JTypeInstruction(int nOpcode) 
	{
		super(new ArgumentType[] { LABEL_LITERAL });
		
		this.opCode = nOpcode;
	}

	@Override
	protected int safeAssemble(Argument[] arguments) throws ParseException {
		// TODO Auto-generated method stub
		return 0;
	}
	
}