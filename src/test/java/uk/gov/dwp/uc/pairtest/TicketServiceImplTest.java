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
        //TOTAL NUMBER OF TICKET =25
        //ADULT TICKET COST = 250
        //CHILD TICKET COSt = 210
        //INFANT TICKET = 0
        //INFANT GETS NO SEAT
        TicketTypeRequest request1 = new TicketTypeRequest(TicketTypeRequest.Type.ADULT, 10);
        TicketTypeRequest request2 = new TicketTypeRequest(TicketTypeRequest.Type.CHILD, 14);
        TicketTypeRequest request3 = new TicketTypeRequest(TicketTypeRequest.Type.INFANT, 1);
        TicketTypeRequest[] tickets = {request1, request2, request3};

        ticketService.purchaseTickets(accountId, tickets);
        verify(ticketPaymentService, times(1)).makePayment(accountId, 460);
        verify(seatReservationService, times(1)).reserveSeat(accountId, 24);
    }
}
