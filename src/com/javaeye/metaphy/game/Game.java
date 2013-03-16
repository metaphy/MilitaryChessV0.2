/**
 * @author Gou Ming Shi
 * http://metaphy.javaeye.com/
 * Sep 22, 2009
 * All Rights Reserved
 */
package com.javaeye.metaphy.game;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JRootPane;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.javaeye.metaphy.game.OperationButton.Operations;
import com.javaeye.metaphy.model.Located;
import com.javaeye.metaphy.model.Piece;
import com.javaeye.metaphy.sound.SoundPlayerRunnable;

public class Game {
	/* The width/height of the screen */
	public static final int SCREEN_WIDTH;
	public static final int SCREEN_HEIGHT;
	/* The width/height of the Game window */
	public static final int FRAME_WIDTH;
	public static final int FRAME_HEIGHT;

	/* Game Status */
	public enum GameStatus {
		BEFORE_GAME, // Before the game
		PLAYING, // Player is playing the game
		END_GAME, // Game over
		REVIEW
		// Review the game
	}
	/*
	 * 单位长度，用像素表示。修改这个值的大小，可以修改整个棋盘以及棋子的大小。 一般来说，这个值应该可配置，以便用户调整窗口大小。
	 * 在我开发机器上(1280×800)这个值 一般使用30~38
	 */
	public static final int GRID_UNIT_LENGTH = 38;
	/* 棋盘方格数 */
	public static final int BOARD_GRID_SIZE = 16;
	/* Swing汉字粗体可真难看,Plain稍微好一点 */
	public static final Font GAME_FONT;
	/* Font of the timer */
	public static final Font TIMER_NUMBER_FONT;
	/* Thread pool */
	public static final ExecutorService EXEC = Executors.newCachedThreadPool();
	/* Extended name of the lineup file */
	public static final String LINEUP_FILE_EXT = ".jql";
	/* ME == this; this ME static instance will be used in all actions */
	public static Game ME;

	/* Playing sound or not */
	private boolean soundOn = true;
	/* Game frame */
	private JFrame frame = null;
	/* Game main panel */
	private GamePanel panel = null;
	/* The board */
	private GameBoard gameBoard = null;
	/* The "About" information */
	private StringBuffer about = new StringBuffer();
	/* The game title and version */
	private String gameTitle = "军棋游戏 V0.2.6";
	/* The icon of the game */
	public static Image icon = null;
	/* The status of the game */
	private GameStatus status;
	/* Playing timer */
	private PlayingTimer timer = new PlayingTimer();

	/**
	 * Static block to initialize some constants
	 */
	static {
		Toolkit tool = Toolkit.getDefaultToolkit();
		Dimension screen = tool.getScreenSize();
		SCREEN_WIDTH = screen.width;
		SCREEN_HEIGHT = screen.height;
		FRAME_WIDTH = GRID_UNIT_LENGTH * (BOARD_GRID_SIZE + 2) + 10;
		FRAME_HEIGHT = GRID_UNIT_LENGTH * (BOARD_GRID_SIZE + 2) + 35;

		if (GRID_UNIT_LENGTH >= 40) {
			GAME_FONT = new Font("宋体", Font.PLAIN, 14);
			TIMER_NUMBER_FONT = new Font("Serif", Font.PLAIN, 22);
		} else if (GRID_UNIT_LENGTH < 40 && GRID_UNIT_LENGTH >= 36) {
			GAME_FONT = new Font("宋体", Font.PLAIN, 13);
			TIMER_NUMBER_FONT = new Font("Serif", Font.PLAIN, 20);
		} else if (GRID_UNIT_LENGTH < 36 && GRID_UNIT_LENGTH >= 30) {
			GAME_FONT = new Font("宋体", Font.PLAIN, 11);
			TIMER_NUMBER_FONT = new Font("Serif", Font.PLAIN, 16);
		} else if (GRID_UNIT_LENGTH < 30 && GRID_UNIT_LENGTH >= 26) {
			GAME_FONT = new Font("宋体", Font.PLAIN, 9);
			TIMER_NUMBER_FONT = new Font("Serif", Font.PLAIN, 14);
		} else if (GRID_UNIT_LENGTH < 26 && GRID_UNIT_LENGTH >= 22) {
			GAME_FONT = new Font("宋体", Font.PLAIN, 7);
			TIMER_NUMBER_FONT = new Font("Serif", Font.PLAIN, 12);
		} else {
			GAME_FONT = new Font("宋体", Font.PLAIN, 5);
			TIMER_NUMBER_FONT = new Font("Serif", Font.PLAIN, 10);
		}
	}

