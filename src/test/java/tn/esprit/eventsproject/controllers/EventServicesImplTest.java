package tn.esprit.eventsproject.controllers;



import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;
import tn.esprit.eventsproject.services.EventServicesImpl;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServicesImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @InjectMocks
    private EventServicesImpl eventServices;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test to avoid interference
        Mockito.reset(eventRepository, participantRepository, logisticsRepository);
    }

    @Test
    void testAddParticipant_Success() {
        Participant participant = new Participant();
        participant.setIdPart(1);

        when(participantRepository.save(any(Participant.class))).thenReturn(participant);

        Participant result = eventServices.addParticipant(participant);

        assertNotNull(result);
        assertEquals(1, result.getIdPart());
        verify(participantRepository, times(1)).save(any(Participant.class));
    }

    @Test
    void testAddAffectEvenParticipant_Success() {
        Event event = new Event();
        event.setDescription("Test Event");

        Participant participant = new Participant();
        participant.setIdPart(1);
        participant.setEvents(new HashSet<>());

        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        Event result = eventServices.addAffectEvenParticipant(event, 1);

        assertNotNull(result);
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectLog_Success() {
        String descriptionEvent = "Test Event";
        Event event = new Event();
        event.setDescription(descriptionEvent);
        event.setLogistics(new HashSet<>());

        Logistics logistics = new Logistics();
        logistics.setDescription("Logistics Test");
        logistics.setReserve(true);

        when(eventRepository.findByDescription(descriptionEvent)).thenReturn(event);
        when(logisticsRepository.save(any(Logistics.class))).thenReturn(logistics);

        Logistics result = eventServices.addAffectLog(logistics, descriptionEvent);

        assertNotNull(result);
        assertEquals("Logistics Test", result.getDescription());
        verify(eventRepository, times(1)).findByDescription(descriptionEvent);
        verify(logisticsRepository, times(1)).save(logistics);
    }

    @Test
    void testGetLogisticsDates_WithValidDates() {
        LocalDate dateDebut = LocalDate.of(2024, 1, 1);
        LocalDate dateFin = LocalDate.of(2024, 12, 31);

        Event event = new Event();
        Logistics logistics = new Logistics();
        logistics.setReserve(true);
        event.setLogistics(new HashSet<>(Collections.singletonList(logistics)));

        when(eventRepository.findByDateDebutBetween(dateDebut, dateFin))
                .thenReturn(Collections.singletonList(event));

        List<Logistics> result = eventServices.getLogisticsDates(dateDebut, dateFin);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isReserve());
        verify(eventRepository, times(1)).findByDateDebutBetween(dateDebut, dateFin);
    }

    @Test
    void testCalculCout_Success() {
        Logistics logistics1 = new Logistics();
        logistics1.setReserve(true);
        logistics1.setPrixUnit(100f);
        logistics1.setQuantite(2);

        Event event = new Event();
        event.setDescription("Event Test");
        event.setLogistics(new HashSet<>(Collections.singletonList(logistics1)));
        event.setCout(0f);

        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR))
                .thenReturn(Collections.singletonList(event));

        eventServices.calculCout();

        verify(eventRepository, times(1)).save(event);
        assertEquals(200f, event.getCout());
    }
}
