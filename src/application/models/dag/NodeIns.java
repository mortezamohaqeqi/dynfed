package application.models.dag;

import java.util.LinkedList;

public class NodeIns  implements Comparable<NodeIns>
{
	int release;
	int wcet;
	int last_start;  // last start time
	LinkedList<NodeIns> prevs;
	int finish = 0;
	int deadline;
	String name;
	
	public NodeIns(int r, int c, int l, String nm, int d) 
	{
		release    = r;
		wcet       = c;
		last_start = l;
		name       = nm;
		deadline   = d;
		prevs      = new LinkedList<NodeIns>(); 
	}

	public void addPred(NodeIns p) 
	{
		prevs.add(p);		
	}

	public int release() 
	{
		return release;
	}

	public int last_start() 
	{
		return last_start;
	}

	public int wcet() 
	{
		return wcet;
	}
	
	// earliest time it can start
	public int earliest_start() 
	{
		int start = release;
		for (NodeIns p : prevs)
		{
			if (p.finish == 0)
			{
				System.out.println("Error: previouse job is not scheduled!");
				return -1;
			}
			start = Math.max(start, p.finish);
		}
		
		return start;
	}
	
	public String name()
	{
		return name;
	}

	public void setFinish(int f) 
	{
		finish = f;		
	}

	@Override
	public int compareTo(NodeIns other) 
	{
		if (deadline < other.deadline)
			return -1;
		else if (deadline == other.deadline)
		{
			if (last_start < other.last_start)
				return -1;
			else if (last_start == other.last_start)
				return 0;
			else
				return 1;
		}
		else
			return 1;
	}
}
