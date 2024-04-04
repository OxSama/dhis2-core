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
package org.hisp.dhis.commons.action;

import com.opensymphony.xwork2.Action;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.hisp.dhis.category.CategoryOptionGroup;
import org.hisp.dhis.category.CategoryService;
import org.hisp.dhis.user.CurrentUserUtil;
import org.hisp.dhis.user.UserDetails;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Chau Thu Tran
 * @version $ GetCategoryOptionGroupsAction.java Feb 13, 2014 6:17:33 PM $
 */
public class GetCategoryOptionGroupsAction extends BaseAction implements Action {
  @Autowired private CategoryService dataElementCategoryService;

  // -------------------------------------------------------------------------
  // Output
  // -------------------------------------------------------------------------

  private List<CategoryOptionGroup> categoryOptionGroups;

  public List<CategoryOptionGroup> getCategoryOptionGroups() {
    return categoryOptionGroups;
  }

  // -------------------------------------------------------------------------
  // Action
  // -------------------------------------------------------------------------

  @Override
  public String execute() {
    canReadType(CategoryOptionGroup.class);

    categoryOptionGroups = new ArrayList<>(dataElementCategoryService.getAllCategoryOptionGroups());

    UserDetails currentUserDetails = CurrentUserUtil.getCurrentUserDetails();
    categoryOptionGroups.forEach(instance -> canReadInstance(instance, currentUserDetails));

    Collections.sort(categoryOptionGroups);

    return SUCCESS;
  }
}