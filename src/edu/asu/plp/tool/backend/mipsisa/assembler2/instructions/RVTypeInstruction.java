package edu.asu.plp.tool.backend.mipsisa.assembler2.instructions;

import static edu.asu.plp.tool.backend.mipsisa.assembler2.arguments.ArgumentType.REGISTER;

import java.text.ParseException;

import edu.asu.plp.tool.backend.mipsisa.assembler2.Argument;
import edu.asu.plp.tool.backend.mipsisa.assembler2.arguments.ArgumentType;

public class RVTypeInstruction extends AbstractInstruction
{
	//srlv
	//rotrv
	
	public static final int MASK_5BIT = 0b011111;
	public static final int MASK_6BIT = 0b111111;
	public static final int RS_POSITION = 21;
	public static final int RT_POSITION = 16;
	public static final int RD_POSITION = 11;
	public static final int SHAMT_POSITION = 6;
	public static final int FUNCT_CODE_POSITION = 0;
	
	private int functCode;
	private int rValue;
	
	public RVTypeInstruction(int functCode, int r_value)
	{
		super(new ArgumentType[] { REGISTER, REGISTER, REGISTER });
		this.functCode = functCode;
		this.rValue = r_value;
	}
	
	@Override
	protected int safeAssemble(Argument[] arguments)
	{
		Argument rdRegisterArgument = arguments[0];
		Argument rsRegisterArgument = arguments[1];
		Argument rtRegisterArgument = arguments[2];
		
		return assembleEncodings(rdRegisterArgument.encode(),
				rsRegisterArgument.encode(), rtRegisterArgument.encode());
	}
	
	private int assembleEncodings(int encodedRDArgument, int encodedRSArgument,
			int encodedRTArgument)
	{
		int encodedBitString = 0;
		encodedBitString |= (encodedRSArgument & MASK_5BIT) << RS_POSITION;
		encodedBitString |= (encodedRTArgument & MASK_5BIT) << RT_POSITION;
		encodedBitString |= (encodedRDArgument & MASK_5BIT) << RD_POSITION;
		encodedBitString |= (rValue & MASK_5BIT) << SHAMT_POSITION;
		encodedBitString |= (functCode & MASK_6BIT) << FUNCT_CODE_POSITION;
		
		return encodedBitString;
	}
}
