/*
 * Copyright (c) 2004-2024, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.hisp.dhis.analytics.data.sql.from.strategy;

import static org.hisp.dhis.analytics.AnalyticsConstants.ANALYTICS_TBL_ALIAS;
import static org.hisp.dhis.analytics.data.sql.AnalyticsColumns.PEENDDATE;
import static org.hisp.dhis.analytics.data.sql.AnalyticsColumns.PESTARTDATE;
import static org.hisp.dhis.analytics.data.sql.AnalyticsColumns.TEXTVALUE;
import static org.hisp.dhis.analytics.data.sql.AnalyticsColumns.VALUE;
import static org.hisp.dhis.util.DateUtils.toMediumDate;

import java.util.Date;
import org.hisp.dhis.analytics.DataQueryParams;
import org.hisp.dhis.analytics.data.sql.from.ColumnBuilder;
import org.hisp.dhis.db.sql.SqlBuilder;

/** Strategy for building first/last value subqueries */
public class FirstLastValueStrategy extends BaseSubqueryStrategy {
  private final ColumnBuilder columnBuilder;
  private final Date startDate;

  public FirstLastValueStrategy(
      DataQueryParams params, SqlBuilder sqlBuilder, ColumnBuilder columnBuilder, Date startDate) {
    super(params, sqlBuilder);
    this.columnBuilder = columnBuilder;
    this.startDate = startDate;
  }

  @Override
  public String buildSubquery() {
    Date latestDate = params.getLatestEndDate();
    String columns = columnBuilder.getFirstOrLastValueColumns();
    String partitionColumns = columnBuilder.getFirstOrLastValuePartitionColumns();
    String order = params.getAggregationType().isFirstPeriodAggregationType() ? "asc" : "desc";
    String fromSourceClause = getFromSourceClause() + " as " + ANALYTICS_TBL_ALIAS;

    return String.format(
        """
            (select %s, row_number() over (partition by %s order by peenddate %s, pestartdate %s) as pe_rank
             from %s
             where %s >= '%s' and
                %s <= '%s' and
                (%s is not null or %s is not null))
            """,
        columns,
        partitionColumns,
        order,
        order,
        fromSourceClause,
        sqlBuilder.quoteAx(PESTARTDATE),
        toMediumDate(startDate),
        sqlBuilder.quoteAx(PEENDDATE),
        toMediumDate(latestDate),
        sqlBuilder.quoteAx(VALUE),
        sqlBuilder.quoteAx(TEXTVALUE));
  }
}
