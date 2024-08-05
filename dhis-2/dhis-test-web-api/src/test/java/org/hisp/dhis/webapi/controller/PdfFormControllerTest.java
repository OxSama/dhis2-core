/*
 * Copyright (c) 2004-2022, University of Oslo
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
package org.hisp.dhis.webapi.controller;

import static org.hisp.dhis.test.webapi.Assertions.assertWebMessage;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.hisp.dhis.dataset.DataSet;
import org.hisp.dhis.period.PeriodType;
import org.hisp.dhis.period.PeriodTypeEnum;
import org.hisp.dhis.test.web.HttpStatus;
import org.hisp.dhis.test.webapi.H2ControllerIntegrationTestBase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link PdfFormController} using (mocked) REST requests.
 *
 * @author Jan Bernitt
 */
class PdfFormControllerTest extends H2ControllerIntegrationTestBase {

  @Test
  void testSendFormPdfDataSet_Empty() {
    assertWebMessage(
        "Conflict",
        409,
        "ERROR",
        "An error occurred, please check import summary.",
        POST("/pdfForm/dataSet", "{}").content(HttpStatus.CONFLICT));
  }

  @Test
  @DisplayName("Should not return Http Error 500 if the DB_Locale is null.")
  void testGetDataSetPdfForm() {
    HttpResponse setting = GET("/userSettings/keyDbLocale");
    Assertions.assertTrue(setting.content().isNull());

    PeriodType periodType = PeriodType.getPeriodType(PeriodTypeEnum.MONTHLY);
    DataSet dataSet = createDataSet('A');
    dataSet.setPeriodType(periodType);
    manager.save(dataSet);

    assertFalse(GET("/pdfForm/dataSet/" + dataSet.getUid()).content("application/pdf").isEmpty());
  }
}
