package ClusterUtility.LinkageMethod;

import ClusterUtility.DocumentVector;
import ClusterUtility.SimilarityMethod.Similarity;
import index.Index;

import java.util.Map;

public class SingleLinkage extends Linkage {

    public SingleLinkage(Index index, Similarity sim) {
        super(index, sim);
    }

    @Override
    public double score(Map<Integer, DocumentVector> documentVectors, DocumentVector newDocVec) {
        double minSim = Double.MAX_VALUE;
        for (DocumentVector docVec: documentVectors.values()) {
            minSim = Math.min(minSim, this.similarity.score(docVec, newDocVec));
        }
        return minSim;
    }
}
