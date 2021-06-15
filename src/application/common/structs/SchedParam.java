package application.common.structs;

import application.common.enums.EPolicy;
import application.common.enums.EPreemptMethod;

public class SchedParam 
{
	EPolicy      policy;
	EPreemptMethod method;
	public int	tick;
	
	final static EPolicy  	  DEFAULT_POLICY = EPolicy.EDF;
	final static EPreemptMethod DEFAULT_METHOD = EPreemptMethod.Non_Preemptive;
	final static public int	  DEFAULT_TICK = 20;
	
	
	public SchedParam(EPolicy p, EPreemptMethod m, int t) 
	{
		policy = p;
		method = m;
		tick   = t;
	}
	
	public static SchedParam defaultParams() 
	{
		return new SchedParam(DEFAULT_POLICY, DEFAULT_METHOD, DEFAULT_TICK);
	}
	
	public EPolicy policy() 
	{
		return policy;
	}
	
	public void setPolicy(EPolicy p) 
	{
		policy = p;		
	}
	
	public int tick () 
	{
		return tick;
	}
	
	public EPreemptMethod method() 
	{
		return method;
	}
}