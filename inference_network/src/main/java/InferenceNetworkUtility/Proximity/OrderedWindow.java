package InferenceNetworkUtility.Proximity;

import RetrievalModels.RetrievalModel;
import index.Index;
import index.Posting;

import java.util.List;

public class OrderedWindow extends Window {

    public OrderedWindow(Index index, RetrievalModel model, List<ProximityNode> children, int windowSize) {
        super(index, model, children);
        this.windowSize = windowSize;

        this.generateWindowPostingList();
        this.postingList.startIteration();
    }

    @Override
    protected int windowOccurrence(int docId) {
        int prevPos = -1;
        int firstPos = -1;
        for (ProximityNode child: this.children) {
            Posting p = child.getCurrentPosting();
            int pos;

            if (prevPos == -1) {
                pos = p.getCurrentPosition();
                firstPos = pos;
                p.skipToNext();
            }
            else {
                p.skipToNextPosition(prevPos);
                pos = p.getCurrentPosition();
                if (pos == -1 || pos - prevPos != this.windowSize) {
                    return -1;
                }
            }

            prevPos = pos;
        }

        // If succeed to find the matched window, we move the first term to next window
        Posting p = this.children.get(0).getCurrentPosting();
        p.skipToNextPosition(prevPos);

        return firstPos;
    }
}