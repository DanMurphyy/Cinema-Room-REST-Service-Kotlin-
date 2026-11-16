package cinema.model

data class CinemaInfo(
    val total_rows: Int,
    val total_columns: Int,
    val available_seats: List<Seat>
)
