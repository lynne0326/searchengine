import java.io.IOException;

/**
 * Created by lingyanjiang on 17/1/29.
 */
public class QrySopAnd extends QrySop {

    @Override
    public double getScore(RetrievalModel r) throws IOException {
        if (r instanceof RetrievalModelUnrankedBoolean) {
            return this.getScoreUnrankedBoolean(r);
        } else if (r instanceof RetrievalModelRankedBoolean) {
            return this.getScoreRankedBoolean(r);
        } else if (r instanceof RetrievalModelIndri) {
            return this.getScoreIndri(r);
        }
        else {
            throw new IllegalArgumentException
                    (r.getClass().getName() + " doesn't support the AND operator.");
        }
    }

    @Override
    public double getDefaultScore(RetrievalModel r, long docid) throws IOException {
        double res = 1.0;
        for (int i = 0; i < this.args.size(); i++) {
            QrySop q = (QrySop) this.args.get(i);
            double cur = Math.pow(q.getDefaultScore(r, docid), (double) 1 / this.args.size());
            res *= cur;
        }
        return res;
    }

    @Override
    public boolean docIteratorHasMatch(RetrievalModel r) {
        if (r instanceof RetrievalModelIndri) {
            return this.docIteratorHasMatchMin(r);
        } else {
            return this.docIteratorHasMatchAll(r);
        }
    }

    /**
     * getScore for the UnrankedBoolean retrieval model.
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    private double getScoreUnrankedBoolean(RetrievalModel r) throws IOException {
        if (!this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            return 1.0;
        }
    }

    /**
     * getScore for the RankedBoolean retrieval model.
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    private double getScoreRankedBoolean(RetrievalModel r) throws IOException {
        double score = 0.0;
        // Get first arg score as the min
        if (this.args.size() > 0) {
            score = ((QrySop) this.args.get(0)).getScore(r);
        }
        // Iterate to get min scores among args
        for (int i = 0; i < this.args.size(); i++) {
            score = Math.min(((QrySop) this.args.get(i)).getScore(r), score);
        }
        return score;
    }

    /**
     * This method calculate the score for and operator under indri model
     * @param r
     * @return
     * @throws IOException
     */
    private double getScoreIndri(RetrievalModel r) throws IOException {
        if (!this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            double res = 1.0;
            for (int i = 0; i < this.args.size(); i++) {
                QrySop q = (QrySop) this.args.get(i);
                if (!q.docIteratorHasMatch(r) || q.docIteratorGetMatch() != this.docIteratorGetMatch()) {
                    double ss = q.getDefaultScore(r, this.docIteratorGetMatch());
                    double cur = Math.pow(ss, (double) 1 / this.args.size());
                    res *= cur;
                } else {
                    double cur = Math.pow(q.getScore(r), (double) 1 / this.args.size());
                    res *= cur;
                }
            }
            return res;
        }
    }
}
