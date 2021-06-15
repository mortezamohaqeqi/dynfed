package application.common.structs;

public class ExpConf 
{
	public final boolean  saveTasks;
	public final boolean  analytical;
	public final int      extensivity;
	public final String   threads;	//	see ConfigInitializer::threadItems()
	
	public ExpConf(boolean analytic, boolean save, int extensive, String threds)
	{
		analytical  = analytic;
		saveTasks   = save;
		extensivity = extensive;
		threads     = threds;
	}	
}
