/*
 * Copyright (c) 2004-2023, University of Oslo
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
package org.hisp.dhis.analytics.common.query.jsonextractor;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.nonNull;
import static org.hisp.dhis.common.ValueType.ORGANISATION_UNIT;
import static org.hisp.dhis.feedback.ErrorCode.E7250;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.hisp.dhis.analytics.common.params.dimension.DimensionIdentifier;
import org.hisp.dhis.analytics.common.params.dimension.DimensionParam;
import org.hisp.dhis.analytics.common.params.dimension.DimensionParamObjectType;
import org.hisp.dhis.analytics.common.query.jsonextractor.JsonEnrollment.JsonEvent;
import org.hisp.dhis.analytics.tei.query.context.querybuilder.OffsetHelper;
import org.hisp.dhis.common.BaseIdentifiableObject;
import org.hisp.dhis.common.IdScheme;
import org.hisp.dhis.common.IllegalQueryException;
import org.hisp.dhis.common.ValueType;
import org.hisp.dhis.legend.LegendSet;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.support.rowset.SqlRowSet;

/**
 * A {@link SqlRowSet} implementation that extracts values from a JSON string column in the wrapped
 * {@link SqlRowSet} and returns them as if they were columns in the row set.
 */
public class SqlRowSetJsonExtractorDelegator extends SqlRowSetDelegator {

  private static final ObjectMapper OBJECT_MAPPER;

  private static final Map<ValueType, IdScheme> DEFAULT_ID_SCHEMES_BY_VALUE_TYPE =
      Map.of(ORGANISATION_UNIT, IdScheme.NAME);

  private static final Map<IdScheme, String> SUFFIX_BY_ID_SCHEME =
      Map.of(
          IdScheme.NAME, "_name",
          IdScheme.CODE, "_code");

  static {
    OBJECT_MAPPER = new ObjectMapper();
    OBJECT_MAPPER.findAndRegisterModules();
  }

  private static final Comparator<JsonEnrollment> ENR_ENROLLMENT_DATE_COMPARATOR =
      comparing(JsonEnrollment::getEnrollmentDate, nullsFirst(naturalOrder())).reversed();

  private static final Comparator<JsonEnrollment.JsonEvent> EVT_EXECUTION_DATE_COMPARATOR =
      comparing(JsonEnrollment.JsonEvent::getOccurredDate, nullsFirst(naturalOrder())).reversed();

  private final transient Map<String, DimensionIdentifier<DimensionParam>> dimIdByKey;

  private final List<String> existingColumnsInRowSet;

  public SqlRowSetJsonExtractorDelegator(
      SqlRowSet sqlRowSet, List<DimensionIdentifier<DimensionParam>> dimensionIdentifiers) {
    super(sqlRowSet);
    this.dimIdByKey = new HashMap<>();
    for (DimensionIdentifier<DimensionParam> dimensionIdentifier : dimensionIdentifiers) {
      if (!dimIdByKey.containsKey(dimensionIdentifier.getKey())) {
        dimIdByKey.put(dimensionIdentifier.getKey(), dimensionIdentifier);
      }
    }
    // we need to know which columns are in the sqlrowset, so that when a column is not present, we
    // can check if it is present in the json string
    this.existingColumnsInRowSet = Arrays.asList(sqlRowSet.getMetaData().getColumnNames());
  }

  @Override
  @SneakyThrows
  public Object getObject(String columnLabel) throws InvalidResultSetAccessException {
    // if the column is present in the rowset, we invoke the default behavior
    if (existingColumnsInRowSet.contains(columnLabel)) {
      return super.getObject(columnLabel);
    }
    // if the column is not present in the rowset, we check if it is present in the json string
    List<JsonEnrollment> enrollments =
        OBJECT_MAPPER.readValue(super.getString("enrollments"), new TypeReference<>() {});

    DimensionIdentifier<DimensionParam> dimensionIdentifier = dimIdByKey.get(columnLabel);

    if (dimensionIdentifier.isEnrollmentDimension()) {
      return getObjectForEnrollments(enrollments, dimensionIdentifier);
    }

    if (dimensionIdentifier.isEventDimension()) {
      return getObjectForEvents(enrollments, dimensionIdentifier);
    }
    throw new IllegalQueryException(E7250, dimensionIdentifier);
  }

  private Object getObjectForEvents(
      List<JsonEnrollment> enrollments, DimensionIdentifier<DimensionParam> dimensionIdentifier) {
    JsonEnrollment jsonEnrollment =
        OffsetHelper.getItemBasedOnOffset(
                enrollments.stream()
                    // gets only enrollments whose program is the same as specified in the dimension
                    .filter(
                        jEnr ->
                            jEnr.getProgramUid()
                                .equals(dimensionIdentifier.getProgram().getElement().getUid())),
                ENR_ENROLLMENT_DATE_COMPARATOR,
                dimensionIdentifier.getProgram().getOffsetWithDefault())
            .orElse(null);

    if (jsonEnrollment == null) {
      return null;
    }

    // sorts enrollments by enrollment date, descending
    JsonEvent jsonEvent =
        OffsetHelper.getItemBasedOnOffset(
                CollectionUtils.emptyIfNull(jsonEnrollment.getEvents()).stream()
                    // gets only events whose program stage is the same as specified in the
                    // dimension
                    .filter(
                        jEvt ->
                            jEvt.getProgramStageUid()
                                .equals(
                                    dimensionIdentifier.getProgramStage().getElement().getUid())),
                EVT_EXECUTION_DATE_COMPARATOR,
                dimensionIdentifier.getProgramStage().getOffsetWithDefault())
            .orElse(null);

    if (jsonEvent == null) {
      return null;
    }

    return Optional.of(jsonEvent)
        // extracts the value of the dimension from the event
        .map(je -> getEventExtractor(dimensionIdentifier.getDimension()).apply(je))
        .orElse(null);
  }

