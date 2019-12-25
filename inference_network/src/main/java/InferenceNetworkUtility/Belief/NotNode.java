package InferenceNetworkUtility.Belief;

import InferenceNetworkUtility.Proximity.ProximityNode;

import java.util.List;

public class NotNode extends BeliefNode {

    public NotNode(List<ProximityNode> children) {
        super(children);
    }

    @Override
    public Double score(int docId) {
        return Math.log(1 - Math.exp(this.children.get(0).score(docId)));
    }
}
