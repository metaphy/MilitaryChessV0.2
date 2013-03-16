/**
 * @author Gou Ming Shi
 * http://metaphy.javaeye.com/
 * Dec 1, 2009
 * All Rights Reserved
 */
package com.javaeye.metaphy.game;

import static com.javaeye.metaphy.game.Game.LINEUP_FILE_EXT;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import com.javaeye.metaphy.model.Coordinate;
import com.javaeye.metaphy.model.Located;
import com.javaeye.metaphy.model.MoveAttack;
import com.javaeye.metaphy.model.Piece;

public class GameBoard {
	/* SoldierStation type */
	public static final byte INVALID = 0x00;
	public static final byte HEADQUARTER = 0x71;
	public static final byte CAMP = 0x72;
	public static final byte STATION_ROAD = 0x73;
	public static final byte STATION_RAILWAY = 0x74;

	/* Soldier type 对于棋子而言 － 无棋子: 0x00 ; 其他 South的棋子使用1位（16进制），North使用2位（16进制） */
	public static final byte FLAG_S = 0x02;
	public static final byte MINE_S = 0x03;
	public static final byte BOMB_S = 0x04;
	public static final byte SILING_S = 0x05;
	public static final byte JUNZHANG_S = 0x06;
	public static final byte SHIZHANG_S = 0x07;
	public static final byte LVZHANG_S = 0x08;
	public static final byte TUANZHANG_S = 0x09;
	public static final byte YINGZHANG_S = 0x0A;
	public static final byte LIANZHANG_S = 0x0B;
	public static final byte PAIZHANG_S = 0x0C;
	public static final byte GONGBING_S = 0x0D;
	public static final byte FLAG_N = 0x12;
	public static final byte MINE_N = 0x13;
	public static final byte BOMB_N = 0x14;
	public static final byte SILING_N = 0x15;
	public static final byte JUNZHANG_N = 0x16;
	public static final byte SHIZHANG_N = 0x17;
	public static final byte LVZHANG_N = 0x18;
	public static final byte TUANZHANG_N = 0x19;
	public static final byte YINGZHANG_N = 0x1A;
	public static final byte LIANZHANG_N = 0x1B;
	public static final byte PAIZHANG_N = 0x1C;
	public static final byte GONGBING_N = 0x1D;
	/* Size of x/y Coordinate */
	public static final int BOARD_ARRAY_SIZE = 17;
	/* Lineup files max index */
	public static final int MAX_LINEUP_FILES = 24;

	/* x,y,z(to indicate it's a soldierStation or piece) */
	private byte[][] stations = new byte[BOARD_ARRAY_SIZE][BOARD_ARRAY_SIZE];
	private byte[][] board = new byte[BOARD_ARRAY_SIZE][BOARD_ARRAY_SIZE];
	/* lineup file */
	private URL lineupFile = null;
	/* for A* path finding */
	private ArrayList<Coordinate> openList = new ArrayList<Coordinate>();
	private ArrayList<Coordinate> closedList = new ArrayList<Coordinate>();

	// private static Logger logger = Logger.getLogger(GameBoard.class);

	/**
	 * Constructor, to init the stations(roads) and the board
	 */
	public GameBoard() {
		// Init the Board -SoldierStations and Roads, pieces
		initSoldierStations();
		initBoard();
		// autoGenerateBoard(Located.SOUTH);
		// autoGenerateBoard(Located.NORTH);
		loadPieces(Located.SOUTH);
		loadPieces(Located.NORTH);
	}

