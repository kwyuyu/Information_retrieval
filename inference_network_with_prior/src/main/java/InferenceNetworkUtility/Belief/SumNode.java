package InferenceNetworkUtility.Belief;

import InferenceNetworkUtility.Proximity.ProximityNode;

import java.util.List;

public class SumNode extends BeliefNode {

    public SumNode(List<ProximityNode> children) {
        super(children);
    }

    @Override
    public Double score(int docId) {
        if (!this.atLeastOneTermExist(docId)) return null;
        double totalScore = 0.0;
        for (ProximityNode child: this.children) {
            totalScore += Math.exp(child.score(docId));
        }
        return Math.log(totalScore / this.children.size());
    }
}
