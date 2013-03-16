/**
 * @author Gou Ming Shi
 * http://metaphy.javaeye.com/
 * Oct 11, 2009
 * All Rights Reserved
 */
package com.javaeye.metaphy.threads;

import java.util.Vector;

import com.javaeye.metaphy.ai.Movement;
import com.javaeye.metaphy.game.Game;
import com.javaeye.metaphy.game.GameBoard;
import com.javaeye.metaphy.game.GamePanel;
import com.javaeye.metaphy.game.PlayingTimer;
import com.javaeye.metaphy.game.Game.GameStatus;
import com.javaeye.metaphy.model.BaseElement;
import com.javaeye.metaphy.model.Coordinate;
import com.javaeye.metaphy.model.Located;
import com.javaeye.metaphy.model.MoveAttack;
import com.javaeye.metaphy.model.Piece;
import com.javaeye.metaphy.model.SoldierStation;
import com.javaeye.metaphy.sound.SoundPlayerRunnable;

public class Move implements Runnable {
	// When flickering, the "showing" time and the "hiding" time
	private static final int ON_SHOW_TIME = 300;
	private static final int ON_HIDE_TIME = 150;
	private static final int MOVE_STANDSTILL_TIME = 60;
	private static final int THREAD_SLEEP_TIME_FOR_TIMER_STOP = 60;
	/* Game */
	private Game game = Game.ME;
	/* Game board */
	private GameBoard gameBoard = game.getGameBoard();
	/* Panel */
	private GamePanel gamePanel = game.getPanel();
	/* To avoid running twice */
	private static boolean moveOnce = false;
	/* the first and second element */
	private Piece first = null;
	private BaseElement second = null;
	/* flicking flag */
	private volatile boolean flickerFlag = true;
	/* To set the chessman visible or not */
	private volatile boolean visible = false;
	/*
	 * Assure that after flickering the first element is visible. But it should
	 * run once only
	 */
	private boolean afterFlickingRunOnce = false;
	/* Path finding */
	private volatile Vector<Coordinate> path = null;
	/* Move and attack result */
	private MoveAttack moveAndAttackResult = null;
	/* The timer */
	private PlayingTimer timer = Game.ME.getTimer();

	public Move() {
		super();
	}

	/**
	 * Set appropriate first or second BaseElement
	 */
	public synchronized void setAppropriateMovable(BaseElement element) {
		path = null;
		moveAndAttackResult = null;
		if (first == null) { // Set the first
			// Only operate on SOUTH chessman
			if (element instanceof Piece
					&& ((Piece) element).getLocated() == timer.getCurrentLocated()) {
				setFirst((Piece) element);
				setSecond(null);
				setFlickerFlag(true);
				// Play the sound
				Game.EXEC.execute(new SoundPlayerRunnable("pick"));
			}
		} else if (second == null) { // Set the second
			if (element instanceof Piece
					&& ((Piece) element).getLocated() != first.getLocated()
					|| element instanceof SoldierStation) {
				// Move and attack (if it was)
				setSecond(element);
				setFlickerFlag(false);
				path = gameBoard.pathFinding(first.getX(), first.getY(), second.getX(),
						second.getY());
				if (path != null && path.size() > 0) {
					moveOnce = false;
					moveAndAttackResult = gameBoard.moveAttack(first.getX(),
							first.getY(), second.getX(), second.getY());
				} else {
					moveOnce = true;
				}
			} else if (element instanceof Piece
					&& ((Piece) element).getLocated() == first.getLocated()) {
				// Re-select the first chessman
				Piece tmp = (Piece) element;
				// Stop the first "flicking"
				setFlickerFlag(false);
				setVisible(true);
				first.setVisible(true);
				first.renderWidget();
				setFirst(tmp);
				setFlickerFlag(true);
				// Play the sound
				Game.EXEC.execute(new SoundPlayerRunnable("pick"));
			}
		}
	}

