import java.io.IOException;

/**
 * Created by lingyanjiang on 17/2/10.
 */
public class QrySopWsum extends QrySopWeighted {

    @Override
    public double getScore(RetrievalModel r) throws IOException {
        if (r instanceof RetrievalModelIndri) {
            return this.getIndriScore((RetrievalModelIndri) r);
        }
        return 0;
    }

    private double getIndriScore(RetrievalModelIndri r) throws IOException {
        double totalWgt = 0;
        for (double wgt : this.weights) {
            totalWgt += wgt;
        }
        if (!this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            double res = 0.0;
            for (int i = 0; i < this.args.size(); i++) {
                QrySop q = (QrySop) this.args.get(i);
                if (!q.docIteratorHasMatch(r) || q.docIteratorGetMatch() != this.docIteratorGetMatch()) {
                    res += q.getDefaultScore(r, this.docIteratorGetMatch()) * this.weights.get(i) / totalWgt;
                } else {
                    res += q.getScore(r) * this.weights.get(i) / totalWgt;
                }
            }
            return res;
        }
    }

    @Override
    public double getDefaultScore(RetrievalModel r, long docid) throws IOException {
        double totalWgt = 0;
        for (double wgt : this.weights) {
            totalWgt += wgt;
        }
        double res = 0.0;
        if (!this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            for (int i = 0; i < this.args.size(); i++) {
                QrySop q = (QrySop) this.args.get(i);
                double cur = q.getDefaultScore(r, docid) * this.weights.get(i) / totalWgt;
                res += cur;
            }
            return res;
        }
    }

    @Override
    public boolean docIteratorHasMatch(RetrievalModel r) {
        return this.docIteratorHasMatchMin(r);
    }

}
