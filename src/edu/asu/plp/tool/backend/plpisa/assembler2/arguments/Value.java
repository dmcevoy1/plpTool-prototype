package edu.asu.plp.tool.backend.plpisa.assembler2.arguments;

import edu.asu.plp.tool.backend.plpisa.assembler2.Argument;

public class Value implements Argument
{
	private String rawValue;
	
	public Value(int value)
	{
		// TODO Auto-generated constructor stub
	}
	
	public Value(String rawValue)
	{
		this.rawValue = rawValue;
	}
	
	@Override
	public int encode()
	{
		// TODO Auto-generated method stub return 0;
		throw new UnsupportedOperationException("The method is not implemented yet.");
	}

	@Override
	public ArgumentType getType()
	{
		// TODO Auto-generated method stub return null;
		throw new UnsupportedOperationException("The method is not implemented yet.");
	}
	
	@Override
	public String raw()
	{
		// TODO Auto-generated method stub return null;
		throw new UnsupportedOperationException("The method is not implemented yet.");
	}
}
