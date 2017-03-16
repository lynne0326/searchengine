/**
 * Copyright (c) 2017, Carnegie Mellon University.  All Rights Reserved.
 */

import java.io.*;
import java.lang.IllegalArgumentException;

/**
 * The SCORE operator for all retrieval models.
 */
public class QrySopScore extends QrySop {

    /**
     *  Document-independent values that should be determined just once.
     *  Some retrieval models have these, some don't.
     */

    /**
     * Indicates whether the query has a match.
     *
     * @param r The retrieval model that determines what is a match
     * @return True if the query matches, otherwise false.
     */
    public boolean docIteratorHasMatch(RetrievalModel r) {
        return this.docIteratorHasMatchFirst(r);
    }

    /**
     * Get a score for the document that docIteratorHasMatch matched.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    public double getScore(RetrievalModel r) throws IOException {
        if (r instanceof RetrievalModelUnrankedBoolean) {
            return this.getScoreUnrankedBoolean(r);
        } else if (r instanceof RetrievalModelRankedBoolean) {
            return this.getScoreRankedBoolean(r);
        } else if (r instanceof RetrievalModelBM25) {
            return this.getScoreBM25((RetrievalModelBM25) r);
        } else if (r instanceof RetrievalModelIndri) {
            return this.getScoreIndri((RetrievalModelIndri) r);
        } else {
            throw new IllegalArgumentException
                    (r.getClass().getName() + " doesn't support the SCORE operator.");
        }
    }

    public double getDefaultScore(RetrievalModel r, long docid) throws IOException {
        if (r instanceof RetrievalModelIndri) {
            return this.getDefaultScoreIndri((RetrievalModelIndri) r, docid);
        } else {
            throw new IllegalArgumentException
                    (r.getClass().getName() + " doesn't support the SCORE operator.");
        }
    }

    /**
     * getScore for the Unranked retrieval model.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    public double getScoreUnrankedBoolean(RetrievalModel r) throws IOException {
        if (!this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            return 1.0;
        }

    }

    /**
     * getScore for the RankedBoolean retrieval model.
     *
     * @param r The retrieval model that determines how scores are calculated.
     * @return The document score.
     * @throws IOException Error accessing the Lucene index
     */
    private double getScoreRankedBoolean(RetrievalModel r) throws IOException {
        if (!this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            QryIop term = (QryIop) this.getArg(0);
            return term.docIteratorGetMatchPosting().tf;
        }
    }


    /**
     * This method is to get doc score for BM 25 model
     * @param r
     * @return
     * @throws IOException
     */
    private double getScoreBM25(RetrievalModelBM25 r) throws IOException {
        if (!this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            QryIop term = (QryIop) this.getArg(0);
            double N = Idx.getNumDocs();
            int df = term.getDf();
            int tf = term.docIteratorGetMatchPosting().tf;
            double k1 = r.k1;
            double b = r.b;
            String field = term.field;
            double avgLength = Idx.getSumOfFieldLengths(field) / (float) Idx.getDocCount(field);
            double docLength = Idx.getFieldLength(field, term.docIteratorGetMatch());
            double score = Math.log((N - df + 0.5) / (df + 0.5)) * tf / (tf + k1 * ((1 - b) + b * (docLength / avgLength)));
            return score;
        }
    }

    /**
     * This method calculate the score using two stage smoothing
     *
     * @param r
     * @return
     * @throws IOException
     */
    private double getScoreIndri(RetrievalModelIndri r) throws IOException {
        if (!this.docIteratorHasMatchCache()) {
            return 0.0;
        } else {
            double score = 0.0;
            QryIop term = (QryIop) this.getArg(0);
            double lambda = r.lambda;
            double mu = r.mu;
            int tf = term.docIteratorGetMatchPosting().tf;
            int docLength = Idx.getFieldLength(term.field, term.docIteratorGetMatch());
            long ctf = term.getCtf();
            double colLength = Idx.getSumOfFieldLengths(term.field);
            double pmle = ctf / colLength;
            score = (1 - lambda) * ((tf + mu * pmle) / (docLength + mu)) + lambda * pmle;
            return score;
        }
    }

    /**
     * This method is to get score of doc with 0 tf
     * @param r
     * @param docid
     * @return
     */
    private double getDefaultScoreIndri(RetrievalModelIndri r, long docid) throws IOException {
        double lambda = r.lambda;
        double mu = r.mu;
        QryIop term = (QryIop) this.getArg(0);
        int docLength = Idx.getFieldLength(term.field, (int) docid);
        long ctf = term.getCtf();
        double colLength = Idx.getSumOfFieldLengths(term.field);
        double pmle = ctf / colLength;
        double score = (1 - lambda) * (0 + mu * pmle) / (docLength + mu) + lambda * pmle;
        return score;
    }

    /**
     * Initialize the query operator (and its arguments), including any
     * internal iterators.  If the query operator is of type QryIop, it
     * is fully evaluated, and the results are stored in an internal
     * inverted list that may be accessed via the internal iterator.
     *
     * @param r A retrieval model that guides initialization
     * @throws IOException Error accessing the Lucene index.
     */
    public void initialize(RetrievalModel r) throws IOException {
        Qry q = this.args.get(0);
        q.initialize(r);
    }

}
