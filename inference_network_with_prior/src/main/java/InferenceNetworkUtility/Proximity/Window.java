package InferenceNetworkUtility.Proximity;

import RetrievalModels.RetrievalModel;
import index.Index;
import index.Posting;
import index.PostingList;

import java.util.List;

public abstract class Window extends ProximityNode {

    protected List<ProximityNode> children;
    private int windowCtf = 0;
    protected int windowSize = 0;

    public Window(Index index, RetrievalModel model, List<ProximityNode> children) {
        super(index, model);
        this.children = children;
        this.postingList = new PostingList();
    }

    @Override
    public Double score(int docId) {
        Posting post = this.postingList.getCurrentPosting();

        int tf = post != null && post.getDocId() == docId ? post.getTermFreq() : 0;
        int docLen = this.index.getDocLength(docId);
        int ctf = this.windowCtf;

        return this.model.scoring(tf, ctf, docLen);
    }



    protected void generateWindowPostingList() {
        for (int doc = 1; doc <= this.index.getDocCount(); doc++) {
            this.skipToNextDoc(doc);

            if (this.isValidDoc(doc)) {
                Posting p = this.generateWindowPosting(doc);
                if (p.getTermFreq() > 0) {
                    this.postingList.add(p);
                    this.windowCtf += p.getTermFreq();
                }
            }
        }
    }

    private Posting generateWindowPosting(int docId) {
        Posting p = new Posting(docId);

        while (this.hasMoreInDoc()) {
            int firstPos = this.windowOccurrence(docId);
            if (firstPos != -1) {
                p.add(firstPos);
            }
        }

        return p;
    }

    private boolean isValidDoc(int docId) {
        // a window should contain all query terms
        for (ProximityNode child: this.children) {
            Posting post = child.postingList.getCurrentPosting();
            if (post == null || post.getDocId() != docId) {
                return false;
            }
        }
        return true;
    }

    private boolean hasMoreInDoc() {
        // has more posting is current document
        for (ProximityNode child: this.children) {
            Posting post = child.getCurrentPosting();
            if (!post.hasMorePosition()) {
                return false;
            }
        }
        return true;
    }

    private void skipToNextDoc(int docId) {
        // skip to next document
        for (ProximityNode child: this.children) {
            child.skipToNext(docId);
        }
    }


    protected abstract int windowOccurrence(int docId);
}
