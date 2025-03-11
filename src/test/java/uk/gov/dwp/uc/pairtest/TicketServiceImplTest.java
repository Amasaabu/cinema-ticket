package uk.gov.dwp.uc.pairtest;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TicketServiceImplTest {

    @Mock
    TicketPaymentService ticketPaymentService;
    @Mock
    SeatReservationService seatReservationService;
    @InjectMocks
    TicketServiceImpl ticketService;

    @Test(expected = InvalidPurchaseException.class)
    public void shouldThrowInvalidPurchaseExceptionForNegativeAccountId() {
        var accountId = -1L;
        TicketTypeRequest request1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 1);
        TicketTypeRequest[] tickets = {request1};
        ticketService.purchaseTickets(accountId, tickets);
    }
    @Test(expected = InvalidPurchaseException.class)
    public void shouldThrowInvalidPurchaseExceptionForNegativeNumberOfTicket(){
        var accountId = 1L;
        TicketTypeRequest request1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, -1);
        TicketTypeRequest[] tickets = {request1};
        ticketService.purchaseTickets(accountId, tickets);
    }
    @Test(expected = InvalidPurchaseException.class)
    public void shouldThrowInvalidPurchaseExceptionIfNoAdult(){
        var accountId = 1L;
        TicketTypeRequest request1 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 5);
        TicketTypeRequest[] tickets = {request1};
        ticketService.purchaseTickets(accountId, tickets);
    }
    @Test(expected = InvalidPurchaseException.class)
    public void shouldNotAllowMoreThan25Tickets(){
        var accountId = 1L;
        TicketTypeRequest request1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest request2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 16);
        TicketTypeRequest[] tickets = {request1, request2};
        ticketService.purchaseTickets(accountId, tickets);
    }
    @Test
    public void shouldAllowPurchaseOfTicket(){
        var accountId = 1L;
        //SEATS TO RESERVE
        final var NUM_OF_ADULT = 10;
        final var NUM_OF_CHILDREN = 14;
        final var NUM_OF_INFANT = 1;

        //EXPECTED COST OF TICKET
        final var ADULT_TICKET_COST = NUM_OF_ADULT * 25;
        final var CHILD_TICKET_COST = NUM_OF_CHILDREN * 15;
        final var INFANT_TICKET_COST = NUM_OF_INFANT * 0;  // Infants have no cost but do not get a seat
        final var EXPECTED_TOTAL_COST = ADULT_TICKET_COST +CHILD_TICKET_COST+INFANT_TICKET_COST;

        TicketTypeRequest request1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, NUM_OF_ADULT);
        TicketTypeRequest request2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, NUM_OF_CHILDREN);
        TicketTypeRequest request3 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, NUM_OF_INFANT);
        TicketTypeRequest[] tickets = {request1, request2, request3};

        ticketService.purchaseTickets(accountId, tickets);
        verify(ticketPaymentService, times(1)).makePayment(accountId, EXPECTED_TOTAL_COST);
        final var EXPECTED_NUMBER_OF_SEAT_TO_RESERVE = NUM_OF_ADULT + NUM_OF_CHILDREN;
        verify(seatReservationService, times(1)).reserveSeat(accountId, EXPECTED_NUMBER_OF_SEAT_TO_RESERVE);
    }
}
