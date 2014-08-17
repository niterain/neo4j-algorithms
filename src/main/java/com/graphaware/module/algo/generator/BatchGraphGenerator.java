package com.graphaware.module.algo.generator;

import com.graphaware.common.util.SameTypePair;
import com.graphaware.module.algo.generator.config.GeneratorConfiguration;
import com.graphaware.module.algo.generator.node.NodeCreator;
import com.graphaware.module.algo.generator.relationship.RelationshipCreator;
import com.graphaware.module.algo.generator.relationship.RelationshipGenerator;
import com.graphaware.tx.executor.NullItem;
import com.graphaware.tx.executor.batch.BatchTransactionExecutor;
import com.graphaware.tx.executor.batch.IterableInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.NoInputBatchTransactionExecutor;
import com.graphaware.tx.executor.batch.UnitOfWork;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link com.graphaware.module.algo.generator.GraphGenerator} for Neo4j using {@link BatchInserter}.
 */
public class BatchGraphGenerator extends BaseGraphGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(BatchGraphGenerator.class);

    private final BatchInserter batchInserter;

    public BatchGraphGenerator(BatchInserter batchInserter) {
        this.batchInserter = batchInserter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateGraph(GeneratorConfiguration configuration) {
        super.generateGraph(configuration);

        batchInserter.shutdown();
        LOG.info("Inserter shut down");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected List<Long> generateNodes(final GeneratorConfiguration config) {
        List<Long> nodes = new ArrayList<>();

        int numberOfNodes = config.getNumberOfNodes();
        NodeCreator nodeCreator = config.getNodeCreator();

        LOG.info("Creating " + numberOfNodes + " nodes");

        for (int i = 0; i < numberOfNodes; i++) {
            nodes.add(nodeCreator.createNode(batchInserter));
        }

        LOG.info("Created " + numberOfNodes + " nodes");

        return nodes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void generateRelationships(GeneratorConfiguration config, List<Long> nodes) {
        LOG.info("Generating relationships");

        List<SameTypePair<Integer>> relationships = config.getRelationshipGenerator().generateEdges();

        LOG.info("Generated relationships, creating them");

        RelationshipCreator relationshipCreator = config.getRelationshipCreator();
        for (SameTypePair<Integer> relationship : relationships) {
            relationshipCreator.createRelationship(nodes.get(relationship.first()), nodes.get(relationship.second()), batchInserter);
        }

        LOG.info("Created relationships, shutting down inserter");
    }
}