	/**
	 * Init the board
	 */
	public void initSoldierStations() {
		// All the points
		for (int i = 0; i < BOARD_ARRAY_SIZE; i++) {
			for (int j = 0; j < BOARD_ARRAY_SIZE; j++) {
				if (i >= 0 && i <= 5 && (j >= 0 && j <= 5 || j >= 11 && j <= 16)
						|| i >= 11 && i <= 16 && (j >= 0 && j <= 5 || j >= 11 && j <= 16)
						|| (i == 6 || i == 8 || i == 10) && (j == 7 || j == 9)
						|| (i == 7 || i == 9) && (j >= 6 && j <= 10)) {
					// 无效点
					stations[i][j] = INVALID;
				} else if ((i == 7 || i == 9) && (j == 0 || j == 16)
						|| (i == 0 || i == 16) && (j == 7 || j == 9)) {
					// 大本营(司令部)
					stations[i][j] = HEADQUARTER;
				} else if ((i == 7 || i == 9) && (j == 2 || j == 4 || j == 12 || j == 14)
						|| i == 8 && (j == 3 || j == 13)
						|| (i == 2 || i == 4 || i == 12 || i == 14) && (j == 7 || j == 9)
						|| (i == 3 || i == 13) && j == 8) {
					// 行营
					stations[i][j] = CAMP;
				} else if ((i == 0 || i == 16) && (j == 6 || j == 8 || j == 10)
						|| (i == 2 || i == 4 || i == 12 || i == 14) && j == 8
						|| (i == 3 || i == 13) && (j == 7 || j == 9)
						|| (j == 0 || j == 16) && (i == 6 || i == 8 || i == 10)
						|| (j == 2 || j == 4 || j == 12 || j == 14) && i == 8
						|| (j == 3 || j == 13) && (i == 7 || i == 9)) {
					// 非行营、非大本营的公路节点
					stations[i][j] = STATION_ROAD;
				} else {
					// 其他，即含铁路的节点
					stations[i][j] = STATION_RAILWAY;
				}
			}
		}
	}

	/**
	 * Init all pieces of the board
	 */
	public void initBoard() {
		for (int i = 0; i < BOARD_ARRAY_SIZE; i++) {
			for (int j = 0; j < BOARD_ARRAY_SIZE; j++) {
				board[i][j] = 0;
			}
		}
	}

	/**
	 * Init all pieces of the board
	 * 
	 * @param loc
	 */
	public void initHalfBoard(Located loc) {
		if (loc == Located.NORTH) {
			for (int i = 0; i < BOARD_ARRAY_SIZE; i++) {
				for (int j = 0; j <= 5; j++) {
					board[i][j] = 0;
				}
			}
		} else {
			for (int i = 0; i < BOARD_ARRAY_SIZE; i++) {
				for (int j = 11; j < BOARD_ARRAY_SIZE; j++) {
					board[i][j] = 0;
				}
			}
		}
	}

	/**
	 * Get a random line up file from "../res/" which name is like
	 * "lineup-x.jql"
	 */
	public void randomLineupFile() {
		Random random = new Random();
		int indx = random.nextInt(MAX_LINEUP_FILES) + 1;
		String file = "lineup-" + indx + LINEUP_FILE_EXT;
		URLClassLoader urlLoader = (URLClassLoader) Game.class.getClassLoader();
		lineupFile = urlLoader.findResource("res/" + file);
	}

