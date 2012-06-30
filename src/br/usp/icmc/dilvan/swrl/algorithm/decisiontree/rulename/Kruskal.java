package br.usp.icmc.dilvan.swrl.algorithm.decisiontree.rulename;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;


// Algoritmo de Kruskal, baseaddo em http://www.cs.columbia.edu/~gskc/Code/AdvancedInternetServices/MinimalSpanningTree/Kruskal.java
public class Kruskal {
	private int max_nodes = 0;
	@SuppressWarnings("rawtypes")
	private HashSet nodes[];               // Array of connected components
	private TreeSet<Aresta> allEdges;              // Priority queue of Edge objects
	private List<Aresta> allNewEdges;            // Edges in Minimal-Spanning Tree

	// Construtor
	public Kruskal(int max_nodes) {
		
		this.max_nodes = max_nodes;
		nodes = new HashSet[max_nodes];      // Create array for components
		allEdges = new TreeSet<Aresta>(new Aresta());  // Create empty priority queue
		allNewEdges = new ArrayList<Aresta>(); // Create vector for MST edges
	}
	public void adicionaAresta(int from,int to, int cost){

		allEdges.add(new Aresta(from, to, cost));  // Update priority queue
		if (nodes[from] == null) {
			// Create set of connect components [singleton] for this node
			nodes[from] = new HashSet(2*max_nodes);
			nodes[from].add(new Integer(from));
		}

		if (nodes[to] == null) {
			// Create set of connect components [singleton] for this node
			nodes[to] = new HashSet(2*max_nodes);
			nodes[to].add(new Integer(to));
		}
	}

	public void executaKruskal() {
		int size = allEdges.size();
		for (int i=0; i<size; i++) {
			Aresta curEdge = (Aresta) allEdges.first();
			if (allEdges.remove(curEdge)) {
				// successful removal from priority queue: allEdges

				if (nodesAreInDifferentSets(curEdge.getFrom(), curEdge.getTo())) {
					// System.out.println("Nodes are in different sets ...");
					HashSet src, dst;
					int dstHashSetIndex;

					if (nodes[curEdge.getFrom()].size() > nodes[curEdge.getTo()].size()) {
						// have to transfer all nodes including curEdge.to
						src = nodes[curEdge.getTo()];
						dst = nodes[dstHashSetIndex = curEdge.getFrom()];
					} else {
						// have to transfer all nodes including curEdge.from
						src = nodes[curEdge.getFrom()];
						dst = nodes[dstHashSetIndex = curEdge.getTo()];
					}

					Object srcArray[] = src.toArray();
					int transferSize = srcArray.length;
					for (int j=0; j<transferSize; j++) {
						// move each node from set: src into set: dst
						// and update appropriate index in array: nodes
						if (src.remove(srcArray[j])) {
							dst.add(srcArray[j]);
							nodes[((Integer) srcArray[j]).intValue()] = nodes[dstHashSetIndex];
						} else {
							// This is a serious problem
							System.out.println("Something wrong: set union");
							System.exit(1);
						}
					}

					allNewEdges.add(curEdge);
					// add new edge to MST edge vector
				} else {
					// System.out.println("Nodes are in the same set ... nothing to do here");
				}

			} else {
				// This is a serious problem
				System.out.println("TreeSet should have contained this element!!");
				System.exit(1);
			}
		}
	}

	private boolean nodesAreInDifferentSets(int a, int b) {
		return(!nodes[a].equals(nodes[b]));
	}
	
	public void imprimaArestas() {
		for (Aresta e: allNewEdges)
			System.out.println("-Nodes: (" + e.getFrom() + ", " + e.getTo() + ") with cost: " + e.getCost());
	}
	
	public List<Aresta> getArestas() {
		return allNewEdges;
	}

}
