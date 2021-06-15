package application.hwmodel.trace;

import java.util.LinkedList;

import application.models.dag.Node;

public class Execution 
{
	public int  core;
	public int  start;
	public int  dur;
	public Node node;
	public EXEC_TYPE type;
	
	public Execution(int cor, int st, int len, Node nd, EXEC_TYPE tp) 
	{
		core  = cor;
		start = st;
		dur   = len;
		type  = tp;
		node  = nd;
	}
		
	public int endingTime() {
		return start + dur;
	}

	public void println() 
	{
		System.out.println("Exec: TYPE = " + type + ", START = " + start + 
				", dur = " + dur + ", core = " + core + ", text = " + node.name());		
	}

	public int graphicalEndTime() {	
		if(type == EXEC_TYPE.Arrival)
			return start;
		else
			return start + dur;
	}
	
	public String toStr() 
	{
		if (type == EXEC_TYPE.Execution)
			return "[" + start + ", " + (start+dur) + "]: " + node.name() + " - ";
		else // deadline miss
			return "@" + start + ": job " + node.name() + " (period = " + node.period() + ", WCET = " + node.wcet() + ")";
	}
	
	public String text()
	{
		return node.name();
	}

	public static LinkedList<Execution> start_points(LinkedList<Execution> trace) 
	{
		LinkedList<Execution> res = new LinkedList<Execution>();
		for (Execution ex : trace)
			if (ex.type == EXEC_TYPE.Execution) // || ex.type == EXEC_TYPE.Overhead)
				res.add(new Execution(ex.core, ex.start, 0, ex.node, EXEC_TYPE.Arrival));
		
		return res;
	}
}
