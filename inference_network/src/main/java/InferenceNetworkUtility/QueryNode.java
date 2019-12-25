package InferenceNetworkUtility;

public abstract class QueryNode {

    public abstract boolean hasMore();
    public abstract Double score(int docId);
    public abstract Integer nextCandidate();
    public abstract void skipToNext(int docId);

}
