package InferenceNetworkUtility.Filter;

import InferenceNetworkUtility.Proximity.ProximityNode;
import InferenceNetworkUtility.QueryNode;

public abstract class FilterNode extends QueryNode {

    protected ProximityNode filterNode;
    protected QueryNode queryNode;

    public FilterNode(ProximityNode filterNode, QueryNode queryNode) {
        this.filterNode = filterNode;
        this.queryNode = queryNode;
    }

    @Override
    public boolean hasMore() {
        return this.queryNode.hasMore();
    }

    @Override
    public void skipToNext(int docId) {
        this.filterNode.skipToNext(docId);
        this.queryNode.skipToNext(docId);
    }

}
