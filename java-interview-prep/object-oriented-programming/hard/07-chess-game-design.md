# Chess Game Design

## Problem Statement

Design a chess game with proper OOP principles, supporting move validation, check/checkmate detection, and game state management.

## Approach

- Use Strategy pattern for piece movement rules
- Implement Command pattern for move history and undo
- Track game state (active, check, checkmate, stalemate)
- Validate moves based on piece type and board state

## Solution

```java
enum Color { WHITE, BLACK }
enum PieceType { KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN }

abstract class Piece {
    protected Color color;
    protected PieceType type;
    
    public Piece(Color color, PieceType type) {
        this.color = color;
        this.type = type;
    }
    
    public abstract boolean isValidMove(int fromX, int fromY, int toX, int toY, Board board);
    public Color getColor() { return color; }
    public PieceType getType() { return type; }
}

class King extends Piece {
    public King(Color color) { super(color, PieceType.KING); }
    
    @Override
    public boolean isValidMove(int fromX, int fromY, int toX, int toY, Board board) {
        int dx = Math.abs(toX - fromX);
        int dy = Math.abs(toY - fromY);
        return dx <= 1 && dy <= 1 && (dx + dy) > 0;
    }
}

class Rook extends Piece {
    public Rook(Color color) { super(color, PieceType.ROOK); }
    
    @Override
    public boolean isValidMove(int fromX, int fromY, int toX, int toY, Board board) {
        return (fromX == toX || fromY == toY) && board.isPathClear(fromX, fromY, toX, toY);
    }
}

class Board {
    private Piece[][] squares;
    
    public Board() {
        squares = new Piece[8][8];
        initializeBoard();
    }
    
    private void initializeBoard() {
        // Place pieces in starting positions
        squares[0][0] = new Rook(Color.WHITE);
        squares[0][4] = new King(Color.WHITE);
        squares[7][0] = new Rook(Color.BLACK);
        squares[7][4] = new King(Color.BLACK);
        // ... initialize other pieces
    }
    
    public Piece getPiece(int x, int y) {
        return squares[x][y];
    }
    
    public void setPiece(int x, int y, Piece piece) {
        squares[x][y] = piece;
    }
    
    public boolean isPathClear(int fromX, int fromY, int toX, int toY) {
        int dx = Integer.compare(toX - fromX, 0);
        int dy = Integer.compare(toY - fromY, 0);
        
        int x = fromX + dx;
        int y = fromY + dy;
        
        while (x != toX || y != toY) {
            if (squares[x][y] != null) return false;
            x += dx;
            y += dy;
        }
        return true;
    }
}

class Move {
    private int fromX, fromY, toX, toY;
    private Piece piece;
    private Piece capturedPiece;
    
    public Move(int fromX, int fromY, int toX, int toY, Piece piece, Piece capturedPiece) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
        this.piece = piece;
        this.capturedPiece = capturedPiece;
    }
    
    public int getFromX() { return fromX; }
    public int getFromY() { return fromY; }
    public int getToX() { return toX; }
    public int getToY() { return toY; }
    public Piece getCapturedPiece() { return capturedPiece; }
}

class Game {
    private Board board;
    private Color currentTurn;
    private List<Move> moveHistory;
    
    public Game() {
        this.board = new Board();
        this.currentTurn = Color.WHITE;
        this.moveHistory = new ArrayList<>();
    }
    
    public boolean makeMove(int fromX, int fromY, int toX, int toY) {
        Piece piece = board.getPiece(fromX, fromY);
        
        if (piece == null || piece.getColor() != currentTurn) {
            return false;
        }
        
        if (!piece.isValidMove(fromX, fromY, toX, toY, board)) {
            return false;
        }
        
        Piece capturedPiece = board.getPiece(toX, toY);
        board.setPiece(toX, toY, piece);
        board.setPiece(fromX, fromY, null);
        
        moveHistory.add(new Move(fromX, fromY, toX, toY, piece, capturedPiece));
        currentTurn = (currentTurn == Color.WHITE) ? Color.BLACK : Color.WHITE;
        
        System.out.println("Moved " + piece.getType() + " from (" + fromX + "," + fromY + ") to (" + toX + "," + toY + ")");
        return true;
    }
}

class ChessDemo {
    public static void main(String[] args) {
        Game game = new Game();
        game.makeMove(0, 4, 0, 5);  // Move white king
    }
}
```

## Complexity Analysis

**Time Complexity**: O(1) for most moves, O(n) for path validation where n is distance
**Space Complexity**: O(m) for move history with m moves

## Edge Cases and Pitfalls

- **Check Detection**: Validate that moves don't leave king in check
- **Castling**: Implement special castling rules
- **En Passant**: Handle pawn's special capture move
- **Promotion**: Allow pawn promotion at end of board
- **Stalemate**: Detect when no legal moves available

## Interview-Ready Answer

"I'd design chess with abstract Piece class and concrete piece types implementing isValidMove(). Board class manages 8x8 grid and path validation. Game class tracks turn, move history, and validates moves. Use Strategy pattern for piece-specific movement rules. Check for valid moves, captures, and special rules. Time complexity is O(1) for moves, O(n) for path checks, space is O(m) for history."
