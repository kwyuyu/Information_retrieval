package InferenceNetworkUtility.Belief;

import InferenceNetworkUtility.Proximity.ProximityNode;

import java.util.List;

public class MaxNode extends BeliefNode {

    public MaxNode(List<ProximityNode> children) {
        super(children);
    }

    @Override
    public Double score(int docId) {
        if (!this.atLeastOneTermExist(docId)) return null;
        double maxScore = -Double.MAX_VALUE;
        for (ProximityNode child: this.children) {
            maxScore = Math.max(maxScore, child.score(docId));
        }
        return maxScore;
    }
}
