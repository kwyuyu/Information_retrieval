package RetrievalModels;

import index.Index;
import index.Posting;
import index.PostingList;

import java.util.*;

public interface RetrievalModel {


    /**
     * @param tf: term frequency
     * @param ctf: collection term frequency
     * @param docLen: document length
     * @return score for the given model
     */
    double scoring(int tf, int ctf, int docLen);
}
