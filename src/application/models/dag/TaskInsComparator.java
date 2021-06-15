package application.models.dag;

import java.util.Comparator;

public class TaskInsComparator implements Comparator<TaskIns> {

	   @Override

	   public int compare(TaskIns t1, TaskIns t2) 
	   {
		   if (t1.releaseTime() != t2.releaseTime())
			   return -(t2.releaseTime() - t1.releaseTime());
		   else
			   return -(t2.period() - t1.period());
	  }
}