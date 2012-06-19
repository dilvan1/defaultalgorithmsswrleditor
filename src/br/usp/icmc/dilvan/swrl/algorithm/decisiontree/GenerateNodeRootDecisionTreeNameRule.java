package br.usp.icmc.dilvan.swrl.algorithm.decisiontree;

import java.util.ArrayList;
import java.util.List;

import br.usp.icmc.dilvan.swrl.algorithm.decisiontree.rulename.Aresta;
import br.usp.icmc.dilvan.swrl.algorithm.decisiontree.rulename.Kruskal;
import br.usp.icmc.dilvan.swrlEditor.client.rpc.swrleditor.RuleSet;
import br.usp.icmc.dilvan.swrlEditor.client.rpc.swrleditor.decisiontree.NodeDecisionTree;
import br.usp.icmc.dilvan.swrlEditor.client.rpc.swrleditor.decisiontree.NodeDecisionTreeRanking;
import br.usp.icmc.dilvan.swrlEditor.client.rpc.swrleditor.decisiontree.NodeDecisionTree.ATOM_TYPE;
import br.usp.icmc.dilvan.swrlEditor.client.rpc.swrleditor.rule.Rule;
import br.usp.icmc.dilvan.swrlEditor.server.swrleditor.decisiontree.GenerateNodeRootDecisionTree;

import edu.stanford.smi.protegex.owl.swrl.model.SWRLFactory;


public class GenerateNodeRootDecisionTreeNameRule implements GenerateNodeRootDecisionTree {
	private RuleSet rules;

	
	private NodeDecisionTreeRanking rootNode;
	
	private List<List<String>> rulesRelated;
	
	
	public GenerateNodeRootDecisionTreeNameRule() {
		super();

		rootNode = new NodeDecisionTreeRanking(null,"");
		rootNode.setValue(NodeDecisionTreeRanking.ROOT_VALUE);
		rootNode.setAtomType(ATOM_TYPE.ROOT);
		
		rulesRelated = new ArrayList<List<String>>();
		
	}

	@Override
	public String getAlgorithmName() {
		return "Arvore geradora maxima";
	}

	@Override
	public void setRuleSet(RuleSet rules) {
		this.rules = rules;
	}

	@Override
	public void run(){
		
		Kruskal k = new Kruskal(rules.size());

		int numeroAtomosIguais;

		for (int i = 0; i < rules.size(); i++){
			rulesRelated.add(new ArrayList<String>());
			
			for (int j = 0; j < rules.size(); j++){
				if (i != j){
					numeroAtomosIguais = numeroAtomosIguais(rules.get(i), rules.get(j));
					if (numeroAtomosIguais > 0){
						
						//if (numeroAtomosIguais > 4)
						rulesRelated.get(i).add("["+numeroAtomosIguais+"] "+rules.get(j).getNameRule());
						
						k.adicionaAresta(i, j, 20-numeroAtomosIguais);
						//k.adicionaAresta(j, i, numeroAtomosIguais);
					}


				}

			}
		}

		k.executaKruskal();
		//k.imprimaArestas();
		List<Aresta> arestas = k.getArestas();
		
		
		Rule rulefrom, ruleto;
		for (Aresta a: arestas){
			rulefrom = rules.get(a.from);
			ruleto = rules.get(a.to);
			
			System.out.println("Nodes: (" + a.from +"-"+rulefrom.getNameRule()+ ", " + a.to +"-"+ruleto.getNameRule()+ ") with cost: " + a.cost);
		}
			
		while (!arestas.isEmpty()){
			Aresta a = arestas.get(0);
			Rule rule = rules.get(a.from);
			
			NodeDecisionTreeRanking newNode = new NodeDecisionTreeRanking(rootNode, rule.getFormatedRuleID());
			newNode.setValue(rule.getNameRule());
			newNode.setAtomType(ATOM_TYPE.CONSEQUENT);
			newNode.setRuleName(rule.getNameRule());
			newNode.setRulesRelated(rulesRelated.get(a.from));
			rootNode.addChildNodes(newNode);
			
			montaTreeNodo(a.from, newNode, arestas);
		}
		
		System.out.println("Arestas vazio: "+arestas.isEmpty());
	}
	
	private void montaTreeNodo(int from, NodeDecisionTreeRanking parentNode, List<Aresta> arestas){
		
		Rule rule;
		Aresta a;
		NodeDecisionTreeRanking newNode;
		for (int i = 0; i < arestas.size(); i++){
			a = arestas.get(i);
			a.cost = 20 - a.cost;
			if (a.from == from){
				rule = rules.get(a.to);
				newNode = new NodeDecisionTreeRanking(parentNode, rule.getFormatedRuleID());
				newNode.setValue("["+a.cost+"] "+rule.getNameRule());
				newNode.setRuleName(rule.getNameRule());
				newNode.setAtomType(ATOM_TYPE.CONSEQUENT);
				newNode.setRulesRelated(rulesRelated.get(a.to));
				parentNode.addChildNodes(newNode);

				a = arestas.remove(i);
				montaTreeNodo(a.to, newNode, arestas);
				
			}else if (a.to == from){
				rule = rules.get(a.from);
				newNode = new NodeDecisionTreeRanking(parentNode, rule.getFormatedRuleID());
				newNode.setValue("["+a.cost+"] "+rule.getNameRule());
				newNode.setRuleName(rule.getNameRule());
				newNode.setAtomType(ATOM_TYPE.CONSEQUENT);
				newNode.setRulesRelated(rulesRelated.get(a.from));
				parentNode.addChildNodes(newNode);

				a = arestas.remove(i);
				montaTreeNodo(a.from, newNode, arestas);
			}
		}
		
		
		
	}

	private int numeroAtomosIguais(Rule a, Rule b){
		int cont = 0;
		
		for (int i = 0; i < a.getNumAtoms(); i++)
			for (int j = 0; j < b.getNumAtoms(); j++)
				if (a.getAtoms().get(i).getAtomID().equals(b.getAtoms().get(j).getAtomID())){
					cont++;
				}

		return cont;
	}


	@Override
	public void setSWRLFactory(SWRLFactory factory){

	}



	@Override
	public NodeDecisionTree getRootNode() {
		return rootNode;
	}

}

