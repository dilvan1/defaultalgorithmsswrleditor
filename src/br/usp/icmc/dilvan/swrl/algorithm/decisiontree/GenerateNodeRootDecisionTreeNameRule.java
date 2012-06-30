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

//Classe responsável por criar o novo algoritmo para árvore geradora máxima
public class GenerateNodeRootDecisionTreeNameRule implements GenerateNodeRootDecisionTree {

	// A lista de regras SWRL
	private RuleSet rules;

	//Nó raiz da árvore gerada
	private NodeDecisionTreeRanking rootNode;

	//Armazena uma lista de regras relacionadas para cada regra
	// é utilizada para visualizar a grafo na árvore geradora máxima 
	private List<List<String>> rulesRelated;

	//Construtor da classe
	public GenerateNodeRootDecisionTreeNameRule() {
		super();

		// Cria o nó raiz
		rootNode = new NodeDecisionTreeRanking(null,"");
		rootNode.setValue(NodeDecisionTreeRanking.ROOT_VALUE);
		rootNode.setAtomType(ATOM_TYPE.ROOT);

		//cria a lista de nodo relacionados para o grafo
		rulesRelated = new ArrayList<List<String>>();
	}

	// Nome do algoritmo
	@Override
	public String getAlgorithmName() {
		return "Arvore geradora maxima";
	}

	//Seta o conjunto de regras
	@Override
	public void setRuleSet(RuleSet rules) {
		this.rules = rules;
	}


	//Executa a criação da árvore geradora máxima
	@Override
	public void run(){

		// instancia um objeto da classe Kruskal responsável por fazer a árvore geradora máxima
		Kruskal k = new Kruskal(rules.size());

		int numeroAtomosIguais;
		int sum = 0;
		int max = 0;
		int count = 0;


		//Lista temporária de relações entre todas a regras
		List<String> rulesA = new ArrayList<String>();
		List<String> rulesB = new ArrayList<String>();
		List<Integer> rulesAB = new ArrayList<Integer>();



		//Percorre o conjunto de regras, relacionado todas as regras entre elas
		//  esses FORs serão removidos após encontrar uma forma melhor do que a média
		for (int i = 0; i < rules.size(); i++){
			for (int j = i+1; j < rules.size(); j++){

				//atraves da função numeroAtomosIguais descobre o nível de igualdade das regras
				numeroAtomosIguais = numeroAtomosIguais(rules.get(i), rules.get(j));

				//Se o grau de igualdade for maior que zero, adiciona as regras a lista de regras relacionadas
				// para extrair as regras para o ManyEyes
				if (numeroAtomosIguais > 0){
					rulesA.add(rules.get(i).getNameRule());
					rulesB.add(rules.get(j).getNameRule());
					rulesAB.add(numeroAtomosIguais);

					sum += numeroAtomosIguais;
					count++;
					if (max < numeroAtomosIguais){
						max = numeroAtomosIguais;
					}
				}
			}
		}

		// Gera a média aritmética
		double media = (sum*1.0)/(count*1.0);
		System.out.println("Soma: " + sum + " Média: " + media + " Count "+ count + " Max:"+max);


		for (int i = 0; i < rulesA.size(); i++)
			if (rulesAB.get(i).intValue() > media) // teste para retirar só os dados acima da média
				System.out.println(rulesA.get(i)+ "\t"+ rulesB.get(i));// Exibe os dados para serem usados para montar os graficos no ManyEyes


		//Novamente percorre o conjunto de regras, relacionado todas as regras entre elas
		// foi mantido esses dois FORs porque os anteriores vão sumir 
		for (int i = 0; i < rules.size(); i++){
			rulesRelated.add(new ArrayList<String>());

			for (int j = i+1; j < rules.size(); j++){

				//atraves da função numeroAtomosIguais descobre o nível de igualdade das regras
				numeroAtomosIguais = numeroAtomosIguais(rules.get(i), rules.get(j));

				//Se o grau de igualdade for maior que zero, adiciona as regras a lista de regras relacionadas 
				if (numeroAtomosIguais > 0){

					//Adiciona a regra j a lista de regras da regra i
					rulesRelated.get(i).add("["+numeroAtomosIguais+"] "+rules.get(j).getNameRule());
					
					// adiciona uma nova aresta ao algorimto de Kruskal
					k.adicionaAresta(i, j, numeroAtomosIguais);
				}



			}
		}

		//Executa o algoritmo de árvore gerado máxima
		k.executaKruskal();
		
		//Pegar as arestas da execução do algoritmo
		List<Aresta> arestas = k.getArestas();

		//Executa até que a lista de arestas esteja vazia
		while (!arestas.isEmpty()){
			
			//Sempre pega a aresta Zero, pois essa é a de maior peso
			Aresta a = arestas.remove(0);
			
			//Recupera a regra da aresta de maior peso
			Rule rule = rules.get(a.getFrom());

			//Cria um novo nó na árvore que esteja ligado ao nó raiz da árvore
			NodeDecisionTreeRanking newNode = new NodeDecisionTreeRanking(rootNode, rule.getFormatedRuleID());
			newNode.setValue(rule.getNameRule());
			newNode.setAtomType(ATOM_TYPE.CONSEQUENT);
			newNode.setRuleName(rule.getNameRule());
			newNode.setRulesRelated(rulesRelated.get(a.getFrom()));
			rootNode.addChildNodes(newNode);

			// Chama o metodo que cria os filhos desse novo nó criado
			montaTreeNodo(a.getFrom(), newNode, arestas);
		}

	}

