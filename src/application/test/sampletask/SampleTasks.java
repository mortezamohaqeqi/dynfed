package application.test.sampletask;

import application.models.dag.Node;
import application.models.dag.Task;

public class SampleTasks 
{
	public static Task task3node()
	{
		Task t = new Task(22, "T3N");
		
		Node n1 = Node.CPUNode(7, "n1");
		Node n2 = Node.CPUNode(15, "n2");
		Node n3 = Node.CPUNode(6, "n3");
		t.addNode(n1);
		t.addNode(n2);
		t.addNode(n3);
		t.addEdge(n1.id(), n2.id());
		t.addEdge(n1.id(), n3.id());
		
		return t;
	}
}
