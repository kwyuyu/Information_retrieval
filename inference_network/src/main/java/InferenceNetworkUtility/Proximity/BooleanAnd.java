package InferenceNetworkUtility.Proximity;

import RetrievalModels.RetrievalModel;
import index.Index;

import java.util.List;



public class BooleanAnd extends UnorderedWindow {

    public BooleanAnd(Index index, RetrievalModel model, List<ProximityNode> children) {
        super(index, model, children, 0);
    }

    @Override
    protected int windowOccurrence(int docId) {
        this.windowSize = this.index.getDocLength(docId);
        return super.windowOccurrence(docId);
    }
}
