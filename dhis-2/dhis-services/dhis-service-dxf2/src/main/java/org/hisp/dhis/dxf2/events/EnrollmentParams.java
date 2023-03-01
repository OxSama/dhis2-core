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
package org.hisp.dhis.dxf2.events;

import static java.util.stream.Collectors.toMap;
import static org.hisp.dhis.dxf2.events.Param.ATTRIBUTES;
import static org.hisp.dhis.dxf2.events.Param.DELETED;
import static org.hisp.dhis.dxf2.events.Param.EVENTS;
import static org.hisp.dhis.dxf2.events.Param.EVENTS_RELATIONSHIPS;
import static org.hisp.dhis.dxf2.events.Param.RELATIONSHIPS;
import static org.hisp.dhis.dxf2.events.Param.fromFieldPath;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Luca Cambi <luca@dhis2.org>
 */
public class EnrollmentParams extends AbstractParams
{
    public static final Set<Param> ALL = EnumSet.of( ATTRIBUTES, RELATIONSHIPS, DELETED,
        EVENTS,
        EVENTS_RELATIONSHIPS );

    private EnrollmentParams( Set<Param> paramsSet )
    {
        super( paramsSet );
    }

    /**
     * Create {@link EventParams} filtering by the prefix {@link Param#EVENTS}.
     * We need to set the {@link Param#DELETED} because we only have a generic
     * includeDeleted from the request
     */
    public EventParams getEventParams()
    {
        return EventParams.builder().empty()
            .with(
                this.params.stream()
                    .filter(
                        p -> p.getPrefix().isPresent() && p.getPrefix().get() == EVENTS )
                    .collect( toMap( p -> fromFieldPath( p.getField() ), p -> true ) ) )
            .with( DELETED, this.params.contains( DELETED ) ).build();
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder extends ParamsBuilder<EnrollmentParams>
    {
        @Override
        public Builder all()
        {
            this.params = EnumSet.copyOf( ALL );
            return this;
        }

        @Override
        public EnrollmentParams build()
        {
            return new EnrollmentParams( this.params );
        }
    }
}