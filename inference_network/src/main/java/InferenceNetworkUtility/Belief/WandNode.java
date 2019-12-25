package InferenceNetworkUtility.Belief;

import InferenceNetworkUtility.Proximity.ProximityNode;

import java.util.List;

public class WandNode extends BeliefNode {

    private List<Double> weights;

    public WandNode(List<ProximityNode> children, List<Double> weights) {
        super(children);
        this.weights = weights;
    }

    @Override
    public Double score(int docId) {
        if (!this.atLeastOneTermExist(docId)) return null;
        double totalScore = 0.0;
        for (int i = 0; i < this.children.size(); i++) {
            totalScore += this.weights.get(i) * this.children.get(i).score(docId);
        }
        return totalScore;
    }
}
