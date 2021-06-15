package application.models.dag.randomgen.randomperiods;

import java.util.Random;

import application.models.dag.Task;

public class UUniFastPeriods extends AbsPeriodAssigner
{
	public UUniFastPeriods(int[] periodValues) 
	{
		super(periodValues);
	}

	// uUniFast
	@Override
	public void assignPeriods(Task[] tasks, double U, Random random) 
	{
		int n = tasks.length;
		double sumU = U;
		for (int i = 1; i < tasks.length; i ++)
		{
			double nextSumU = sumU * Math.pow(random.nextDouble(), 1.0 / (n-i));			
			double Upart    = sumU - nextSumU;
			setPeriod(tasks[i-1], Upart, random);
			sumU = nextSumU;
		}		
		setPeriod(tasks[n-1], sumU, random);	
	}
	
	// T = round(C / U)
	protected void setPeriod(Task task, double u, Random random) 
	{
		double rawPeriod = (double)task.volume() / u;
		task.setPeriod((int)Math.round(rawPeriod));
	}
}



//				double period = (double)tasks[i-1].volume() / Upart;
//				period = logUniform(period);

//				if (tasks[i-1].criticalPath()/Upart > period)
//				{
//					nextSumU = sumU * Math.pow(rand(), 1.0 / (n-i));			
//					Upart = sumU - nextSumU;
//					period = (double)tasks[i-1].volume() / Upart;
//					period = logUniform(period);				
//				}