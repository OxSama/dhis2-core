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
package org.hisp.dhis.merge.category.optioncombo;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.hisp.dhis.analytics.CategoryDimensionStore;
import org.hisp.dhis.category.CategoryOptionCombo;
import org.hisp.dhis.category.CategoryOptionComboStore;
import org.hisp.dhis.category.CategoryOptionGroupStore;
import org.hisp.dhis.category.CategoryStore;
import org.hisp.dhis.organisationunit.OrganisationUnitStore;
import org.springframework.stereotype.Component;

/**
 * Merge handler for data types.
 *
 * @author david mackessy
 */
@Component
@RequiredArgsConstructor
public class DataCategoryOptionComboMergeHandler {

  private final CategoryStore categoryStore;
  private final CategoryOptionComboStore categoryOptionComboStore;
  private final CategoryOptionGroupStore categoryOptionGroupStore;
  private final OrganisationUnitStore organisationUnitStore;
  private final CategoryDimensionStore categoryDimensionStore;

  /** */
  public void handleDataValues(List<CategoryOptionCombo> sources, CategoryOptionCombo target) {
    // TODO x 2
  }

  /** */
  public void handleDataValueAudits(List<CategoryOptionCombo> sources, CategoryOptionCombo target) {
    // TODO x 2
  }

  /** */
  public void handleDataApprovals(List<CategoryOptionCombo> sources, CategoryOptionCombo target) {
    // TODO
  }

  /** */
  public void handleDataApprovalAudits(
      List<CategoryOptionCombo> sources, CategoryOptionCombo target) {
    // TODO
  }

  /** */
  public void handleEvents(List<CategoryOptionCombo> sources, CategoryOptionCombo target) {
    // TODO
  }

  /** */
  public void handleCompleteDataSetRegistrations(
      List<CategoryOptionCombo> sources, CategoryOptionCombo target) {
    // TODO
  }
}
