package InferenceNetworkUtility.Belief;

import InferenceNetworkUtility.Proximity.ProximityNode;

import java.util.List;

public class AndNode extends BeliefNode {

    public AndNode(List<ProximityNode> children) {
        super(children);
    }

    @Override
    public Double score(int docId) {
        if (!this.atLeastOneTermExist(docId)) return null;
        double totalScore = 0.0;
        for (ProximityNode child: this.children) {
            totalScore += child.score(docId);
        }
        return totalScore;
    }
}
