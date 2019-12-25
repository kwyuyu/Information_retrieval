package InferenceNetworkUtility.Proximity;

import RetrievalModels.RetrievalModel;
import index.Index;
import index.Posting;

public class TermNode extends ProximityNode {

    private String term;

    public TermNode(String term, Index index, RetrievalModel model) {
        super(index, model);

        this.term = term;
        this.postingList = this.index.getPostings(term);

        this.postingList.startIteration();
    }

    @Override
    public Double score(int docId) {
        Posting post = this.postingList.getCurrentPosting();

        int tf = post != null && post.getDocId() == docId ? post.getTermFreq() : 0;
        int docLen = this.index.getDocLength(docId);
        int ctf = this.index.getTermFreq(this.term);

        return this.model.scoring(tf, ctf, docLen);
    }

}
