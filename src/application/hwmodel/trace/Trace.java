package application.hwmodel.trace;

import java.util.LinkedList;

import application.util.Util;
import application.models.dag.Node;
import application.models.dag.TaskIns;

public class Trace 
{
	private String resourceName;
	private LinkedList<Execution>[] trace;
	private boolean shifted = false; // for test purpose
	
	public Trace(String rsrcName, int cores) 
	{
		trace = createTrace(cores);
		resourceName = rsrcName;
	}
	
	public void add(int core, int start, int dt, Node n) 
	{
		addExec(core, start, dt, n, EXEC_TYPE.Execution);
	}
	
	public void addOH(int core, int start, int dt, Node n) 
	{		
		addExec(core, start, dt, n, EXEC_TYPE.Overhead);
	}
	
	public void addCommOH(int core, int start, int dt, Node n) 
	{
		addExec(core, start, dt, n, EXEC_TYPE.CommOverhead);
	}
	
	private void addExec(int core, int start, int dt, Node n, EXEC_TYPE type) 
	{
		Util.assert_(dt > 0, "Zero execution.");
		
		Execution ex = new Execution(core, start, dt, n, type);
		if (trace[core].isEmpty())
		{	
			trace[core].add(ex);
			return;
		}
		Execution last = trace[core].getLast();
		
		// merge if the same job
		if (last.type == ex.type && last.node == ex.node && last.endingTime()==ex.start) 
			last.dur += ex.dur; 
		else
			trace[core].add(ex);
	}
	
	public void addDLMiss(Node j) 
	{
		int dl   = j.owner().absDeadline();
		int core = missedNodeCore(j);
		trace[core].add(new Execution(core, dl, 0, j, EXEC_TYPE.DLMiss));
	}
		
	public int cores()
	{
		return trace.length;
	}
	
	public int missedNodeCore(Node n)
	{
		for (LinkedList<Execution> tr : trace)
		{
			if (tr.isEmpty())
				continue;
			if (jobEq(tr.getLast().node, n))
				return tr.getLast().core;
		}
		return lastTaskCore(n);
	}	
	

	public boolean jobEq(Node n1, Node n2) 
	{
		 if (n1.owner() == n2.owner())
			 if (n1.name().equalsIgnoreCase(n2.name()))
				 return true;
		 return false;
	}

	private int lastTaskCore(Node node) 
	{
		Execution last = null;
		for (LinkedList<Execution> tr : trace)
		{
			Execution e = lastExec(tr, node.owner());
			if (last == null)
				last = e;
			else if (e != null)
			{
				if (e.endingTime() > last.endingTime())
					last = e;
			}
		}
		if (last != null)
			return last.core;
		
		return 0;	// default
	}

	private static Execution lastExec(LinkedList<Execution> trace, TaskIns task) 
	{
		Execution last = null;
		for (Execution ex : trace)
		{
			if (ex.node.owner() == task)
			{
				if (last == null)
					last = ex;
				else if (last.endingTime() < ex.endingTime())
					last = ex;
			}
		}
		return last;
	}

	public LinkedList<Execution> all() 
	{
		LinkedList<Execution> all = new LinkedList<Execution>();
		for (int c = 0; c < cores(); c++)
			for (Execution ex : trace[c])
				all.add(ex);
		
		return all;
	}

	public void print()
	{
		int cores = cores();
		for (int c = 0; c < cores; c++)
		{
			System.out.print("Core " + c + ": ");
			for (Execution ex : trace[c])
			{
				if (ex.type == EXEC_TYPE.Execution)
					System.out.print(ex.toStr());
			}
			System.out.println();
		}
		
		System.out.println("-------------------------------------");
		System.out.println("Deadline Misses: ");	
		for (int c = 0; c < cores; c++)
			for (Execution ex : trace[c])
				if (ex.type == EXEC_TYPE.DLMiss)
					System.out.println(ex.toStr());

		System.out.println("-------------------------------------");
	}	

	public String resourceName() 
	{		
		return resourceName;
	}

	
	public String toStr() 
	{		
		String str = "Schedule:\r\n";
		int cores = cores();
		for (int c = 0; c < cores; c++)
		{
			str += "Core " + c + ": ";
			for (Execution ex : trace[c])
			{
				if (ex.type == EXEC_TYPE.Execution)
					str += ex.toStr();
			}
			str += "\r\n";
		}
		
		str += "-------------------------------------\r\n";
		str += ("Deadline Misses: \r\n");
		for (int c = 0; c < cores; c++)
			for (Execution ex : trace[c])
				if (ex.type == EXEC_TYPE.DLMiss)
					str += ex.toStr() + "\r\n";

		str += "-------------------------------------\r\n";
		return str;
	}
	
	public String toStr(int core)
	{
		String str = "";
		for (Execution ex : trace[core])
		{
			if (ex.type == EXEC_TYPE.Execution)
				str += ex.toStr();
		}
		return str;
	}
	
	public String toStrDLMiss()
	{
		String str = "";
		for (int c = 0; c < cores(); c++)
			for (Execution ex : trace[c])
				if (ex.type == EXEC_TYPE.DLMiss)
					str += ex.toStr() + " on core " + (c+1) + "\r\n";
		return str;
	}
	
	public void shiftCores(int offset) 
	{
		if (offset == 0)
			return ;
		
		Util.assert_(shifted == false, "Trace is supposed to be shifted only once.");
		shifted = true;
		
		for (int c = 0; c < cores(); c++)
			for (Execution ex : trace[c])
				ex.core += offset;
	}
	
	private LinkedList<Execution>[] createTrace(int size)
	{
		LinkedList<Execution>[] arr = new LinkedList[size];
		for (int c = 0; c < size; c++)
			arr[c] = new LinkedList<Execution>();
		return arr;
	}
}