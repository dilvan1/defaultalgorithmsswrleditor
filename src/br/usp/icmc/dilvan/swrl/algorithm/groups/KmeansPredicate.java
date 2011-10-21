package br.usp.icmc.dilvan.swrl.algorithm.groups;

import br.usp.icmc.dilvan.swrlEditor.client.rpc.swrleditor.RuleSet;
import br.usp.icmc.dilvan.swrlEditor.server.swrleditor.groups.MatrizPredicateCharacteristic;


public class KmeansPredicate extends KmeansAtom {

	
	public KmeansPredicate(){
	}
	
	@Override
	public void setRuleSet(RuleSet rules) {
		this.rules = rules;
		matriz = new MatrizPredicateCharacteristic();
		createGroups(numGroups);
		setCentersDefault();
		matriz.addRule(rules);
	}

	
	@Override
	public String getAlgorithmName() {
		return "K-Means (Similarity predicate)";
	}
}
