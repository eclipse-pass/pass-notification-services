/*
 *
 *  * Copyright 2018 Johns Hopkins University
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.dataconservancy.pass.notification.impl;

import org.dataconservancy.pass.model.Submission;
import org.dataconservancy.pass.model.SubmissionEvent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecipientAnalyzerTest {

    private String preparer1 = "preparer_one@example.org";

    private String preparer2 = "preparer_two@example.org";

    private String submitter = "submitter@example.org";

    private Collection<String> preparers;

    private Submission submission;

    private SubmissionEvent event;

    private Function<Collection<String>, Collection<String>> whitelist;

    private RecipientAnalyzer underTest;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        whitelist = mock(Function.class);
        when(whitelist.apply(any())).thenAnswer(inv -> inv.getArgument(0));

        submission = mock(Submission.class);

        event = mock(SubmissionEvent.class);

        underTest = new RecipientAnalyzer(whitelist);

        preparers = Arrays.asList(preparer1, preparer2);

        when(submission.getSubmitter()).thenReturn(URI.create(submitter));
        when(submission.getPreparers()).thenReturn(preparers.stream().map(URI::create).collect(toList()));
    }

    @Test
    public void analyzeApprovalRequested() {
        perform(Collections.singleton(submitter), SubmissionEvent.EventType.APPROVAL_REQUESTED);
    }

    @Test
    public void analyzeApprovalRequestedNewUser() {
        perform(Collections.singleton(submitter), SubmissionEvent.EventType.APPROVAL_REQUESTED_NEWUSER);
    }

    @Test
    public void analyzeChangesRequested() {
        perform(preparers, SubmissionEvent.EventType.CHANGES_REQUESTED);
    }

    @Test
    public void analyzeCancelledBySubmitter() {
        when(event.getPerformedBy()).thenReturn(URI.create(submitter));
        perform(preparers, SubmissionEvent.EventType.CANCELLED);
    }

    @Test
    public void analyzeCancelledByPreparer() {
        when(event.getPerformedBy()).thenReturn(URI.create(preparer1));
        // FIXME other preparers should get a notification to
        perform(Collections.singleton(submitter), SubmissionEvent.EventType.CANCELLED);
    }

    @Test
    public void analyzeSubmitted() {
        perform(preparers, SubmissionEvent.EventType.SUBMITTED);
    }

    private void perform(Collection<String> expectedRecipients, SubmissionEvent.EventType type) {
        when(event.getEventType()).thenReturn(type);

        Collection<String> actualRecipients = underTest.apply(submission, event);

        actualRecipients.forEach(actualRecipient -> assertTrue(expectedRecipients.contains(actualRecipient)));
        expectedRecipients.forEach(expectedRecipient -> assertTrue(actualRecipients.contains(expectedRecipient)));

        verify(event, atLeastOnce()).getEventType();
        verify(whitelist).apply(any());
    }
}