  private Object getObjectForEnrollments(
      List<JsonEnrollment> enrollments, DimensionIdentifier<DimensionParam> dimensionIdentifier) {
    Stream<JsonEnrollment> jsonEnrollmentStream =
        enrollments.stream()
            // gets only enrollments whose program is the same as specified in the dimension
            .filter(
                jsonEnrollment ->
                    jsonEnrollment
                        .getProgramUid()
                        .equals(dimensionIdentifier.getProgram().getElement().getUid()));

    return OffsetHelper.getItemBasedOnOffset(
            jsonEnrollmentStream,
            // sorts enrollments by enrollment date, descending
            ENR_ENROLLMENT_DATE_COMPARATOR,
            // skips the number of enrollments specified in the dimension (offset)
            dimensionIdentifier.getProgram().getOffsetWithDefault())
        // extracts the value of the dimension from the enrollment
        .map(
            jsonEnrollment ->
                getEnrollmentExtractor(dimensionIdentifier.getDimension()).apply(jsonEnrollment))
        .orElse(null);
  }

  /**
   * Returns a function that extracts the value of the dimension from the event.
   *
   * @param dimension the dimension
   * @return the function to extract the value of the dimension from the event
   */
  @SuppressWarnings("unchecked")
  private Function<JsonEnrollment.JsonEvent, Object> getEventExtractor(DimensionParam dimension) {
    if (dimension.isStaticDimension()) {
      return EventExtractor.byDimension(dimension.getStaticDimension()).getExtractor();
    }
    if (dimension.getDimensionParamObjectType().equals(DimensionParamObjectType.DATA_ELEMENT)) {
      // it is a data element dimension here
      return jsonEvent -> {
        String rawValue =
            Optional.of(jsonEvent)
                .map(JsonEvent::getEventDataValues)
                .map(map -> map.get(dimension.getQueryItem().getItemId()))
                .map(o -> (Map<String, Object>) o)
                .map(map -> map.get(getValueFieldName(dimension)))
                .map(Objects::toString)
                .orElse(null);

        if (Objects.isNull(rawValue)) {
          return null;
        }

        // apply legendSet mapping if present
        if (dimension.getQueryItem().hasLegendSet()) {
          return mapByLegendSet(dimension.getQueryItem().getLegendSet(), rawValue);
        }
        return dimension.transformValue(rawValue);
      };
    }
    if (dimension
        .getDimensionParamObjectType()
        .equals(DimensionParamObjectType.ORGANISATION_UNIT)) {
      return JsonEvent::getOrgUnitUid;
    }
    throw new IllegalStateException("Unknown dimension identifier " + dimension);
  }

  private Object mapByLegendSet(LegendSet legendSet, String rawValue) {
    // RawValue should be a double
    double value = Double.parseDouble(rawValue);
    return legendSet.getLegends().stream()
        .filter(legend -> value >= legend.getStartValue() && value < legend.getEndValue())
        .findFirst()
        .map(BaseIdentifiableObject::getDisplayName)
        .orElse(null);
  }

  private String getValueFieldName(DimensionParam dimension) {
    ValueType valueType = dimension.getValueType();
    IdScheme idScheme =
        Optional.of(dimension)
            .map(DimensionParam::getIdScheme)
            .orElse(nonNull(valueType) ? DEFAULT_ID_SCHEMES_BY_VALUE_TYPE.get(valueType) : null);
    if (nonNull(valueType)
        && nonNull(idScheme)
        && DEFAULT_ID_SCHEMES_BY_VALUE_TYPE.containsKey(valueType)
        && SUFFIX_BY_ID_SCHEME.containsKey(idScheme)) {
      return "value" + SUFFIX_BY_ID_SCHEME.get(idScheme);
    }
    return "value";
  }

  /**
   * Returns a function that extracts the value of the dimension from the enrollment.
   *
   * @param dimension the dimension
   * @return the function to extract the value of the dimension from the enrollment
   */
  private Function<JsonEnrollment, Object> getEnrollmentExtractor(DimensionParam dimension) {
    if (dimension.isStaticDimension()) {
      return EnrollmentExtractor.byDimension(dimension.getStaticDimension()).getExtractor();
    }
    if (dimension
        .getDimensionParamObjectType()
        .equals(DimensionParamObjectType.ORGANISATION_UNIT)) {
      return JsonEnrollment::getOrgUnitUid;
    }
    throw new IllegalQueryException(E7250, dimension.toString());
  }
}
