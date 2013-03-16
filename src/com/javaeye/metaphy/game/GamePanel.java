/**
 * @author Gou Ming Shi
 * http://metaphy.javaeye.com/
 * Sep 22, 2009
 * All Rights Reserved
 */
package com.javaeye.metaphy.game;

import static com.javaeye.metaphy.game.Game.BOARD_GRID_SIZE;
import static com.javaeye.metaphy.game.Game.GRID_UNIT_LENGTH;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Line2D;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.javaeye.metaphy.action.GamePanelAction;
import com.javaeye.metaphy.ai.Movement;
import com.javaeye.metaphy.game.OperationButton.Operations;
import com.javaeye.metaphy.model.BaseElement;
import com.javaeye.metaphy.model.Coordinate;
import com.javaeye.metaphy.model.Located;
import com.javaeye.metaphy.model.Piece;
import com.javaeye.metaphy.model.SoldierStation;

@SuppressWarnings("serial")
public class GamePanel extends JPanel {
	/* Board */
	private GameBoard gameBoard = Game.ME.getGameBoard();
	/* Draw base line or not */
	private boolean debugDrawBaseline = false;
	/* Draw background image or not */
	private boolean debugDrawBackgroundImage = false;
	/* File path of the background */
	private String backgroundImageFile = "res/images/background.jpg";
	/* back ground image */
	private Image backgroundImage = null;
	/* Operation Buttons */
	private OperationButton operations[] = new OperationButton[6];
	/* SaveLineupFile radios */
	private SaveLineupRadioPane saveLineupRadioPane = new SaveLineupRadioPane();
	/* Command input box and output box */
	private CommandBox commandBox = new CommandBox();
	/* Board */
	private byte[][] board = gameBoard.getBoard();
	private byte[][] stations = gameBoard.getStations();
	/* Piece list */
	private ArrayList<Piece> pieces = new ArrayList<Piece>();
	/* SoldierStation list */
	private ArrayList<SoldierStation> ssList = new ArrayList<SoldierStation>();
	/* Path arrows */
	private Vector<Movement> arrowsList = new Vector<Movement>();

	/* Cached the arrow images */
	private static final Image[] ARROWS_IMAGE = new Image[8];

