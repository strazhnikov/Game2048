import java.util.*;

public class Model {
    private static final int FIELD_WIDTH = 4;
    private Tile[][] gameTiles;
    int score;
    int maxTile;
    Stack<Tile[][]> previousStates = new Stack<>();
    Stack<Integer> previousScores = new Stack<>();
    boolean isSaveNeeded = true;

    public Model() {
        gameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        resetGameTiles();
        score = 0;
        maxTile = 0;
    }

    public Tile[][] getGameTiles() {
        return gameTiles;
    }

    void resetGameTiles() {
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[i].length; j++) {
                gameTiles[i][j] = new Tile();
            }
        }
        addTile();
        addTile();
    }

    private void addTile() {
        List<Tile> emptyTiles = getEmptyTiles();
        if (emptyTiles.size() > 0) {
            int index = (int) (emptyTiles.size() * Math.random());
            emptyTiles.get(index).value = (Math.random() < 0.9 ? 2 : 4);
        }
    }

    private List<Tile> getEmptyTiles() {
        List<Tile> emptyTiles = new ArrayList<>();
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[i].length; j++) {
                if (gameTiles[i][j].isEmpty()) {
                    emptyTiles.add(gameTiles[i][j]);
                }
            }
        }
        return emptyTiles;
    }

    private boolean compressTiles(Tile[] tiles) {
        Tile[] compressedTiles = new Tile[tiles.length];
        int j = 0;
        boolean isCompressed = false;

        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i].value != 0) {
                compressedTiles[j++] = tiles[i];
            }
        }
        for (int i = j; i < tiles.length; i++) {
            compressedTiles[i] = new Tile();
        }
        isCompressed = !Arrays.equals(tiles, compressedTiles);

        for (int i = 0; i < tiles.length; i++) {
            tiles[i] = compressedTiles[i];
        }

        return isCompressed;
    }

    private boolean mergeTiles(Tile[] tiles) {
        boolean isMerged = false;
        for (int i = 0; i < tiles.length - 1; i++) {
            if (tiles[i].value == tiles[i+1].value) {
                int sum = tiles[i].value + tiles[i+1].value;
                if (sum > 0) {
                    isMerged = true;
                    if (sum > maxTile) maxTile = sum;
                    score += sum;
                    tiles[i] = new Tile(sum);
                    tiles[i + 1] = new Tile();
                }
            }
        }
        if (isMerged) compressTiles(tiles);
        return isMerged;
    }
    
    private void clockwise() {
        Tile[][] newGameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                newGameTiles[i][j] = gameTiles[FIELD_WIDTH - j - 1][i];
            }
        }
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = newGameTiles[i][j];
            }
        }
    }

    private void counterclockwise() {
        Tile[][] newGameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                newGameTiles[i][j] = gameTiles[j][FIELD_WIDTH - i - 1];
            }
        }
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                gameTiles[i][j] = newGameTiles[i][j];
            }
        }
    }

    void left() {
        boolean isChanged = false;
        if (isSaveNeeded) saveState(gameTiles);
        for (int i = 0; i < gameTiles.length; i++) {
            boolean isCompressed = compressTiles(gameTiles[i]);
            boolean isMerged = mergeTiles(gameTiles[i]);
            if (!isChanged) isChanged = isCompressed || isMerged;
        }
        if (isChanged) addTile();
        isSaveNeeded = true;
    }

    void up() {
        saveState(gameTiles);
        counterclockwise();
        left();
        clockwise();
    }

    void down() {
        saveState(gameTiles);
        clockwise();
        left();
        counterclockwise();
    }

    void right() {
        saveState(gameTiles);
        clockwise();
        clockwise();
        left();
        counterclockwise();
        counterclockwise();
    }

    public boolean canMove() {
        for (int i = 0; i < FIELD_WIDTH - 1; i++) {
            for (int j = 0; j < FIELD_WIDTH - 1; j++) {
                if (gameTiles[i][j].isEmpty() || gameTiles[i][j+1].isEmpty() || gameTiles[i+1][j].isEmpty()) return true;
                if ((gameTiles[i][j].value == gameTiles[i][j+1].value) || (gameTiles[i][j].value == gameTiles[i+1][j].value)) return true;
            }
        }
        //check the bottom right element
        if (gameTiles[FIELD_WIDTH-1][FIELD_WIDTH-1].isEmpty()) return true;
        if ((gameTiles[FIELD_WIDTH-1][FIELD_WIDTH-1].value == gameTiles[FIELD_WIDTH-1][FIELD_WIDTH-2].value) || (gameTiles[FIELD_WIDTH-1][FIELD_WIDTH-1].value == gameTiles[FIELD_WIDTH-2][FIELD_WIDTH-1].value)) return true;
        return false;
    }

    private void saveState(Tile[][] gameTiles) {
        Tile[][] newGameTiles = new Tile[FIELD_WIDTH][FIELD_WIDTH];
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                newGameTiles[i][j] = new Tile(gameTiles[i][j].value);
            }
        }
        previousStates.push(newGameTiles);
        previousScores.push(score);
        isSaveNeeded = false;
    }

    public void rollback() {
        if (!previousStates.empty() && !previousScores.empty()) {
            gameTiles = previousStates.pop();
            score = previousScores.pop();
        }
    }

    public void randomMove() {
        int n = ((int) (Math.random() * 100)) % 4;
        if (n == 0) left();
        else if (n == 1) right();
        else if (n == 2) up();
        else if (n == 3) down();
    }

    boolean hasBoardChanged() {
        if (previousStates.empty()) return false;
        Tile[][] boardFromStack = previousStates.peek();
        for (int i = 0; i < FIELD_WIDTH; i++) {
            for (int j = 0; j < FIELD_WIDTH; j++) {
                if (gameTiles[i][j].value != boardFromStack[i][j].value) return true;
            }
        }
        return false;
    }

    MoveEfficiency getMoveEfficiency(Move move) {

        move.move();

        int emptyTiles = getEmptyTiles().size();
        int moveScore = score;
        if (!hasBoardChanged()) {
            emptyTiles = -1;
            moveScore = 0;
        }

        rollback();

        return new MoveEfficiency(emptyTiles, moveScore, move);
    }

    public void autoMove() {
        PriorityQueue<MoveEfficiency> queue = new PriorityQueue<>(4, Collections.reverseOrder());
        queue.offer(getMoveEfficiency(this::left));
        queue.offer(getMoveEfficiency(new Move() {
            @Override
            public void move() {
                up();
            }
        }));
        queue.offer(getMoveEfficiency(this::right));
        queue.offer(getMoveEfficiency(this::down));
        queue.peek().getMove().move();
    }
    
    /*
    @Override
    public String toString() {
        StringBuilder matrix = new StringBuilder();
        for (int i = 0; i < gameTiles.length; i++) {
            for (int j = 0; j < gameTiles[i].length; j++) {
                matrix.append(gameTiles[i][j] + " ");
            }
            matrix.append(System.lineSeparator());
        }
        matrix.append(System.lineSeparator());
        return matrix.toString();
    }
    */
}
