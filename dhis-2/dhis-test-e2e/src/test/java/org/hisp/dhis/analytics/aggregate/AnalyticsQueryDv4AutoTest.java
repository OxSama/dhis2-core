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
package org.hisp.dhis.analytics.aggregate;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hisp.dhis.analytics.ValidationHelper.validateHeader;
import static org.hisp.dhis.analytics.ValidationHelper.validateRow;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import java.util.List;
import java.util.Map;
import org.hisp.dhis.AnalyticsApiTest;
import org.hisp.dhis.actions.RestApiActions;
import org.hisp.dhis.dto.ApiResponse;
import org.hisp.dhis.helpers.QueryParamsBuilder;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** Groups e2e tests for "/analytics" aggregate endpoint. */
public class AnalyticsQueryDv4AutoTest extends AnalyticsApiTest {

  private RestApiActions actions;

  @BeforeAll
  public void setup() {
    actions = new RestApiActions("analytics");
  }

  @Test
  public void queryAnc3CoverageByDistrictsLast4Quarters() throws JSONException {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("filter=dx:sB79w2hiLp8")
            .add("skipData=false")
            .add("includeNumDen=false")
            .add("displayProperty=NAME")
            .add("skipMeta=false")
            .add(
                "dimension=ou:TEQlaapDQoK;Vth0fbpFcsO;bL4ooGhyHRQ;jmIPBj66vD6;qhqAxPSTUXp;LEVEL-wjP19dkFeIk,pe:LAST_4_QUARTERS")
            .add("relativePeriodDate=2022-01-01");

    // When
    ApiResponse response = actions.get(params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(3)))
        .body("rows", hasSize(equalTo(20)))
        .body("height", equalTo(20))
        .body("width", equalTo(3))
        .body("headerWidth", equalTo(3));

    // Assert metaData.
    String expectedMetaData =
        "{\"items\":{\"sB79w2hiLp8\":{\"name\":\"ANC 3 Coverage\"},\"TEQlaapDQoK\":{\"name\":\"Port Loko\"},\"Vth0fbpFcsO\":{\"name\":\"Kono\"},\"ou\":{\"name\":\"Organisation unit\"},\"bL4ooGhyHRQ\":{\"name\":\"Pujehun\"},\"wjP19dkFeIk\":{\"uid\":\"wjP19dkFeIk\",\"name\":\"District\"},\"2021Q4\":{\"name\":\"October - December 2021\"},\"2021Q2\":{\"name\":\"April - June 2021\"},\"2021Q3\":{\"name\":\"July - September 2021\"},\"2021Q1\":{\"name\":\"January - March 2021\"},\"dx\":{\"name\":\"Data\"},\"pe\":{\"name\":\"Period\"},\"LAST_4_QUARTERS\":{\"name\":\"Last 4 quarters\"},\"qhqAxPSTUXp\":{\"name\":\"Koinadugu\"},\"jmIPBj66vD6\":{\"name\":\"Moyamba\"}},\"dimensions\":{\"dx\":[\"sB79w2hiLp8\"],\"pe\":[\"2021Q1\",\"2021Q2\",\"2021Q3\",\"2021Q4\"],\"ou\":[\"qhqAxPSTUXp\",\"Vth0fbpFcsO\",\"jmIPBj66vD6\",\"TEQlaapDQoK\",\"bL4ooGhyHRQ\"],\"co\":[]}}";
    String actualMetaData = new JSONObject((Map) response.extract("metaData")).toString();
    assertEquals(expectedMetaData, actualMetaData, false);

    // Assert headers.
    validateHeader(response, 0, "ou", "Organisation unit", "TEXT", "java.lang.String", false, true);
    validateHeader(response, 1, "pe", "Period", "TEXT", "java.lang.String", false, true);
    validateHeader(response, 2, "value", "Value", "NUMBER", "java.lang.Double", false, false);

