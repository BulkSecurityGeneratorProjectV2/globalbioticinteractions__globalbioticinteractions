package org.eol.globi.tool;

import org.eol.globi.util.BatchListener;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

public class TransactionPerBatch implements BatchListener {
    private final GraphDatabaseService graphDb;
    private Transaction tx;

    public TransactionPerBatch(GraphDatabaseService graphDb) {
        this.graphDb = graphDb;
    }

    @Override
    public void onStartBatch() {
        onFinishBatch();
        tx = graphDb.beginTx();
    }

    @Override
    public void onFinishBatch() {
        if (tx != null) {
            tx.success();
            tx.close();
        }
    }
}