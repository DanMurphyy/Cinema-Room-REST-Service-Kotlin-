package cinema.response

import cinema.model.Ticket
import com.fasterxml.jackson.annotation.JsonProperty

data class ReturnResponse(
    @JsonProperty("returned_ticket")
    val returnedTicket: Ticket
)
