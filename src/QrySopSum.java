import java.io.IOException;
import java.util.Arrays;

/**
 * Created by lingyanjiang on 17/2/14.
 */
public class QrySopSum extends QrySop {

    @Override
    public double getScore(RetrievalModel r) throws IOException {
        if (r instanceof RetrievalModelBM25) {
            return this.bm25Score((RetrievalModelBM25) r);
        }
        return 0;
    }

    @Override
    public double getDefaultScore(RetrievalModel r, long docid) throws IOException {
        return this.getScore(r);
    }

    @Override
    public boolean docIteratorHasMatch(RetrievalModel r) {
        return this.docIteratorHasMatchMin(r);
    }

    /**
     * This method is to calculate score under BM25 model
     * @param r
     * @return
     * @throws IOException
     */
    private double bm25Score(RetrievalModelBM25 r) throws IOException {
        if (!this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            //Get parameters
            double k3 = r.k3;
            double sum = 0.0;
            double[] scores = new double[this.args.size()];
            int [] weights = new int[this.args.size()];
            //Assume all weights are 1
            Arrays.fill(weights, 1);
            for (int i = 0; i < this.args.size(); i++) {
                QrySop q = (QrySop) this.args.get(i);
                if (!q.docIteratorHasMatchCache() || q.docIteratorGetMatch() != this.docIteratorGetMatch()) {
                    continue;
                }
                scores[i] = q.getScore(r);
                sum += scores[i] * (k3 + 1) * weights[i] / (k3 + weights[i]);
            }
            return sum;
        }
    }
}
