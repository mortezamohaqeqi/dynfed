package application.models.dag;

import application.hwmodel.enums.EResourceType;
import application.util.Util;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Set;

public class Task 
{
	static int id_counter = 1;
	protected int id;  // for debugging purpose
	int    period;
	int    criticalPath;
	String name;
	Hashtable<Node, Integer> criticalWCETs; // sum of WCETs from a node through its critical path
	LinkedList<Node> BFS;
	private boolean uptodate = false;
	
	protected LinkedList<Node> nodes;
	
	public Task(int p, int id_)
	{
		id            = id_;
		period        = p;
		name          = "" + id_;
		criticalPath  = 0;
		nodes         = new LinkedList<Node>();
		BFS           = new LinkedList<Node>();
		criticalWCETs = new Hashtable<Node, Integer>();
	}
	
	public Task(int id_)
	{
		this(0 /* default period */, id_);
	}
	
	public Task(int p, String namn)
	{
		id            = getId(); 
		period        = p;
		name          = namn;
		criticalPath  = 0;
		nodes         = new LinkedList<Node>();
		BFS           = new LinkedList<Node>();
		criticalWCETs = new Hashtable<Node, Integer>();
	}

	public void addNode(Node n)
	{
		nodes.add(n);
		uptodate = false;
	}
	
	public int period()
	{
		return period;
	}
	
	public int volume()
	{
		int vol = 0;
		for (Node n : nodes)
		//for (int i = 0; i < nodes.size(); i++)
		{
			vol += n.wcet(); // nodes.get(i).wcet();
		}
		return vol;
	}

	public double volume(EResourceType resource) 
	{
		int vol = 0;
		for (int i = 0; i < nodes.size(); i++)
		{
			vol += Math.max(0, nodes.get(i).wcet(resource));
		}
		return vol;
	}

	// Returns the longest aggregated WCET in the longest path from n to a leaf. 
	public int criticalPath(Node n)
	{
		if (uptodate == false)
			updateCache();
		
		return criticalWCETs.get(n);
	}
	
	public int criticalPath()
	{
		if (uptodate == false)
			updateCache();
		
		return criticalPath;
	}
		
	public LinkedList<Node> nextNodes(Node node)
	{
		LinkedList<Node> nexts    = new LinkedList<Node>();
		for (Node n : nodes)
			if (n.predecessors.contains(node))
		    	nexts.add(n);
		
		return nexts;
	}	

	// Returns task set xml in TIMESPro format
	public static String toXML(Task[] tasks, String name)
	{
		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><taskSet name=\"" + name + "\">" +
	            "<contextclause/><globals></globals><procedure/>";
		
		for (Task t : tasks)
			xml += t.toXML();
		
		xml += "</taskSet>";
		return xml;
	}
	
	private String toXML() 
	{
		String name = name();
		String xml = "<task name=\"" + name + "\" priority=\"1\"><variables></variables>";
		
//		int i = 0;
//		for (Node n : nodes)
//		{
//			int x = 241 + 10 * i;
//			xml += "<jobType labelx=\"" + x + "\" labely=\"169\" x=\"" + x + "\" y=\"219\"><name>" + n.name() + "</name><WCET>1000</WCET><deadline>1000</deadline></jobType>";
//			xml += "<dependency><src>J0</src><dst>J0</dst><edge_index>3</edge_index><interRelease x=\"328\" y=\"189\">1</interRelease><guard>True</guard><code/><nails xs=\"319,318\" ys=\"179,262\"/></dependency></task>";
//				
//		}
//		
		int[] layers = new int[nodes.size()]; 
		for (Node n : nodes)
		{
			String job = n.absname();
			int l = layer(n);
			int x = 50 + l * 100; // random.nextInt(600);
			int y = 50 + layers[l-1]++ * 100; // random.nextInt(400);
			xml += "<jobType labelx=\"" + x + "\" labely=\"" + y + "\" x=\"" + x + "\" y=\"" + (y+30) + "\">";
			xml += "<name>" + job + "</name>" +
						"<WCET>" + n.wcet() + "</WCET>" +
						"<deadline>" + period + "</deadline></jobType>";
		}
		for (Node n : nodes)
		{
			String dst = n.absname();
			for (Node p : n.predecessors)
			{
				String src = p.absname();
				xml +=  "<dependency><src>" + src + "</src><dst>" + dst + "</dst>" +
						"<edge_index>3</edge_index><interRelease x=\"328\" y=\"189\">1</interRelease><guard>True</guard><code/>" +
								"</dependency>";
			}
		}
		xml += "</task>";		

		return xml;
	}
	
	public String name() 
	{
		return name;
	}

	// returns all nodes in a BFS order
	public LinkedList<Node> BFS()
	{		
		if (uptodate == false)
			updateCache();
		
		return BFS;		
	}

	public void addMemoryNode(int memTime) 
	{
		Node node = Node.MemoryNode(memTime, name() + "_" + 0); 
		for (Node n : nodes)
			if (n.predecessors.isEmpty())
				n.predecessors.add(node);
			
		addNode(node);		
	}
	
	public int id()
	{
		return id;
	}

	public LinkedList<Node> nodes() 
	{
		return nodes;
	}
	
	public void getNodes(LinkedList<Node> list) 
	{
		list.addAll(nodes);
	}
	
