package InferenceNetworkUtility.Filter;

import InferenceNetworkUtility.Proximity.ProximityNode;
import InferenceNetworkUtility.QueryNode;

public class FilterRequire extends FilterNode {

    public FilterRequire(ProximityNode filterNode, QueryNode queryNode) {
        super(filterNode, queryNode);
    }

    @Override
    public Integer nextCandidate() {
        return Math.max(this.filterNode.nextCandidate(), this.queryNode.nextCandidate());
    }

    @Override
    public Double score(int docId) {
        if (this.filterNode.getCurrentDocId() == docId) {
            return this.queryNode.score(docId);
        }
        return null;
    }
}