	/**
	 * Init the pieces of Located location
	 */
	public void loadPieces(Located located) {
		/* the default file length: 50 bytes */
		final int LINEUP_FILE_LENGTH = 50;

		// // Test Begin
		// URLClassLoader urlLoader = (URLClassLoader)
		// Game.class.getClassLoader();
		// if (located == Located.NORTH) {
		// lineupFile = urlLoader.findResource("res/lineup-3.jql");
		// } else if (located == Located.SOUTH) {
		// lineupFile = urlLoader.findResource("res/lineup-2.jql");
		// }
		// // Test End

		byte[] bytes = new byte[LINEUP_FILE_LENGTH];
		DataInputStream dis = null;
		int x = 0; // The x Coordinate
		int y = 0; // The y Coordinate
		try {
			// Init lineupFile
			if (lineupFile == null) {
				randomLineupFile();
			}
			dis = new DataInputStream(lineupFile.openStream());
			dis.read(bytes, 0, LINEUP_FILE_LENGTH);
			// Ignore the first 20 bytes
			for (int i = 20; i < bytes.length; i++) {
				if (bytes[i] != INVALID) {
					if (located == Located.SOUTH) {
						x = 6 + (i - 20) % 5;
						y = 11 + (i - 20) / 5;
						board[x][y] = bytes[i];
					} else if (located == Located.NORTH) {
						x = 10 - (i - 20) % 5;
						y = 5 - (i - 20) / 5;
						board[x][y] = (byte) ((int) bytes[i] + 0x10);
					}
				}
			}
			dis.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				dis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		lineupFile = null;
	}

	/**
	 * Automatically generate the board for Located
	 */
	public void autoGenerateBoard(Located loc) {
		int[] halfPieces = new int[25];
		Random r = new Random();
		int randomHeadquarter0 = r.nextInt(2);
		r = new Random();
		int randomHeadquarter1 = r.nextInt(11);
		initHalfBoard(loc);
		halfPieces[0] = 1; // flag
		int locatedJ = 0, startJ = 0, endJ = 0;
		if (loc == Located.NORTH) {
			locatedJ = 0;
			startJ = 0;
			endJ = 5;
		} else if (loc == Located.SOUTH) {
			locatedJ = 16;
			startJ = 11;
			endJ = 16;
		}

		if (randomHeadquarter0 == 0) {
			board[7][locatedJ] = sortedPiece(loc, 0);
			if (randomHeadquarter1 < 6) {
				board[9][locatedJ] = sortedPiece(loc, 9);
				halfPieces[9] = 1;
			} else {
				board[9][locatedJ] = sortedPiece(loc, 1);
				halfPieces[1] = 1;
			}
		} else {
			board[9][locatedJ] = sortedPiece(loc, 0);
			if (randomHeadquarter1 < 1) {
				board[7][locatedJ] = sortedPiece(loc, 12);
				halfPieces[12] = 1;
			} else if (randomHeadquarter1 < 6) {
				board[7][locatedJ] = sortedPiece(loc, 9);
				halfPieces[9] = 1;
			} else {
				board[7][locatedJ] = sortedPiece(loc, 1);
				halfPieces[1] = 1;
			}
		}

		for (int j = startJ; j <= endJ; j++) {
			for (int i = 6; i <= 10; i++) {
				if (stations[i][j] != HEADQUARTER && stations[i][j] != CAMP) {
					int index = r.nextInt(25);
					while (halfPieces[index] == 1) {
						index = r.nextInt(25);
					}
					halfPieces[index] = 1;
					board[i][j] = sortedPiece(loc, index);
				}
			}
		}
		if (!lineupRulesCompliance()) {
			// Generate the board again
			autoGenerateBoard(loc);
		}
	}

	/**
	 * Get the piece according to the index. Flag, mine * 3, bomb * 2, gong * 3,
	 * pai *3, lian *3, ying *2, tuan *2, lv *2, shi *2, jun, siling
	 */
	private byte sortedPiece(Located loc, int index) {
		if (loc == Located.NORTH) {
			if (index == 0) {
				return FLAG_N;
			} else if (index > 0 && index <= 3) {
				return MINE_N;
			} else if (index > 3 && index <= 5) {
				return BOMB_N;
			} else if (index > 5 && index <= 8) {
				return GONGBING_N;
			} else if (index > 8 && index <= 11) {
				return PAIZHANG_N;
			} else if (index > 11 && index <= 14) {
				return LIANZHANG_N;
			} else if (index > 14 && index <= 16) {
				return YINGZHANG_N;
			} else if (index > 16 && index <= 18) {
				return TUANZHANG_N;
			} else if (index > 18 && index <= 20) {
				return LVZHANG_N;
			} else if (index > 20 && index <= 22) {
				return SHIZHANG_N;
			} else if (index > 22 && index <= 23) {
				return JUNZHANG_N;
			} else if (index > 23 && index <= 24) {
				return SILING_N;
			} else {
				return INVALID;
			}
		} else {
			if (index == 0) {
				return FLAG_S;
			} else if (index > 0 && index <= 3) {
				return MINE_S;
			} else if (index > 3 && index <= 5) {
				return BOMB_S;
			} else if (index > 5 && index <= 8) {
				return GONGBING_S;
			} else if (index > 8 && index <= 11) {
				return PAIZHANG_S;
			} else if (index > 11 && index <= 14) {
				return LIANZHANG_S;
			} else if (index > 14 && index <= 16) {
				return YINGZHANG_S;
			} else if (index > 16 && index <= 18) {
				return TUANZHANG_S;
			} else if (index > 18 && index <= 20) {
				return LVZHANG_S;
			} else if (index > 20 && index <= 22) {
				return SHIZHANG_S;
			} else if (index > 22 && index <= 23) {
				return JUNZHANG_S;
			} else if (index > 23 && index <= 24) {
				return SILING_S;
			} else {
				return INVALID;
			}
		}
	}

	/**
	 * The path from beginning -> end Coordinate
	 */
	public Vector<Coordinate> pathFinding(int x0, int y0, int x, int y) {
		// Validation
		if (stations[x0][y0] == INVALID || stations[x][y] == INVALID) {
			return null;
		}
		// The pieces in HEADQUARTER and the mines can NOT move
		if (stations[x0][y0] == HEADQUARTER || board[x0][y0] == MINE_S
				|| board[x0][y0] == MINE_N) {
			return null;
		}
		// Can not move to a camp which was occupied
		if (stations[x][y] == CAMP && board[x][y] != INVALID) {
			return null;
		}
		// Piece -> Piece which are belong to the same player
		if (board[x0][y0] > 0x10 && board[x][y] > 0x10 || board[x0][y0] < 0x10
				&& board[x][y] < 0x10 && board[x][y] != INVALID) {
			return null;
		}

		Coordinate beginning = new Coordinate(x0, y0);
		Coordinate end = new Coordinate(x, y);
		Vector<Coordinate> path = new Vector<Coordinate>();
		// Road station or Camp ( | - ) , move only 1 step
		if (stations[x0][y0] == STATION_ROAD || stations[x][y] == STATION_ROAD
				|| stations[x0][y0] == CAMP || stations[x][y] == CAMP
				|| stations[x][y] == HEADQUARTER) {
			if (roadAdjacent(x0, y0, x, y)) {
				path.add(new Coordinate(x, y));
				return path;
			}
		}

		// Camp, move only 1 step (/ \)
		if (stations[x0][y0] == CAMP || stations[x][y] == CAMP) {
			if (campAdjacent(x0, y0, x, y)) {
				path.add(new Coordinate(x, y));
				return path;
			}
		}

		// Railway, A* path finding
		openList = new ArrayList<Coordinate>();
		closedList = new ArrayList<Coordinate>();
		if (stations[x0][y0] == STATION_RAILWAY
				&& stations[x][y] == STATION_RAILWAY
				&& (validRailwayRoad(x0, y0, x, y) || board[x0][y0] == GONGBING_S || board[x0][y0] == GONGBING_N)) {
			ArrayList<Coordinate> adjacent = new ArrayList<Coordinate>();
			boolean engineer = board[x0][y0] == GONGBING_S || board[x0][y0] == GONGBING_N;
			Coordinate current = null;
			openList.add(beginning);
			do {
				// Find the minimum F value Coordinate from the openList
				current = lookForMinF(end);
				openList.remove(current);
				closedList.add(current);
				// Get all adjacent XYs of current
				adjacent = allAdjacents(beginning, current, end, engineer);
				// logger.debug("All adjacents of current(" + current.value +
				// ") = " + adjacent.size());

				for (Coordinate adj : adjacent) { // Traverse all adjacents of
					// current Coordinate
					if (!closedListContains(adj)
							&& (board[adj.x][adj.y] == INVALID || adj.equals(end))) {
						if (!openListContains(adj)) {
							adj.parent = current;
							openList.add(adj);
						} else {
							if (getCostG(current.parent, current)
									+ getCostG(current, adj) < getCostG(current.parent,
									adj)) {
								adj.parent = current;
							}
						}
					}
				}
			} while (!openListContains(end) && openList.size() > 0);
			end.parent = current;

			if (openListContains(end)) { // Find the path
				Coordinate t = end;
				while (t != beginning) {
					path.add(t);
					t = t.parent;
				}
			}
		}
		// Convert the path array
		for (int i = path.size() - 1; i > (path.size() - 1) / 2; i--) {
			Coordinate tmp = path.get(i);
			path.set(i, path.get(path.size() - 1 - i));
			path.set(path.size() - 1 - i, tmp);
		}
		return path;
	}

	/**
	 * Get the adjacent points of current
	 */
	private ArrayList<Coordinate> allAdjacents(Coordinate beginning, Coordinate current,
			Coordinate end, boolean engineer) {
		ArrayList<Coordinate> adjacent = new ArrayList<Coordinate>();

		if (engineer) {
			for (int i = -1; i <= 1; i += 2) {
				if (stations[current.x][current.y + i] == STATION_RAILWAY) {
					adjacent.add(new Coordinate(current.x, current.y + i));
				} else if (current.y + i * 2 >= 0 && current.y + i * 2 < BOARD_ARRAY_SIZE
						&& stations[current.x][current.y + i * 2] == STATION_RAILWAY) {
					adjacent.add(new Coordinate(current.x, current.y + i * 2));
				}
				if (stations[current.x + i][current.y] == STATION_RAILWAY) {
					adjacent.add(new Coordinate(current.x + i, current.y));
				} else if (current.x + i * 2 >= 0 && current.x + i * 2 < BOARD_ARRAY_SIZE
						&& stations[current.x + i * 2][current.y] == STATION_RAILWAY) {
					adjacent.add(new Coordinate(current.x + i * 2, current.y));
				}
			}
			if (adjacent.size() == 3) { // process Round_railway
				for (int i = -1; i <= 1; i += 2) {
					for (int j = -1; j <= 1; j += 2) {
						Coordinate c = new Coordinate(current.x + i, current.y + j);
						if (engineerAdjacentTurnAround(c)) {
							adjacent.add(c);
						}
					}
				}
			}
		} else { // Not the engineer
			for (int i = -1; i <= 1; i += 2) { // The beginning and end are on
				// the same line/column
				if (current.x == end.x
						&& stations[current.x][current.y + i] == STATION_RAILWAY) {
					adjacent.add(new Coordinate(current.x, current.y + i));
				} else if (current.x == end.x && (current.y + i * 2) >= 0
						&& (current.y + i * 2) < BOARD_ARRAY_SIZE
						&& stations[current.x][current.y + i * 2] == STATION_RAILWAY) {
					adjacent.add(new Coordinate(current.x, current.y + i * 2));
				}

				if (current.y == end.y
						&& stations[current.x + i][current.y] == STATION_RAILWAY) {
					adjacent.add(new Coordinate(current.x + i, current.y));
				} else if (current.y == end.y && (current.x + i * 2) >= 0
						&& (current.x + i * 2) < BOARD_ARRAY_SIZE
						&& stations[current.x + i * 2][current.y] == STATION_RAILWAY) {
					adjacent.add(new Coordinate(current.x + i * 2, current.y));
				}
			}
			// On turn around railway
			if (onTurnAroundRailway(beginning, end)) {
				int[] turnAround = { 601, 602, 603, 604, 605, 506, 406, 306, 206, 106,
						1001, 1002, 1003, 1004, 1005, 1106, 1206, 1306, 1406, 1506, 110,
						210, 310, 410, 510, 611, 612, 613, 614, 615, 1510, 1410, 1310,
						1210, 1110, 1011, 1012, 1013, 1014, 1015 };
				for (int i = 0; i < turnAround.length; i++) {
					if (getCostG(current, new Coordinate(turnAround[i])) <= 14) {
						adjacent.add(new Coordinate(turnAround[i]));
					}
				}
			}
		}
		return adjacent;
	}

	/**
	 * True if c1 and c2 on turn around railway
	 */
	private boolean onTurnAroundRailway(Coordinate c1, Coordinate c2) {
		int[][] turnAround = { { 601, 602, 603, 604, 605, 506, 406, 306, 206, 106 },
				{ 1001, 1002, 1003, 1004, 1005, 1106, 1206, 1306, 1406, 1506 },
				{ 110, 210, 310, 410, 510, 611, 612, 613, 614, 615 },
				{ 1510, 1410, 1310, 1210, 1110, 1011, 1012, 1013, 1014, 1015 } };
		for (int i = 0; i < turnAround.length; i++) {
			boolean foundC1 = false;
			boolean foundC2 = false;
			for (int j = 0; j < turnAround[i].length; j++) {
				if (turnAround[i][j] == c1.value)
					foundC1 = true;
				if (turnAround[i][j] == c2.value)
					foundC2 = true;
			}
			if (foundC1 && foundC2)
				return true;
		}
		return false;
	}

	/**
	 * True if c on engineer turnAround rail way
	 */
	private boolean engineerAdjacentTurnAround(Coordinate c) {
		int[] turnAround = { 506, 605, 1005, 1106, 1110, 1011, 611, 510 };
		for (int i = 0; i < turnAround.length; i++) {
			if (turnAround[i] == c.value)
				return true;
		}
		return false;
	}

	/**
	 * True if openList contains the target
	 */
	private boolean openListContains(Coordinate target) {
		for (Coordinate c : openList) {
			if (c.equals(target))
				return true;
		}
		return false;
	}

	/**
	 * True if closedList contains the target
	 */
	private boolean closedListContains(Coordinate target) {
		for (Coordinate c : closedList) {
			if (c.equals(target))
				return true;
		}
		return false;
	}

	/**
	 * Look for the Coordinate that has the min F value from openList list
	 */
	private Coordinate lookForMinF(Coordinate target) {
		Coordinate c = openList.get(0);
		for (int i = 1; i < openList.size(); i++) {
			Coordinate tmp = openList.get(i);
			if (getCostG(tmp.parent, tmp) + getDistanceH(tmp, target) < getCostG(
					c.parent, c)
					+ getDistanceH(c, target)) {
				c = tmp;
			}
		}
		return c;
	}

	/**
	 * The G function - cost from c0 to c1
	 */
	private int getCostG(Coordinate c0, Coordinate c1) {
		// c.parent compare to c, if c is the beginning, then c.parent is NULL
		if (c0 == null || c1 == null) {
			return 0;
		}

		// Validation
		if (stations[c0.x][c0.y] == INVALID || stations[c1.x][c1.y] == INVALID) {
			return Integer.MAX_VALUE;
		}

		if (c0.x == c1.x || c0.y == c1.y) {
			return abs(c0.x - c1.x) * 10 + abs(c0.y - c1.y) * 10;
		} else if (abs(c0.x - c1.x) == 1 && abs(c0.y - c1.y) == 1) {
			return 14;
		} else {
			return Integer.MAX_VALUE;
		}
	}

	/**
	 * The H fucntion - Manhattan distance from x0,y0 to x,y
	 */
	private int getDistanceH(int x0, int y0, int x, int y) {
		return (abs(x0 - x) + abs(y0 - y)) * 10;
	}

	private int getDistanceH(Coordinate c0, Coordinate c1) {
		return getDistanceH(c0.x, c0.y, c1.x, c1.y);
	}

	/**
	 * abs()
	 */
	private int abs(int x) {
		return x >= 0 ? x : -x;
	}

	/**
	 * Road adjacent
	 */
	private boolean roadAdjacent(int x0, int y0, int x, int y) {
		return x0 == x && abs(y0 - y) == 1 || y0 == y && abs(x0 - x) == 1;
	}

	/**
	 * Camp adjacent
	 */
	private boolean campAdjacent(int x0, int y0, int x, int y) {
		return abs(x0 - x) == 1 && abs(y0 - y) == 1;
	}

	/**
	 * Valid railway road
	 */
	private boolean validRailwayRoad(int x0, int y0, int x, int y) {
		return x0 == x || y0 == y
				|| (x0 == 6 && y0 >= 1 && y0 <= 5 && y == 6 && x >= 1 && x <= 5)
				|| (y0 == 6 && x0 >= 1 && x0 <= 5 && x == 6 && y >= 1 && y <= 5)
				|| (y0 == 10 && x0 >= 1 && x0 <= 5 && x == 6 && y >= 11 && y <= 15)
				|| (x0 == 6 && y0 >= 11 && y0 <= 15 && y == 10 && x >= 1 && x <= 5)
				|| (x0 == 10 && y0 >= 11 && y0 <= 15 && y == 10 && x >= 11 && x <= 15)
				|| (y0 == 10 && x0 >= 11 && x0 <= 15 && x == 10 && y >= 11 && y <= 15)
				|| (y0 == 6 && x0 >= 11 && x0 <= 15 && x == 10 && y >= 1 && y <= 5)
				|| (x0 == 10 && y0 >= 1 && y0 <= 5 && y == 6 && x >= 11 && x <= 15);
	}

	/**
	 * Move and attack
	 */
	public MoveAttack moveAttack(int x0, int y0, int x, int y) {
		// Validation
		if (stations[x0][y0] == INVALID || stations[x][y] == INVALID
				|| board[x0][y0] == INVALID) {
			return MoveAttack.INVALID;
		}
		// Validation - Same location of two pieces
		if (board[x0][y0] < 0x10 && board[x][y] < 0x10 && board[x][y] > 0x00
				|| board[x0][y0] > 0x10 && board[x][y] > 0x10) {
			return MoveAttack.INVALID;
		}
		// Only moving
		if (board[x0][y0] != INVALID && board[x][y] == INVALID) {
			board[x][y] = board[x0][y0];
			board[x0][y0] = INVALID;
			return MoveAttack.MOVE;
		}
		// Attack
		if (board[x][y] == FLAG_S || board[x][y] == FLAG_N) {
			// Game over
			board[x][y] = board[x0][y0];
			board[x0][y0] = INVALID;
			return MoveAttack.GAME_OVER;
		} else if (board[x0][y0] == BOMB_S || board[x0][y0] == BOMB_N
				|| board[x][y] == BOMB_S || board[x][y] == BOMB_N) {
			// Bomb
			board[x0][y0] = INVALID;
			board[x][y] = INVALID;
			return MoveAttack.EQUAL;
		} else if (board[x][y] == MINE_S || board[x][y] == MINE_N) {
			if (board[x0][y0] == GONGBING_S || board[x0][y0] == GONGBING_N) {
				// Mine
				board[x][y] = board[x0][y0];
				board[x0][y0] = INVALID;
				return MoveAttack.KILL;
			} else {
				board[x0][y0] = INVALID;
				return MoveAttack.KILLED;
			}
		} else {
			// Soldiers
			byte tmp = 0;
			if (board[x0][y0] > 0x10) {
				tmp = (byte) ((int) board[x0][y0] - 0x10);
			} else {
				tmp = (byte) ((int) board[x0][y0] + 0x10);
			}
			if (tmp < board[x][y]) {
				board[x][y] = board[x0][y0];
				board[x0][y0] = INVALID;
				return MoveAttack.KILL;
			} else if (tmp == board[x][y]) {
				board[x0][y0] = INVALID;
				board[x][y] = INVALID;
				return MoveAttack.EQUAL;
			} else {
				board[x0][y0] = INVALID;
				return MoveAttack.KILLED;
			}
		}
	}

	/**
	 * Whether or not obey the lineup Rules. True if all are ok.
	 */
	public boolean lineupRulesCompliance() {
		for (int j = 0; j < BOARD_ARRAY_SIZE; j++) {
			for (int i = 0; i < BOARD_ARRAY_SIZE; i++) {
				// 军旗没有在司令部
				if ((board[i][j] == FLAG_N || board[i][j] == FLAG_S)
						&& stations[i][j] != HEADQUARTER) {
					return false;
				}
				// 炸弹在第一排
				if (board[i][j] == BOMB_N && j == 5 || board[i][j] == BOMB_S && j == 11) {
					return false;
				}
				// 地雷没有在后两排
				if (board[i][j] == MINE_N && j != 0 && j != 1 || board[i][j] == MINE_S
						&& j != 15 && j != 16) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Get a copy of board[][]
	 */
	public byte[][] getCopyOfBoard() {
		byte[][] ret = new byte[BOARD_ARRAY_SIZE][BOARD_ARRAY_SIZE];
		for (int j = 0; j < BOARD_ARRAY_SIZE; j++) {
			for (int i = 0; i < BOARD_ARRAY_SIZE; i++) {
				ret[i][j] = board[i][j];
			}
		}
		return ret;
	}

	/**
	 * Recover the board
	 */
	public void recoverBoard(byte[][] b) {
		for (int j = 0; j < BOARD_ARRAY_SIZE; j++) {
			for (int i = 0; i < BOARD_ARRAY_SIZE; i++) {
				this.board[i][j] = b[i][j];
			}
		}
	}

	/**
	 * Check wheter the game is over
	 * 
	 * @return 0: game is not over; 1: game over, North is winner; 2: South is
	 *         winner; 3: drawn
	 */
	public int checkGameOver() {
		boolean northCanMove = false, southCanMove = false;
		for (int j = 0; j < BOARD_ARRAY_SIZE; j++) {
			for (int i = 0; i < BOARD_ARRAY_SIZE; i++) {
				for (int tj = 0; tj < BOARD_ARRAY_SIZE; tj++) {
					for (int ti = 0; ti < BOARD_ARRAY_SIZE; ti++) {
						Vector<Coordinate> path = pathFinding(i, j, ti, tj);
						if (path != null && path.size() > 0) {
							if (board[i][j] > 0x10) {
								northCanMove = true;
							} else if (board[i][j] > 0x00 && board[i][j] < 0x10) {
								southCanMove = true;
							}
						}
						if (northCanMove && southCanMove) {
							return 0;
						}
					}
				}
			}
		}
		if (northCanMove) {
			return 1;
		} else if (southCanMove) {
			return 2;
		} else {
			return 3;
		}
	}

	/**
	 * Getter, setter
	 */
	public URL getLineupFile() {
		return lineupFile;
	}

	public void setLineupFile(URL lineupFile) {
		this.lineupFile = lineupFile;
	}

	public byte[][] getBoard() {
		return board;
	}

	public byte[][] getStations() {
		return stations;
	}

	/**
	 * Print the board[][][], for test
	 */
	public void print() {
		Piece piece = new Piece();
		// Print all pieces
		for (int j = 0; j < BOARD_ARRAY_SIZE; j++) {
			for (int i = 0; i < BOARD_ARRAY_SIZE; i++) {
				if (board[i][j] == 0x00) {
					System.out.print("-");
				} else {
					System.out.print(piece.pieceTitle(board[i][j]));
					if (board[i][j] > 0x10) {
						System.out.print("*");
					}
				}
				System.out.print("\t");
			}
			System.out.print("\n");
		}

	}

	public static void main(String[] args) {
		GameBoard b = new GameBoard();
		b.print();
	}
}
