package org.hisp.dhis.analytics.table;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hisp.dhis.analytics.AnalyticsTableManager;
import org.hisp.dhis.analytics.AnalyticsTableType;
import org.hisp.dhis.analytics.AnalyticsTableUpdateParams;
import org.hisp.dhis.analytics.table.model.AnalyticsTable;
import org.hisp.dhis.analytics.table.model.AnalyticsTablePartition;
import org.hisp.dhis.common.IdentifiableObjectUtils;
import org.hisp.dhis.dataelement.DataElementService;
import org.hisp.dhis.db.model.Index;
import org.hisp.dhis.db.model.Table;
import org.hisp.dhis.organisationunit.OrganisationUnitService;
import org.hisp.dhis.resourcetable.ResourceTableService;
import org.hisp.dhis.scheduling.JobProgress;

import java.util.List;

import static org.hisp.dhis.scheduling.JobProgress.FailurePolicy.SKIP_ITEM_OUTLIER;


@RequiredArgsConstructor
@Slf4j
public class PostgresAnalyticsTableStrategy implements AnalyticsTableStrategy {

    private final AnalyticsTableManager tableManager;
    private final OrganisationUnitService organisationUnitService;
    private final DataElementService dataElementService;
    private final ResourceTableService resourceTableService;
    private final int parallelJobs;

    @Override
    public boolean validateState(JobProgress progress, AnalyticsTableType tableType) {
        return tableManager.validState();
    }

    @Override
    public TableStrategyOpResult preCreateTables(AnalyticsTableUpdateParams params, JobProgress progress) {
        progress.runStage(() -> tableManager.preCreateTables(params));
        return TableStrategyOpResult.EXECUTED;
    }

    @Override
    public TableStrategyOpResult dropStagingTables(List<AnalyticsTable> tables, JobProgress progress) {
        dropTables(tables, progress);
        return TableStrategyOpResult.EXECUTED;
    }

    @Override
    public TableStrategyOpResult createTables(List<AnalyticsTable> tables, JobProgress progress) {
        progress.runStage(tables, AnalyticsTable::getName, tableManager::createTable);
        return TableStrategyOpResult.EXECUTED;
    }

    @Override
    public TableStrategyOpResult populateTables(AnalyticsTableUpdateParams params, List<AnalyticsTablePartition> tables, JobProgress progress) {
        doPopulateTables(params, tables, progress);
        return TableStrategyOpResult.EXECUTED;
    }

    @Override
    public int applyAggregationLevels(AnalyticsTableType tableType, List<? extends Table> tables, JobProgress progress) {
        return doApplyAggregationLevels(tableType, tables, progress);
    }

    @Override
    public TableStrategyOpResult createIndexes(List<Index> indexes, JobProgress progress, AnalyticsTableType tableType) {

        int indexSize = indexes.size();
        progress.startingStage(
                "Creating " + indexSize + " indexes " + tableType, indexSize, SKIP_ITEM_OUTLIER);
        doCreateIndexes(indexes, progress);
        return TableStrategyOpResult.EXECUTED;
    }

    @Override
    public TableStrategyOpResult optimizeTables(List<AnalyticsTable> tables, JobProgress progress) {

        progress.runStageInParallel(
                this.parallelJobs, tables, Table::getName, tableManager::vacuumTable);
        return TableStrategyOpResult.EXECUTED;
    }

    @Override
    public TableStrategyOpResult swapTables(AnalyticsTableUpdateParams params, List<AnalyticsTable> tables, JobProgress progress, AnalyticsTableType tableType) {

        doSwapTables(params, tables, progress, tableType);
        return TableStrategyOpResult.EXECUTED;
    }

    @Override
    public TableStrategyOpResult analyzeTables(List<AnalyticsTable> tables, JobProgress progress) {

        progress.runStageInParallel(
                this.parallelJobs, tables, Table::getName, tableManager::analyzeTable);
        return TableStrategyOpResult.EXECUTED;
    }

    /**
     * Drops the given analytics tables.
     *
     * @param tables   the list of {@link AnalyticsTable}.
     * @param progress the {@link JobProgress}.
     */
    private void dropTables(List<AnalyticsTable> tables, JobProgress progress) {
        progress.runStage(tables, AnalyticsTable::getName, tableManager::dropTable);
    }

    /**
     * Populates the given analytics tables.
     *
     * @param params     the {@link AnalyticsTableUpdateParams}.
     * @param partitions the {@link AnalyticsTablePartition}.
     * @param progress   the {@link JobProgress}.
     */
    private void doPopulateTables(
            AnalyticsTableUpdateParams params,
            List<AnalyticsTablePartition> partitions,
            JobProgress progress) {
        int parallelism = Math.min(this.parallelJobs, partitions.size());
        log.info("Populate table task number: " + parallelism);

        progress.runStageInParallel(
                parallelism,
                partitions,
                AnalyticsTablePartition::getName,
                partition -> tableManager.populateTable(params, partition));
    }

    /**
     * Creates indexes on the given tables.
     *
     * @param indexes  the list of {@link Index}.
     * @param progress the {@link JobProgress}.
     */
    private void doCreateIndexes(List<Index> indexes, JobProgress progress) {
        progress.runStageInParallel(
                this.parallelJobs, indexes, Index::getName, tableManager::createIndex);
    }

    private int doApplyAggregationLevels(
            AnalyticsTableType tableType, List<? extends Table> tables, JobProgress progress) {
        // TODO eliminate dep on organisationUnitService
        int maxLevels = organisationUnitService.getNumberOfOrganisationalLevels();

        int aggLevels = 0;
        // TODO can this be optimized by using a single query that takes all the levels?
        // e.g SELECT * FROM dataelement WHERE aggregationLevel IN (1, 2, 3, 4, 5)
        for (int i = 0; i < maxLevels; i++) {
            int level = maxLevels - i;

            List<String> dataElements =
                    IdentifiableObjectUtils.getUids(
                            // TODO eliminate dep on dataElementService
                            dataElementService.getDataElementsByAggregationLevel(level));

            if (!dataElements.isEmpty()) {
                progress.startingStage(
                        "Applying aggregation level " + level + " " + tableType, tables.size());
                progress.runStageInParallel(
                        this.parallelJobs,
                        tables,
                        Table::getName,
                        partition -> tableManager.applyAggregationLevels(partition, dataElements, level));

                aggLevels += dataElements.size();
            }
        }

        return aggLevels;
    }

    /**
     * Swaps the given analytics tables.
     *
     * @param params    the {@link AnalyticsTableUpdateParams}.
     * @param tables    the list of {@link AnalyticsTable}.
     * @param progress  the {@link JobProgress}.
     * @param tableType
     */
    private void doSwapTables(
            AnalyticsTableUpdateParams params, List<AnalyticsTable> tables, JobProgress progress, AnalyticsTableType tableType) {
        // TODO remove dep on resourceTableService
        resourceTableService.dropAllSqlViews(progress);

        progress.startingStage("Swapping analytics tables " + tableType, tables.size());
        progress.runStage(
                tables, AnalyticsTable::getName, table -> tableManager.swapTable(params, table));

        resourceTableService.createAllSqlViews(progress);
    }
}
