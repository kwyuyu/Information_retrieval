package InferenceNetworkUtility.Proximity;

import RetrievalModels.RetrievalModel;
import index.Index;
import index.Posting;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UnorderedWindow extends Window {

    public UnorderedWindow(Index index, RetrievalModel model, List<ProximityNode> children, int windowSize) {
        super(index, model, children);
        this.windowSize = windowSize;

        this.generateWindowPostingList();
        this.postingList.startIteration();
    }

    @Override
    protected int windowOccurrence(int docId) {
        int minPos = this.findMinPos();
        Set<Integer> childrenInWindow = new HashSet<>();

        for (int i = 0; i < this.children.size(); i++) {
            Posting post = this.children.get(i).getCurrentPosting();

            while (post.hasMorePosition() && post.getCurrentPosition() <= minPos + this.windowSize - 1) {
                childrenInWindow.add(i);
                post.skipToNextPosition(minPos + this.windowSize);
            }
        }

        return childrenInWindow.size() == this.children.size() ? minPos : -1;
    }

    private int findMinPos() {
        int minPos = Integer.MAX_VALUE;
        for (ProximityNode child: this.children) {
            int pos = child.getCurrentPosting().getCurrentPosition();
            minPos = Math.min(minPos, pos);
        }
        return minPos;
    }
}
