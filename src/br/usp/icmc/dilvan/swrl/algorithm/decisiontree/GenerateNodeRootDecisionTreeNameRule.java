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

//Classe respons�vel por criar o novo algoritmo para �rvore geradora m�xima
public class GenerateNodeRootDecisionTreeNameRule implements GenerateNodeRootDecisionTree {

	// A lista de regras SWRL
	private RuleSet rules;

	//N� raiz da �rvore gerada
	private NodeDecisionTreeRanking rootNode;

	//Armazena uma lista de regras relacionadas para cada regra
	// � utilizada para visualizar a grafo na �rvore geradora m�xima 
	private List<List<String>> rulesRelated;

	//Construtor da classe
	public GenerateNodeRootDecisionTreeNameRule() {
		super();

		// Cria o n� raiz
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


	//Executa a cria��o da �rvore geradora m�xima
	@Override
	public void run(){

		// instancia um objeto da classe Kruskal respons�vel por fazer a �rvore geradora m�xima
		Kruskal k = new Kruskal(rules.size());

		int numeroAtomosIguais;
		int sum = 0;
		int max = 0;
		int count = 0;


		//Lista tempor�ria de rela��es entre todas a regras
		List<String> rulesA = new ArrayList<String>();
		List<String> rulesB = new ArrayList<String>();
		List<Integer> rulesAB = new ArrayList<Integer>();



		//Percorre o conjunto de regras, relacionado todas as regras entre elas
		//  esses FORs ser�o removidos ap�s encontrar uma forma melhor do que a m�dia
		for (int i = 0; i < rules.size(); i++){
			for (int j = i+1; j < rules.size(); j++){

				//atraves da fun��o numeroAtomosIguais descobre o n�vel de igualdade das regras
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

		// Gera a m�dia aritm�tica
		double media = (sum*1.0)/(count*1.0);
		System.out.println("Soma: " + sum + " M�dia: " + media + " Count "+ count + " Max:"+max);


		for (int i = 0; i < rulesA.size(); i++)
			if (rulesAB.get(i).intValue() > media) // teste para retirar s� os dados acima da m�dia
				System.out.println(rulesA.get(i)+ "\t"+ rulesB.get(i));// Exibe os dados para serem usados para montar os graficos no ManyEyes


		//Novamente percorre o conjunto de regras, relacionado todas as regras entre elas
		// foi mantido esses dois FORs porque os anteriores v�o sumir 
		for (int i = 0; i < rules.size(); i++){
			rulesRelated.add(new ArrayList<String>());

			for (int j = i+1; j < rules.size(); j++){

				//atraves da fun��o numeroAtomosIguais descobre o n�vel de igualdade das regras
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

		//Executa o algoritmo de �rvore gerado m�xima
		k.executaKruskal();
		
		//Pegar as arestas da execu��o do algoritmo
		List<Aresta> arestas = k.getArestas();

		//Executa at� que a lista de arestas esteja vazia
		while (!arestas.isEmpty()){
			
			//Sempre pega a aresta Zero, pois essa � a de maior peso
			Aresta a = arestas.remove(0);
			
			//Recupera a regra da aresta de maior peso
			Rule rule = rules.get(a.getFrom());

			//Cria um novo n� na �rvore que esteja ligado ao n� raiz da �rvore
			NodeDecisionTreeRanking newNode = new NodeDecisionTreeRanking(rootNode, rule.getFormatedRuleID());
			newNode.setValue(rule.getNameRule());
			newNode.setAtomType(ATOM_TYPE.CONSEQUENT);
			newNode.setRuleName(rule.getNameRule());
			newNode.setRulesRelated(rulesRelated.get(a.getFrom()));
			rootNode.addChildNodes(newNode);

			// Chama o metodo que cria os filhos desse novo n� criado
			montaTreeNodo(a.getFrom(), newNode, arestas);
		}

	}

	//Monta todos os nodos filhos do parentNode
	private void montaTreeNodo(int parentFrom, NodeDecisionTreeRanking parentNode, List<Aresta> arestas){

		Rule rule;
		Aresta a;
		NodeDecisionTreeRanking newNode;
		
		//Percorre todas as arestas restantes, verificando quais arestas s�o ligadas ao parentFrom
		for (int i = 0; i < arestas.size(); i++){
			a = arestas.get(i);
			//Testa o From da aresta com parentFrom
			if (a.getFrom() == parentFrom){
				
				//Cria o n� filho
				rule = rules.get(a.getTo());
				newNode = new NodeDecisionTreeRanking(parentNode, rule.getFormatedRuleID());
				newNode.setValue("["+a.getCost()+"] "+rule.getNameRule());
				newNode.setRuleName(rule.getNameRule());
				newNode.setAtomType(ATOM_TYPE.CONSEQUENT);
				newNode.setRulesRelated(rulesRelated.get(a.getTo()));
				
				
				//Adiciona o n� filho criado ao parentNode
				parentNode.addChildNodes(newNode);

				//Remove ele da lista das arestas
				a = arestas.remove(i);
				
				//Chama recursivamente esse mesmo m�todo passando o n� filho como parentNode
				montaTreeNodo(a.getTo(), newNode, arestas);

			}
			//Testa o To da aresta com parentFrom
			else if (a.getTo() == parentFrom){
				//Cria o n� filho
				rule = rules.get(a.getFrom());
				newNode = new NodeDecisionTreeRanking(parentNode, rule.getFormatedRuleID());
				newNode.setValue("["+a.getCost()+"] "+rule.getNameRule());
				newNode.setRuleName(rule.getNameRule());
				newNode.setAtomType(ATOM_TYPE.CONSEQUENT);
				newNode.setRulesRelated(rulesRelated.get(a.getFrom()));
				
				//Adiciona o n� filho criado ao parentNode
				parentNode.addChildNodes(newNode);

				//Remove ele da lista das arestas
				a = arestas.remove(i);
				
				//Chama recursivamente esse mesmo m�todo passando o n� filho como parentNode
				montaTreeNodo(a.getFrom(), newNode, arestas);
			}
		}
	}

	// M�todo respons�vel por contar o n�mero de �tomos iguais.
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

