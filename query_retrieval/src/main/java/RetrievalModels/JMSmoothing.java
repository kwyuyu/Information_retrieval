package RetrievalModels;

import index.Index;

public class JMSmoothing extends RetrievalModel {

    private double lambda;

    public JMSmoothing(Index index, double lambda) {
        this.index = index;
        this.lambda = lambda;
        this.collSize = this.index.getCollectionSize();
    }

    @Override
    public double scoring(int tf, int ctf, int docLen) {
        return Math.log((1 - this.lambda) * (tf * 1.0 / docLen) + this.lambda * (ctf / this.collSize));
    }
}