	static {
		try {
			URLClassLoader urlLoader = (URLClassLoader) (Game.class.getClassLoader());
			URL url = urlLoader.findResource("res/images/Arrow0.png");
			ARROWS_IMAGE[0] = ImageIO.read(url);
			url = urlLoader.findResource("res/images/Arrow1.png");
			ARROWS_IMAGE[1] = ImageIO.read(url);
			url = urlLoader.findResource("res/images/Arrow2.png");
			ARROWS_IMAGE[2] = ImageIO.read(url);
			url = urlLoader.findResource("res/images/Arrow3.png");
			ARROWS_IMAGE[3] = ImageIO.read(url);
			url = urlLoader.findResource("res/images/Arrow4.png");
			ARROWS_IMAGE[4] = ImageIO.read(url);
			url = urlLoader.findResource("res/images/Arrow5.png");
			ARROWS_IMAGE[5] = ImageIO.read(url);
			url = urlLoader.findResource("res/images/Arrow6.png");
			ARROWS_IMAGE[6] = ImageIO.read(url);
			url = urlLoader.findResource("res/images/Arrow7.png");
			ARROWS_IMAGE[7] = ImageIO.read(url);
			System.out.println("Images cached!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor of the game panel
	 */
	public GamePanel() {
		super();
		setFont(Game.TIMER_NUMBER_FONT);
		setLayout(null);
		setFocusable(true); // enable the JPanle to accept key input

		// Add pieces and soldier stations
		addBaseElements();
		// Add operation buttons
		addOptionButtons();
		// Add radios pane
		add(saveLineupRadioPane);
		// Add the command input box and output box
		add(commandBox);
		add(commandBox.getCobwsp());
		// Add the playing timer
		add(Game.ME.getTimer());
		// Add action listeners
		addMouseListener(new GamePanelAction(this));
		addKeyListener(new GamePanelAction(this));
	}

	/**
	 * Add pieces and soldier stations
	 */
	private void addBaseElements() {
		// Add all pieces firstly
		for (int j = 0; j < GameBoard.BOARD_ARRAY_SIZE; j++) {
			for (int i = 0; i < GameBoard.BOARD_ARRAY_SIZE; i++) {
				// Initialize the pieces
				if (board[i][j] != 0x00) {
					Piece piece = new Piece(i, j, board[i][j]);
					if (board[i][j] > 0x10) {
						piece.setShowCaption(false);
					}
					piece.renderWidget();
					piece.addWidgetAction();
					pieces.add(piece); // add to the vector
					add(piece.getWidget());
				}
			}
		}
		// Then add all soldierStations
		for (int j = 0; j < GameBoard.BOARD_ARRAY_SIZE; j++) {
			for (int i = 0; i < GameBoard.BOARD_ARRAY_SIZE; i++) {
				// Initialize the soldierStations and add them on the panel
				if (stations[i][j] != 0x00) {
					SoldierStation ss = new SoldierStation(i, j, stations[i][j]);
					ss.renderWidget();
					ss.addWidgetAction();
					ssList.add(ss);
					add(ss.getWidget());
				}
			}
		}
	}

	/**
	 * Get the Staion or Piece
	 */
	public BaseElement getBaseElement(Coordinate c) {
		Piece p = getPiece(c);
		if (p != null) {
			return p;
		}
		for (SoldierStation ss : ssList) {
			if (ss.getX() == c.x && ss.getY() == c.y) {
				return ss;
			}
		}
		return null;
	}

	/**
	 * Add operation buttons
	 */
	private void addOptionButtons() {
		// Add operation buttons
		operations[0] = new OperationButton(Operations.START_GAME);
		operations[1] = new OperationButton(Operations.CALLIN_LINEUP);
		operations[2] = new OperationButton(Operations.SAVE_LINEUP);
		operations[3] = new OperationButton(Operations.CALLIN_REPEAT);
		operations[4] = new OperationButton(Operations.PASS);
		operations[5] = new OperationButton(Operations.GIVE_UP);
		// Hide the "Pass" "GiveUP" buttons before game started
		operations[4].setVisible(false);
		operations[5].setVisible(false);

		add(operations[0]);
		add(operations[1]);
		add(operations[2]);
		add(operations[3]);
		add(operations[4]);
		add(operations[5]);
	}

	/**
	 * Paint
	 * 
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;

		// Draw background image
		if (isDebugDrawBackgroundImage()) {
			// read background image
			try {
				URLClassLoader urlLoader = (URLClassLoader) (Game.class.getClassLoader());
				URL url = urlLoader.findResource(backgroundImageFile);
				backgroundImage = ImageIO.read(url);
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (backgroundImage != null) {
				int imgWidth = backgroundImage.getWidth(this);
				int imgHeight = backgroundImage.getHeight(this);
				g2.drawImage(backgroundImage, 0, 0, null);
				for (int i = 0; i * imgWidth <= getWidth(); i++)
					for (int j = 0; j * imgHeight <= getHeight(); j++)
						if (i + j > 0)
							g2.copyArea(0, 0, imgWidth, imgHeight, i * imgWidth, j
									* imgHeight);
			}
		}

		// Draw base line
		if (isDebugDrawBaseline()) {
			for (int i = 0; i <= BOARD_GRID_SIZE; i++) {
				// Draw horizontal line
				g2.setPaint(Color.LIGHT_GRAY);
				Line2D line = new Line2D.Double(GRID_UNIT_LENGTH, (i + 1)
						* GRID_UNIT_LENGTH, BOARD_GRID_SIZE * GRID_UNIT_LENGTH
						+ GRID_UNIT_LENGTH, (i + 1) * GRID_UNIT_LENGTH);
				g2.draw(line);
				// Draw vertical line
				line = new Line2D.Double((i + 1) * GRID_UNIT_LENGTH, GRID_UNIT_LENGTH,
						(i + 1) * GRID_UNIT_LENGTH, BOARD_GRID_SIZE * GRID_UNIT_LENGTH
								+ GRID_UNIT_LENGTH);
				g2.draw(line);
				// Draw column number
				g2.setPaint(Color.BLACK);
				g2.drawString(String.valueOf(i), (i + 1) * GRID_UNIT_LENGTH,
						GRID_UNIT_LENGTH - GRID_UNIT_LENGTH / 2);
				// Draw line number
				g2.drawString(String.valueOf(i), GRID_UNIT_LENGTH - GRID_UNIT_LENGTH * 3
						/ 4, (i + 1) * GRID_UNIT_LENGTH);
			}
		}

		// Draw all the roads and railways
		for (int j = 0; j < GameBoard.BOARD_ARRAY_SIZE; j++) { // Every column
			for (int i = 0; i < GameBoard.BOARD_ARRAY_SIZE; i++) {// Every row
				if (stations[i][j] != GameBoard.INVALID) {
					if (stations[i][j] == GameBoard.STATION_ROAD
							|| stations[i][j] == GameBoard.HEADQUARTER
							|| stations[i][j] == GameBoard.CAMP) {
						if (j + 1 < GameBoard.BOARD_ARRAY_SIZE
								&& stations[i][j + 1] != GameBoard.INVALID) {
							drawRoad(g2, i, j, i, j + 1, GameBoard.STATION_ROAD);
						}
						if (i + 1 < GameBoard.BOARD_ARRAY_SIZE
								&& stations[i + 1][j] != GameBoard.INVALID) {
							drawRoad(g2, i, j, i + 1, j, GameBoard.STATION_ROAD);
						}
					}

					if (stations[i][j] == GameBoard.STATION_RAILWAY) {
						if (j + 1 < GameBoard.BOARD_ARRAY_SIZE
								&& stations[i][j + 1] == GameBoard.STATION_RAILWAY) {
							drawRoad(g2, i, j, i, j + 1, GameBoard.STATION_RAILWAY);
						} else if (j + 1 < GameBoard.BOARD_ARRAY_SIZE
								&& (stations[i][j + 1] == GameBoard.STATION_ROAD
										|| stations[i][j + 1] == GameBoard.CAMP || stations[i][j + 1] == GameBoard.HEADQUARTER)) {
							drawRoad(g2, i, j, i, j + 1, GameBoard.STATION_ROAD);
						} else if (j + 2 < GameBoard.BOARD_ARRAY_SIZE
								&& stations[i][j + 1] == GameBoard.INVALID
								&& stations[i][j + 2] == GameBoard.STATION_RAILWAY) {
							drawRoad(g2, i, j, i, j + 2, GameBoard.STATION_RAILWAY);
						}
						if (i + 1 < GameBoard.BOARD_ARRAY_SIZE
								&& stations[i + 1][j] == GameBoard.STATION_RAILWAY) {
							drawRoad(g2, i, j, i + 1, j, GameBoard.STATION_RAILWAY);
						} else if (i + 1 < GameBoard.BOARD_ARRAY_SIZE
								&& (stations[i + 1][j] == GameBoard.STATION_ROAD
										|| stations[i + 1][j] == GameBoard.CAMP || stations[i + 1][j] == GameBoard.HEADQUARTER)) {
							drawRoad(g2, i, j, i + 1, j, GameBoard.STATION_ROAD);
						} else if (i + 2 < GameBoard.BOARD_ARRAY_SIZE
								&& stations[i + 1][j] == GameBoard.INVALID
								&& stations[i + 2][j] == GameBoard.STATION_RAILWAY) {
							drawRoad(g2, i, j, i + 2, j, GameBoard.STATION_RAILWAY);
						}
					}

					// Draw / or \ path connected with camp
					if (stations[i][j] == GameBoard.CAMP)
						for (int m = -1; m <= 1; m += 2)
							for (int n = -1; n <= 1; n += 2)
								if (stations[i + m][j + n] == GameBoard.CAMP
										|| stations[i + m][j + n] == GameBoard.STATION_RAILWAY)
									drawRoad(g2, i, j, i + m, j + n,
											GameBoard.STATION_ROAD);
				}
			}
		}
		// Draw round railway
		drawRoad(g2, 5, 6, 6, 5, GameBoard.STATION_RAILWAY);
		drawRoad(g2, 5, 10, 6, 11, GameBoard.STATION_RAILWAY);
		drawRoad(g2, 10, 5, 11, 6, GameBoard.STATION_RAILWAY);
		drawRoad(g2, 10, 11, 11, 10, GameBoard.STATION_RAILWAY);

		drawArrows(g2);
	}

	/**
	 * Draw the roads and railways
	 */
	private void drawRoad(Graphics2D g2, int x0, int y0, int x, int y, byte type) {
		g2.setPaint(Color.BLACK);
		// Road
		if (type == GameBoard.STATION_ROAD) {
			Line2D line = new Line2D.Double((x0 + 1) * Game.GRID_UNIT_LENGTH, (y0 + 1)
					* Game.GRID_UNIT_LENGTH, (x + 1) * Game.GRID_UNIT_LENGTH, (y + 1)
					* Game.GRID_UNIT_LENGTH);
			g2.draw(line);
		}
		// Railway
		if (type == GameBoard.STATION_RAILWAY) {
			// adjustment for the single lien
			int adjustmentX = 0, adjustmentY = 0;
			if (x == x0) {
				adjustmentX = 2;
			} else if (y == y0) {
				adjustmentY = 2;
			} else { // Round railway
				adjustmentX = 1;
				adjustmentY = 1;
			}
			Line2D line = new Line2D.Double((x0 + 1) * Game.GRID_UNIT_LENGTH
					+ adjustmentX, (y0 + 1) * Game.GRID_UNIT_LENGTH + adjustmentY,
					(x + 1) * Game.GRID_UNIT_LENGTH + adjustmentX, (y + 1)
							* Game.GRID_UNIT_LENGTH + adjustmentY);
			g2.draw(line);
			line = new Line2D.Double((x0 + 1) * Game.GRID_UNIT_LENGTH - adjustmentX,
					(y0 + 1) * Game.GRID_UNIT_LENGTH - adjustmentY, (x + 1)
							* Game.GRID_UNIT_LENGTH - adjustmentX, (y + 1)
							* Game.GRID_UNIT_LENGTH - adjustmentY);
			g2.draw(line);

			if (adjustmentX == adjustmentY) { // Round railway
				line = new Line2D.Double((x0 + 1) * Game.GRID_UNIT_LENGTH + adjustmentX,
						(y0 + 1) * Game.GRID_UNIT_LENGTH - adjustmentY, (x + 1)
								* Game.GRID_UNIT_LENGTH + adjustmentX, (y + 1)
								* Game.GRID_UNIT_LENGTH - adjustmentY);
				g2.draw(line);
				line = new Line2D.Double((x0 + 1) * Game.GRID_UNIT_LENGTH - adjustmentX,
						(y0 + 1) * Game.GRID_UNIT_LENGTH + adjustmentY, (x + 1)
								* Game.GRID_UNIT_LENGTH - adjustmentX, (y + 1)
								* Game.GRID_UNIT_LENGTH + adjustmentY);
				g2.draw(line);
			}

			g2.setPaint(Color.GREEN);
			line = new Line2D.Double((x0 + 1) * Game.GRID_UNIT_LENGTH, (y0 + 1)
					* Game.GRID_UNIT_LENGTH, (x + 1) * Game.GRID_UNIT_LENGTH, (y + 1)
					* Game.GRID_UNIT_LENGTH);
			g2.draw(line);
		}

	}

	/**
	 * Draw path arrows
	 */
	private void drawArrows(Graphics2D g2) {
		int imageIndex = 0;
		int adjust = 8;
		for (int i = 0; i < arrowsList.size(); i++) {
			Movement mm = arrowsList.get(i);
			if (mm.starty() == mm.endy()) {
				 adjust = 8;
				if (mm.endx() > mm.startx()) {
					imageIndex = 0;
				} else {
					imageIndex = 4;
				}
			} else if (mm.startx() == mm.endx()) {
				adjust = 8;
				if (mm.endy() > mm.starty()) {
					imageIndex = 6;
				} else {
					imageIndex = 2;
				}
			} else if (mm.endx() > mm.startx() && mm.endy() < mm.starty()) {
				adjust = 12;
				imageIndex = 1;
			} else if (mm.endx() < mm.startx() && mm.endy() < mm.starty()) {
				adjust = 12;
				imageIndex = 3;
			} else if (mm.endx() < mm.startx() && mm.endy() > mm.starty()) {
				adjust = 12;
				imageIndex = 5;
			} else if (mm.endx() > mm.startx() && mm.endy() > mm.starty()) {
				adjust = 12;
				imageIndex = 7;
			}
			int x = (mm.endx() - mm.startx()) * GRID_UNIT_LENGTH / 2 + (mm.startx() + 1)
					* GRID_UNIT_LENGTH - adjust;
			int y = (mm.endy() - mm.starty()) * GRID_UNIT_LENGTH / 2 + (mm.starty() + 1)
					* GRID_UNIT_LENGTH - adjust;

			g2.drawImage(ARROWS_IMAGE[imageIndex], x, y, null);
		}
	}

	/**
	 * Exchange the location of p1, p2
	 */
	public void exchangeChessman(Piece piece1, Piece piece2) {
		int x = piece1.getX();
		int y = piece1.getY();
		piece1.setX(piece2.getX());
		piece1.setY(piece2.getY());
		piece2.setX(x);
		piece2.setY(y);
		// board
		gameBoard.getBoard()[piece1.getX()][piece1.getY()] = piece1.getType();
		gameBoard.getBoard()[x][y] = piece2.getType();
		// widget
		piece1.renderWidget();
		piece2.renderWidget();
	}

	/**
	 * Pieces list
	 */
	public ArrayList<Piece> getPieces() {
		return pieces;
	}

	public void setPieces(ArrayList<Piece> pieces) {
		this.pieces = pieces;
	}

	/**
	 * Get the piece
	 */
	public Piece getPiece(Coordinate c) {
		for (Piece piece : pieces) {
			if (piece.isVisible() && piece.getX() == c.x && piece.getY() == c.y) {
				return piece;
			}
		}
		return null;
	}

	/**
	 * Get the flag
	 */
	public Piece getPieceFlag(Located loc) {
		for (Piece piece : pieces) {
			if (loc == Located.NORTH) {
				if (piece.getType() == GameBoard.FLAG_N) {
					return piece;
				}
			} else if (loc == Located.SOUTH) {
				if (piece.getType() == GameBoard.FLAG_S) {
					return piece;
				}
			}
		}
		return null;
	}

	/**
	 * @return the arrowsList
	 */
	public Vector<Movement> getArrowsList() {
		return arrowsList;
	}

	/**
	 * @param arrowsList
	 *            the arrowsList to set
	 */
	public void setArrowsList(Vector<Movement> arrowsList) {
		this.arrowsList = arrowsList;
	}

	public String getBackgroundImageFile() {
		return backgroundImageFile;
	}

	public void setBackgroundImageFile(String backgroundImageFile) {
		this.backgroundImageFile = backgroundImageFile;
	}

	public CommandBox getCommandBox() {
		return commandBox;
	}

	public void setCommandBox(CommandBox commandBox) {
		this.commandBox = commandBox;
	}

	public boolean isDebugDrawBackgroundImage() {
		return debugDrawBackgroundImage;
	}

	public void setDebugDrawBackgroundImage(boolean debugDrawBackgroundImage) {
		this.debugDrawBackgroundImage = debugDrawBackgroundImage;
	}

	public boolean isDebugDrawBaseline() {
		return debugDrawBaseline;
	}

	public void setDebugDrawBaseline(boolean debugDrawBaseline) {
		this.debugDrawBaseline = debugDrawBaseline;
	}

	public Image getBackgroundImage() {
		return backgroundImage;
	}

	public void setBackgroundImage(Image backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	public SaveLineupRadioPane getSaveLineupRadioPane() {
		return saveLineupRadioPane;
	}

	public void setSaveLineupRadioPane(SaveLineupRadioPane saveLineupRadioPane) {
		this.saveLineupRadioPane = saveLineupRadioPane;
	}

	/*
	 * Set the operation buttons displayed on the screen or not
	 */
	public void setAllOpertionButtonsVisible(boolean flag) {
		for (int i = 0; i < operations.length; i++)
			operations[i].setVisible(flag);
	}

	public void setOneOpertionButtonVisible(Operations ops, boolean flag) {
		for (int i = 0; i < operations.length; i++) {
			if (operations[i].getOperation() == ops) {
				operations[i].setVisible(flag);
			}
		}
	}
}
