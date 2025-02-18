enum class Player { RED, BLACK }

data class Position(val row: Int, val col: Int)

sealed class Piece(val player: Player) {
    class Normal(player: Player) : Piece(player)
    class King(player: Player) : Piece(player)
}

class CheckersBoard {
    private val board: Array<Array<Piece?>> = Array(8) { arrayOfNulls(8) }

    init {
        setupBoard()
    }

    private fun setupBoard() {
        for (row in 0..2) placePieces(row, Player.RED)
        for (row in 5..7) placePieces(row, Player.BLACK)
    }

    private fun placePieces(row: Int, player: Player) {
        for (col in 0 until 8 step 2) {
            val adjustedCol = if (row % 2 == 0) col + 1 else col
            board[row][adjustedCol] = Piece.Normal(player)
        }
    }

    fun movePiece(from: Position, to: Position): Boolean {
        val piece = board[from.row][from.col] ?: return false

        // Simple move validation
        if (!isValidMove(from, to, piece)) return false

        board[to.row][to.col] = piece
        board[from.row][from.col] = null
        return true
    }

    private fun isValidMove(from: Position, to: Position, piece: Piece): Boolean {
        val rowDiff = to.row - from.row
        val colDiff = to.col - from.col

        // Ensure diagonal movement
        if (kotlin.math.abs(rowDiff) != kotlin.math.abs(colDiff)) return false

        // Ensure correct movement direction
        if (piece is Piece.Normal) {
            val direction = if (piece.player == Player.RED) 1 else -1
            if (rowDiff != direction && kotlin.math.abs(rowDiff) != 2) return false
        }

        return board[to.row][to.col] == null
    }

    fun displayBoard() {
        for (row in board) {
            println(row.joinToString(" ") { piece -> piece?.player?.name?.first()?.toString() ?: "." })
        }
    }
}

fun main() {
    val game = CheckersBoard()
    game.displayBoard()
}
