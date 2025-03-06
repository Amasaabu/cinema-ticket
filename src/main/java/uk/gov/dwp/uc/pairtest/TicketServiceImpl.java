package uk.gov.dwp.uc.pairtest;

import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;


public class TicketServiceImpl implements TicketService {

    private static final int MAX_TICKETS = 25;
    private static final int ADULT_TICKET_PRICE =25;
    private static final int CHILD_TICKET_PRICE = 15;
    private static final int INFANT_TICKET_PRICE= 0;

    //payment and seat reservation service instantiated
    private final TicketPaymentServiceImpl paymentService = new TicketPaymentServiceImpl();
    private final SeatReservationServiceImpl seatService = new SeatReservationServiceImpl();

    /**
     * Validates that the account ID is valid.
     * An account ID is  valid if it is not null and greater than 0.
     *
     * @param accountId the account ID to validate
     * @throws InvalidPurchaseException if the account ID is null or not greater than 0
     */
    private void validateAccountId(Long accountId) {
        //ensure account id is greater than 0
        if (accountId==null||accountId<=0) {
            throw new InvalidPurchaseException();
        }
    }
    /**
     * Validates the ticket requests to ensure they meet the required criteria.
     * - There must be at least one adult ticket in the requests.
     * - The total number of tickets requested must not exceed the maximum allowed.
     *
     * @param ticketTypeRequests an array of {@link TicketTypeRequest} objects to be validated
     * @throws InvalidPurchaseException if the total number of tickets exceeds the limit or if there is no adult ticket
     */
    private void validateTicket(TicketTypeRequest... ticketTypeRequests) {
        //first there must be at least an adult for every request
        var adultRequest = 0;
        var totalTicket = 0;
        for (var ticket: ticketTypeRequests) {
            totalTicket = totalTicket + ticket.getNoOfTickets();
            if (ticket.getTicketType()== TicketTypeRequest.Type.ADULT) {
                adultRequest = adultRequest + ticket.getNoOfTickets();
            }
        }
        //validate total ticket requested does not go above the max allowed purchasable
        if (totalTicket>MAX_TICKETS) throw new InvalidPurchaseException();
        //validate there is at least an adult since there must be an adult present when all ticket type is purchased
        if (adultRequest<=0) throw new InvalidPurchaseException();
    }
    /**
     * Calculates total cost of ticket request based on the type of ticket
     * - Cost is calculated as BigDecimal as we are dealing with financial data
     *
     * @param ticketTypeRequests, an Array of TicketTypeRequest
     * @return The total cost of ticket as int
     * @throws InvalidPurchaseException if an invalid ticket type is entered
     * */
    private int calculateTotalCost (TicketTypeRequest... ticketTypeRequests) {
        var totalCost = 0;
        for (var ticket: ticketTypeRequests) {
            switch (ticket.getTicketType()) {
                case ADULT:
                    totalCost = totalCost + (ADULT_TICKET_PRICE*ticket.getNoOfTickets());
                    break;
                case CHILD:
                    totalCost = totalCost + (CHILD_TICKET_PRICE * ticket.getNoOfTickets());
                    break;
                case INFANT:
                    totalCost = totalCost + (INFANT_TICKET_PRICE * ticket.getNoOfTickets());
                    break;
                default:
                    //ticket can only be infant, child and adult any other is invalid
                    throw new InvalidPurchaseException();
            }
        }
        return totalCost;
    }
    /**
     * Calculates the number of seat to book
     *
     * @param ticketTypeRequests, an Array of TicketTypeRequest
     * @return The total number of seats to reserve
     * */
    private int calculateNumberOfSeat (TicketTypeRequest... ticketTypeRequests){
        var totalNumberOfSeatToReserve=0;
        for (var ticket: ticketTypeRequests) {
            // as per requirement only adult and child can reserve
            if (ticket.getTicketType()== TicketTypeRequest.Type.ADULT || ticket.getTicketType()==TicketTypeRequest.Type.CHILD) {
                totalNumberOfSeatToReserve = totalNumberOfSeatToReserve+ticket.getNoOfTickets();
            }
        }
        return  totalNumberOfSeatToReserve;
    }

    /**
     * Should only have private methods other than the one below.
     */
    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {
        //Validate accountID as per requirement
        validateAccountId(accountId);

        //Validate the ticket as per requirement
        validateTicket(ticketTypeRequests);

        //calculate total cost
        var totalCost = calculateTotalCost(ticketTypeRequests);
        //process payment
        paymentService.makePayment(accountId, totalCost);
        //now book seats
        var totalSeatsToReserve = calculateNumberOfSeat(ticketTypeRequests);
        seatService.reserveSeat(accountId, totalSeatsToReserve);

    }

}
