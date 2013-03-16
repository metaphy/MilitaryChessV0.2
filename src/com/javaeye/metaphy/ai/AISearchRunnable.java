/**
 * @author Gou Ming Shi
 * http://metaphy.javaeye.com/
 * Dec 20, 2009
 * All Rights Reserved
 */
package com.javaeye.metaphy.ai;

import java.util.Collections;
import java.util.Random;
import java.util.Vector;

import com.javaeye.metaphy.action.util.GameSwingExecutor;
import com.javaeye.metaphy.game.PlayingTimer;
import com.javaeye.metaphy.model.BaseElement;
import com.javaeye.metaphy.model.Located;
import com.javaeye.metaphy.threads.Move;
import com.javaeye.metaphy.threads.RunnableSingleton;

public class AISearchRunnable extends GameSearch implements Runnable {
	PlayingTimer pt = game.getTimer();

	/**
	 * AI search and moving and attacking
	 */
	@Override
	public void run() {
		Random r = new Random();
		int rint = r.nextInt(12);
		int depth = 0;
		if (rint <= 1) {
			depth = 0;
		} else if (rint <= 7) {
			depth = 1;
		} else if (rint <= 10) {
			depth = 2;
		} else if (rint <= 11) {
			depth = 3;
		}

		// gameBoard.print();
		long m = System.currentTimeMillis();
		byte[][] boardCopy = gameBoard.getCopyOfBoard();
		Vector<Movement> moves = possibleMoves(true);
		int[] values = new int[moves.size()];

		for (int i = 0; i < moves.size(); i++) {
			Movement mm = moves.get(i);
			values[i] = Integer.MAX_VALUE;
			gameBoard.moveAttack(mm.startx(), mm.starty(), mm.endx(), mm.endy());
			mm.setValue(evaluation(true));
			gameBoard.recoverBoard(boardCopy);
		}
		// 给Possible Move按value从大到小排序
		Collections.sort(moves);

		for (int i = 0; i < moves.size(); i++) {
//			System.out.println(moves.get(i));
			gameBoard.moveAttack(moves.get(i).startx(), moves.get(i).starty(), moves.get(
					i).endx(), moves.get(i).endy());
			values[i] = alphaBeta(depth, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
			gameBoard.recoverBoard(boardCopy);

			// Check the search time
			if (PlayingTimer.PLAYER_THINKING_TIME - pt.getCounter()
					* PlayingTimer.TIMER_SLEEP_TIME / 1000 < 15) {
				break;
			}
		}

		// Check current player
		if (pt.getCurrentLocated() != Located.NORTH) {
			return;
		}

		int index = 0;
		int value = Integer.MAX_VALUE;
		for (int i = 0; i < values.length; i++) {
			if (values[i] < value) {
				index = i; // 电脑选择对Human来说估值最小的move
				value = values[i];
			}
		}
		long n = System.currentTimeMillis();
		System.out.println("Searchiing time = " + (n - m) + " ms");
		System.out.println("Depth = " + depth);
		// After thinking, AI gets the better way and tries to move and attack
		BaseElement b1 = gamePanel.getBaseElement(moves.get(index).getStart());
		BaseElement b2 = gamePanel.getBaseElement(moves.get(index).getEnd());
		Move moveRunnable = new Move();
		moveRunnable.setAppropriateMovable(b1);
		RunnableSingleton.instance().setMoveRunnable(moveRunnable);
		GameSwingExecutor.instance().execute(moveRunnable);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		moveRunnable.setAppropriateMovable(b2);
		GameSwingExecutor.instance().execute(moveRunnable);
	}
}
