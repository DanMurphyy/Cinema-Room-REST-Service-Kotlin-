package cinema.response

import com.fasterxml.jackson.annotation.JsonProperty

data class StatisticsResponse(
    @JsonProperty("current_income")
    val currentIncome: Int,

    @JsonProperty("number_of_available_seats")
    val numberOfAvailableSeats: Int,

    @JsonProperty("number_of_purchased_tickets")
    val numberOfPurchasedTickets: Int
)
