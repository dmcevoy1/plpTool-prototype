package edu.asu.plp.tool.backend.mipsisa.sim;

public enum SimulatorFlag
{
	PLP_SIM_FWD_NO_EVENTS(0xF000FFFF),
	PLP_SIM_FWD_EX_EX_RT(0x00010000),
	PLP_SIM_FWD_EX_EX_RS(0x00020000),
	PLP_SIM_FWD_MEM_MEM (0x04000000),
	PLP_SIM_FWD_MEM_EX_RT(0x00100000),
	PLP_SIM_FWD_MEM_EX_RS(0x00200000),
	PLP_SIM_FWD_MEM_EX_LW_RT(0x01000000),
	PLP_SIM_FWD_MEM_EX_LW_RS(0x02000000),
	PLP_SIM_IF_STALL_SET(0x00000100),
	PLP_SIM_ID_STALL_SET(0x00000200),
	PLP_SIM_EX_STALL_SET(0x00000400),
	PLP_SIM_MEM_STALL_SET(0x00000800),
	PLP_SIM_IRQ(0x10000000),
	PLP_SIM_IRQ_SERVICED(0x20000000);
	
	private long oldFlag;
	
	SimulatorFlag(long flag)
	{
		this.oldFlag = flag;
	}
	
	public long getFlag()
	{
		return oldFlag;
	}
}
