package org.hisp.dhis.email;

/*
 * Copyright (c) 2004-2017, University of Oslo
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

import java.util.List;
import java.util.stream.Collectors;

import org.hisp.dhis.common.DeliveryChannel;
import org.hisp.dhis.program.message.ProgramMessage;
import org.hisp.dhis.program.message.MessageBatchCreatorService;
import org.hisp.dhis.outboundmessage.OutboundMessage;
import org.hisp.dhis.outboundmessage.OutboundMessageBatch;

/**
* @author Zubair <rajazubair.asghar@gmail.com>
*/

public class EmailMessageBatchCreatorService
    implements MessageBatchCreatorService
{
    @Override
    public OutboundMessageBatch getMessageBatch( List<ProgramMessage> programMessages )
    {
        List<OutboundMessage> messages = programMessages.parallelStream()
            .filter( pm -> pm.getDeliveryChannels().contains( DeliveryChannel.EMAIL ) )
            .map( pm -> createEmailMessage( pm ) )
            .collect( Collectors.toList() );
        
        return new OutboundMessageBatch( messages, DeliveryChannel.EMAIL );
    }

    private OutboundMessage createEmailMessage( ProgramMessage programMessage )
    {
        return new OutboundMessage( programMessage.getSubject(), programMessage.getText(),
                programMessage.getRecipients().getEmailAddresses() );
    }
}