	/**
	 * Constructor of the Game
	 */
	public Game() {
		try {
			// -------------------------------------------------------
			// Keep this as the first line of Game() constructor!
			ME = this;
			// -------------------------------------------------------

			gameBoard = new GameBoard();// Initialize the game board

			frame = new JFrame(); // Initialize JFrame and JPanel
			panel = new GamePanel();
			frame.add(panel);

			setUIFont(GAME_FONT); // Set the font of the Frame title
			frame.setTitle(gameTitle);
			// Set the icon
			URLClassLoader urlLoader = (URLClassLoader) (Game.class.getClassLoader());
			URL url = urlLoader.findResource("res/images/icon.png");
			try {
				if (url != null) {
					icon = ImageIO.read(url);
					if (icon != null)
						frame.setIconImage(icon);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			frame.setUndecorated(true); // Set Window Decoration Style
			frame.getRootPane().setWindowDecorationStyle(JRootPane.FRAME);

			frame.setLocation((SCREEN_WIDTH - FRAME_WIDTH) / 2,
					(SCREEN_HEIGHT - FRAME_HEIGHT) / 2); // Set JFrame size and
			// location
			frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setResizable(false);
			frame.setVisible(true);
			
			about(); // The "About" information
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Set UI font
	 */
	private void setUIFont(Font f) {
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get(key);
			if (value instanceof FontUIResource) {
				UIManager.put(key, f);
			}
		}
	}

	/**
	 * Game Over
	 */
	public void roundOver(Located winner) {
		// Clear the arrows image
		panel.getArrowsList().clear();
		panel.repaint();
		
		EXEC.execute(new SoundPlayerRunnable("game_end"));
		setGameStatus(GameStatus.END_GAME);

		String message = null;
		if (winner == null) {
			message = "平局！不要跑，再来一盘！";
		} else if (winner == Located.SOUTH) {
			message = "恭喜你赢了！";
		} else if (winner == Located.NORTH) {
			message = "你输了！";
		}

		// Show a dialog to indicate who's the Winner!
		JOptionPane.showConfirmDialog(getPanel(), message + "本次游戏结束，是否需要保存复盘？", "游戏结束",
				JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);

		// Save the game if the answer is "YES"

		// GAME - RESTART
		setGameStatus(GameStatus.BEFORE_GAME);
		timer.setVisible(false);
		timer.setCurrentLocated(Located.SOUTH);

		panel.setAllOpertionButtonsVisible(true);
		panel.setOneOpertionButtonVisible(Operations.PASS, false);
		panel.setOneOpertionButtonVisible(Operations.GIVE_UP, false);

		// Re-load all pieces
		gameBoard.initBoard();
		// gameBoard.loadPieces(Located.SOUTH);
		gameBoard.autoGenerateBoard(Located.SOUTH);
		gameBoard.autoGenerateBoard(Located.NORTH);
		int index = 0;
		for (int j = 0; j < GameBoard.BOARD_ARRAY_SIZE; j++) {
			for (int i = 0; i < GameBoard.BOARD_ARRAY_SIZE; i++) {
				// Re-load the pieces
				if (gameBoard.getBoard()[i][j] != 0x00) {
					Piece piece = panel.getPieces().get(index++);
					if (index <= 25) {// North
						piece.setShowCaption(false);
					}
					piece.setVisible(true);
					piece.setType(gameBoard.getBoard()[i][j]);
					piece.setX(i);
					piece.setY(j);
					piece.renderWidget();
				}
			}
		}
	}

	/**
	 * The about information
	 */
	private void about() {
		about.append("作者:苟明诗\n");
		about.append("版本:");
		about.append(gameTitle);
		about.append("\n");
		about.append("欢迎访问http://metaphy.javaeye.com/");
	}

	/**
	 * Quit the game
	 */
	public void quit() {
		System.exit(0);
	}

	/**
	 * Get the game JFrame
	 */
	public JFrame getContainer() {
		return frame;
	}

	/**
	 * Get the game panel, which is the key element for the game views and
	 * actions
	 * 
	 * @return
	 */
	public GamePanel getPanel() {
		return panel;
	}

	public String getAbout() {
		return about.toString();
	}

	public String getGameTitle() {
		return gameTitle;
	}

	public void setGameTitle(String gameTitle) {
		this.gameTitle = gameTitle;
	}

	public GameStatus getGameStatus() {
		return status;
	}

	public void setGameStatus(GameStatus gameStatus) {
		this.status = gameStatus;
	}

	public PlayingTimer getTimer() {
		return timer;
	}

	public void setTimer(PlayingTimer timer) {
		this.timer = timer;
	}

	public static Image getIcon() {
		return icon;
	}

	public static void setIcon(Image icon) {
		Game.icon = icon;
	}

	public boolean isSoundOn() {
		return soundOn;
	}

	public void setSoundOn(boolean soundOn) {
		this.soundOn = soundOn;
	}

	public GameBoard getGameBoard() {
		return gameBoard;
	}

	public void setGameBoard(GameBoard gameBoard) {
		this.gameBoard = gameBoard;
	}

	/**
	 * Main entry of the game
	 */
	public static void main(String[] args) {
		// Set the font for all buttons, textfields, textarea
		UIManager.put("Button.font", GAME_FONT);
		UIManager.put("TextField.font", GAME_FONT);
		UIManager.put("TextArea.font", GAME_FONT);
		UIManager.put("Border.font", GAME_FONT);
		UIManager.put("RadioButton.font", GAME_FONT);
		// New a game
		Game game = new Game();
		game.setGameStatus(GameStatus.BEFORE_GAME);
	}
}
