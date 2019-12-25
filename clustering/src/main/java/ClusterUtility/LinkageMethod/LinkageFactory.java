package ClusterUtility.LinkageMethod;

import ClusterUtility.SimilarityMethod.Similarity;
import index.Index;

public class LinkageFactory {

    public static Linkage getLinkage(LinkageFunction l, Index index, Similarity sim) {
        switch (l) {
            case SINGLE:
                return new SingleLinkage(index, sim);
            case COMPLETE:
                return new CompleteLinkage(index, sim);
            case AVERAGE:
                return new AverageLinkage(index, sim);
            case AVERAGEGROUP:
                return new AverageGroupLinkage(index, sim);
        }
        return null;
    }
}
