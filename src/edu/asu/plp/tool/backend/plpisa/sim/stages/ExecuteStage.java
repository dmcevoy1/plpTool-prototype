package edu.asu.plp.tool.backend.plpisa.sim.stages;

import com.google.common.eventbus.EventBus;

import edu.asu.plp.tool.backend.plpisa.InstructionExtractor;
import edu.asu.plp.tool.backend.plpisa.sim.stages.events.ExecuteStageStateRequest;
import edu.asu.plp.tool.backend.plpisa.sim.stages.events.ExecuteStageStateResponse;
import edu.asu.plp.tool.backend.plpisa.sim.stages.events.InstructionDecodeCompletion;
import edu.asu.plp.tool.backend.plpisa.sim.stages.state.ExecuteStageState;
import edu.asu.plp.tool.backend.plpisa.sim.stages.state.MemoryStageState;

public class ExecuteStage implements Stage
{
	private EventBus bus;
	private ExecuteEventHandler eventHandler;
	
	private ExecuteStageState state;
	private MemoryStageState currentMemoryStageState;
	
	public ExecuteStage(EventBus simulatorBus)
	{
		this.bus = simulatorBus;
		this.eventHandler = new ExecuteEventHandler();
		
		this.bus.register(eventHandler);
		
		this.state = new ExecuteStageState();
		
		reset();
	}
	
