package InferenceNetworkUtility.Proximity;

import InferenceNetworkUtility.QueryNode;
import RetrievalModels.RetrievalModel;
import index.Index;
import index.Posting;
import index.PostingList;

public abstract class ProximityNode extends QueryNode {

    protected PostingList postingList;
    protected Index index;
    protected RetrievalModel model;

    public ProximityNode(Index index, RetrievalModel model) {
        this.index = index;
        this.model = model;
    }

    @Override
    public boolean hasMore() {
        return this.postingList.hasMore();
    }

    @Override
    public Integer nextCandidate() {
        Integer nextDocId = null;
        try {
            nextDocId = this.postingList.getCurrentPosting().getDocId();
        }
        catch (NullPointerException ex) {
            // ignore
        }
        return nextDocId;
    }

    @Override
    public void skipToNext(int docId) {
        this.postingList.skipTo(docId);
    }

    public Posting getCurrentPosting() {
        return this.postingList.getCurrentPosting();
    }

    public Integer getCurrentDocId() {
        return this.postingList.getCurrentPosting().getDocId();
    }
}
