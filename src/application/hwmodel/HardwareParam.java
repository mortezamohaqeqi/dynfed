package application.hwmodel;

import application.hwmodel.enums.ETimeConstrant;

public class HardwareParam 
{
	public       int            cores;
	public final int            memories = 1;
	public final int            memTime;
	public final int            preemptOverhead;
	public final int            commOverhead;
	public final ETimeConstrant timeconstraint;
	
	public static final int            DEFAULT_CORES = 8;
	public static final int            DEFAULT_MEMTIME = 0;
	public static final int            DEFAULT_PREEMPT_OH = 0;
	public static final int            DEFAULT_COMM_OH = 0;
	public static final ETimeConstrant DEFAULT_CONSTRAINT = ETimeConstrant.Firm;
	
	public HardwareParam(int ncore, int memoryTime, int preemptTime, int commTime, ETimeConstrant constraint) 
	{
		cores           = ncore;
		memTime         = memoryTime;
		commOverhead    = commTime;
		timeconstraint  = constraint;
		preemptOverhead = preemptTime;
	}
	
	public static HardwareParam defaultHardware()
	{
		return new HardwareParam();
	}
	
	public static HardwareParam defaultHardware(int cores)
	{
		HardwareParam HP = new HardwareParam();
		HP.cores = cores;
		return HP;
	}
	
	private HardwareParam() 
	{
		cores           = DEFAULT_CORES;
		memTime         = DEFAULT_MEMTIME;
		preemptOverhead = DEFAULT_PREEMPT_OH;
		commOverhead    = DEFAULT_COMM_OH;
		timeconstraint  = DEFAULT_CONSTRAINT;		
	}	
}