	/*
	 * Run method
	 */
	@Override
	public void run() {
		try {
			if (first != null) {
				// Piece flickering
				while (isFlickerFlag()) {
					first.setVisible(visible);
					first.renderWidget();
					try {
						int sleepTime = visible ? ON_SHOW_TIME : ON_HIDE_TIME;
						Thread.sleep(sleepTime);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					setVisible(!visible);
				}

				// To assure that after flickering, the first one is visible
				if (!afterFlickingRunOnce) {
					first.setVisible(true);
					first.renderWidget();
					afterFlickingRunOnce = true;
				}

				// Piece moving only once when there is a path
				if (path != null && path.size() > 0 && !moveOnce) {
					moveOnce = true;

					// Need to check the currentPlayer again to fix Bug0001
					if (first.getLocated() == timer.getCurrentLocated()) {
						// Stop the timer to allow chessman moving
						timer.stop();
						/*
						 * timer.stop() sometimes doesn't work; that's because
						 * just after timer.stop(), the Timer entered
						 * "Thread.sleep(normalSleepTime)". Then when Timer
						 * wakes up, the timer.start() was invoked. So let this
						 * thread sleep for a little time to make the
						 * timer.stop() work properly.
						 */
						Thread.sleep(THREAD_SLEEP_TIME_FOR_TIMER_STOP);

						// Move and attack
						GameStatus gameStatus = moveAndAttack();

						if (gameStatus == GameStatus.PLAYING) {
							// Check to determine whether the game is over
							int checkgo = gameBoard.checkGameOver();
							if (checkgo == 0) { // Game is not over
								/*
								 * Change player's turn after moving, but we
								 * need to check whether it's changed by the
								 * timer ---- To fix Bug0001
								 */
								if (first.getLocated() == timer.getCurrentLocated()) {
									timer.changeCurrentPlayer();
								}
								// Re-start and re-enable the timer
								timer.start();
								Game.EXEC.execute(timer);
							} else if (checkgo == 1) {
								game.roundOver(Located.NORTH);
							} else if (checkgo == 2) {
								game.roundOver(Located.SOUTH);
							} else {
								game.roundOver(null);
							}
						} else if (gameStatus == GameStatus.END_GAME) {
							// GAME - OVER
							game.roundOver(first.getLocated());
						}
					}
				}
				RunnableSingleton.instance().setMoveRunnable(null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Determine whether the first attacks the second and moving the first
	 */
	private synchronized GameStatus moveAndAttack() {
		try {
			boolean attack = false;
			// Determine whether the first attacks the second
			if (first instanceof Piece && second instanceof Piece) {
				attack = true;
			}
			// Clear the arrows image
			gamePanel.getArrowsList().clear();
			gamePanel.repaint();

			// Moving the first chessman
			for (int i = 0; i < path.size(); i++) {
				Coordinate c = path.get(i);
				// Not attack, or attack but the Moving steps
				if (!attack || attack && (i < path.size() - 1)) {
					Movement mm = new Movement(first.getX(),first.getY(),c.x, c.y);
					gamePanel.getArrowsList().add(mm);
					first.setX(c.x);
					first.setY(c.y);
					first.renderWidget();
					gamePanel.repaint();
					// Play the "moving" sound
					Game.EXEC.execute(new SoundPlayerRunnable("move"));
					Thread.sleep(MOVE_STANDSTILL_TIME);
				} else { // Attack, and the last step
					Piece secondPiece = (Piece) second;
					Movement mm = new Movement(first.getX(),first.getY(),secondPiece.getX(), secondPiece.getY());
					gamePanel.getArrowsList().add(mm);
					
					first.setX(c.x);
					first.setY(c.y);
					first.renderWidget();
					gamePanel.repaint();
					if (moveAndAttackResult == MoveAttack.KILL) {
						secondPiece.setVisible(false);
						secondPiece.renderWidget();
						// Playing the sound
						Game.EXEC.execute(new SoundPlayerRunnable("kill"));
					} else if (moveAndAttackResult == MoveAttack.KILLED) {
						first.setVisible(false);
						first.renderWidget();
						if (first.getType() == GameBoard.SILING_N // SiLing dead
								|| first.getType() == GameBoard.SILING_S) {
							Piece flag = null;
							if (first.getType() == GameBoard.SILING_N) {
								flag = gamePanel.getPieceFlag(Located.NORTH);
							} else if (first.getType() == GameBoard.SILING_S) {
								flag = gamePanel.getPieceFlag(Located.SOUTH);
							}
							if (flag != null) {
								flag.setShowCaption(true);
								flag.renderWidget();
								Game.EXEC.execute(new SoundPlayerRunnable("showflag"));
							}
						} else {
							Game.EXEC.execute(new SoundPlayerRunnable("killed"));
						}
					} else if (moveAndAttackResult == MoveAttack.EQUAL) {
						first.setVisible(false);
						first.renderWidget();
						secondPiece.setVisible(false);
						secondPiece.renderWidget();

						if (first.getType() == GameBoard.SILING_N // SiLing dead
								|| first.getType() == GameBoard.SILING_S
								|| secondPiece.getType() == GameBoard.SILING_N
								|| secondPiece.getType() == GameBoard.SILING_S) {
							Piece flag = null;
							if (first.getType() == GameBoard.SILING_N
									|| secondPiece.getType() == GameBoard.SILING_N) {
								flag = gamePanel.getPieceFlag(Located.NORTH);
								flag.setShowCaption(true);
								flag.renderWidget();
							}
							if (first.getType() == GameBoard.SILING_S
									|| secondPiece.getType() == GameBoard.SILING_S) {
								flag = gamePanel.getPieceFlag(Located.SOUTH);
								flag.setShowCaption(true);
								flag.renderWidget();
							}
							Game.EXEC.execute(new SoundPlayerRunnable("showflag"));
						} else {
							Game.EXEC.execute(new SoundPlayerRunnable("equal"));
						}
					} else if (moveAndAttackResult == MoveAttack.GAME_OVER) {
						secondPiece.setVisible(false);
						secondPiece.renderWidget();
						// first is Bomb
						if (first.getType() == GameBoard.BOMB_N
								|| first.getType() == GameBoard.BOMB_S) {
							first.setVisible(false);
							first.renderWidget();
						}
						// Game status: END_GAME
						return GameStatus.END_GAME;
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return GameStatus.PLAYING;
	}

	/*
	 * Need to clear the cached Runnable
	 */
	public boolean needClearSingleton() {
		return second != null;
	}

	public Piece getFirst() {
		return first;
	}

	public void setFirst(Piece first) {
		this.first = first;
	}

	public BaseElement getSecond() {
		return second;
	}

	public void setSecond(BaseElement second) {
		this.second = second;
	}

	public boolean isFlickerFlag() {
		return flickerFlag;
	}

	public void setFlickerFlag(boolean flickerFlag) {
		this.flickerFlag = flickerFlag;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
}
