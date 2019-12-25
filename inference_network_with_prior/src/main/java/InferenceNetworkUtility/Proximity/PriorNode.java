package InferenceNetworkUtility.Proximity;

import RetrievalModels.RetrievalModel;
import index.Index;
import index.Posting;

public class PriorNode extends ProximityNode {

    private String priorType;

    public PriorNode(Index index, RetrievalModel model, String priorType) {
        super(index, model);
        this.priorType = priorType;
    }

    @Override
    public boolean hasMore() {
        return false;
    }

    @Override
    public Integer nextCandidate() {
        return null;
    }

    @Override
    public void skipToNext(int docId) {
        return;
    }

    @Override
    public Posting getCurrentPosting() {
        return null;
    }

    @Override
    public Integer getCurrentDocId() {
        return null;
    }

    @Override
    public Double score(int docId) {
        return this.index.getPrior(this.priorType, docId);
    }
}
