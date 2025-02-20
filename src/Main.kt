// Player class
enum class Player { RED, BLACK }

// Position class
data class Position(val row: Int, val col: Int)

// sealed Piece class
sealed class Piece(val player: Player) {
    class Normal(player: Player) : Piece(player)
    class King(player: Player) : Piece(player)
}

// Checkers board class
class CheckersBoard {
    // Initializing the board, currentPlayer, and the bool of double jumping
    private val board: Array<Array<Piece?>> = Array(8) { arrayOfNulls(8) }
    var currentPlayer: Player = Player.RED
    private var mustContinueJumping: Position? = null

    // Set up the board to initialize
    init {
        setupBoard()
    }

    // Set up the board
    private fun setupBoard() {
        for (row in 0..2) placePieces(row, Player.RED)
        for (row in 5..7) placePieces(row, Player.BLACK)
    }

    // Place the pieces on the board
    private fun placePieces(row: Int, player: Player) {
        for (col in 0 until 8 step 2) {
            val adjustedCol = if (row % 2 == 0) col + 1 else col
            board[row][adjustedCol] = Piece.Normal(player)
        }
    }

    // Move the piece, including King movement
    fun movePiece(from: Position, to: Position): Boolean {
        val piece = board[from.row][from.col] ?: return false

        if (piece.player != currentPlayer || (mustContinueJumping != null && mustContinueJumping != from)) return false
        if (!isValidMove(from, to, piece)) return false

        val capturedPiecePos = getCapturedPiecePosition(from, to)
        if (capturedPiecePos != null) board[capturedPiecePos.row][capturedPiecePos.col] = null

        // King movement logic
        val newPiece = convertToKing(to, piece) ?: piece
        board[to.row][to.col] = newPiece
        board[from.row][from.col] = null

        // Double jumping logic
        if (capturedPiecePos != null && hasJumpMove(to, newPiece)) {
            mustContinueJumping = to
        } else {
            mustContinueJumping = null
            switchTurn()
        }
        return true
    }

    // Switching turns
    private fun switchTurn() {
        currentPlayer = if (currentPlayer == Player.RED) Player.BLACK else Player.RED
    }

    // Logic on whether a move is valid (including King movement)
    private fun isValidMove(from: Position, to: Position, piece: Piece): Boolean {
        val rowDiff = to.row - from.row
        val colDiff = to.col - from.col

        if (kotlin.math.abs(rowDiff) != kotlin.math.abs(colDiff)) return false
        if (board[to.row][to.col] != null) return false

        if (piece is Piece.Normal) {
            val direction = if (piece.player == Player.RED) 1 else -1
            if (rowDiff != direction && kotlin.math.abs(rowDiff) != 2) return false
        }

        if (kotlin.math.abs(rowDiff) == 2) {
            val capturedPiecePos = getCapturedPiecePosition(from, to)
            val capturedPiece = capturedPiecePos?.let { board[it.row][it.col] }
            if (capturedPiece == null || capturedPiece.player == piece.player) return false
        }
        return true
    }

    // Gets the captured piece off the board
    private fun getCapturedPiecePosition(from: Position, to: Position): Position? {
        return if (kotlin.math.abs(from.row - to.row) == 2) {
            Position((from.row + to.row) / 2, (from.col + to.col) / 2)
        } else null
    }

    // Convert a normal piece to a King piece
    private fun convertToKing(to: Position, piece: Piece): Piece? {
        return if (piece is Piece.Normal && ((piece.player == Player.RED && to.row == 7) || (piece.player == Player.BLACK && to.row == 0))) {
            Piece.King(piece.player)
        } else null
    }

    // Determine if the player has a double jump move
    private fun hasJumpMove(pos: Position, piece: Piece): Boolean {
        val directions = listOf(-2 to -2, -2 to 2, 2 to -2, 2 to 2)
        return directions.any { (rowDiff, colDiff) ->
            val newPos = Position(pos.row + rowDiff, pos.col + colDiff)
            newPos.row in 0..7 && newPos.col in 0..7 && isValidMove(pos, newPos, piece)
        }
    }

    // Determining if the game is over
    fun isGameOver(): Boolean {
        return board.flatten().filterNotNull().none { it.player == currentPlayer && hasMove(it) }
    }

    // Determining if the player has any moves left
    private fun hasMove(piece: Piece): Boolean {
        for (row in 0..7) {
            for (col in 0..7) {
                val pos = Position(row, col)
                if (board[row][col] == piece) {
                    val directions = listOf(-1 to -1, -1 to 1, 1 to -1, 1 to 1, -2 to -2, -2 to 2, 2 to -2, 2 to 2)
                    if (directions.any { (rowDiff, colDiff) ->
                            val newPos = Position(row + rowDiff, col + colDiff)
                            newPos.row in 0..7 && newPos.col in 0..7 && isValidMove(pos, newPos, piece)
                        }) return true
                }
            }
        }
        return false
    }

    // Display the board
    fun displayBoard() {
        println("Current Player: $currentPlayer")
        println("  0 1 2 3 4 5 6 7")
        for ((index, row) in board.withIndex()) {
            print("$index ")
            println(row.joinToString(" ") { piece -> when (piece) {
                is Piece.Normal -> if (piece.player == Player.RED) "r" else "b"
                is Piece.King -> if (piece.player == Player.RED) "R" else "B"
                else -> "."
            } })
        }
    }
}

// A clear console function
fun clearConsole() {
    repeat(50) { println() }
}

// Main game function (main)
fun main() {
    val game = CheckersBoard()
    while (true) {
        game.displayBoard()
        if (game.isGameOver()) {
            println("Game over! ${if (game.currentPlayer == Player.RED) "Black" else "Red"} wins!")
            break
        }
        println("Enter move (e.g., '2 3 3 4') or 'q' to quit: ")
        val input = readlnOrNull()?.trim() ?: break
        if (input == "q") break
        val coords = input.split(" ").mapNotNull { it.toIntOrNull() }
        if (coords.size == 4) {
            if (game.movePiece(Position(coords[0], coords[1]), Position(coords[2], coords[3]))) {
                clearConsole()
                println("Move successful!")
            } else {
                clearConsole()
                println("Invalid move, try again.")
            }
        } else {
            clearConsole()
            println("Invalid input format.")
        }
    }
}
