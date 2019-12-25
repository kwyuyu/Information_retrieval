package InferenceNetworkUtility.Belief;

import InferenceNetworkUtility.Proximity.ProximityNode;

import java.util.List;

public class OrNode extends BeliefNode {

    public OrNode(List<ProximityNode> children) {
        super(children);
    }

    @Override
    public Double score(int docId) {
        if (!this.atLeastOneTermExist(docId)) return null;
        double totalScore = 0.0;
        for (ProximityNode child: this.children) {
            totalScore += Math.log(1 - Math.exp(child.score(docId)));
        }
        return Math.log(1 - Math.exp(totalScore));
    }
}
