/**
 * Author by metaphy
 * Sep 26, 2009
 * All Rights Reserved.
 */
package com.javaeye.metaphy.action.operation;

import java.awt.event.ActionEvent;

import com.javaeye.metaphy.action.BaseAction;
import com.javaeye.metaphy.action.util.GameSwingExecutor;
import com.javaeye.metaphy.game.Game;
import com.javaeye.metaphy.game.GamePanel;
import com.javaeye.metaphy.game.PlayingTimer;
import com.javaeye.metaphy.game.Game.GameStatus;
import com.javaeye.metaphy.game.OperationButton.Operations;
import com.javaeye.metaphy.sound.SoundPlayerRunnable;
import com.javaeye.metaphy.threads.Adjust;

public class OperationStartGame extends BaseAction {

	public void actionPerformed(ActionEvent e) {
		// Validate the game status firstly
		if (game.getGameStatus() == GameStatus.BEFORE_GAME) {
			GamePanel panel = game.getPanel();
			panel.setAllOpertionButtonsVisible(false);
			panel.setOneOpertionButtonVisible(Operations.PASS, true);
			panel.setOneOpertionButtonVisible(Operations.GIVE_UP, true);

			game.setGameStatus(GameStatus.PLAYING);

			// When the game "starts", the ChessmanAdjust Runnable should stop
			Adjust adjustRunnable = runnableSingle.getAdjustRunnable();
			if (adjustRunnable != null) {
				adjustRunnable.setFlickerFlag(false);
				// new Thread.run (runnable)
				GameSwingExecutor.instance().execute(adjustRunnable);
			}

			// Play the "Start Game" sound
			Game.EXEC.execute(new SoundPlayerRunnable("start"));

			// Show the playing timer
			PlayingTimer timer = game.getTimer();
			timer.start();
			timer.setVisible(true);

			// Run the timer
			Game.EXEC.execute(timer);

			// Test - Print the board
//			game.getGameBoard().print();
		}
	}
}
