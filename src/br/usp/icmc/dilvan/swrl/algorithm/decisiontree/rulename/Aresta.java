package br.usp.icmc.dilvan.swrl.algorithm.decisiontree.rulename;

import java.util.Comparator;


public class Aresta implements Comparator<Object>{
			private int from, to, cost;
			
			public Aresta() {}
			
			public Aresta(int f, int t, int c) {
				from = f; 
				to = t; 
				cost = c;
			}
			
			public int compare(Object o1, Object o2) {
				
				int cost1 = ((Aresta) o1).cost;
				int cost2 = ((Aresta) o2).cost;
				int from1 = ((Aresta) o1).from;
				int from2 = ((Aresta) o2).from;
				int to1   = ((Aresta) o1).to;
				int to2   = ((Aresta) o2).to;

				if (cost2<cost1)
					return(-1);
				else if (cost1==cost2 && from1==from2 && to1==to2)
					return(0);
				else if (cost1==cost2)
					return(-1);
				else if (cost2>cost1)
					return(1); 
				else
					return(0);


			}
			public boolean equals(Object obj) {
				Aresta e = (Aresta) obj;
				return (cost==e.cost && from==e.from && to==e.to);
			}

			public int getFrom() {
				return from;
			}

			public void setFrom(int from) {
				this.from = from;
			}

			public int getTo() {
				return to;
			}

			public void setTo(int to) {
				this.to = to;
			}

			public int getCost() {
				return cost;
			}

			public void setCost(int cost) {
				this.cost = cost;
			}
}
