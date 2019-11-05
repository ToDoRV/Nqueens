import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Nqueens {

    private static final Integer MIN_DIMENSION = 4;
    private static final Integer START_INCLUSIVE = 0;
    private static final Integer BASE_COUNT_OF_CONFLICTS = -1;
    private static final Integer COEFFICIENT_OF_MAX_ITERATIONS = 2;
    private static final Integer ALLOWABLE_COUNT_OF_CONFLICTS = 0;
    private static final Integer FIRST_COLUMN = 0;
    private static final Integer FIRST_ROW = 0;

    private static final String YELLOW = "\u001B[43m";
    private static final String BLACK = "\u001B[100m";
    private static final String BLACK_QUEEN_COLOR = "\u001B[1;97m";
    private static final String RESET_COLOR = "\u001B[0m";

    private Integer dimension;
    private ArrayList<Integer> positionsByColumns;
    private ArrayList<Integer> rowConflicts;
    private ArrayList<Integer> majorDiagonalConflicts;
    private ArrayList<Integer> minorDiagonalConflicts;

    public Nqueens() {
        Scanner input = new Scanner(System.in);
        while ((dimension = input.nextInt()) < MIN_DIMENSION) {
            System.out.println("Please, enter correct input!");
        }

        initialization();
    }

    private void initialization() {
        positionsByColumns = new ArrayList<>(IntStream.range(START_INCLUSIVE, dimension)
                .boxed().collect(Collectors.toList()));
        Collections.shuffle(positionsByColumns);

        rowConflicts = new ArrayList<>(Collections.nCopies(dimension, BASE_COUNT_OF_CONFLICTS));
        checkForRowConflicts();

        Integer diagonalCapacity = 2 * dimension - 1;

        majorDiagonalConflicts = new ArrayList<>(Collections.nCopies(diagonalCapacity, BASE_COUNT_OF_CONFLICTS));
        checkForMajorDiagonalConflicts();
        minorDiagonalConflicts = new ArrayList<>(Collections.nCopies(diagonalCapacity, BASE_COUNT_OF_CONFLICTS));
        checkForMinorDiagonalConflicts();
    }

    private void checkForRowConflicts() {
        for (Integer row : positionsByColumns) {
            rowConflicts.set(row, rowConflicts.get(row) + 1);
        }
    }

    private void checkForMajorDiagonalConflicts() {
        for (int i = 0; i < dimension; ++i) {
            Integer index = getMajorDiagonalIndex(positionsByColumns.get(i), i);
            majorDiagonalConflicts.set(index, majorDiagonalConflicts.get(index) + 1);
        }
    }

    private Integer getMajorDiagonalIndex(Integer row, Integer column) {
        return column - row + dimension - 1;
    }

    private void checkForMinorDiagonalConflicts() {
        for (int i = 0; i < dimension; ++i) {
            Integer index = i + positionsByColumns.get(i);
            minorDiagonalConflicts.set(index, minorDiagonalConflicts.get(index) + 1);
        }
    }

    public void solveTheProblem() { // Min conflicts
        while (theProblemIsNotSolved()) {
            initialization();
        }
    }

    private boolean theProblemIsNotSolved() {
        Integer i = 0;
        Integer maxIterations = COEFFICIENT_OF_MAX_ITERATIONS * dimension;
        while (i <= maxIterations) {
            if (!hasConflicts()) {
                return false;
            }
            ++i;

            Integer columnWithMaxConflicts = getColumnWhoseQueenHashMaxConflicts();
            Integer rowWithMinConflicts = getRowWhichWillHaveMinConflicts(columnWithMaxConflicts);
            recalculateConflicts(columnWithMaxConflicts, rowWithMinConflicts);
            positionsByColumns.set(columnWithMaxConflicts, rowWithMinConflicts);
        }

        if (hasConflicts()) {
            return true;
        }
        return false;
    }

    private boolean hasConflicts() {
        return hasConflictsInList(rowConflicts)
                || hasConflictsInList(majorDiagonalConflicts)
                || hasConflictsInList(minorDiagonalConflicts);
    }

    private boolean hasConflictsInList(ArrayList<Integer> listConflicts) {
        for (Integer value : listConflicts) {
            if (value > ALLOWABLE_COUNT_OF_CONFLICTS) {
                return true;
            }
        }
        return false;
    }

    private Integer getColumnWhoseQueenHashMaxConflicts() {
        ArrayList<Integer> indexesOfColumnsWithMaxConflicts = new ArrayList<>();

        Integer maxColumnConflicts = calculateConflicts(FIRST_COLUMN);
        indexesOfColumnsWithMaxConflicts.add(FIRST_COLUMN);

        for (int i = 1; i < dimension; ++i) {
            Integer currentColumnConflicts = calculateConflicts(i);
            if (currentColumnConflicts > maxColumnConflicts) {
                maxColumnConflicts = currentColumnConflicts;
                indexesOfColumnsWithMaxConflicts = new ArrayList<>();
                indexesOfColumnsWithMaxConflicts.add(i);
            } else if (currentColumnConflicts.equals(maxColumnConflicts)) {
                indexesOfColumnsWithMaxConflicts.add(i);
            }
        }

        Random rand = new Random();
        return indexesOfColumnsWithMaxConflicts.get(rand.nextInt(indexesOfColumnsWithMaxConflicts.size()));
    }

    private Integer calculateConflicts(Integer column) {
        Integer positionOfQueen = positionsByColumns.get(column);
        return rowConflicts.get(positionOfQueen)
                + majorDiagonalConflicts.get(getMajorDiagonalIndex(positionOfQueen, column))
                + minorDiagonalConflicts.get(positionOfQueen + column);
    }

    private Integer getRowWhichWillHaveMinConflicts(Integer column) {
        ArrayList<Integer> indexesOfRowsWithMinConflicts = new ArrayList<>();

        Integer minRowConflicts = calculateConflicts(column, FIRST_ROW);
        indexesOfRowsWithMinConflicts.add(FIRST_ROW);

        for (int i = 1; i < dimension; ++i) {
            Integer currentRowConflicts = calculateConflicts(column, i);
            if (currentRowConflicts < minRowConflicts) {
                minRowConflicts = currentRowConflicts;
                indexesOfRowsWithMinConflicts = new ArrayList<>();
                indexesOfRowsWithMinConflicts.add(i);
            } else if (currentRowConflicts.equals(minRowConflicts)) {
                indexesOfRowsWithMinConflicts.add(i);
            }
        }

        Random rand = new Random();
        return indexesOfRowsWithMinConflicts.get(rand.nextInt(indexesOfRowsWithMinConflicts.size()));
    }

    private Integer calculateConflicts(Integer column, Integer row) {
        if (row.equals(positionsByColumns.get(column))) {
            return calculateConflicts(column);
        }

        return rowConflicts.get(row) + 1
                + majorDiagonalConflicts.get(getMajorDiagonalIndex(row, column)) + 1
                + minorDiagonalConflicts.get(row + column) + 1;
    }

    private void recalculateConflicts(Integer column, Integer newRow) {
        Integer oldRow = positionsByColumns.get(column);
        rowConflicts.set(oldRow, rowConflicts.get(oldRow) - 1);
        Integer oldIndexOfMajorDiagonal = getMajorDiagonalIndex(oldRow, column);
        majorDiagonalConflicts.set(oldIndexOfMajorDiagonal, majorDiagonalConflicts.get(oldIndexOfMajorDiagonal) - 1);
        Integer oldIndexOfMinorDiagonal = column + oldRow;
        minorDiagonalConflicts.set(oldIndexOfMinorDiagonal, minorDiagonalConflicts.get(oldIndexOfMinorDiagonal) - 1);

        rowConflicts.set(newRow, rowConflicts.get(newRow) + 1);
        Integer newIndexOfMajorDiagonal = getMajorDiagonalIndex(newRow, column);
        majorDiagonalConflicts.set(newIndexOfMajorDiagonal, majorDiagonalConflicts.get(newIndexOfMajorDiagonal) + 1);
        Integer newIndexOfMinorDiagonal = column + newRow;
        minorDiagonalConflicts.set(newIndexOfMinorDiagonal, minorDiagonalConflicts.get(newIndexOfMinorDiagonal) + 1);
    }

    public void printBoard() {
        for (int i = 0; i < dimension; ++i) {
            for (int j = 0; j < dimension; ++j) {
                if ((j + i) % 2 == 0) {
                    System.out.print(YELLOW);
                } else {
                    System.out.print(BLACK);
                }
                if (positionsByColumns.get(j).equals(i)) {
                    System.out.print(BLACK_QUEEN_COLOR + " ♛ ");
                } else {
                    System.out.print("   ");
                }
                System.out.print(RESET_COLOR);
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        Nqueens nqueens = new Nqueens();
        long startTime = System.nanoTime();
        nqueens.solveTheProblem();
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;

        System.out.println("The problem is solved!");
        System.out.print("Do you want to see the board? (y or n): ");
        Scanner input = new Scanner(System.in);
        char answer = input.next().charAt(0);
        if (answer == 'y' || answer == 'Y') {
            nqueens.printBoard();
        }

        System.out.print("Do you want to see the time? (y or n): ");
        answer = input.next().charAt(0);
        if (answer == 'y' || answer == 'Y') {
            System.out.print((double) totalTime / 1000000000);
            System.out.println("sec");
        }
    }
}
