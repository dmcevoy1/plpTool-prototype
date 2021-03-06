package edu.asu.plp.tool.backend.mipsisa.assembler2.instructions;

import static edu.asu.plp.tool.backend.mipsisa.assembler2.arguments.ArgumentType.REGISTER;

import java.text.ParseException;

import edu.asu.plp.tool.backend.mipsisa.assembler2.Argument;
import edu.asu.plp.tool.backend.mipsisa.assembler2.arguments.ArgumentType;
import edu.asu.plp.tool.backend.mipsisa.assembler2.arguments.RegisterArgument;

public class RMTypeInstruction extends AbstractInstruction
{
	//mflo $rs
	
	public static final int MASK_5BIT = 0b011111;
	public static final int MASK_6BIT = 0b111111;
	public static final int RS_POSITION = 21;
	public static final int RT_POSITION = 16;
	public static final int RD_POSITION = 11;
	public static final int SHAMT_POSITION = 6;
	public static final int FUNCT_CODE_POSITION = 0;
	
	private int functCode;
	
	public RMTypeInstruction(int functCode)
	{
		super(new ArgumentType[] { REGISTER });
		this.functCode = functCode;
	}
	
	@Override
	protected int safeAssemble(Argument[] arguments)
	{
		Argument rsRegisterArgument = arguments[0];
		
		return assembleEncodings(rsRegisterArgument.encode());
	}
	
	private int assembleEncodings(int encodedRSArgument)
	{	
		int encodedBitString = 0;
		encodedBitString |= (encodedRSArgument & MASK_5BIT) << RD_POSITION;
		encodedBitString |= (functCode & MASK_6BIT) << FUNCT_CODE_POSITION;
		
		return encodedBitString;
	}
}
