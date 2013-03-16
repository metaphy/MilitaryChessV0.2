/**
 * @author Gou Ming Shi
 * http://metaphy.javaeye.com/
 * Sep 22, 2009
 * All Rights Reserved
 */
package com.javaeye.metaphy.game;

import static com.javaeye.metaphy.game.Game.GRID_UNIT_LENGTH;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import com.javaeye.metaphy.action.operation.OperationGiveUp;
import com.javaeye.metaphy.action.operation.OperationOpenLineupFile;
import com.javaeye.metaphy.action.operation.OperationPass;
import com.javaeye.metaphy.action.operation.OperationSaveLineupFile;
import com.javaeye.metaphy.action.operation.OperationStartGame;

public class OperationButton extends JButton {
	public enum Operations {
		START_GAME, CALLIN_LINEUP, SAVE_LINEUP, CALLIN_REPEAT, PASS, GIVE_UP
	}
	public static final int BUTTON_START_X = GRID_UNIT_LENGTH * 13;
	public static final int BUTTON_START_Y = GRID_UNIT_LENGTH * 13;
	public static final int BUTTON_GAP_Y = GRID_UNIT_LENGTH / 4;
	public static final int BUTTON_WIDTH = GRID_UNIT_LENGTH * 2;
	public static final int BUTTON_HEIGHT = GRID_UNIT_LENGTH * 7 / 10;

	private static final long serialVersionUID = 4528132025624013030L;

	// indicates which is the operation
	private Operations operation;

	public OperationButton() {
		super();
		setOpaque(true);
		setAutoscrolls(false);
		Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED,
				Color.white, Color.white, Color.gray, Color.gray);
		border = BorderFactory.createLineBorder(Color.gray);

		setBorder(border);
		setFocusable(false);
	}

	public OperationButton(Operations operation) {
		this();
		this.operation = operation;
		setText(getCaption());
		setBounds(getBound());

		addActions();
	}

	/*
	 * Add action listeners on the buttons
	 */
	private void addActions() {
		switch (operation) {
		case START_GAME:
			addActionListener(new OperationStartGame());
			break;
		case CALLIN_LINEUP:
			addActionListener(new OperationOpenLineupFile());
			break;
		case SAVE_LINEUP:
			addActionListener(new OperationSaveLineupFile());
			break;
		case CALLIN_REPEAT:
			break;
		case PASS:
			addActionListener(new OperationPass());
			break;
		case GIVE_UP:
			addActionListener(new OperationGiveUp());
			break;
		}
	}
	/**
	 * Get the caption of the button
	 * 
	 * @return
	 */
	private String getCaption() {
		String caption = "";
		switch (operation) {
		case START_GAME:
			caption = "开始游戏";
			break;
		case CALLIN_LINEUP:
			caption = "调入布局";
			break;
		case SAVE_LINEUP:
			caption = "保存布局";
			break;
		case CALLIN_REPEAT:
			caption = "调入复盘*";
			break;
		case PASS:
			caption = "跳  过";
			break;
		case GIVE_UP:
			caption = "投  降";
			break;

		}
		return caption;
	}

	/*
	 * the location of the button
	 */
	private Rectangle getBound() {
		Rectangle rec = null;
		switch (operation) {
		case START_GAME:
		case PASS:	// The both buttons are placed in the same location
			rec = new Rectangle(BUTTON_START_X, BUTTON_START_Y, BUTTON_WIDTH,
					BUTTON_HEIGHT);
			break;
		case CALLIN_LINEUP:
			rec = new Rectangle(BUTTON_START_X, BUTTON_START_Y + BUTTON_HEIGHT
					+ BUTTON_GAP_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
			break;
		case SAVE_LINEUP:
			rec = new Rectangle(BUTTON_START_X, BUTTON_START_Y
					+ (BUTTON_HEIGHT + BUTTON_GAP_Y) * 2, BUTTON_WIDTH,
					BUTTON_HEIGHT);
			break;
		case CALLIN_REPEAT:
		case GIVE_UP:
			rec = new Rectangle(BUTTON_START_X, BUTTON_START_Y
					+ (BUTTON_HEIGHT + BUTTON_GAP_Y) * 3, BUTTON_WIDTH,
					BUTTON_HEIGHT);
			break;

		}
		return rec;
	}

	public Operations getOperation() {
		return operation;
	}

	public void setOperation(Operations operation) {
		this.operation = operation;
	}

}
