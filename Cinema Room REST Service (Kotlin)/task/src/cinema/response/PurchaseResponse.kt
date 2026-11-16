package cinema.response

import cinema.model.Ticket

data class PurchaseResponse(
    val token: String,
    val ticket: Ticket
)
