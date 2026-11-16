package cinema

import cinema.model.CinemaInfo
import cinema.response.ErrorResponse
import cinema.response.PurchaseRequest
import cinema.model.Seat
import cinema.model.Ticket
import cinema.response.PurchaseResponse
import cinema.response.ReturnRequest
import cinema.response.ReturnResponse
import cinema.response.StatisticsResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class CinemaController {

    private val totalRows = 9
    private val totalColumns = 9

    // Token -> Purchased ticket
    private val purchasedTickets = mutableMapOf<String, Ticket>()

    @GetMapping("/seats")
    fun getSeats(): CinemaInfo {
        val availableSeats = (1..totalRows).flatMap { row ->
            (1..totalColumns)
                .filter { column ->
                    purchasedTickets.values.none {
                        it.row == row && it.column == column
                    }
                }
                .map { column ->
                    Seat(
                        row = row,
                        column = column,
                        price = getPrice(row),
                    )
                }
        }
        return CinemaInfo(
            total_rows = totalRows,
            total_columns = totalColumns,
            available_seats = availableSeats
        )
    }

    @PostMapping("/purchase")
    fun purchase(@RequestBody request: PurchaseRequest): ResponseEntity<Any> {

        val row = request.row
        val column = request.column

        // 1. Validate row and column.
        if (row !in 1..totalRows || column !in 1..totalColumns) {
            return ResponseEntity
                .badRequest()
                .body(ErrorResponse("The number of a row or a column is out of bounds!"))
        }

        // 2. Check whether the seat is already purchased.
        val alreadyPurchased = purchasedTickets.values.any {
            it.row == row && it.column == column
        }

        if (alreadyPurchased) {
            return ResponseEntity
                .badRequest()
                .body(ErrorResponse("The ticket has been already purchased!"))
        }

        // Create ticket and generate a unique token.
        val ticket = Ticket(
            row = row,
            column = column,
            price = getPrice(row)
        )

        val token = UUID.randomUUID().toString()

        // Store the ticket using its token.
        purchasedTickets[token] = ticket

        return ResponseEntity.ok(
            PurchaseResponse(
                token = token,
                ticket = ticket
            )
        )
    }

    @PostMapping("/return")
    fun returnTicket(
        @RequestBody request: ReturnRequest
    ): ResponseEntity<Any> {

        // Find and remove the ticket using its token.
        val ticket = purchasedTickets.remove(request.token)
            ?: return ResponseEntity.badRequest().body(
                ErrorResponse("Wrong token!")
            )

        return ResponseEntity.ok(
            ReturnResponse(returnedTicket = ticket)
        )
    }

    @GetMapping("/stats")
    fun getStats(
        @RequestParam(required = false) password: String?
    ): ResponseEntity<Any> {

        // Check manager password.
        if (password != "super_secret") {
            return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse("The password is wrong!"))
        }

        val purchasedCount = purchasedTickets.size

        val statistics = StatisticsResponse(
            currentIncome = purchasedTickets.values.sumOf { it.price },
            numberOfAvailableSeats = totalRows * totalColumns - purchasedCount,
            numberOfPurchasedTickets = purchasedCount
        )

        return ResponseEntity.ok(statistics)
    }

    private fun getPrice(row: Int): Int {
        return if (row <= 4) 10 else 8
    }
}