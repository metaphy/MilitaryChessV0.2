/**
 * @author Gou Ming Shi
 * http://metaphy.javaeye.com/
 * Sep 24, 2009
 * All Rights Reserved
 */
package com.javaeye.metaphy.action.command;

import java.util.ArrayList;

import com.javaeye.metaphy.action.util.GameSwingExecutor;
import com.javaeye.metaphy.game.Game;
import com.javaeye.metaphy.game.GameBoard;
import com.javaeye.metaphy.game.GamePanel;
import com.javaeye.metaphy.game.Game.GameStatus;
import com.javaeye.metaphy.model.BaseElement;
import com.javaeye.metaphy.model.Coordinate;
import com.javaeye.metaphy.model.Located;
import com.javaeye.metaphy.model.Piece;
import com.javaeye.metaphy.threads.Move;
import com.javaeye.metaphy.threads.RunnableSingleton;

public class CommandCenter {
	private Game game = Game.ME;
	private String command = null;
	private String outputBoxText = null;
	private StringBuffer result = null;
	private String[] allCommands = { "clear", "sound on/off", "draw baseline",
			"draw background", "load s/n", "show s/n", "hide s/n",
			"adjust coordinate coordinate", "move coordinate coordinate", "help",
			"about", "quit" };

	/**
	 * Input a command
	 */
	public void input(String command, String output) {
		this.command = command;
		this.outputBoxText = output;
		this.result = new StringBuffer(outputBoxText);
	}

