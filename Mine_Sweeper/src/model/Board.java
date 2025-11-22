package model;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import enums.CellType;
import enums.Difficulty;
import enums.MoveResult;

public class Board {
    private int rows;
    private int cols;
    private int mineCount;
    private int questionsCount;
    private int surpriseCount;
    List<Point> emptyPositions = new ArrayList<>();
    private Cell[][] cells;

    public Board(Difficulty difficulty) {
        this.rows = difficulty.rows;
        this.cols = difficulty.cols;
        this.mineCount = difficulty.mines;
        this.questionsCount=difficulty.questions;
        this.surpriseCount= difficulty.surprise;
        initBoard();
    }

    private void initBoard() {
        cells = new Cell[rows][cols];

        // Create all cells
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                cells[r][c] = new Cell();
            }
        }

        placeMines();
        calculateNumbers();
    }

    private void placeMines() {
        
        List<Point> positions = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                positions.add(new Point(r, c));
            }
        }

        // Randomize the order
        Collections.shuffle(positions);

        // Place mines
        for (int i = 0; i < mineCount; i++) {
            Point p = positions.get(i);
            cells[p.x][p.y].setType(CellType.MINE);
        }
        
        
        
        
    }
   
    	private void calculateNumbers() {
    		
    	    for (int r = 0; r < rows; r++) {
    	        for (int c = 0; c < cols; c++) {

    	            // Skip mines – they don't need numbers
    	            if (cells[r][c].getType() == CellType.MINE) {
    	                cells[r][c].setAdjacentMines(-1);  // optional
    	                continue;
    	            }

    	            int count = 0;

    	            // Check the 8 neighbors
    	            for (int rowNb = -1; rowNb <= 1; rowNb++) {
    	                for (int columnNb = -1; columnNb <= 1; columnNb++) {

    	                    // Skip the cell itself
    	                    if (rowNb == 0 && columnNb == 0) continue;

    	                    int nr = r + rowNb;
    	                    int nc = c + columnNb;

    	                    // Check bounds
    	                    if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
    	                        if (cells[nr][nc].getType() == CellType.MINE) {
    	                            count++;
    	                        }
    	                    }
    	                }
    	            }

    	           
    	            cells[r][c].setAdjacentMines(count);
    	            
    	            if(count==0) {
    	            emptyPositions.add(new Point(r,c));
    	            }

    	        }
    	    }
    	}
    	public void addQuestionsAndSurprises() {
    		 Collections.shuffle(emptyPositions);
    		 if(this.surpriseCount+this.questionsCount>emptyPositions.size()) {
    			 System.out.println("invalid game");
    			 return;
    		 }
    		 for(int i =0 ; i<surpriseCount;i++) {
    			 Point p = emptyPositions.get(i);
    			 cells[p.x][p.y].setType(CellType.SURPRISE);
    			 
    		 }
    		 for(int i =surpriseCount ; i<questionsCount+surpriseCount;i++) {
    			 Point p = emptyPositions.get(i);
    			 cells[p.x][p.y].setType(CellType.QUESTION);
    			 
    		 }
    	}
    	public MoveResult revealCell(int r, int c) {

    	    if (!inBounds(r, c)) return MoveResult.INVALID;

    	    Cell cell = cells[r][c];

    	    if (cell.isRevealed() || cell.isFlagged()) {
    	        return MoveResult.INVALID;
    	    }

    	    // Reveal the cell
    	    cell.reveal();

    	    // Handle cell types
    	    switch (cell.getType()) {

    	        case MINE:
    	            return MoveResult.MINE;

    	        case QUESTION:
    	            return MoveResult.QUESTION;

    	        case SURPRISE:
    	            return MoveResult.SURPRISE;

    	        case EMPTY:
    	        default:
    	            if (cell.getAdjacentMines() == 0) {
    	                floodReveal(r, c);
    	            }
    	            return MoveResult.SAFE;
    	    }
    	}
    	private boolean inBounds(int r, int c) {
    	    return r >= 0 && r < rows && c >= 0 && c < cols;
    	}
    	
    	private void floodReveal(int r, int c) {
    	    for (int dr = -1; dr <= 1; dr++) {
    	        for (int dc = -1; dc <= 1; dc++) {

    	            if (dr == 0 && dc == 0) continue;

    	            int nr = r + dr;
    	            int nc = c + dc;

    	            if (!inBounds(nr, nc)) continue;

    	            Cell neighbor = cells[nr][nc];

    	            if (neighbor.isRevealed() || neighbor.isFlagged()) continue;

    	            neighbor.reveal();

    	            if (neighbor.getAdjacentMines() == 0 && neighbor.getType() == CellType.EMPTY) {
    	                floodReveal(nr, nc);
    	            }
    	        }
    	    }
    	}

		public int getRows() {
			return rows;
		}

		public void setRows(int rows) {
			this.rows = rows;
		}

		public int getCols() {
			return cols;
		}

		public void setCols(int cols) {
			this.cols = cols;
		}

		public int getMineCount() {
			return mineCount;
		}

		public void setMineCount(int mineCount) {
			this.mineCount = mineCount;
		}

		public int getQuestionsCount() {
			return questionsCount;
		}

		public void setQuestionsCount(int questionsCount) {
			this.questionsCount = questionsCount;
		}

		public int getSurpriseCount() {
			return surpriseCount;
		}

		public void setSurpriseCount(int surpriseCount) {
			this.surpriseCount = surpriseCount;
		}

		public List<Point> getEmptyPositions() {
			return emptyPositions;
		}

		public void setEmptyPositions(List<Point> emptyPositions) {
			this.emptyPositions = emptyPositions;
		}

		public Cell[][] getCells() {
			return cells;
		}

		public void setCell(Cell[][] cells) {
			this.cells = cells;
		}
		public Cell getCell(int r,int c) {
			return cells[r][c];
		}
    		
    	}
    

