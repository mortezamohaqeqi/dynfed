/*
 * There is a set of given period values. 
 * They are assigned to the tasks one by one.
 */
package application.models.dag.randomgen.randomperiods;

import java.util.Random;

import application.models.dag.Task;

public class UniformPeriods extends AbsPeriodAssigner
{
	public UniformPeriods(int[] periodValues) 
	{
		super(periodValues);
	}

	@Override
	public void assignPeriods(Task[] tasks, double U, Random random) 
	{
		int ind = 0;
		for (Task t : tasks)
			t.setPeriod(periodSet[ind++ % periodSet.length]);			
	}
}
