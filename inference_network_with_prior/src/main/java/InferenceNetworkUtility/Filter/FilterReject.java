package InferenceNetworkUtility.Filter;

import InferenceNetworkUtility.Proximity.ProximityNode;
import InferenceNetworkUtility.QueryNode;

public class FilterReject extends FilterNode {

    public FilterReject(ProximityNode filterNode, QueryNode queryNode) {
        super(filterNode, queryNode);
    }

    @Override
    public Integer nextCandidate() {
        return this.queryNode.nextCandidate();
    }

    @Override
    public Double score(int docId) {
        if (this.filterNode.getCurrentDocId() != docId) {
            return this.queryNode.score(docId);
        }
        return null;
    }
}