	/**
	 * Execute the command
	 */
	public String execute() {
		if (command == null) {
			return "";
		}
		GameBoard gameBoard = game.getGameBoard();
		GamePanel gamePanel = game.getPanel();
		command = command.trim();
		String[] commandArray = command.split(" ");

		if (commandArray[0].equalsIgnoreCase("clear")) { // Clear the output box
			if (commandArray.length == 1) {
				// New the result buffer to clear the output box
				result = new StringBuffer();
				result.append(">Completed - ");
				result.append(command);
				result.append("\n");
			} else { // parameters error
				result.append(">Incorrect parameter(s) - ");
				result.append(command);
				result.append("\n");
			}
		} else if (commandArray[0].equalsIgnoreCase("load")) {
			if (game.getGameStatus() == GameStatus.BEFORE_GAME) {
				// Re-load South/North by the random linup file
				if (commandArray.length == 2) { // 1 parameter needed
					gameBoard.randomLineupFile();
					Located loc = null;
					if (commandArray[1].equalsIgnoreCase("s")) { // South
						loc = Located.SOUTH;
					} else if (commandArray[1].equalsIgnoreCase("n")) {
						loc = Located.NORTH;
					} else { // parameters error
						result.append(">Incorrect parameter(s) - ");
						result.append(command);
						result.append("\n");
						return result.toString();
					}
					gameBoard.autoGenerateBoard(loc); 
					ArrayList<Piece> pieces = gamePanel.getPieces();
					byte[][] board = gameBoard.getBoard();
					int index = 0;
					int startJ = 0, endJ = 0;
					if (loc == Located.NORTH) {
						endJ = 6;
					} else if (loc == Located.SOUTH) {
						// pieces 前一半是North，后一半（从25开始）是South pieces
						index = 25;
						startJ = 11;
						endJ = 17;
					}
					for (int j = startJ; j < endJ; j++) {
						for (int i = 0; i < GameBoard.BOARD_ARRAY_SIZE; i++) {
							// Re-load the pieces
							if (board[i][j] != 0x00) {
								Piece piece = pieces.get(index++);
								piece.setType(board[i][j]);
								piece.renderWidget();
							}
						}
					}
					result.append(">Completed - ");
					result.append(command);
					result.append("\n");
				} else { // parameters error
					result.append(">Incorrect parameter(s) - ");
					result.append(command);
					result.append("\n");
				}
			}
		} else if (commandArray[0].equalsIgnoreCase("adjust")) {
			if (commandArray.length == 3) { // 2 parameter needed
				int c1, c2;
				try {
					c1 = Integer.parseInt(commandArray[1]);
					c2 = Integer.parseInt(commandArray[2]);
				} catch (Exception e) {
					result.append(">Incorrect parameter(s) - ");
					result.append(command);
					result.append("\n");
					return result.toString();
				}
				Coordinate coordinate1 = new Coordinate(c1);
				Coordinate coordinate2 = new Coordinate(c2);
				Piece piece1 = gamePanel.getPiece(coordinate1);
				Piece piece2 = gamePanel.getPiece(coordinate2);
				if (piece1 != null && piece2 != null) {
					gamePanel.exchangeChessman(piece1, piece2);
					result.append(">Completed - ");
					result.append(command);
					result.append("\n");
				} else {
					result.append(">Incorrect parameter(s) - ");
					result.append(command);
					result.append("\n");
				}
			}
		} else if (commandArray[0].equalsIgnoreCase("show")
				|| commandArray[0].equalsIgnoreCase("hide")) {
			boolean displayed = commandArray[0].equalsIgnoreCase("show") ? true : false;
			if (commandArray.length == 2) { // 1 parameter needed
				Located loc = null;
				// Determine which player - s: south ; n : north
				if (commandArray[1].equalsIgnoreCase("s")) {
					loc = Located.SOUTH;
				} else if (commandArray[1].equalsIgnoreCase("n")) {
					loc = Located.NORTH;
				} else { // parameters error
					result.append(">Incorrect parameter(s) - ");
					result.append(command);
					result.append("\n");
				}
				if (loc != null) { // Process the command
					ArrayList<Piece> list = gamePanel.getPieces();
					for (Piece piece : list) {
						if (piece.getLocated() == loc) {
							piece.setShowCaption(displayed);
							piece.renderWidget();
						}
					}
					result.append(">Completed - ");
					result.append(command);
					result.append("\n");
				}
			} else { // parameters error
				result.append(">Incorrect parameter(s) - ");
				result.append(command);
				result.append("\n");
			}
		} else if (commandArray[0].equalsIgnoreCase("iseedeadpeople")) {
			// == "show n"
			command = "show n";
			execute();
		} else if (commandArray[0].equalsIgnoreCase("move")) {
			if (game.getGameStatus() == GameStatus.PLAYING && commandArray.length == 3) {
				int c1, c2;
				// Parameters validation begin
				try {
					c1 = Integer.parseInt(commandArray[1]);
					c2 = Integer.parseInt(commandArray[2]);
				} catch (Exception e) {
					result.append(">Incorrect parameter(s) - ");
					result.append(command);
					result.append("\n");
					return result.toString();
				}
				BaseElement b1 = gamePanel.getBaseElement(new Coordinate(c1));
				BaseElement b2 = gamePanel.getBaseElement(new Coordinate(c2));

				if (b1 == null || b2 == null || !(b1 instanceof Piece) 
						|| ((Piece) b1).getLocated()!= game.getTimer().getCurrentLocated()) {
					result.append(">Incorrect parameter(s) - ");
					result.append(command);
					result.append("\n");
					return result.toString();
				}
				
				// Parameters validation end
				Move moveRunnable = new Move();
				RunnableSingleton.instance().setMoveRunnable(moveRunnable);
				moveRunnable.setAppropriateMovable(b1);
				GameSwingExecutor.instance().execute(moveRunnable);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				moveRunnable.setAppropriateMovable(b2);
				GameSwingExecutor.instance().execute(moveRunnable);
				result.append(">Completed - ");
				result.append(command);
				result.append("\n");
			}
		} else if (commandArray[0].equalsIgnoreCase("help")
				|| commandArray[0].equals("?")) { // Display all commands
			if (commandArray.length == 1) {
				result.append(">All commands:\n");
				for (int i = 0; i < allCommands.length; i++) {
					result.append(allCommands[i]);
					result.append("\n");
				}
			} else { // parameters error
				result.append(">Incorrect parameter(s) - ");
				result.append(command);
				result.append("\n");
			}
		} else if (commandArray[0].equalsIgnoreCase("draw")) {
			// Draw background or baseline
			if (commandArray.length == 2) {
				if (commandArray[1].equalsIgnoreCase("background")) {
					game.getPanel().setDebugDrawBackgroundImage(
							!game.getPanel().isDebugDrawBackgroundImage());
				} else if (commandArray[1].equalsIgnoreCase("baseline")) {
					game.getPanel().setDebugDrawBaseline(
							!game.getPanel().isDebugDrawBaseline());
				} else {
					result.append(">Incorrect parameter(s) - ");
					result.append(command);
					result.append("\n");
					return result.toString();
				}
				game.getPanel().repaint();
				result.append(">Completed - ");
				result.append(command);
				result.append("\n");
			} else { // parameters error
				result.append(">Incorrect parameter(s) - ");
				result.append(command);
				result.append("\n");
			}
		} else if (commandArray[0].equalsIgnoreCase("sound")) { // sound on/off
			if (commandArray.length == 2) {
				if (commandArray[1].equalsIgnoreCase("on")) {
					game.setSoundOn(true);
				} else if (commandArray[1].equalsIgnoreCase("off")) {
					game.setSoundOn(false);
				} else {
					result.append(">Incorrect parameter(s) - ");
					result.append(command);
					result.append("\n");
					return result.toString();
				}
				result.append(">Completed - ");
				result.append(command);
				result.append("\n");
			} else { // parameters error
				result.append(">Incorrect parameter(s) - ");
				result.append(command);
				result.append("\n");
			}
		} else if (commandArray[0].equalsIgnoreCase("about")) { // About
			if (commandArray.length == 1) {
				result.append(">");
				result.append(game.getAbout());
				result.append("\n");
			} else { // parameters error
				result.append(">Incorrect parameter(s) - ");
				result.append(command);
				result.append("\n");
			}
		} else if (commandArray[0].equalsIgnoreCase("quit")) { // Quit
			if (commandArray.length == 1) {
				game.quit();
			} else { // parameters error
				result.append(">Incorrect parameter(s) - ");
				result.append(command);
				result.append("\n");
			}
		} else { // Command is not recognized as an internal or external
			// command, operable program or batch file.
			result.append(">Bad command - ");
			result.append(command);
			result.append("\n");
		}
		return result.toString();
	}

	public String output() {
		return result.toString();
	}
}
