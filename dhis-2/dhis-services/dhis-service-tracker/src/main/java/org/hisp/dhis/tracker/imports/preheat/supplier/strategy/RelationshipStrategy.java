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
package org.hisp.dhis.tracker.imports.preheat.supplier.strategy;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import org.hisp.dhis.common.CodeGenerator;
import org.hisp.dhis.hibernate.HibernateGenericStore;
import org.hisp.dhis.relationship.Relationship;
import org.hisp.dhis.tracker.imports.preheat.TrackerPreheat;
import org.hisp.dhis.tracker.imports.preheat.mappers.RelationshipMapper;
import org.hisp.dhis.tracker.imports.preheat.supplier.DetachUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Luciano Fiandesio
 */
@Component
@StrategyFor(
    value = org.hisp.dhis.tracker.imports.domain.Relationship.class,
    mapper = RelationshipMapper.class)
public class RelationshipStrategy extends HibernateGenericStore<Relationship>
    implements ClassBasedSupplierStrategy {

  public RelationshipStrategy(
      EntityManager entityManager, JdbcTemplate jdbcTemplate, ApplicationEventPublisher publisher) {
    super(entityManager, jdbcTemplate, publisher, Relationship.class, false);
  }

  @Override
  public void add(List<List<String>> splitList, TrackerPreheat preheat) {
    List<Relationship> relationships = retrieveRelationships(splitList);

    preheat.putRelationships(
        DetachUtils.detach(
            this.getClass().getAnnotation(StrategyFor.class).mapper(), relationships));
  }

  private List<org.hisp.dhis.relationship.Relationship> retrieveRelationships(
      List<List<String>> splitList) {
    List<String> uids =
        splitList.stream().flatMap(List::stream).filter(CodeGenerator::isValidUid).toList();

    return getIncludingDeleted(uids);
  }

  private List<Relationship> getIncludingDeleted(List<String> uids) {
    List<Relationship> relationships = new ArrayList<>();
    List<List<String>> uidsPartitions = Lists.partition(uids, 20000);

    for (List<String> uidsPartition : uidsPartitions) {
      if (!uidsPartition.isEmpty()) {
        relationships.addAll(
            getSession()
                .createQuery("from Relationship as r where r.uid in (:uids)", Relationship.class)
                .setParameter("uids", uidsPartition)
                .list());
      }
    }

    return relationships;
  }
}
