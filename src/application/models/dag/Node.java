package application.models.dag;

import application.hwmodel.enums.EResourceType;
import application.util.Util;

import java.util.EnumMap;
import java.util.Hashtable;
import java.util.LinkedList;


public class Node 
{
	LinkedList<Node>    predecessors;
	private	String	    name;	// for debug
	private	TaskIns     owner; // this is temporal variable only for simulation purposes
	private	int         lastCore; // Index of the last core executed this node. Core index starts at 0.
	private	final EnumMap<EResourceType, Integer> WCET;
	private	int 		indexInTask = -1;	// used for indexing inside task
	
	private int 		finishTime = -1; //node finish time of PreReservedScheduler 
	private final int 	id;
	private static int  id_counter = 1;
	
	// Constructor
	public Node(EnumMap<EResourceType, Integer> wcet, String name)
	{
		this.WCET    = wcet;
		this.name    = name;
		this.id      = id_counter++;
		predecessors = new LinkedList<Node>();
	}
	
	public void addPred(Node n)
	{
		predecessors.add(n);
	}
	
	public String name()
	{
		return name;
	}
	
	public String absname()
	{
		String s = name;
		// s = "J" + s.substring(s.indexOf("_"));
		return s;		
	}
	
	public TaskIns owner()
	{
		return owner;		
	}
	
	public int wcet(EResourceType resource)
	{
		//if (resource == EResourceType.CPU)
		//	return cpuWCET;
			
		if (WCET.containsKey(resource))
			return WCET.get(resource);
		return -1;
	}

	// used when only one wcet exists (CPU or memory or ...)
	public int wcet()
	{
		Util.assert_(singleVersion(), "Multiversion nodes not implemented!");
		return WCET.values().iterator().next();
	}
	
	public void setWcet(int c) 
	{
		WCET.put(EResourceType.CPU, c);
	}

	public LinkedList<Node> parents() 
	{
		return predecessors;
	}
	
	public String toStr() 
	{
		return "Period = " + owner.period() + ", node = " + name + 
				" CPU wcet = " + wcet(EResourceType.CPU) + ", cp = " + owner.criticalPath(this) + ", parents = " + parents().size();
	}

	public void setOwner(TaskIns task) 
	{
		owner = task;
	}

	public int period() 
	{
		return owner.period();
	}

	public boolean conforms(EResourceType resrc) 
	{
		if (WCET.containsKey(resrc))
			return true;
		else
			return false;
	}

	public int remained() 
	{
		return owner().remained(this);
	}

	public static Node CPUNode(int wcet, String name)
	{
		return new Node(wcets(wcet, EResourceType.CPU), name);
	}

	public static Node MemoryNode(int wcet, String name)
	{
		return new Node(wcets(wcet, EResourceType.Memory), name);
	}
	
	private	static EnumMap<EResourceType, Integer> wcets(int wcet, EResourceType type)
	{
		EnumMap<EResourceType, Integer> wcets = new EnumMap<EResourceType, Integer>(EResourceType.class);
		wcets.put(type, wcet);
		return wcets;
	}

	public boolean singleVersion() 
	{
		return WCET.size() == 1;
	}
	
	public boolean memoryNode() 
	{
		return WCET.containsKey(EResourceType.Memory);
	}
	
	public int lastCore() 
	{
		return lastCore;
	}
	
	public void setLastCore(int c) 
	{
		lastCore = c;
	}

	public static LinkedList<Node> copy(LinkedList<Node> nodes) 
	{
		Hashtable<Node, Node> map = new Hashtable<Node, Node>(nodes.size() * 2); 
		LinkedList<Node> copy     = new LinkedList<Node>();
		for (Node n : nodes)
		{
			Node cp = n.cloneMe();
			copy.add(cp);
			map.put(n, cp);
		}
		
		// Reconstruct edges
		for (Node n : nodes)
		{
			Node cp = map.get(n);
			for (Node pred : n.predecessors)
				cp.addPred(map.get(pred));
		}
		return copy;
	}
	
	public void setIndex(int id) 
	{
		indexInTask = id;
	}

	public int index() 
	{
		return indexInTask;
	}
	
	Node cloneMe() 
	{
		return new Node(WCET, name);
	}
	
	//used by PrereservedScheduler
	public void setFinishTime(int t) 
	{
		finishTime = t;
	}
	
	public int getFinishTime() 
	{
		return finishTime;
	}

	public int getMaxPredFinishTime() 
	{	
		int maxfinishtime = 0;
		if (parents() != null) {
			for (Node node :predecessors) {
				if (node.finishTime > maxfinishtime)
					maxfinishtime = node.finishTime;
			}
		}
		return maxfinishtime;
	}

	public void setName(String name2) 
	{
		name = name2;
	}

	public int id() {
		return id;
	}

	public void removePred(int source) {
		for (Node p : predecessors)
			if (p.id == source)
			{
				predecessors.remove(p);
				return;
			}
	}
}