	//Monta todos os nodos filhos do parentNode
	private void montaTreeNodo(int parentFrom, NodeDecisionTreeRanking parentNode, List<Aresta> arestas){

		Rule rule;
		Aresta a;
		NodeDecisionTreeRanking newNode;
		
		//Percorre todas as arestas restantes, verificando quais arestas são ligadas ao parentFrom
		for (int i = 0; i < arestas.size(); i++){
			a = arestas.get(i);
			//Testa o From da aresta com parentFrom
			if (a.getFrom() == parentFrom){
				
				//Cria o nó filho
				rule = rules.get(a.getTo());
				newNode = new NodeDecisionTreeRanking(parentNode, rule.getFormatedRuleID());
				newNode.setValue("["+a.getCost()+"] "+rule.getNameRule());
				newNode.setRuleName(rule.getNameRule());
				newNode.setAtomType(ATOM_TYPE.CONSEQUENT);
				newNode.setRulesRelated(rulesRelated.get(a.getTo()));
				
				
				//Adiciona o nó filho criado ao parentNode
				parentNode.addChildNodes(newNode);

				//Remove ele da lista das arestas
				a = arestas.remove(i);
				
				//Chama recursivamente esse mesmo método passando o nó filho como parentNode
				montaTreeNodo(a.getTo(), newNode, arestas);

			}
			//Testa o To da aresta com parentFrom
			else if (a.getTo() == parentFrom){
				//Cria o nó filho
				rule = rules.get(a.getFrom());
				newNode = new NodeDecisionTreeRanking(parentNode, rule.getFormatedRuleID());
				newNode.setValue("["+a.getCost()+"] "+rule.getNameRule());
				newNode.setRuleName(rule.getNameRule());
				newNode.setAtomType(ATOM_TYPE.CONSEQUENT);
				newNode.setRulesRelated(rulesRelated.get(a.getFrom()));
				
				//Adiciona o nó filho criado ao parentNode
				parentNode.addChildNodes(newNode);

				//Remove ele da lista das arestas
				a = arestas.remove(i);
				
				//Chama recursivamente esse mesmo método passando o nó filho como parentNode
				montaTreeNodo(a.getFrom(), newNode, arestas);
			}
		}
	}

	// Método responsável por contar o número de átomos iguais.
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

	// Retorna o nodo raiz
	@Override
	public NodeDecisionTree getRootNode() {
		return rootNode;
	}

}

