package application.models.dag.randomgen.randomperiods;

import java.util.Random;

import application.common.enums.taskgen.EAppDomain;
import application.common.enums.taskgen.ENumOfTasksPolicy;
import application.common.structs.TasksParam;
import application.models.dag.Task;

public abstract class AbsPeriodAssigner 
{
	protected int[] periodSet;
	
	public abstract void assignPeriods(Task[] tasks, double U, Random randoms);
	
	public AbsPeriodAssigner(int[] periodValues)
	{
		periodSet = periodValues;
	}
	
	public void assignPeriod(Task t, Random rng)
	{
		int ind = rng.nextInt(periodSet.length);
		t.setPeriod(periodSet[ind]);
	}
		
	protected double util(Task task) 
	{
		return (double)task.volume() / task.period();
	}
	
	
	// Factory method:
	public static AbsPeriodAssigner createInstance(TasksParam tasks) 
	{	
		ENumOfTasksPolicy numOfTasks = tasks.numOfTasks;
		int periods[] = tasks.periods();
		
		if (numOfTasks == ENumOfTasksPolicy.FIXED) 
		{
			if (tasks.period_src == EAppDomain.RELAXED)
				return new UUniFastPeriods(periods);
			else
				return new UUniFastDiscretePeriods(periods);
		}
		
		else if (numOfTasks == ENumOfTasksPolicy.UTILIZATION_DEPENDENT)
			return new UniformPeriods(periods);
		
		return null;
	}	
}
