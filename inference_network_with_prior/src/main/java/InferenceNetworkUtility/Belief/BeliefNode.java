package InferenceNetworkUtility.Belief;

import InferenceNetworkUtility.Proximity.ProximityNode;
import InferenceNetworkUtility.QueryNode;

import java.util.List;

public abstract class BeliefNode extends QueryNode {

    protected List<ProximityNode> children;

    public BeliefNode(List<ProximityNode> children) {
        this.children = children;
    }

    @Override
    public boolean hasMore() {
        for (ProximityNode child: this.children) {
            if (child.hasMore()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Integer nextCandidate() {
        int minDocId = Integer.MAX_VALUE;
        for (ProximityNode child: this.children) {
            if (child.nextCandidate() != null) {
                minDocId = Math.min(minDocId, child.nextCandidate());
            }
        }
        return minDocId;
    }

    @Override
    public void skipToNext(int docId) {
        for (ProximityNode child: this.children) {
            child.skipToNext(docId);
        }
    }

    protected boolean atLeastOneTermExist(int docId) {
        int count = 0;
        for (ProximityNode child: this.children) {
            if (child.nextCandidate() != null && docId == child.getCurrentDocId()) {
                count += 1;
            }
        }
        return count != 0;
    }

}
