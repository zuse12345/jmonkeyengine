/**
 * Knights Tour
 */
public class KnightsTour {

    int[][] boardspots;

    private void createBoard(int size){
        boardspots = new int[size][size];
    }

    private void setSpot(int x, int y, int moveNum) {
        boardspots[x][y] = moveNum;
    }

    private void resetSpot(int x, int y) {
        boardspots[x][y] = 0;
    }

    public boolean isValid(int x, int y) {
        if (x >= boardspots.length || y >= boardspots.length){
            return false;
        }else if (x < 0 || y < 0){
            return false;
        }else{
            return true;
        }
    }

    public int getSpotValue(int x, int y) {
        return boardspots[x][y];
    }

    @Override
    public String toString(){
        String print = "";
        for (int x = 0; x < boardspots.length; x++){
            for (int y = 0; y < boardspots.length; y++){
                print += String.format("%1$2d", boardspots[x][y]) + " ";
            }
            print += "\n";
        }
        return print;
    }

    private boolean makeMove(int x, int y, int totalMoves) {
        setSpot(x, y, totalMoves);

        if (totalMoves == 64){
            return true;
        }

        if (isValid(x - 2, y - 1) && getSpotValue(x - 2, y - 1) == 0){
            if (makeMove(x - 2, y - 1, totalMoves + 1)){
                return true;
            }
        }
        if (isValid(x - 1, y - 2) && getSpotValue(x - 1, y - 2) == 0){
            if (makeMove(x - 1, y - 2, totalMoves + 1)){
                return true;
            }
        }
        if (isValid(x + 1, y - 2) && getSpotValue(x + 1, y - 2) == 0){
            if (makeMove(x + 1, y - 2, totalMoves + 1)){
                return true;
            }
        }
        if (isValid(x + 2, y - 1) && getSpotValue(x + 2, y - 1) == 0){
            if (makeMove(x + 2, y - 1, totalMoves + 1)){
                return true;
            }
        }
        if (isValid(x + 2, y + 1) && getSpotValue(x + 2, y + 1) == 0){
            if (makeMove(x + 2, y + 1, totalMoves + 1)){
                return true;
            }
        }
        if (isValid(x + 1, y + 2) && getSpotValue(x + 1, y + 2) == 0){
            if (makeMove(x + 1, y + 2, totalMoves + 1)){
                return true;
            }
        }
        if (isValid(x - 1, y + 2) && getSpotValue(x - 1, y + 2) == 0){
            if (makeMove(x - 1, y + 2, totalMoves + 1)){
                return true;
            }
        }
        if (isValid(x - 2, y + 1) && getSpotValue(x - 2, y + 1) == 0){
            if (makeMove(x - 2, y + 1, totalMoves + 1)){
                return true;
            }
        }

        resetSpot(x, y);
        return false;
    }

    public boolean computeTour(){
        return makeMove(0, 0, 1);
    }

    public static void main(String[] args) {
        KnightsTour tour = new KnightsTour();
        tour.createBoard(8);
        if (tour.computeTour()){
            System.out.println("Tour Found!");
            System.out.println(tour);
        }
    }
        
}
