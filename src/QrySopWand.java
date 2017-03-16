import java.io.IOException;

/**
 * Created by lingyanjiang on 17/2/15.
 */
public class QrySopWand extends QrySopWeighted {

    @Override
    public double getScore(RetrievalModel r) throws IOException {
        if (r instanceof RetrievalModelIndri) {
            return this.getScoreIndri(r);
        } else {
            throw new IllegalArgumentException
                    (r.getClass().getName() + " doesn't support the AND operator.");
        }
    }

    /**
     * This class is to calculate indri score
     * @param r
     * @return
     * @throws IOException
     */
    private double getScoreIndri(RetrievalModel r) throws IOException {
        double totalWgt = 0;
        for (double wgt : this.weights) {
            totalWgt += wgt;
        }
        if (!this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            double res = 1.0;
            for (int i = 0; i < this.args.size(); i++) {
                QrySop q = (QrySop) this.args.get(i);
                if (!q.docIteratorHasMatch(r) || q.docIteratorGetMatch() != this.docIteratorGetMatch()) {
                    double ss = q.getDefaultScore(r, this.docIteratorGetMatch());
                    double cur = Math.pow(ss, this.weights.get(i) / totalWgt);
                    res *= cur;
                } else {
                    double cur = Math.pow(q.getScore(r), this.weights.get(i) / totalWgt);
                    res *= cur;
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
        double res = 1.0;
        if (!this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            for (int i = 0; i < this.args.size(); i++) {
                QrySop q = (QrySop) this.args.get(i);
                double cur = Math.pow(q.getDefaultScore(r, docid), this.weights.get(i) / totalWgt);
                res *= cur;
            }
            return res;
        }
    }

    @Override
    public boolean docIteratorHasMatch(RetrievalModel r) {
        if (r instanceof RetrievalModelIndri) {
            return this.docIteratorHasMatchMin(r);
        } else {
            return this.docIteratorHasMatchAll(r);
        }
    }
}