	public int nnodes() 
	{
		return nodes.size();
	}

	//===================================================================
	//Private functions
	private void updateCache() 
	{
		updateCriticalPaths();
		updateBFS();
		uptodate = true;		
	}
	
	private void updateBFS() 
	{
		LinkedList<Node> copy = new LinkedList<Node>(nodes);
		LinkedList<Node> res  = new LinkedList<Node>();
		
		while (copy.size() > 0)
		{
			LinkedList<Node> roots = new LinkedList<Node>();
			
			for (Node n : copy)
			{
				if (allPrevsAdded(res, n))
				{
					roots.add(n);
				}
			}
			for (Node n : roots)
			{
				 res.add(n);
				 copy.remove(n);			
			}
		}
		Util.assert_(res.size() == nodes.size(), "BFS: not all nodes traversed.");
		BFS = res;
	}

	private void updateCriticalPaths()
	{
		criticalWCETs.clear();
		
		for (Node n : nodes)
			criticalWCETs.put(n, n.wcet());
		
		Set<Node> layer = leaves();		
		while (true)
		{
			Set<Node> prevLayer = new HashSet<Node>();
			for (Node n : layer)
			{
				for (Node p : n.predecessors)
				{
					criticalWCETs.put(p, Math.max(criticalWCETs.get(p),	criticalWCETs.get(n) + p.wcet()));
					prevLayer.add(p);
				}					
			}
			if (prevLayer.isEmpty())
				break;
			layer = prevLayer;
		}
		criticalPath = Math.max(0, Util.max(criticalWCETs.values()));
	}
	
	//  returns all nodes in layer l
	// source layer = 1
	public LinkedList<Node> layer(int l)
	{
		LinkedList<Node> res = new LinkedList<Node>();
		for (Node n : nodes)
			if (layer(n) == l)
				res.add(n);
		
		return res;
	}
	
	public int layer(Node n)
	{
		if (n.predecessors.isEmpty())
			return 1;
		
		int l = 0;
		for (Node p : n.predecessors)
			l = Math.max(l, layer(p));

		return 1 + l;
	}
	
	private boolean allPrevsAdded(LinkedList<Node> list, Node n) 
	{
		for (Node p : n.predecessors)
			if (list.contains(p) == false)
				return false;
		
		return true;
	}
	
	public LinkedList<Node> roots() 
	{
		LinkedList<Node> res = new LinkedList<Node>();
		for (Node n : nodes)
		{
			if (n.predecessors == null)
				res.add(n);
			else if (n.predecessors.size() == 0)
				res.add(n);
		}
				
		return res;		
	}

	/* public Set<Node> leaves()
	{
		Set<Node> res = new HashSet<Node>();
		for (Node n : nodes)
			if (isLeaf(n))
				res.add(n);
		return res;
	}
	
	private boolean isLeaf(Node n)
	{
		for (Node next : nodes)
			if (next.predecessors.contains(n))
				return false;

		return true;
	}
	*/
	
	public Set<Node> leaves()
	{
		Set<Node> res = new HashSet<Node>(nodes);
		for (Node n : nodes)
			for (Node p : n.predecessors)
				res.remove(p);

		return res;
	}
	
	public Task clone()
	{
		Task copy = new Task(period, id);
		for (Node n : nodes)
			copy.addNode(n);
		return copy;
	}
	
	public void setDirty() 
	{
		uptodate = false;
	}

	public void updateNode(String name2, int wcet, int nodeId) 
	{
		for (Node n : nodes) {
			if (n.id() == nodeId) {
				if (n.wcet() != wcet)
					setDirty();		
				n.setWcet(wcet);
				n.setName(name2);
				break;
			}
		}
	}

	public void addEdge(int sourceId, int targetId) {
		Node source = null;
		Node target = null;
		for (Node n : nodes)
		{
			if (n.id() == sourceId)
				source = n;
			else if (n.id() == targetId)
				target = n;
			
			if (source != null && target != null)
			{
				target.addPred(source);
				setDirty();	
				return;
			}
		}		
	}

	public void addEdge(Node source, Node target) 
	{
		target.predecessors.add(source);
		setDirty();
	}

	public void removeEdge(int source, int target) {
		for (Node n : nodes)
			if (n.id() == target)
			{
				n.removePred(source);
				setDirty();	
				return;
			}	
	}
	
	public void removeEdge(Node source, Node target) 
	{
		target.predecessors.remove(source);
		setDirty();
	}

	public void removeNode(int nodeId) {
		setDirty();
		for (Node n : nodes)
			n.removePred(nodeId);
		
		for (Node n : nodes)
			if (n.id() == nodeId)
			{
				nodes.remove(n);
				return;
			}
	}

	public void setPeriod(int prid) 
	{
		if (period != prid)
		{
			setDirty();
			period = prid;
		}		
	}

	public void scaleWCETs(double scale) 
	{
		setDirty();
		for (Node n : nodes)
		{
			int c = (int) Math.round(n.wcet() * scale);
			n.setWcet(Math.max(c, 1));
		}
	}
	
	public static String[] names(Task[] tasks) {
		String[] names = new String[tasks.length];
		for (int i = 0; i < tasks.length; i++)
			names[i] = tasks[i].name();
		return names;
	}
	
	
	private static synchronized int getId() 
	{
		id_counter++;
		return id_counter - 1;
	}
}
