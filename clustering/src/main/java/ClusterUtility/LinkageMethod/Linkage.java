package ClusterUtility.LinkageMethod;

import ClusterUtility.DocumentVector;
import ClusterUtility.SimilarityMethod.Similarity;
import index.Index;

import java.util.Map;

public abstract class Linkage {

    protected Index index;
    protected Similarity similarity;

    public Linkage(Index index, Similarity sim) {
        this.index = index;
        this.similarity = sim;
    }

    public abstract double score(Map<Integer, DocumentVector> documentVectors, DocumentVector newDocVec);
}
