/**
 * @author Gou Ming Shi
 * http://metaphy.javaeye.com/
 * Oct 14, 2009
 * All Rights Reserved
 */
package com.javaeye.metaphy.game;

import static com.javaeye.metaphy.game.Game.GRID_UNIT_LENGTH;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

import javax.swing.JComponent;

import com.javaeye.metaphy.action.util.GameSwingExecutor;
import com.javaeye.metaphy.ai.AISearchRunnable;
import com.javaeye.metaphy.game.OperationButton.Operations;
import com.javaeye.metaphy.model.Located;
import com.javaeye.metaphy.model.Piece;
import com.javaeye.metaphy.sound.SoundPlayerRunnable;
import com.javaeye.metaphy.threads.Move;
import com.javaeye.metaphy.threads.RunnableSingleton;

@SuppressWarnings("serial")
public class PlayingTimer extends JComponent implements Runnable {
	// 30s for one player to think and take actions
	public static final int PLAYER_THINKING_TIME = 60;
	// Timer sleep time
	public static final int TIMER_SLEEP_TIME = 50;
	// After 20s, playing the alert sound to alert the player
	private static final int ALERT_PLAYER_TIME = PLAYER_THINKING_TIME / 4 * 3;

	// Timer to keep running
	private volatile boolean keepRunning = false;
	// For painting
	private volatile int arcLen = 0;
	private volatile int counter = 0;
	// Current player (Located)
	private volatile Located currentLocated = Located.SOUTH;
//	private Logger logger = Logger.getLogger(PlayingTimer.class);

	public PlayingTimer() {
		super();
		this.setVisible(false);
		// Locate the Timer on the JPanel
		Rectangle rec = new Rectangle(GRID_UNIT_LENGTH * 13,
				GRID_UNIT_LENGTH * 2, GRID_UNIT_LENGTH * 2,
				GRID_UNIT_LENGTH * 2);
		this.setBounds(rec);
		this.setBackground(Color.black);
	}

	/**
	 * Run
	 */
	@Override
	public void run() {
		counter = 0;
		while (keepRunning) {
			counter++;
			double counterSecs = counter / 20.0;

			arcLen = ((int) counterSecs % PLAYER_THINKING_TIME) * 360
					/ PLAYER_THINKING_TIME;
			repaint();
			
			try {
				Thread.sleep(TIMER_SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// Change player when the time exceeds xx seconds
			if (counter / 20 >= PLAYER_THINKING_TIME) {
				changeCurrentPlayer();
			}

			// Play sound to alert the player to take action
			if (counter / 20 >= ALERT_PLAYER_TIME && counter % 20 == 0) {
				Game.EXEC.execute(new SoundPlayerRunnable("timer"));
			}
		}
//		logger.debug("Timer stopped! -- Current player = " + currentLocated
//				+ "! Counter = " + (counterForLogger++));
	}

	/**
	 * Timer start
	 */
	public void start() {
		keepRunning = true;
	}

	/**
	 * Timer stop
	 */
	public void stop() {
		keepRunning = false;
//		logger.debug("Timer stop(): I need to be stopped!!!");
	}

	/**
	 * Change current player
	 */
	public synchronized void changeCurrentPlayer() {
		// Initialize the variable
		counter = 0;
		// Stop chessman flickering after changing current player
		Move moveRunnable = RunnableSingleton.instance().getMoveRunnable();
		if (moveRunnable != null) {
			moveRunnable.setFlickerFlag(false);
			GameSwingExecutor.instance().execute(moveRunnable);
		}
		// Change current player
		currentLocated = (currentLocated == Located.SOUTH) ? Located.NORTH
				: Located.SOUTH;
		
		// AI begins to thinking
		if (currentLocated == Located.NORTH) {
			// Another thread
			Game.EXEC.execute(new AISearchRunnable());
			// Hide the button
			Game.ME.getPanel().setOneOpertionButtonVisible(Operations.PASS, false);
			Game.ME.getPanel().setOneOpertionButtonVisible(Operations.GIVE_UP, false);
		} else {
			Game.ME.getPanel().setOneOpertionButtonVisible(Operations.PASS, true);
			Game.ME.getPanel().setOneOpertionButtonVisible(Operations.GIVE_UP, true);
		}
	}

	public Located getCurrentLocated() {
		return currentLocated;
	}
	
	public void setCurrentLocated(Located currentPlayer) {
		this.currentLocated = currentPlayer;
	}
	
	/**
	 * Whether or not the thread should keep running
	 */
	public boolean isKeepRunning() {
		return keepRunning;
	}

	/**
	 * Paint
	 */
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		// Anti-aliasing
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		// Draw background, in color - Green for South / Orange for North
		Color color = currentLocated == Located.SOUTH ? Piece.COLOR_SOUTH
				: Piece.COLOR_NORTH;
		g2.setColor(color);
		Ellipse2D circle = new Ellipse2D.Float(0, 0, GRID_UNIT_LENGTH * 2 - 1,
				GRID_UNIT_LENGTH * 2 - 1);
		g2.fill(circle);

		// Draw the used portion
		g2.setColor(Color.WHITE);
		Arc2D arc = new Arc2D.Float(0, 0, GRID_UNIT_LENGTH * 2 - 1,
				GRID_UNIT_LENGTH * 2 - 1, 90, -arcLen, Arc2D.PIE);
		g2.fill(arc);

		// Draw the number - The time left in seconds
		g2.setColor(Color.black);

		int timeLeft = PLAYER_THINKING_TIME - counter / 20;
		int drawingX = 0, drawingY = 0;
		if (timeLeft < 10) {
			drawingX = GRID_UNIT_LENGTH - GRID_UNIT_LENGTH / 6 + 1;
			drawingY = GRID_UNIT_LENGTH + GRID_UNIT_LENGTH / 6;
		} else {
			drawingX = GRID_UNIT_LENGTH - GRID_UNIT_LENGTH / 3 + 2;
			drawingY = GRID_UNIT_LENGTH + GRID_UNIT_LENGTH / 6;
		}
		g2.drawString(String.valueOf(timeLeft), drawingX, drawingY);
		
		// Set stroke
		g2.setStroke(new BasicStroke(1.3F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		g2.setColor(Color.GRAY);
		
		// Draw the border of the timer
		g2.draw(circle);
	}

	/**
	 * @return the counter
	 */
	public int getCounter() {
		return counter;
	}

	/**
	 * @param counter the counter to set
	 */
	public void setCounter(int counter) {
		this.counter = counter;
	}
}