	@Override
	public void evaluate()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void clock()
	{
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void printVariables()
	{
		/*
		 * String rt_forwarded = (sim_flags & (PLP_SIM_FWD_EX_EX_RT |
		 * PLP_SIM_FWD_MEM_EX_RT)) == 0 ? "" : " (forwarded)";
		 */
		
		/*
		 * String rs_forwarded = (sim_flags & (PLP_SIM_FWD_EX_EX_RS |
		 * PLP_SIM_FWD_MEM_EX_RS)) == 0 ? "" : " (forwarded)";
		 */
		int spaceSize = -35;
		
		System.out.println("EX vars");
		System.out.println(String.format("%" + spaceSize + "s %08x %s", "\tInstruction",
				state.currentInstruction,
				InstructionExtractor.format(state.currentInstruction)));
				
		String formattedInstructionAddress = ((state.currentInstructionAddress == -1
				|| state.bubble) ? "--------"
						: String.format("%08x", state.currentInstructionAddress));
		System.out.println(String.format("%" + spaceSize + "s %s", "\tInstructionAddress",
				formattedInstructionAddress));
				
		System.out.println(String.format("%" + spaceSize + "s %x", "\tForwardCt1MemToReg",
				state.forwardCt1Memtoreg));
		System.out.println(String.format("%" + spaceSize + "s %x", "\tForwardCt1Regwrite",
				state.forwardCt1Regwrite));
		System.out.println(String.format("%" + spaceSize + "s %x", "\tForwardCt1Memwrite",
				state.forwardCt1Memwrite));
		System.out.println(String.format("%" + spaceSize + "s %x", "\tForwardCt1Memread",
				state.forwardCt1Memread));
		System.out.println(String.format("%" + spaceSize + "s %08x",
				"\tForwardCt1LinkAddress", state.forwardCt1Linkaddress));
		System.out.println(String.format("%" + spaceSize + "s %x", "\tForwardCt1Jal",
				state.forwardCt1Jal));
				
		System.out.println(
				String.format("%" + spaceSize + "s %x", "\tct1AluSrc", state.ct1Alusrc));
		System.out.println(
				String.format("%" + spaceSize + "s %08x", "\tct1AluOp", state.ct1Aluop));
		System.out.println(
				String.format("%" + spaceSize + "s %x", "\tct1RegDst", state.ct1Regdest));
		System.out.println(String.format("%" + spaceSize + "s %x", "\tct1AddressRt",
				state.ct1RtAddress));
		System.out.println(String.format("%" + spaceSize + "s %x", "\tct1AddressRd",
				state.ct1RdAddress));
				
		System.out.println(String.format("%" + spaceSize + "s %08x", "\tct1Branchtarget",
				state.ct1Branchtarget));
		System.out.println(
				String.format("%" + spaceSize + "s %x", "\tct1Jump", state.ct1Jump));
		System.out.println(
				String.format("%" + spaceSize + "s %x", "\tct1Branch", state.ct1Branch));
		System.out.println(String.format("%" + spaceSize + "s %08x", "\tct1JumpTarget",
				state.ct1JumpTarget));
		System.out.println(
				String.format("%" + spaceSize + "s %x", "\tct1Pcsrc", state.ct1Pcsrc));
		System.out.println(String.format("%" + spaceSize + "s %x", "\tct1ForwardX",
				state.ct1Forwardx));
		System.out.println(String.format("%" + spaceSize + "s %x", "\tct1ForwardY",
				state.ct1Forwardy));
				
		System.out.println(String.format("%" + spaceSize + "s %08x",
				"\tDataImmediateSignExtended", state.dataImmediateSignextended));
		System.out.println(
				String.format("%" + spaceSize + "s %08x", "\tDataRs", state.dataRs));
		System.out.println(
				String.format("%" + spaceSize + "s %08x", "\tDataRt", state.dataRt));
		System.out.println(String.format("%" + spaceSize + "s %08x", "\tDataX (ALU0)*",
				state.dataX)); // + rs_forwarded
		System.out.println(
				String.format("%" + spaceSize + "s %08x", "\tDataEffY*", state.dataEffY)); // +
																							// rt_forwarded
		System.out.println(String.format("%" + spaceSize + "s %08x", "\tDataY (ALU1)*",
				state.dataY));
		
		System.out.println(String.format("%" + spaceSize + "s %08x", "\tInternalAluOut",
				state.internalAluOut));
		System.out.println();
	}
	
	@Override
	public void printNextVariables()
	{
		int spaceSize = -35;
		
		System.out.println("EX next vars");
		System.out.println(String.format("%" + spaceSize + "s %08x %s", "\tInstruction",
				state.nextInstruction,
				InstructionExtractor.format(state.nextInstruction)));
				
		String formattedInstructionAddress = ((state.currentInstructionAddress == -1)
				? "--------" : String.format("%08x", state.nextInstructionAddress));
		System.out.println(String.format("%" + spaceSize + "s %s", "\tInstructionAddress",
				formattedInstructionAddress));
				
		System.out.println(String.format("%" + spaceSize + "s %x",
				"\tNextForwardCt1MemToReg", state.nextForwardCt1Memtoreg));
		System.out.println(String.format("%" + spaceSize + "s %x",
				"\tNextForwardCt1Regwrite", state.nextForwardCt1Regwrite));
		System.out.println(String.format("%" + spaceSize + "s %x",
				"\tNextForwardCt1Memwrite", state.nextForwardCt1Memwrite));
		System.out.println(String.format("%" + spaceSize + "s %x",
				"\tNextForwardCt1Memread", state.nextForwardCt1Memread));
		System.out.println(String.format("%" + spaceSize + "s %08x",
				"\tForwardCt1LinkAddress", state.nextForwardCt1LinkAddress));
		System.out.println(String.format("%" + spaceSize + "s %x", "\tNextForwardCt1Jal",
				state.nextForwardCt1Jal));
				
		System.out.println(String.format("%" + spaceSize + "s %x", "\tnextCt1AluSrc",
				state.nextCt1AluSrc));
		System.out.println(String.format("%" + spaceSize + "s %08x", "\tnextCt1AluOp",
				state.nextCt1AluOp));
		System.out.println(String.format("%" + spaceSize + "s %x", "\tnextCt1RegDst",
				state.nextCt1Regdest));
		System.out.println(String.format("%" + spaceSize + "s %x", "\tnextCt1AddressRt",
				state.nextCt1RtAddress));
		System.out.println(String.format("%" + spaceSize + "s %x", "\tnextCt1AddressRd",
				state.nextCt1RdAddress));
				
		System.out.println(String.format("%" + spaceSize + "s %08x",
				"\tnextCt1Branchtarget", state.nextCt1BranchTarget));
		System.out.println(String.format("%" + spaceSize + "s %x", "\tnextCt1Jump",
				state.nextCt1Jump));
		System.out.println(String.format("%" + spaceSize + "s %x", "\tnextCt1Branch",
				state.nextCt1Branch));
				
		System.out.println(String.format("%" + spaceSize + "s %08x",
				"\nextDataImmediateSignExtended", state.nextDataImmediateSignExtended));
		System.out.println(String.format("%" + spaceSize + "s %08x", "\nextDataRs",
				state.nextDataRs));
		System.out.println(String.format("%" + spaceSize + "s %08x", "\nextDataRt",
				state.nextDataRt));
		System.out.println();
	}
	
	@Override
	public String printInstruction()
	{
		String formattedInstructionAddress = (state.currentInstructionAddress == -1
				|| state.bubble) ? "--------"
						: String.format("08x", state.currentInstructionAddress);
						
		// TODO add MIPSInstr format like ability
		String instruction = String.format("%s %s %s %08x %s", "Execute:",
				formattedInstructionAddress, "Instruction:", state.currentInstruction,
				" : " + InstructionExtractor.format(state.currentInstruction));
				
		return instruction;
	}
	
	@Override
	public void reset()
	{
		// TODO Auto-generated method stub
		
	}
	
	public class ExecuteEventHandler
	{
		private ExecuteEventHandler()
		{
		
		}
		
		public void instructionDecodeCompletionEvent(InstructionDecodeCompletion event)
		{
			ExecuteStageState postState = event.getPostState();
			
			if (event.willClearLogic())
			{
				postState.nextForwardCt1Memtoreg = 0;
				postState.nextForwardCt1Regwrite = 0;
				postState.nextForwardCt1Memwrite = 0;
				postState.nextForwardCt1Memread = 0;
				postState.nextForwardCt1Jal = 0;
				postState.nextCt1AluSrc = 0;
				postState.nextCt1Regdest = 0;
				postState.nextCt1Jump = 0;
				postState.nextCt1Branch = 0;
			}
		}
		
		public void stateRequested(ExecuteStageStateRequest event)
		{
			bus.post(new ExecuteStageStateResponse(state.clone()));
		}
	}
	
}
