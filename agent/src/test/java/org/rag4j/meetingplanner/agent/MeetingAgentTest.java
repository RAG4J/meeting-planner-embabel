package org.rag4j.meetingplanner.agent;

import com.embabel.agent.testing.unit.FakeOperationContext;
import com.embabel.agent.testing.unit.FakePromptRunner;
import org.junit.jupiter.api.Test;
import org.rag4j.meetingplanner.agent.model.meeting.MeetingRequest;
import org.rag4j.meetingplanner.agent.model.meeting.MeetingResponse;
import org.rag4j.meetingplanner.agent.model.person.Agenda;
import org.rag4j.meetingplanner.agent.model.person.Participants;
import org.rag4j.meetingplanner.agent.model.person.Person;
import org.rag4j.meetingplanner.agent.service.PersonFinder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MeetingAgentTest {
    @Test
    void testMeetingAgent() throws Exception {
        var context = FakeOperationContext.create();
        var promptRunner = (FakePromptRunner) context.promptRunner();
        MeetingResponse meetingResponse = mock(MeetingResponse.class);
        MeetingRequest request = new MeetingRequest();
        request.setTitle("Project Kickoff");
        request.setDescription("Initial meeting to kickoff the project.");
        request.setDate(java.time.LocalDate.now().plusDays(1));
        request.setStartTime(java.time.LocalTime.of(10, 0));
        request.setDurationMinutes(60);
        request.setLocation("Conference Room A");
        request.setParticipants(List.of("test1@test.nl", "test2@test.nl"));
        PersonFinder personFinder = mock(PersonFinder.class);
        Person person1 = new Person("test1@test.nl", "Test1 person", new Agenda());
        Person person2 = new Person("test2@test.nl", "Test2 person", new Agenda());
        context.expectResponse(meetingResponse);

        var agent = new MeetingAgent(personFinder);

        agent.bookMeeting(request, new Participants(List.of(person1, person2)), context);

        String prompt = promptRunner.getLlmInvocations().getFirst().getPrompt();
        assertTrue(prompt.contains("Project Kickoff"), "Expected prompt to contain 'Alice Smith'");

    }

    @Test
    void findsParticipantsByEmailAddresses() {
        PersonFinder personFinder = mock(PersonFinder.class);
        Person person1 = new Person("test1@test.nl", "Test1 person", new Agenda());
        Person person2 = new Person("test2@test.nl", "Test2 person", new Agenda());
        when(personFinder.findByEmail("test1@test.nl")).thenReturn(person1);
        when(personFinder.findByEmail("test2@test.nl")).thenReturn(person2);

        MeetingAgent agent = new MeetingAgent(personFinder);
        MeetingRequest request = new MeetingRequest();
        request.setParticipants(List.of("test1@test.nl", "test2@test.nl"));

        Participants participants = agent.findParticipants(request);
        assertNotNull(participants);
        assertEquals(2, participants.participants().size());
        assertTrue(participants.participants().contains(person1));
        assertTrue(participants.participants().contains(person2));
    }
}
