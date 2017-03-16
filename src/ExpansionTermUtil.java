import java.io.*;
import java.util.*;

/**
 * Created by lingyanjiang on 17/3/14.
 */
public class ExpansionTermUtil {

    static class Entry {
        String term;
        double score;

        public Entry(String term, double score) {
            this.term = term;
            this.score = score;
        }
    }

    /**
     * This method is to get potential terms for all queries
     * @param fieldName
     * @return
     * @throws IOException
     */
    public static Map<String, Set<String>> getPotentialTerms(Map<String, ScoreList> scoreLists, String fieldName) throws IOException {
        Map<String, Set<String>> res = new HashMap<>();
        if (scoreLists == null || scoreLists.size() < 1) return res;
        for (String key : scoreLists.keySet()) {
            ScoreList scoreList = scoreLists.get(key);
            Set<String> tmp = new HashSet<>();
            for (int i = 0; i < scoreList.size(); i++) {
                int curDoc = scoreList.getDocid(i);
                TermVector termVector = new TermVector(curDoc, fieldName);
                for (int j = 0; j < termVector.positionsLength(); j++) {
                    if (termVector.stemString(j) == null || termVector.stemString(j).indexOf(".") != -1 || termVector.stemString(j).indexOf(",") != -1)
                        continue;
                    tmp.add(termVector.stemString(j));
                }
            }
            res.put(key, tmp);
        }
        return res;
    }

    public static Map<String, List<Entry>> getTopTerms(Map<String, Set<String>> map, Map<String, ScoreList> scoreListMap, double mu, int m) throws IOException {
        Map<String, List<Entry>> res = new HashMap<>();
        for (String key : map.keySet()) {
            res.put(key, new ArrayList<>());
            //For each query
            PriorityQueue<Entry> pq = new PriorityQueue<>(m, new Comparator<Entry>() {
                @Override
                public int compare(Entry o1, Entry o2) {
                    return o1.score - o2.score < 0 ? -1 : (o1.score == o2.score ? 0 : 1);
                }
            });

            Set<String> candidates = map.get(key);
            ScoreList docs = scoreListMap.get(key);
            for (String term : candidates) {
                double score = 0.0;
                //Calculate score for term in each doc in top n docs
                for (int i = 0; i < docs.size(); i++) {
                    int docId = docs.getDocid(i);
                    double docScore = docs.getDocidScore(i);
                    String field = "body";
                    if (term.split("[.]").length >= 2) {
                        field = term.split("[.]")[1].trim();
                        term = term.split("[.]")[0].trim();
                    }
                    TermVector termVector = new TermVector(docId, field);

                    int idx = termVector.indexOfStem(term);
                    int tf = idx == -1 ? 0 : termVector.stemFreq(idx);
                    int docLen = Idx.getFieldLength(field, docId);
                    double ctf = Idx.getTotalTermFreq(field, term);
                    double colLength = Idx.getSumOfFieldLengths(field);
                    double pmle = ctf / colLength;
                    score += ((tf + mu * pmle) / (docLen + mu)) * docScore * Math.log(colLength / ctf);
                }
                pq.offer(new Entry(term, score));
                if (pq.size() > m) pq.poll();
            }
            while (!pq.isEmpty()) {
                res.get(key).add(pq.poll());
            }
        }
        return res;
    }

    public static void writeExpanQuery(String outputFile, Map<String, List<Entry>> queries) {
        try (BufferedWriter bufOut = new BufferedWriter(new FileWriter(new File(outputFile)))) {
            for (String key : queries.keySet()) {
                StringBuilder sb = new StringBuilder(key + ": ").append("#wand (");
                List<Entry> tmp = queries.get(key);
                for (Entry entry : tmp) {
                    sb.append(String.format("%.4f", entry.score) + " ").append(entry.term + " ");
                }
                sb.append(")");
                bufOut.write(sb.toString());
                bufOut.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public static void writeExpanQuery(String originalFile, String expansionFile, String tmpFile, double w) {
//        try (BufferedReader bufIn = new BufferedReader(new FileReader(new File(originalFile)));
//             BufferedReader bufIn2 = new BufferedReader(new FileReader(new File(expansionFile)));
//             BufferedWriter bufOut = new BufferedWriter(new FileWriter(new File(tmpFile)))
//        ) {
//            String line1 = null;
//            String line2 = null;
//            while ((line1 = bufIn.readLine()) != null) {
//                if (line1.isEmpty()) continue;
//                StringBuilder sb = new StringBuilder(line1.split(":")[0].trim() + ": #wand ( ").append(w+" #and ( ").append(line1.split(":")[1].trim()).append(") ");
//                line2 = bufIn2.readLine();
//                sb.append(1 - w).append(" ").append(line2.split(":")[1].trim()).append(" )");
//                bufOut.write(sb.toString());
//                bufOut.newLine();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

}
