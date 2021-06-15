/**
 * 
 */
package application.models.dag;

import java.util.Collection;
import java.util.LinkedList;

import application.common.Middleware;
import application.util.Timer;
import application.util.Util;

/**
 * @author M.
 *
 */
public class TaskIns extends Task 
{
	private int                      release_time;
	private boolean					 done = false;
	
	/**
	 * lateness = finish time - abs. deadline
	 */
	private int lateness = Integer.MAX_VALUE;
	private int  []remained;	
	private LinkedList<Node> []successors;
	private int  []remainedPreds;
	private Middleware       middleWare = null;
	
	// Constructor
	public TaskIns(Task structure, int release, Middleware mw)
	{
		super(structure.period, structure.id);
		this.nodes          = /*structure.nodes; //*/ Node.copy(structure.nodes);
		this.release_time   = release;
		this.middleWare     = mw; 
		
		// set deadlines
		remained      = new int[nodes.size()];
		remainedPreds = new int[nodes.size()];
		successors    = new LinkedList[nodes.size()];
		int idx = 0;
		for (Node n : nodes)
		{
			n.setOwner(this);
			n.setIndex(idx);
			remained[idx] = Integer.MAX_VALUE;
			successors[idx] = new LinkedList<Node>();
			idx++;
		}
		for (Node n : nodes)
			for (Node p : n.predecessors)
				successors[p.index()].add(n);
		
		for (Node n : nodes)
		{
			remainedPreds[n.index()] = n.predecessors.size();
			if (n.predecessors.size() == 0) // it is a source node
				releaseNode(n);
		}
	}
	
	private void releaseNode(Node n) 
	{
		if (middleWare != null)
			middleWare.nodeReleased(n);	
	}

	public int releaseTime()
	{
		return release_time;
	}
	
	// returns executed time
	public void nodeScheduled(Node n, int t)
	{	
		Util.assert_(n.singleVersion(), "Nodes running on multiple resources (e.g., CPU and GPU) not implemented");
		Util.assert_(t > 0, "Zero execution (it affects eligibleNodes())");
		Util.assert_(remained[n.index()] > 0, "Finished node is scheduled.");
			
		int rem = 0;		
		if (remained[n.index()] == Integer.MAX_VALUE) 
		{
			rem = n.wcet();
		}
		else 
		{
			rem = remained[n.index()];
		}
		remained[n.index()] = Math.max(0, rem - t);
		
		if (done(n))
			onNodeFinish(n);
	}
	
	public LinkedList<Node> unfinishedNodes()
	{
		LinkedList<Node> res = new LinkedList<Node>();
		if (done)
			return res;
			
		for (Node n : nodes)
		    if (remained[n.index()] > 0)
		    	res.add(n);
		
		return res;
	}	
	

	//SFIFO BFS eligible nodes.
	public LinkedList<Node> eligibleNodesBFS()
	{
		LinkedList<Node> eligs = new LinkedList<Node>();
		if (done())
			return eligs;
			
		for (Node n : BFS())
		{
			if (done(n))
				continue;
			
			if (isEligible(n))
		    	eligs.add(n);
			else
				break;
		}
		return eligs;
	}

	public boolean owns(Node node)
	{
		if (nodes.contains(node))
			return true;
		return false;
	}
	
	// return absolute deadline of this instance of task
	public int absDeadline()
	{
		return release_time + period;
	}	
	
	public boolean done() 
	{
		if (done)
			return true;
				
		for (Node n : nodes)
			if (done(n) == false)
				return false;
		
		done = true;
		return true;
	}
	
	public boolean done(Node n) 
	{	
		return remained[n.index()] <= 0;
	}

	public int remained(Node node) 
	{
		if (remained[node.index()] != Integer.MAX_VALUE)
			return remained[node.index()];
			
		return node.wcet();
	}
	
	public boolean progressed() 
	{
		for (Node n : nodes)
			if (progressed(n))
				return true;
		return false;
	}
	
	public boolean progressed(Node n) 
	{
		if (remained[n.index()] == Integer.MAX_VALUE)
			return false;
		
		return true;
	}	

	public void setLateness(int late) 
	{
		lateness = late;
	}

	public int lateness() {
		return lateness;
	}
		
	public int responseTime() {
		int t = 0;
		for (Node j : nodes) {
			if (t < j.getFinishTime()) {
				t = j.getFinishTime();
			}
		}		
		return t;
	}
	
	/**** Private functions ***/
	private void onNodeFinish(Node n) 
	{
		for (Node s : successors[n.index()])
		{
			remainedPreds[s.index()]--;
			Util.assert_(remainedPreds[s.index()] >= 0, "Too many predecessors executed!");
			if (allPredDone(s))
				releaseNode(s);
		}
	}
	
	// Returns true if all predecessors of 'node' are scheduled.
	private boolean isEligible(Node node)
	{		
		if (done(node))
			return false;
		
		return allPredDone(node);
	}
	
	private boolean allPredDone(Node node)
	{		
		return remainedPreds[node.index()] == 0;
	}

}