    // Assert rows.
    validateRow(response, List.of("qhqAxPSTUXp", "2021Q1", "41.96"));
    validateRow(response, List.of("qhqAxPSTUXp", "2021Q2", "44.18"));
    validateRow(response, List.of("qhqAxPSTUXp", "2021Q3", "51.67"));
    validateRow(response, List.of("qhqAxPSTUXp", "2021Q4", "20.46"));
    validateRow(response, List.of("Vth0fbpFcsO", "2021Q1", "42.31"));
    validateRow(response, List.of("Vth0fbpFcsO", "2021Q2", "48.77"));
    validateRow(response, List.of("Vth0fbpFcsO", "2021Q3", "43.88"));
    validateRow(response, List.of("Vth0fbpFcsO", "2021Q4", "15.94"));
    validateRow(response, List.of("jmIPBj66vD6", "2021Q1", "98.27"));
    validateRow(response, List.of("jmIPBj66vD6", "2021Q2", "99.02"));
    validateRow(response, List.of("jmIPBj66vD6", "2021Q3", "96.13"));
    validateRow(response, List.of("jmIPBj66vD6", "2021Q4", "83.89"));
    validateRow(response, List.of("TEQlaapDQoK", "2021Q1", "45.78"));
    validateRow(response, List.of("TEQlaapDQoK", "2021Q2", "55.62"));
    validateRow(response, List.of("TEQlaapDQoK", "2021Q3", "57.1"));
    validateRow(response, List.of("TEQlaapDQoK", "2021Q4", "36.57"));
    validateRow(response, List.of("bL4ooGhyHRQ", "2021Q1", "75.32"));
    validateRow(response, List.of("bL4ooGhyHRQ", "2021Q2", "74.83"));
    validateRow(response, List.of("bL4ooGhyHRQ", "2021Q3", "82.02"));
    validateRow(response, List.of("bL4ooGhyHRQ", "2021Q4", "0.74"));
  }

  @Test
  public void queryAnc3CoverageThisYeargauge() throws JSONException {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("filter=pe:THIS_YEAR")
            .add("skipData=false")
            .add("includeNumDen=false")
            .add("displayProperty=NAME")
            .add("skipMeta=false")
            .add("dimension=dx:sB79w2hiLp8")
            .add("relativePeriodDate=2022-01-01");

    // When
    ApiResponse response = actions.get(params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(2)))
        .body("rows", hasSize(equalTo(1)))
        .body("height", equalTo(1))
        .body("width", equalTo(2))
        .body("headerWidth", equalTo(2));

    // Assert metaData.
    String expectedMetaData =
        "{\"items\":{\"sB79w2hiLp8\":{\"name\":\"ANC 3 Coverage\"},\"dx\":{\"name\":\"Data\"},\"pe\":{\"name\":\"Period\"},\"THIS_YEAR\":{\"name\":\"This year\"},\"2022\":{\"name\":\"2022\"}},\"dimensions\":{\"dx\":[\"sB79w2hiLp8\"],\"pe\":[\"2022\"],\"co\":[]}}";
    String actualMetaData = new JSONObject((Map) response.extract("metaData")).toString();
    assertEquals(expectedMetaData, actualMetaData, false);

    // Assert headers.
    validateHeader(response, 0, "dx", "Data", "TEXT", "java.lang.String", false, true);
    validateHeader(response, 1, "value", "Value", "NUMBER", "java.lang.Double", false, false);

    // Assert rows.
    validateRow(response, List.of("sB79w2hiLp8", "65.83"));
  }

  @Test
  public void queryAnc3rdVisitsByFacilityTypeLast12Months100StackedColumns() throws JSONException {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("filter=ou:ImspTQPwCqd,dx:Jtf34kNZhzP")
            .add("skipData=false")
            .add("includeNumDen=false")
            .add("displayProperty=NAME")
            .add("skipMeta=false")
            .add(
                "dimension=J5jldMd8OHv:uYxK4wmcPqA;EYbopBOJWsW;RXL3lPSK8oG;CXw2yu5fodb,pe:LAST_12_MONTHS")
            .add("relativePeriodDate=2022-01-01");

    // When
    ApiResponse response = actions.get(params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(3)))
        .body("rows", hasSize(equalTo(48)))
        .body("height", equalTo(48))
        .body("width", equalTo(3))
        .body("headerWidth", equalTo(3));

    // Assert metaData.
    String expectedMetaData =
        "{\"items\":{\"J5jldMd8OHv\":{\"name\":\"Facility Type\"},\"202109\":{\"name\":\"September 2021\"},\"202107\":{\"name\":\"July 2021\"},\"202108\":{\"name\":\"August 2021\"},\"202105\":{\"name\":\"May 2021\"},\"uYxK4wmcPqA\":{\"name\":\"CHP\"},\"202106\":{\"name\":\"June 2021\"},\"202103\":{\"name\":\"March 2021\"},\"202104\":{\"name\":\"April 2021\"},\"LAST_12_MONTHS\":{\"name\":\"Last 12 months\"},\"202112\":{\"name\":\"December 2021\"},\"ImspTQPwCqd\":{\"name\":\"Sierra Leone\"},\"202110\":{\"name\":\"October 2021\"},\"202111\":{\"name\":\"November 2021\"},\"dx\":{\"name\":\"Data\"},\"pq2XI5kz2BY\":{\"name\":\"Fixed\"},\"Jtf34kNZhzP\":{\"name\":\"ANC 3rd visit\"},\"PT59n8BQbqM\":{\"name\":\"Outreach\"},\"RXL3lPSK8oG\":{\"name\":\"Clinic\"},\"ou\":{\"name\":\"Organisation unit\"},\"CXw2yu5fodb\":{\"name\":\"CHC\"},\"202101\":{\"name\":\"January 2021\"},\"202102\":{\"name\":\"February 2021\"},\"pe\":{\"name\":\"Period\"},\"EYbopBOJWsW\":{\"name\":\"MCHP\"}},\"dimensions\":{\"dx\":[\"Jtf34kNZhzP\"],\"pe\":[\"202101\",\"202102\",\"202103\",\"202104\",\"202105\",\"202106\",\"202107\",\"202108\",\"202109\",\"202110\",\"202111\",\"202112\"],\"J5jldMd8OHv\":[\"uYxK4wmcPqA\",\"EYbopBOJWsW\",\"RXL3lPSK8oG\",\"CXw2yu5fodb\"],\"ou\":[\"ImspTQPwCqd\"],\"co\":[\"pq2XI5kz2BY\",\"PT59n8BQbqM\"]}}";
    String actualMetaData = new JSONObject((Map) response.extract("metaData")).toString();
    assertEquals(expectedMetaData, actualMetaData, false);

    // Assert headers.
    validateHeader(
        response, 0, "J5jldMd8OHv", "Facility Type", "TEXT", "java.lang.String", false, true);
    validateHeader(response, 1, "pe", "Period", "TEXT", "java.lang.String", false, true);
    validateHeader(response, 2, "value", "Value", "NUMBER", "java.lang.Double", false, false);

    // Assert rows.
    validateRow(response, List.of("RXL3lPSK8oG", "202102", "472"));
    validateRow(response, List.of("RXL3lPSK8oG", "202101", "549"));
    validateRow(response, List.of("RXL3lPSK8oG", "202104", "440"));
    validateRow(response, List.of("RXL3lPSK8oG", "202103", "502"));
    validateRow(response, List.of("uYxK4wmcPqA", "202105", "2636"));
    validateRow(response, List.of("RXL3lPSK8oG", "202109", "634"));
    validateRow(response, List.of("uYxK4wmcPqA", "202104", "2294"));
    validateRow(response, List.of("uYxK4wmcPqA", "202103", "2520"));
    validateRow(response, List.of("uYxK4wmcPqA", "202102", "2205"));
    validateRow(response, List.of("RXL3lPSK8oG", "202106", "603"));
    validateRow(response, List.of("uYxK4wmcPqA", "202101", "2164"));
    validateRow(response, List.of("RXL3lPSK8oG", "202105", "500"));
    validateRow(response, List.of("RXL3lPSK8oG", "202108", "495"));
    validateRow(response, List.of("RXL3lPSK8oG", "202107", "617"));
    validateRow(response, List.of("uYxK4wmcPqA", "202109", "2685"));
    validateRow(response, List.of("uYxK4wmcPqA", "202108", "2814"));
    validateRow(response, List.of("uYxK4wmcPqA", "202107", "2717"));
    validateRow(response, List.of("uYxK4wmcPqA", "202106", "2809"));
    validateRow(response, List.of("uYxK4wmcPqA", "202112", "1813"));
    validateRow(response, List.of("uYxK4wmcPqA", "202111", "2250"));
    validateRow(response, List.of("uYxK4wmcPqA", "202110", "2135"));
    validateRow(response, List.of("CXw2yu5fodb", "202107", "4097"));
    validateRow(response, List.of("CXw2yu5fodb", "202106", "4643"));
    validateRow(response, List.of("CXw2yu5fodb", "202109", "4505"));
    validateRow(response, List.of("CXw2yu5fodb", "202108", "4137"));
    validateRow(response, List.of("EYbopBOJWsW", "202102", "5134"));
    validateRow(response, List.of("EYbopBOJWsW", "202101", "5016"));
    validateRow(response, List.of("CXw2yu5fodb", "202110", "3219"));
    validateRow(response, List.of("CXw2yu5fodb", "202112", "2983"));
    validateRow(response, List.of("CXw2yu5fodb", "202111", "3807"));
    validateRow(response, List.of("EYbopBOJWsW", "202112", "4168"));
    validateRow(response, List.of("EYbopBOJWsW", "202111", "7058"));
    validateRow(response, List.of("EYbopBOJWsW", "202110", "4930"));
    validateRow(response, List.of("RXL3lPSK8oG", "202112", "518"));
    validateRow(response, List.of("EYbopBOJWsW", "202109", "5977"));
    validateRow(response, List.of("EYbopBOJWsW", "202108", "5894"));
    validateRow(response, List.of("EYbopBOJWsW", "202107", "5898"));
    validateRow(response, List.of("EYbopBOJWsW", "202106", "6237"));
    validateRow(response, List.of("EYbopBOJWsW", "202105", "6729"));
    validateRow(response, List.of("RXL3lPSK8oG", "202111", "565"));
    validateRow(response, List.of("EYbopBOJWsW", "202104", "5444"));
    validateRow(response, List.of("EYbopBOJWsW", "202103", "5645"));
    validateRow(response, List.of("RXL3lPSK8oG", "202110", "483"));
    validateRow(response, List.of("CXw2yu5fodb", "202103", "3974"));
    validateRow(response, List.of("CXw2yu5fodb", "202102", "3392"));
    validateRow(response, List.of("CXw2yu5fodb", "202105", "4179"));
    validateRow(response, List.of("CXw2yu5fodb", "202104", "3542"));
    validateRow(response, List.of("CXw2yu5fodb", "202101", "3318"));
  }

  @Test
  public void queryAncAtFacilitiesInBoThisMonthWithHiddenEmptyColumns() throws JSONException {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("skipData=false")
            .add("includeNumDen=true")
            .add("displayProperty=NAME")
            .add("skipMeta=false")
            .add(
                "dimension=ou:zFDYIgyGmXG;BGGmAwx33dj;YmmeuGbqOwR;YuQRtpLP10I;LEVEL-m9lBJogzE95,pe:THIS_MONTH,dx:fbfJHSPpUQD;cYeuwXTCPkU;Jtf34kNZhzP;hfdmMSPBgLG;bqK6eSIwo3h;yTHydhurQQU;V37YqbqpEhV;SA7WeFZnUci")
            .add("relativePeriodDate=2022-01-01");

    // When
    ApiResponse response = actions.get(params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(9)))
        .body("rows", hasSize(equalTo(114)))
        .body("height", equalTo(114))
        .body("width", equalTo(9))
        .body("headerWidth", equalTo(9));

    // Assert metaData.
    String expectedMetaData =
        "{\"items\":{\"p9ZtyC3LQ9f\":{\"name\":\"Niagorehun CHP\"},\"lvxIJAb2QJo\":{\"name\":\"Sembehun Mamagewor MCHP\"},\"AXZq6q7Dr6E\":{\"name\":\"Buma MCHP\"},\"cJkZLwhL8RP\":{\"name\":\"Kasse MCHP\"},\"m9lBJogzE95\":{\"uid\":\"m9lBJogzE95\",\"name\":\"Facility\"},\"xt08cuqf1ys\":{\"name\":\"Mokoba MCHP\"},\"SA7WeFZnUci\":{\"name\":\"IPT 2nd dose given by TBA\"},\"THIS_MONTH\":{\"name\":\"This month\"},\"jGYT5U5qJP6\":{\"name\":\"Gbaiima CHC\"},\"zFDYIgyGmXG\":{\"uid\":\"zFDYIgyGmXG\",\"code\":\"OU_542\",\"name\":\"Bargbo\"},\"hfdmMSPBgLG\":{\"name\":\"ANC 4th or more visits\"},\"dx\":{\"name\":\"Data\"},\"KvE0PYQzXMM\":{\"name\":\"Mano Yorgbo MCHP\"},\"pq2XI5kz2BY\":{\"name\":\"Fixed\"},\"vELbGdEphPd\":{\"name\":\"Jimmi CHC\"},\"Tht0fnjagHi\":{\"name\":\"Serabu Hospital Mission\"},\"Jtf34kNZhzP\":{\"name\":\"ANC 3rd visit\"},\"PT59n8BQbqM\":{\"name\":\"Outreach\"},\"ctMepV9p92I\":{\"name\":\"Gbangbalia MCHP\"},\"prNiMdHuaaU\":{\"name\":\"Serabu (Bumpe Ngao) UFC\"},\"kEkU53NrFmy\":{\"name\":\"Taninahun (BN) CHP\"},\"DiszpKrYNg8\":{\"name\":\"Ngelehun CHC\"},\"E497Rk80ivZ\":{\"name\":\"Bumpe CHC\"},\"YmmeuGbqOwR\":{\"uid\":\"YmmeuGbqOwR\",\"code\":\"OU_544\",\"name\":\"Gbo\"},\"yTHydhurQQU\":{\"name\":\"IPT 1st dose given by TBA\"},\"fA43H8Ds0Ja\":{\"name\":\"Momajo MCHP\"},\"ou\":{\"name\":\"Organisation unit\"},\"fbfJHSPpUQD\":{\"name\":\"ANC 1st visit\"},\"YuQRtpLP10I\":{\"uid\":\"YuQRtpLP10I\",\"code\":\"OU_539\",\"name\":\"Badjia\"},\"EJoI3HArJ2W\":{\"name\":\"Bum Kaku MCHP\"},\"CTOMXJg41hz\":{\"name\":\"Kaniya MCHP\"},\"wwM3YPvBKu2\":{\"name\":\"Ngolahun CHC\"},\"RTixJpRqS4C\":{\"name\":\"Kpetema CHP\"},\"g8upMTyEZGZ\":{\"name\":\"Njandama MCHP\"},\"202201\":{\"name\":\"January 2022\"},\"BGGmAwx33dj\":{\"uid\":\"BGGmAwx33dj\",\"code\":\"OU_543\",\"name\":\"Bumpe Ngao\"},\"pe\":{\"name\":\"Period\"},\"cYeuwXTCPkU\":{\"name\":\"ANC 2nd visit\"},\"bqK6eSIwo3h\":{\"name\":\"IPT 1st dose given at PHU\"},\"am6EFqHGKeU\":{\"name\":\"Mokpende MCHP\"},\"V37YqbqpEhV\":{\"name\":\"IPT 2nd dose given at PHU\"},\"tZxqVn3xNrA\":{\"name\":\"Wallehun MCHP\"},\"EFTcruJcNmZ\":{\"name\":\"Yengema CHP\"}},\"dimensions\":{\"dx\":[\"fbfJHSPpUQD\",\"cYeuwXTCPkU\",\"Jtf34kNZhzP\",\"hfdmMSPBgLG\",\"bqK6eSIwo3h\",\"yTHydhurQQU\",\"V37YqbqpEhV\",\"SA7WeFZnUci\"],\"pe\":[\"202201\"],\"ou\":[\"EJoI3HArJ2W\",\"AXZq6q7Dr6E\",\"E497Rk80ivZ\",\"jGYT5U5qJP6\",\"ctMepV9p92I\",\"vELbGdEphPd\",\"CTOMXJg41hz\",\"cJkZLwhL8RP\",\"RTixJpRqS4C\",\"KvE0PYQzXMM\",\"xt08cuqf1ys\",\"am6EFqHGKeU\",\"fA43H8Ds0Ja\",\"DiszpKrYNg8\",\"wwM3YPvBKu2\",\"p9ZtyC3LQ9f\",\"g8upMTyEZGZ\",\"lvxIJAb2QJo\",\"prNiMdHuaaU\",\"Tht0fnjagHi\",\"kEkU53NrFmy\",\"tZxqVn3xNrA\",\"EFTcruJcNmZ\"],\"co\":[\"pq2XI5kz2BY\",\"PT59n8BQbqM\"]}}";
    String actualMetaData = new JSONObject((Map) response.extract("metaData")).toString();
    assertEquals(expectedMetaData, actualMetaData, false);

    // Assert headers.
    validateHeader(response, 0, "dx", "Data", "TEXT", "java.lang.String", false, true);
    validateHeader(response, 1, "ou", "Organisation unit", "TEXT", "java.lang.String", false, true);
    validateHeader(response, 2, "pe", "Period", "TEXT", "java.lang.String", false, true);
    validateHeader(response, 3, "value", "Value", "NUMBER", "java.lang.Double", false, false);
    validateHeader(
        response, 4, "numerator", "Numerator", "NUMBER", "java.lang.Double", false, false);
    validateHeader(
        response, 5, "denominator", "Denominator", "NUMBER", "java.lang.Double", false, false);
    validateHeader(response, 6, "factor", "Factor", "NUMBER", "java.lang.Double", false, false);
    validateHeader(
        response, 7, "multiplier", "Multiplier", "NUMBER", "java.lang.Double", false, false);
    validateHeader(response, 8, "divisor", "Divisor", "NUMBER", "java.lang.Double", false, false);

    // Assert rows.
    validateRow(
        response, List.of("Jtf34kNZhzP", "am6EFqHGKeU", "202201", "28", "", "", "", "", ""));
    validateRow(
        response, List.of("bqK6eSIwo3h", "DiszpKrYNg8", "202201", "57", "", "", "", "", ""));
    validateRow(
        response, List.of("fbfJHSPpUQD", "prNiMdHuaaU", "202201", "30", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "prNiMdHuaaU", "202201", "32", "", "", "", "", ""));
    validateRow(response, List.of("bqK6eSIwo3h", "xt08cuqf1ys", "202201", "9", "", "", "", "", ""));
    validateRow(
        response, List.of("fbfJHSPpUQD", "vELbGdEphPd", "202201", "160", "", "", "", "", ""));
    validateRow(response, List.of("hfdmMSPBgLG", "prNiMdHuaaU", "202201", "1", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "am6EFqHGKeU", "202201", "20", "", "", "", "", ""));
    validateRow(response, List.of("yTHydhurQQU", "RTixJpRqS4C", "202201", "7", "", "", "", "", ""));
    validateRow(
        response, List.of("Jtf34kNZhzP", "prNiMdHuaaU", "202201", "15", "", "", "", "", ""));
    validateRow(
        response, List.of("bqK6eSIwo3h", "kEkU53NrFmy", "202201", "32", "", "", "", "", ""));
    validateRow(
        response, List.of("V37YqbqpEhV", "prNiMdHuaaU", "202201", "20", "", "", "", "", ""));
    validateRow(response, List.of("fbfJHSPpUQD", "ctMepV9p92I", "202201", "5", "", "", "", "", ""));
    validateRow(
        response, List.of("V37YqbqpEhV", "jGYT5U5qJP6", "202201", "12", "", "", "", "", ""));
    validateRow(response, List.of("hfdmMSPBgLG", "vELbGdEphPd", "202201", "3", "", "", "", "", ""));
    validateRow(response, List.of("yTHydhurQQU", "ctMepV9p92I", "202201", "9", "", "", "", "", ""));
    validateRow(response, List.of("Jtf34kNZhzP", "jGYT5U5qJP6", "202201", "9", "", "", "", "", ""));
    validateRow(
        response, List.of("Jtf34kNZhzP", "vELbGdEphPd", "202201", "39", "", "", "", "", ""));
    validateRow(response, List.of("Jtf34kNZhzP", "xt08cuqf1ys", "202201", "3", "", "", "", "", ""));
    validateRow(response, List.of("V37YqbqpEhV", "vELbGdEphPd", "202201", "4", "", "", "", "", ""));
    validateRow(
        response, List.of("bqK6eSIwo3h", "EFTcruJcNmZ", "202201", "10", "", "", "", "", ""));
    validateRow(response, List.of("bqK6eSIwo3h", "EJoI3HArJ2W", "202201", "6", "", "", "", "", ""));
    validateRow(response, List.of("V37YqbqpEhV", "xt08cuqf1ys", "202201", "5", "", "", "", "", ""));
    validateRow(response, List.of("SA7WeFZnUci", "EJoI3HArJ2W", "202201", "3", "", "", "", "", ""));
    validateRow(response, List.of("cYeuwXTCPkU", "g8upMTyEZGZ", "202201", "6", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "jGYT5U5qJP6", "202201", "20", "", "", "", "", ""));
    validateRow(response, List.of("hfdmMSPBgLG", "ctMepV9p92I", "202201", "3", "", "", "", "", ""));
    validateRow(response, List.of("V37YqbqpEhV", "EJoI3HArJ2W", "202201", "1", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "xt08cuqf1ys", "202201", "20", "", "", "", "", ""));
    validateRow(
        response, List.of("bqK6eSIwo3h", "E497Rk80ivZ", "202201", "32", "", "", "", "", ""));
    validateRow(
        response, List.of("bqK6eSIwo3h", "am6EFqHGKeU", "202201", "35", "", "", "", "", ""));
    validateRow(response, List.of("bqK6eSIwo3h", "p9ZtyC3LQ9f", "202201", "7", "", "", "", "", ""));
    validateRow(
        response, List.of("V37YqbqpEhV", "DiszpKrYNg8", "202201", "57", "", "", "", "", ""));
    validateRow(
        response, List.of("Jtf34kNZhzP", "EFTcruJcNmZ", "202201", "16", "", "", "", "", ""));
    validateRow(response, List.of("fbfJHSPpUQD", "fA43H8Ds0Ja", "202201", "6", "", "", "", "", ""));
    validateRow(response, List.of("Jtf34kNZhzP", "ctMepV9p92I", "202201", "3", "", "", "", "", ""));
    validateRow(response, List.of("yTHydhurQQU", "CTOMXJg41hz", "202201", "4", "", "", "", "", ""));
    validateRow(
        response, List.of("Jtf34kNZhzP", "fA43H8Ds0Ja", "202201", "39", "", "", "", "", ""));
    validateRow(
        response, List.of("hfdmMSPBgLG", "fA43H8Ds0Ja", "202201", "13", "", "", "", "", ""));
    validateRow(response, List.of("Jtf34kNZhzP", "g8upMTyEZGZ", "202201", "3", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "KvE0PYQzXMM", "202201", "13", "", "", "", "", ""));
    validateRow(
        response, List.of("bqK6eSIwo3h", "KvE0PYQzXMM", "202201", "13", "", "", "", "", ""));
    validateRow(
        response, List.of("yTHydhurQQU", "DiszpKrYNg8", "202201", "57", "", "", "", "", ""));
    validateRow(
        response, List.of("V37YqbqpEhV", "cJkZLwhL8RP", "202201", "22", "", "", "", "", ""));
    validateRow(
        response, List.of("fbfJHSPpUQD", "DiszpKrYNg8", "202201", "13", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "vELbGdEphPd", "202201", "79", "", "", "", "", ""));
    validateRow(response, List.of("hfdmMSPBgLG", "KvE0PYQzXMM", "202201", "4", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "E497Rk80ivZ", "202201", "35", "", "", "", "", ""));
    validateRow(response, List.of("Jtf34kNZhzP", "KvE0PYQzXMM", "202201", "7", "", "", "", "", ""));
    validateRow(
        response, List.of("bqK6eSIwo3h", "jGYT5U5qJP6", "202201", "69", "", "", "", "", ""));
    validateRow(response, List.of("bqK6eSIwo3h", "g8upMTyEZGZ", "202201", "7", "", "", "", "", ""));
    validateRow(response, List.of("hfdmMSPBgLG", "am6EFqHGKeU", "202201", "8", "", "", "", "", ""));
    validateRow(response, List.of("SA7WeFZnUci", "RTixJpRqS4C", "202201", "5", "", "", "", "", ""));
    validateRow(response, List.of("fbfJHSPpUQD", "cJkZLwhL8RP", "202201", "7", "", "", "", "", ""));
    validateRow(
        response, List.of("yTHydhurQQU", "cJkZLwhL8RP", "202201", "15", "", "", "", "", ""));
    validateRow(
        response, List.of("fbfJHSPpUQD", "RTixJpRqS4C", "202201", "18", "", "", "", "", ""));
    validateRow(
        response, List.of("hfdmMSPBgLG", "EFTcruJcNmZ", "202201", "23", "", "", "", "", ""));
    validateRow(
        response, List.of("SA7WeFZnUci", "DiszpKrYNg8", "202201", "46", "", "", "", "", ""));
    validateRow(response, List.of("V37YqbqpEhV", "kEkU53NrFmy", "202201", "7", "", "", "", "", ""));
    validateRow(response, List.of("SA7WeFZnUci", "CTOMXJg41hz", "202201", "7", "", "", "", "", ""));
    validateRow(response, List.of("Jtf34kNZhzP", "p9ZtyC3LQ9f", "202201", "7", "", "", "", "", ""));
    validateRow(
        response, List.of("fbfJHSPpUQD", "CTOMXJg41hz", "202201", "20", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "CTOMXJg41hz", "202201", "25", "", "", "", "", ""));
    validateRow(
        response, List.of("V37YqbqpEhV", "RTixJpRqS4C", "202201", "12", "", "", "", "", ""));
    validateRow(response, List.of("Jtf34kNZhzP", "E497Rk80ivZ", "202201", "5", "", "", "", "", ""));
    validateRow(response, List.of("hfdmMSPBgLG", "p9ZtyC3LQ9f", "202201", "1", "", "", "", "", ""));
    validateRow(
        response, List.of("hfdmMSPBgLG", "CTOMXJg41hz", "202201", "29", "", "", "", "", ""));
    validateRow(
        response, List.of("Jtf34kNZhzP", "RTixJpRqS4C", "202201", "13", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "p9ZtyC3LQ9f", "202201", "18", "", "", "", "", ""));
    validateRow(response, List.of("V37YqbqpEhV", "E497Rk80ivZ", "202201", "1", "", "", "", "", ""));
    validateRow(
        response, List.of("bqK6eSIwo3h", "ctMepV9p92I", "202201", "24", "", "", "", "", ""));
    validateRow(
        response, List.of("Jtf34kNZhzP", "CTOMXJg41hz", "202201", "27", "", "", "", "", ""));
    validateRow(
        response, List.of("bqK6eSIwo3h", "vELbGdEphPd", "202201", "40", "", "", "", "", ""));
    validateRow(
        response, List.of("fbfJHSPpUQD", "kEkU53NrFmy", "202201", "32", "", "", "", "", ""));
    validateRow(
        response, List.of("V37YqbqpEhV", "CTOMXJg41hz", "202201", "18", "", "", "", "", ""));
    validateRow(response, List.of("cYeuwXTCPkU", "kEkU53NrFmy", "202201", "7", "", "", "", "", ""));
    validateRow(
        response, List.of("bqK6eSIwo3h", "prNiMdHuaaU", "202201", "37", "", "", "", "", ""));
    validateRow(
        response, List.of("fbfJHSPpUQD", "EFTcruJcNmZ", "202201", "10", "", "", "", "", ""));
    validateRow(
        response, List.of("fbfJHSPpUQD", "p9ZtyC3LQ9f", "202201", "24", "", "", "", "", ""));
    validateRow(response, List.of("yTHydhurQQU", "EJoI3HArJ2W", "202201", "4", "", "", "", "", ""));
    validateRow(
        response, List.of("V37YqbqpEhV", "ctMepV9p92I", "202201", "15", "", "", "", "", ""));
    validateRow(
        response, List.of("bqK6eSIwo3h", "fA43H8Ds0Ja", "202201", "72", "", "", "", "", ""));
    validateRow(
        response, List.of("yTHydhurQQU", "xt08cuqf1ys", "202201", "12", "", "", "", "", ""));
    validateRow(
        response, List.of("hfdmMSPBgLG", "RTixJpRqS4C", "202201", "14", "", "", "", "", ""));
    validateRow(response, List.of("hfdmMSPBgLG", "DiszpKrYNg8", "202201", "1", "", "", "", "", ""));
    validateRow(response, List.of("fbfJHSPpUQD", "EJoI3HArJ2W", "202201", "6", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "EFTcruJcNmZ", "202201", "18", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "EJoI3HArJ2W", "202201", "11", "", "", "", "", ""));
    validateRow(response, List.of("hfdmMSPBgLG", "xt08cuqf1ys", "202201", "1", "", "", "", "", ""));
    validateRow(response, List.of("cYeuwXTCPkU", "ctMepV9p92I", "202201", "8", "", "", "", "", ""));
    validateRow(
        response, List.of("Jtf34kNZhzP", "DiszpKrYNg8", "202201", "48", "", "", "", "", ""));
    validateRow(response, List.of("bqK6eSIwo3h", "RTixJpRqS4C", "202201", "8", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "fA43H8Ds0Ja", "202201", "18", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "cJkZLwhL8RP", "202201", "14", "", "", "", "", ""));
    validateRow(response, List.of("Jtf34kNZhzP", "EJoI3HArJ2W", "202201", "1", "", "", "", "", ""));
    validateRow(
        response, List.of("V37YqbqpEhV", "EFTcruJcNmZ", "202201", "18", "", "", "", "", ""));
    validateRow(response, List.of("hfdmMSPBgLG", "cJkZLwhL8RP", "202201", "9", "", "", "", "", ""));
    validateRow(
        response, List.of("fbfJHSPpUQD", "am6EFqHGKeU", "202201", "35", "", "", "", "", ""));
    validateRow(
        response, List.of("fbfJHSPpUQD", "xt08cuqf1ys", "202201", "15", "", "", "", "", ""));
    validateRow(
        response, List.of("fbfJHSPpUQD", "KvE0PYQzXMM", "202201", "10", "", "", "", "", ""));
    validateRow(response, List.of("Jtf34kNZhzP", "cJkZLwhL8RP", "202201", "6", "", "", "", "", ""));
    validateRow(response, List.of("Jtf34kNZhzP", "kEkU53NrFmy", "202201", "7", "", "", "", "", ""));
    validateRow(response, List.of("SA7WeFZnUci", "xt08cuqf1ys", "202201", "3", "", "", "", "", ""));
    validateRow(response, List.of("V37YqbqpEhV", "g8upMTyEZGZ", "202201", "3", "", "", "", "", ""));
    validateRow(response, List.of("bqK6eSIwo3h", "cJkZLwhL8RP", "202201", "7", "", "", "", "", ""));
    validateRow(
        response, List.of("fbfJHSPpUQD", "E497Rk80ivZ", "202201", "50", "", "", "", "", ""));
    validateRow(
        response, List.of("fbfJHSPpUQD", "jGYT5U5qJP6", "202201", "23", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "DiszpKrYNg8", "202201", "36", "", "", "", "", ""));
    validateRow(
        response, List.of("V37YqbqpEhV", "KvE0PYQzXMM", "202201", "12", "", "", "", "", ""));
    validateRow(response, List.of("fbfJHSPpUQD", "g8upMTyEZGZ", "202201", "7", "", "", "", "", ""));
    validateRow(
        response, List.of("V37YqbqpEhV", "p9ZtyC3LQ9f", "202201", "12", "", "", "", "", ""));
    validateRow(
        response, List.of("cYeuwXTCPkU", "RTixJpRqS4C", "202201", "30", "", "", "", "", ""));
    validateRow(
        response, List.of("bqK6eSIwo3h", "CTOMXJg41hz", "202201", "13", "", "", "", "", ""));
    validateRow(
        response, List.of("V37YqbqpEhV", "am6EFqHGKeU", "202201", "20", "", "", "", "", ""));
  }

  @Test
  public void queryAncByAreaLast12Months() throws JSONException {
// Given
    QueryParamsBuilder params = new QueryParamsBuilder().add("filter=ou:ImspTQPwCqd")
            .add("skipData=false")
            .add("includeNumDen=true")
            .add("displayProperty=NAME")
            .add("skipMeta=false")
            .add("dimension=dx:Uvn6LCg7dVU;ReUHfIn0pTQ;OdiHJayrsKo;Lzg9LtG1xg3;sB79w2hiLp8;AUqdhY4mpvp;dwEq7wi6nXV;c8fABiNpT0B,pe:LAST_12_MONTHS,uIuxlbV1vRT:J40PpdN4Wkk;b0EsAxm8Nge;jqBqIXoXpfy;nlX2VoouN63")
            .add("relativePeriodDate=2022-01-01");

// When
    ApiResponse response = actions.get(params);

// Then
    response.validate().statusCode(200)
            .body("headers", hasSize(equalTo(9)))
            .body("rows", hasSize(equalTo(384)))
            .body("height", equalTo(384))
            .body("width", equalTo(9))
            .body("headerWidth", equalTo(9));

// Assert metaData.
    String expectedMetaData = "{\"items\":{\"nlX2VoouN63\":{\"name\":\"Eastern Area\"},\"sB79w2hiLp8\":{\"name\":\"ANC 3 Coverage\"},\"OdiHJayrsKo\":{\"name\":\"ANC 2 Coverage\"},\"202109\":{\"name\":\"September 2021\"},\"202107\":{\"name\":\"July 2021\"},\"202108\":{\"name\":\"August 2021\"},\"AUqdhY4mpvp\":{\"name\":\"ANC => 4 Coverage\"},\"202105\":{\"name\":\"May 2021\"},\"202106\":{\"name\":\"June 2021\"},\"Lzg9LtG1xg3\":{\"name\":\"ANC visits per clinical professional\"},\"202103\":{\"name\":\"March 2021\"},\"202104\":{\"name\":\"April 2021\"},\"LAST_12_MONTHS\":{\"name\":\"Last 12 months\"},\"202112\":{\"name\":\"December 2021\"},\"ImspTQPwCqd\":{\"name\":\"Sierra Leone\"},\"202110\":{\"name\":\"October 2021\"},\"202111\":{\"name\":\"November 2021\"},\"c8fABiNpT0B\":{\"name\":\"ANC IPT 2 Coverage\"},\"dx\":{\"name\":\"Data\"},\"Uvn6LCg7dVU\":{\"name\":\"ANC 1 Coverage\"},\"ou\":{\"name\":\"Organisation unit\"},\"jqBqIXoXpfy\":{\"name\":\"Southern Area\"},\"uIuxlbV1vRT\":{\"name\":\"Area\"},\"J40PpdN4Wkk\":{\"name\":\"Northern Area\"},\"202101\":{\"name\":\"January 2021\"},\"202102\":{\"name\":\"February 2021\"},\"b0EsAxm8Nge\":{\"name\":\"Western Area\"},\"pe\":{\"name\":\"Period\"},\"ReUHfIn0pTQ\":{\"name\":\"ANC 1-3 Dropout Rate\"},\"dwEq7wi6nXV\":{\"name\":\"ANC IPT 1 Coverage\"}},\"dimensions\":{\"dx\":[\"Uvn6LCg7dVU\",\"ReUHfIn0pTQ\",\"OdiHJayrsKo\",\"Lzg9LtG1xg3\",\"sB79w2hiLp8\",\"AUqdhY4mpvp\",\"dwEq7wi6nXV\",\"c8fABiNpT0B\"],\"pe\":[\"202101\",\"202102\",\"202103\",\"202104\",\"202105\",\"202106\",\"202107\",\"202108\",\"202109\",\"202110\",\"202111\",\"202112\"],\"ou\":[\"ImspTQPwCqd\"],\"uIuxlbV1vRT\":[\"J40PpdN4Wkk\",\"b0EsAxm8Nge\",\"jqBqIXoXpfy\",\"nlX2VoouN63\"],\"co\":[]}}";String actualMetaData = new JSONObject((Map)response.extract("metaData")).toString();
    assertEquals(expectedMetaData, actualMetaData, false);

// Assert headers.
    validateHeader(response,0,"dx","Data","TEXT","java.lang.String",false,true);
    validateHeader(response,1,"pe","Period","TEXT","java.lang.String",false,true);
    validateHeader(response,2,"uIuxlbV1vRT","Area","TEXT","java.lang.String",false,true);
    validateHeader(response,3,"value","Value","NUMBER","java.lang.Double",false,false);
    validateHeader(response,4,"numerator","Numerator","NUMBER","java.lang.Double",false,false);
    validateHeader(response,5,"denominator","Denominator","NUMBER","java.lang.Double",false,false);
    validateHeader(response,6,"factor","Factor","NUMBER","java.lang.Double",false,false);
    validateHeader(response,7,"multiplier","Multiplier","NUMBER","java.lang.Double",false,false);
    validateHeader(response,8,"divisor","Divisor","NUMBER","java.lang.Double",false,false);

// Assert rows.
    validateRow(response, List.of("Uvn6LCg7dVU","202101","J40PpdN4Wkk","76.88","4473.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202101","b0EsAxm8Nge","100.66","6538.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202101","jqBqIXoXpfy","123.85","4596.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202101","nlX2VoouN63","96.66","2463.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202102","J40PpdN4Wkk","90.39","4750.0","68500.0","1303.57","36500","28"));
    validateRow(response, List.of("Uvn6LCg7dVU","202102","b0EsAxm8Nge","96.66","5671.0","76477.0","1303.57","36500","28"));
    validateRow(response, List.of("Uvn6LCg7dVU","202102","jqBqIXoXpfy","132.31","4435.0","43695.0","1303.57","36500","28"));
    validateRow(response, List.of("Uvn6LCg7dVU","202102","nlX2VoouN63","93.72","2157.0","30002.0","1303.57","36500","28"));
    validateRow(response, List.of("Uvn6LCg7dVU","202103","J40PpdN4Wkk","82.75","4814.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202103","b0EsAxm8Nge","121.04","7862.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202103","jqBqIXoXpfy","122.79","4557.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202103","nlX2VoouN63","100.86","2570.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202104","J40PpdN4Wkk","81.9","4611.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202104","b0EsAxm8Nge","92.16","5793.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202104","jqBqIXoXpfy","116.17","4172.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202104","nlX2VoouN63","90.11","2222.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202105","J40PpdN4Wkk","111.28","6474.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202105","b0EsAxm8Nge","179.55","11662.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202105","jqBqIXoXpfy","146.8","5448.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202105","nlX2VoouN63","112.4","2864.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202106","J40PpdN4Wkk","95.36","5369.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202106","b0EsAxm8Nge","138.9","8731.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202106","jqBqIXoXpfy","137.38","4934.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202106","nlX2VoouN63","110.02","2713.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202107","J40PpdN4Wkk","84.55","4919.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202107","b0EsAxm8Nge","116.85","7590.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202107","jqBqIXoXpfy","144.7","5370.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202107","nlX2VoouN63","102.94","2623.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202108","J40PpdN4Wkk","93.78","5456.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202108","b0EsAxm8Nge","115.38","7494.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202108","jqBqIXoXpfy","129.67","4812.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202108","nlX2VoouN63","94.07","2397.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202109","J40PpdN4Wkk","97.81","5507.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202109","b0EsAxm8Nge","114.18","7177.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202109","jqBqIXoXpfy","149.3","5362.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202109","nlX2VoouN63","90.35","2228.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202110","J40PpdN4Wkk","61.11","3555.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202110","b0EsAxm8Nge","109.33","7101.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202110","jqBqIXoXpfy","110.37","4096.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202110","nlX2VoouN63","44.66","1138.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202111","J40PpdN4Wkk","60.9","3429.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202111","b0EsAxm8Nge","118.04","7420.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202111","jqBqIXoXpfy","154.79","5559.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202111","nlX2VoouN63","51.66","1274.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("Uvn6LCg7dVU","202112","J40PpdN4Wkk","76.44","4447.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202112","b0EsAxm8Nge","79.64","5173.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202112","jqBqIXoXpfy","88.82","3296.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("Uvn6LCg7dVU","202112","nlX2VoouN63","45.45","1158.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("ReUHfIn0pTQ","202101","J40PpdN4Wkk","50.44","2256.0","4473.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202101","b0EsAxm8Nge","46.96","3070.0","6538.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202101","jqBqIXoXpfy","37.38","1718.0","4596.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202101","nlX2VoouN63","31.71","781.0","2463.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202102","J40PpdN4Wkk","40.99","1947.0","4750.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202102","b0EsAxm8Nge","39.5","2240.0","5671.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202102","jqBqIXoXpfy","40.81","1810.0","4435.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202102","nlX2VoouN63","26.98","582.0","2157.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202103","J40PpdN4Wkk","40.57","1953.0","4814.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202103","b0EsAxm8Nge","46.02","3618.0","7862.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202103","jqBqIXoXpfy","33.25","1515.0","4557.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202103","nlX2VoouN63","21.25","546.0","2570.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202104","J40PpdN4Wkk","34.42","1587.0","4611.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202104","b0EsAxm8Nge","45.57","2640.0","5793.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202104","jqBqIXoXpfy","31.14","1299.0","4172.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202104","nlX2VoouN63","27.59","613.0","2222.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202105","J40PpdN4Wkk","49.91","3231.0","6474.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202105","b0EsAxm8Nge","57.51","6707.0","11662.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202105","jqBqIXoXpfy","40.31","2196.0","5448.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202105","nlX2VoouN63","30.17","864.0","2864.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202106","J40PpdN4Wkk","42.28","2270.0","5369.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202106","b0EsAxm8Nge","43.05","3759.0","8731.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202106","jqBqIXoXpfy","33.26","1641.0","4934.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202106","nlX2VoouN63","23.44","636.0","2713.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202107","J40PpdN4Wkk","40.58","1996.0","4919.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202107","b0EsAxm8Nge","38.29","2906.0","7590.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202107","jqBqIXoXpfy","35.05","1882.0","5370.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202107","nlX2VoouN63","26.38","692.0","2623.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202108","J40PpdN4Wkk","41.31","2254.0","5456.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202108","b0EsAxm8Nge","39.83","2985.0","7494.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202108","jqBqIXoXpfy","33.77","1625.0","4812.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202108","nlX2VoouN63","19.94","478.0","2397.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202109","J40PpdN4Wkk","41.46","2283.0","5507.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202109","b0EsAxm8Nge","33.62","2413.0","7177.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202109","jqBqIXoXpfy","34.33","1841.0","5362.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202109","nlX2VoouN63","15.57","347.0","2228.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202110","J40PpdN4Wkk","42.08","1496.0","3555.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202110","b0EsAxm8Nge","43.23","3070.0","7101.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202110","jqBqIXoXpfy","32.08","1314.0","4096.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202110","nlX2VoouN63","4.66","53.0","1138.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202111","J40PpdN4Wkk","50.86","1744.0","3429.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202111","b0EsAxm8Nge","37.3","2768.0","7420.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202111","jqBqIXoXpfy","1.48","82.0","5559.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202111","nlX2VoouN63","2.28","29.0","1274.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202112","J40PpdN4Wkk","43.67","1942.0","4447.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202112","b0EsAxm8Nge","43.67","2259.0","5173.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202112","jqBqIXoXpfy","27.88","919.0","3296.0","100.0","100","1"));
    validateRow(response, List.of("ReUHfIn0pTQ","202112","nlX2VoouN63","13.39","155.0","1158.0","100.0","100","1"));
    validateRow(response, List.of("OdiHJayrsKo","202101","J40PpdN4Wkk","60.83","3539.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202101","b0EsAxm8Nge","81.3","5281.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202101","jqBqIXoXpfy","105.55","3917.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202101","nlX2VoouN63","87.44","2228.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202102","J40PpdN4Wkk","82.99","4361.0","68500.0","1303.57","36500","28"));
    validateRow(response, List.of("OdiHJayrsKo","202102","b0EsAxm8Nge","92.98","5455.0","76477.0","1303.57","36500","28"));
    validateRow(response, List.of("OdiHJayrsKo","202102","jqBqIXoXpfy","130.94","4389.0","43695.0","1303.57","36500","28"));
    validateRow(response, List.of("OdiHJayrsKo","202102","nlX2VoouN63","86.94","2001.0","30002.0","1303.57","36500","28"));
    validateRow(response, List.of("OdiHJayrsKo","202103","J40PpdN4Wkk","73.95","4302.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202103","b0EsAxm8Nge","96.99","6300.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202103","jqBqIXoXpfy","115.22","4276.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202103","nlX2VoouN63","94.27","2402.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202104","J40PpdN4Wkk","76.68","4317.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202104","b0EsAxm8Nge","89.92","5652.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202104","jqBqIXoXpfy","117.67","4226.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202104","nlX2VoouN63","78.1","1926.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202105","J40PpdN4Wkk","84.4","4910.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202105","b0EsAxm8Nge","124.46","8084.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202105","jqBqIXoXpfy","134.87","5005.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202105","nlX2VoouN63","100.58","2563.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202106","J40PpdN4Wkk","81.17","4570.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202106","b0EsAxm8Nge","126.99","7982.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202106","jqBqIXoXpfy","158.74","5701.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202106","nlX2VoouN63","110.59","2727.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202107","J40PpdN4Wkk","74.12","4312.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202107","b0EsAxm8Nge","108.37","7039.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202107","jqBqIXoXpfy","134.57","4994.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202107","nlX2VoouN63","95.01","2421.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202108","J40PpdN4Wkk","77.28","4496.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202108","b0EsAxm8Nge","96.82","6289.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202108","jqBqIXoXpfy","135.22","5018.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202108","nlX2VoouN63","93.83","2391.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202109","J40PpdN4Wkk","78.67","4429.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202109","b0EsAxm8Nge","108.04","6791.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202109","jqBqIXoXpfy","133.85","4807.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202109","nlX2VoouN63","89.91","2217.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202110","J40PpdN4Wkk","50.88","2960.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202110","b0EsAxm8Nge","90.56","5882.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202110","jqBqIXoXpfy","89.54","3323.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202110","nlX2VoouN63","50.31","1282.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202111","J40PpdN4Wkk","48.31","2720.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202111","b0EsAxm8Nge","124.84","7847.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202111","jqBqIXoXpfy","143.76","5163.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202111","nlX2VoouN63","56.13","1384.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("OdiHJayrsKo","202112","J40PpdN4Wkk","70.58","4106.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202112","b0EsAxm8Nge","62.48","4058.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202112","jqBqIXoXpfy","84.88","3150.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("OdiHJayrsKo","202112","nlX2VoouN63","46.58","1187.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("Lzg9LtG1xg3","202101","J40PpdN4Wkk","21.08","11184.0","530.54","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202101","b0EsAxm8Nge","30.33","16916.0","557.74","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202101","jqBqIXoXpfy","210.48","12837.0","60.99","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202101","nlX2VoouN63","31.91","7157.0","224.28","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202102","J40PpdN4Wkk","24.7","13324.0","539.34","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202102","b0EsAxm8Nge","29.39","16665.0","567.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202102","jqBqIXoXpfy","210.69","13063.0","62.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202102","nlX2VoouN63","28.69","6542.0","228.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202103","J40PpdN4Wkk","25.41","13480.0","530.54","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202103","b0EsAxm8Nge","37.54","20940.0","557.74","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202103","jqBqIXoXpfy","243.89","14875.0","60.99","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202103","nlX2VoouN63","36.31","8143.0","224.28","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202104","J40PpdN4Wkk","25.11","13324.0","530.54","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202104","b0EsAxm8Nge","30.01","16739.0","557.74","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202104","jqBqIXoXpfy","218.41","13321.0","60.99","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202104","nlX2VoouN63","29.74","6669.0","224.28","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202105","J40PpdN4Wkk","30.77","16324.0","530.54","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202105","b0EsAxm8Nge","49.71","27723.0","557.74","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202105","jqBqIXoXpfy","260.71","15901.0","60.99","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202105","nlX2VoouN63","39.13","8777.0","224.28","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202106","J40PpdN4Wkk","27.54","14611.0","530.54","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202106","b0EsAxm8Nge","44.05","24567.0","557.74","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202106","jqBqIXoXpfy","265.9","16217.0","60.99","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202106","nlX2VoouN63","39.46","8851.0","224.28","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202107","J40PpdN4Wkk","30.2","13560.0","449.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202107","b0EsAxm8Nge","4426.2","22131.0","5.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202107","jqBqIXoXpfy","56.95","16173.0","284.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202107","nlX2VoouN63","23.51","8204.0","349.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202108","J40PpdN4Wkk","32.7","14684.0","449.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202108","b0EsAxm8Nge","4235.4","21177.0","5.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202108","jqBqIXoXpfy","53.5","15194.0","284.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202108","nlX2VoouN63","22.84","7971.0","349.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202109","J40PpdN4Wkk","34.36","15427.0","449.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202109","b0EsAxm8Nge","4437.2","22186.0","5.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202109","jqBqIXoXpfy","56.13","15941.0","284.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202109","nlX2VoouN63","21.56","7523.0","349.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202110","J40PpdN4Wkk","21.15","9495.0","449.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202110","b0EsAxm8Nge","4024.2","20121.0","5.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202110","jqBqIXoXpfy","43.74","12421.0","284.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202110","nlX2VoouN63","12.04","4201.0","349.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202111","J40PpdN4Wkk","19.59","8797.0","449.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202111","b0EsAxm8Nge","4706.2","23531.0","5.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202111","jqBqIXoXpfy","66.29","18825.0","284.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202111","nlX2VoouN63","13.26","4626.0","349.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202112","J40PpdN4Wkk","27.0","12122.0","449.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202112","b0EsAxm8Nge","2873.4","14367.0","5.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202112","jqBqIXoXpfy","38.41","10908.0","284.0","1.0","1","1"));
    validateRow(response, List.of("Lzg9LtG1xg3","202112","nlX2VoouN63","11.56","4036.0","349.0","1.0","1","1"));
    validateRow(response, List.of("sB79w2hiLp8","202101","J40PpdN4Wkk","38.11","2217.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202101","b0EsAxm8Nge","53.39","3468.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202101","jqBqIXoXpfy","77.55","2878.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202101","nlX2VoouN63","66.01","1682.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202102","J40PpdN4Wkk","53.34","2803.0","68500.0","1303.57","36500","28"));
    validateRow(response, List.of("sB79w2hiLp8","202102","b0EsAxm8Nge","58.48","3431.0","76477.0","1303.57","36500","28"));
    validateRow(response, List.of("sB79w2hiLp8","202102","jqBqIXoXpfy","78.31","2625.0","43695.0","1303.57","36500","28"));
    validateRow(response, List.of("sB79w2hiLp8","202102","nlX2VoouN63","68.43","1575.0","30002.0","1303.57","36500","28"));
    validateRow(response, List.of("sB79w2hiLp8","202103","J40PpdN4Wkk","49.18","2861.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202103","b0EsAxm8Nge","65.34","4244.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202103","jqBqIXoXpfy","81.97","3042.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202103","nlX2VoouN63","79.43","2024.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202104","J40PpdN4Wkk","53.71","3024.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202104","b0EsAxm8Nge","50.16","3153.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202104","jqBqIXoXpfy","80.0","2873.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202104","nlX2VoouN63","65.25","1609.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202105","J40PpdN4Wkk","55.74","3243.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202105","b0EsAxm8Nge","76.29","4955.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202105","jqBqIXoXpfy","87.63","3252.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202105","nlX2VoouN63","78.49","2000.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202106","J40PpdN4Wkk","55.04","3099.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202106","b0EsAxm8Nge","79.1","4972.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202106","jqBqIXoXpfy","91.69","3293.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202106","nlX2VoouN63","84.23","2077.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202107","J40PpdN4Wkk","50.24","2923.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202107","b0EsAxm8Nge","72.11","4684.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202107","jqBqIXoXpfy","93.99","3488.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202107","nlX2VoouN63","75.78","1931.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202108","J40PpdN4Wkk","55.04","3202.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202108","b0EsAxm8Nge","69.42","4509.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202108","jqBqIXoXpfy","85.88","3187.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202108","nlX2VoouN63","75.31","1919.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202109","J40PpdN4Wkk","57.26","3224.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202109","b0EsAxm8Nge","75.79","4764.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202109","jqBqIXoXpfy","98.04","3521.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202109","nlX2VoouN63","76.28","1881.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202110","J40PpdN4Wkk","35.39","2059.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202110","b0EsAxm8Nge","62.06","4031.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202110","jqBqIXoXpfy","74.96","2782.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202110","nlX2VoouN63","42.58","1085.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202111","J40PpdN4Wkk","29.93","1685.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202111","b0EsAxm8Nge","74.01","4652.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202111","jqBqIXoXpfy","152.5","5477.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202111","nlX2VoouN63","50.49","1245.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("sB79w2hiLp8","202112","J40PpdN4Wkk","43.06","2505.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202112","b0EsAxm8Nge","44.86","2914.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202112","jqBqIXoXpfy","64.05","2377.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("sB79w2hiLp8","202112","nlX2VoouN63","39.36","1003.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202101","J40PpdN4Wkk","16.42","955.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202101","b0EsAxm8Nge","25.08","1629.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202101","jqBqIXoXpfy","38.96","1446.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202101","nlX2VoouN63","30.77","784.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202102","J40PpdN4Wkk","26.83","1410.0","68500.0","1303.57","36500","28"));
    validateRow(response, List.of("AUqdhY4mpvp","202102","b0EsAxm8Nge","35.93","2108.0","76477.0","1303.57","36500","28"));
    validateRow(response, List.of("AUqdhY4mpvp","202102","jqBqIXoXpfy","48.15","1614.0","43695.0","1303.57","36500","28"));
    validateRow(response, List.of("AUqdhY4mpvp","202102","nlX2VoouN63","35.15","809.0","30002.0","1303.57","36500","28"));
    validateRow(response, List.of("AUqdhY4mpvp","202103","J40PpdN4Wkk","25.83","1503.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202103","b0EsAxm8Nge","39.01","2534.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202103","jqBqIXoXpfy","80.84","3000.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202103","nlX2VoouN63","45.01","1147.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202104","J40PpdN4Wkk","24.37","1372.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202104","b0EsAxm8Nge","34.06","2141.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202104","jqBqIXoXpfy","57.08","2050.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202104","nlX2VoouN63","36.98","912.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202105","J40PpdN4Wkk","29.17","1697.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202105","b0EsAxm8Nge","46.53","3022.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202105","jqBqIXoXpfy","59.17","2196.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202105","nlX2VoouN63","52.98","1350.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202106","J40PpdN4Wkk","27.94","1573.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202106","b0EsAxm8Nge","45.85","2882.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202106","jqBqIXoXpfy","63.74","2289.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202106","nlX2VoouN63","54.1","1334.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202107","J40PpdN4Wkk","24.17","1406.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202107","b0EsAxm8Nge","43.39","2818.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202107","jqBqIXoXpfy","62.54","2321.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202107","nlX2VoouN63","48.23","1229.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202108","J40PpdN4Wkk","26.3","1530.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202108","b0EsAxm8Nge","44.42","2885.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202108","jqBqIXoXpfy","58.66","2177.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202108","nlX2VoouN63","49.61","1264.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202109","J40PpdN4Wkk","40.27","2267.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202109","b0EsAxm8Nge","54.95","3454.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202109","jqBqIXoXpfy","62.68","2251.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202109","nlX2VoouN63","48.54","1197.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202110","J40PpdN4Wkk","15.83","921.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202110","b0EsAxm8Nge","47.83","3107.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202110","jqBqIXoXpfy","59.82","2220.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202110","nlX2VoouN63","27.31","696.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202111","J40PpdN4Wkk","17.1","963.0","68500.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202111","b0EsAxm8Nge","57.46","3612.0","76477.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202111","jqBqIXoXpfy","73.12","2626.0","43695.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202111","nlX2VoouN63","29.32","723.0","30002.0","1216.67","36500","30"));
    validateRow(response, List.of("AUqdhY4mpvp","202112","J40PpdN4Wkk","18.29","1064.0","68500.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202112","b0EsAxm8Nge","34.21","2222.0","76477.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202112","jqBqIXoXpfy","56.18","2085.0","43695.0","1177.42","36500","31"));
    validateRow(response, List.of("AUqdhY4mpvp","202112","nlX2VoouN63","27.0","688.0","30002.0","1177.42","36500","31"));
    validateRow(response, List.of("dwEq7wi6nXV","202101","J40PpdN4Wkk","110.66","4950.0","4473.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202101","b0EsAxm8Nge","110.66","7235.0","6538.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202101","jqBqIXoXpfy","112.21","5157.0","4596.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202101","nlX2VoouN63","99.35","2447.0","2463.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202102","J40PpdN4Wkk","111.52","5297.0","4750.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202102","b0EsAxm8Nge","126.31","7163.0","5671.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202102","jqBqIXoXpfy","98.67","4376.0","4435.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202102","nlX2VoouN63","100.46","2167.0","2157.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202103","J40PpdN4Wkk","102.41","4930.0","4814.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202103","b0EsAxm8Nge","115.57","9086.0","7862.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202103","jqBqIXoXpfy","97.54","4445.0","4557.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202103","nlX2VoouN63","100.7","2588.0","2570.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202104","J40PpdN4Wkk","104.32","4810.0","4611.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202104","b0EsAxm8Nge","114.24","6618.0","5793.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202104","jqBqIXoXpfy","109.3","4560.0","4172.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202104","nlX2VoouN63","93.34","2074.0","2222.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202105","J40PpdN4Wkk","114.46","7410.0","6474.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202105","b0EsAxm8Nge","147.48","17199.0","11662.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202105","jqBqIXoXpfy","108.9","5933.0","5448.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202105","nlX2VoouN63","116.97","3350.0","2864.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202106","J40PpdN4Wkk","94.13","5054.0","5369.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202106","b0EsAxm8Nge","114.75","10019.0","8731.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202106","jqBqIXoXpfy","103.53","5108.0","4934.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202106","nlX2VoouN63","108.04","2931.0","2713.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202107","J40PpdN4Wkk","89.55","4405.0","4919.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202107","b0EsAxm8Nge","182.92","13884.0","7590.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202107","jqBqIXoXpfy","101.38","5444.0","5370.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202107","nlX2VoouN63","105.57","2769.0","2623.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202108","J40PpdN4Wkk","76.94","4198.0","5456.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202108","b0EsAxm8Nge","127.69","9569.0","7494.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202108","jqBqIXoXpfy","85.08","4094.0","4812.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202108","nlX2VoouN63","117.02","2805.0","2397.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202109","J40PpdN4Wkk","111.59","6145.0","5507.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202109","b0EsAxm8Nge","125.92","9037.0","7177.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202109","jqBqIXoXpfy","105.86","5676.0","5362.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202109","nlX2VoouN63","111.31","2480.0","2228.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202110","J40PpdN4Wkk","123.74","4399.0","3555.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202110","b0EsAxm8Nge","123.15","8745.0","7101.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202110","jqBqIXoXpfy","120.56","4938.0","4096.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202110","nlX2VoouN63","155.62","1771.0","1138.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202111","J40PpdN4Wkk","113.39","3888.0","3429.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202111","b0EsAxm8Nge","136.11","10099.0","7420.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202111","jqBqIXoXpfy","110.43","6139.0","5559.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202111","nlX2VoouN63","118.92","1515.0","1274.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202112","J40PpdN4Wkk","108.52","4826.0","4447.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202112","b0EsAxm8Nge","127.49","6595.0","5173.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202112","jqBqIXoXpfy","125.79","4146.0","3296.0","100.0","100","1"));
    validateRow(response, List.of("dwEq7wi6nXV","202112","nlX2VoouN63","118.91","1377.0","1158.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202101","J40PpdN4Wkk","119.81","4240.0","3539.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202101","b0EsAxm8Nge","107.76","5691.0","5281.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202101","jqBqIXoXpfy","136.3","5339.0","3917.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202101","nlX2VoouN63","86.85","1935.0","2228.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202102","J40PpdN4Wkk","90.07","3928.0","4361.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202102","b0EsAxm8Nge","207.81","11336.0","5455.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202102","jqBqIXoXpfy","77.81","3415.0","4389.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202102","nlX2VoouN63","91.7","1835.0","2001.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202103","J40PpdN4Wkk","87.45","3762.0","4302.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202103","b0EsAxm8Nge","129.98","8189.0","6300.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202103","jqBqIXoXpfy","90.41","3866.0","4276.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202103","nlX2VoouN63","78.48","1885.0","2402.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202104","J40PpdN4Wkk","77.0","3324.0","4317.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202104","b0EsAxm8Nge","97.86","5531.0","5652.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202104","jqBqIXoXpfy","81.66","3451.0","4226.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202104","nlX2VoouN63","86.19","1660.0","1926.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202105","J40PpdN4Wkk","96.23","4725.0","4910.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202105","b0EsAxm8Nge","114.86","9285.0","8084.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202105","jqBqIXoXpfy","75.0","3754.0","5005.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202105","nlX2VoouN63","86.89","2227.0","2563.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202106","J40PpdN4Wkk","84.92","3881.0","4570.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202106","b0EsAxm8Nge","87.72","7002.0","7982.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202106","jqBqIXoXpfy","73.95","4216.0","5701.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202106","nlX2VoouN63","74.77","2039.0","2727.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202107","J40PpdN4Wkk","70.27","3030.0","4312.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202107","b0EsAxm8Nge","89.4","6293.0","7039.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202107","jqBqIXoXpfy","83.34","4162.0","4994.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202107","nlX2VoouN63","101.24","2451.0","2421.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202108","J40PpdN4Wkk","67.97","3056.0","4496.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202108","b0EsAxm8Nge","166.34","10461.0","6289.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202108","jqBqIXoXpfy","66.74","3349.0","5018.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202108","nlX2VoouN63","95.78","2290.0","2391.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202109","J40PpdN4Wkk","99.71","4416.0","4429.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202109","b0EsAxm8Nge","108.6","7375.0","6791.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202109","jqBqIXoXpfy","86.71","4168.0","4807.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202109","nlX2VoouN63","89.22","1978.0","2217.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202110","J40PpdN4Wkk","85.47","2530.0","2960.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202110","b0EsAxm8Nge","135.84","7990.0","5882.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202110","jqBqIXoXpfy","104.45","3471.0","3323.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202110","nlX2VoouN63","103.51","1327.0","1282.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202111","J40PpdN4Wkk","99.96","2719.0","2720.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202111","b0EsAxm8Nge","88.4","6937.0","7847.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202111","jqBqIXoXpfy","89.5","4621.0","5163.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202111","nlX2VoouN63","109.68","1518.0","1384.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202112","J40PpdN4Wkk","76.81","3154.0","4106.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202112","b0EsAxm8Nge","150.76","6118.0","4058.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202112","jqBqIXoXpfy","114.0","3591.0","3150.0","100.0","100","1"));
    validateRow(response, List.of("c8fABiNpT0B","202112","nlX2VoouN63","98.99","1175.0","1187.0","100.0","100","1"));

  }

  @Test
  public void queryAncByAreaThisYear() throws JSONException {
    // Given
    QueryParamsBuilder params =
        new QueryParamsBuilder()
            .add("filter=ou:ImspTQPwCqd,pe:THIS_YEAR")
            .add("skipData=false")
            .add("includeNumDen=false")
            .add("displayProperty=NAME")
            .add("skipMeta=false")
            .add(
                "dimension=dx:Uvn6LCg7dVU;OdiHJayrsKo;sB79w2hiLp8,uIuxlbV1vRT:J40PpdN4Wkk;b0EsAxm8Nge;jqBqIXoXpfy;nlX2VoouN63")
            .add("relativePeriodDate=2022-01-01");

    // When
    ApiResponse response = actions.get(params);

    // Then
    response
        .validate()
        .statusCode(200)
        .body("headers", hasSize(equalTo(3)))
        .body("rows", hasSize(equalTo(12)))
        .body("height", equalTo(12))
        .body("width", equalTo(3))
        .body("headerWidth", equalTo(3));

    // Assert metaData.
    String expectedMetaData =
        "{\"items\":{\"nlX2VoouN63\":{\"name\":\"Eastern Area\"},\"sB79w2hiLp8\":{\"name\":\"ANC 3 Coverage\"},\"ou\":{\"name\":\"Organisation unit\"},\"OdiHJayrsKo\":{\"name\":\"ANC 2 Coverage\"},\"jqBqIXoXpfy\":{\"name\":\"Southern Area\"},\"THIS_YEAR\":{\"name\":\"This year\"},\"2022\":{\"name\":\"2022\"},\"uIuxlbV1vRT\":{\"name\":\"Area\"},\"J40PpdN4Wkk\":{\"name\":\"Northern Area\"},\"b0EsAxm8Nge\":{\"name\":\"Western Area\"},\"ImspTQPwCqd\":{\"name\":\"Sierra Leone\"},\"dx\":{\"name\":\"Data\"},\"pe\":{\"name\":\"Period\"},\"Uvn6LCg7dVU\":{\"name\":\"ANC 1 Coverage\"}},\"dimensions\":{\"dx\":[\"Uvn6LCg7dVU\",\"OdiHJayrsKo\",\"sB79w2hiLp8\"],\"pe\":[\"2022\"],\"ou\":[\"ImspTQPwCqd\"],\"uIuxlbV1vRT\":[\"J40PpdN4Wkk\",\"b0EsAxm8Nge\",\"jqBqIXoXpfy\",\"nlX2VoouN63\"],\"co\":[]}}";
    String actualMetaData = new JSONObject((Map) response.extract("metaData")).toString();
    assertEquals(expectedMetaData, actualMetaData, false);

    // Assert headers.
    validateHeader(response, 0, "dx", "Data", "TEXT", "java.lang.String", false, true);
    validateHeader(response, 1, "uIuxlbV1vRT", "Area", "TEXT", "java.lang.String", false, true);
    validateHeader(response, 2, "value", "Value", "NUMBER", "java.lang.Double", false, false);

    // Assert rows.
    validateRow(response, List.of("Uvn6LCg7dVU", "J40PpdN4Wkk", "82.82"));
    validateRow(response, List.of("Uvn6LCg7dVU", "b0EsAxm8Nge", "113.08"));
    validateRow(response, List.of("Uvn6LCg7dVU", "jqBqIXoXpfy", "126.23"));
    validateRow(response, List.of("Uvn6LCg7dVU", "nlX2VoouN63", "84.32"));
    validateRow(response, List.of("OdiHJayrsKo", "J40PpdN4Wkk", "70.32"));
    validateRow(response, List.of("OdiHJayrsKo", "b0EsAxm8Nge", "98.27"));
    validateRow(response, List.of("OdiHJayrsKo", "jqBqIXoXpfy", "119.7"));
    validateRow(response, List.of("OdiHJayrsKo", "nlX2VoouN63", "80.8"));
    validateRow(response, List.of("sB79w2hiLp8", "J40PpdN4Wkk", "47.06"));
    validateRow(response, List.of("sB79w2hiLp8", "b0EsAxm8Nge", "63.81"));
    validateRow(response, List.of("sB79w2hiLp8", "jqBqIXoXpfy", "86.92"));
    validateRow(response, List.of("sB79w2hiLp8", "nlX2VoouN63", "65.45"));
  }
}
