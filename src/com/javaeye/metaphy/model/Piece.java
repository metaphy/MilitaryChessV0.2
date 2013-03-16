/**
 * @author Gou Ming Shi
 * http://metaphy.javaeye.com/
 * Dec 3, 2009
 * All Rights Reserved
 */
package com.javaeye.metaphy.model;

import static com.javaeye.metaphy.game.Game.GRID_UNIT_LENGTH;

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import com.javaeye.metaphy.action.PieceAction;
import com.javaeye.metaphy.game.GameBoard;

public class Piece extends BaseElement {
	/* The side width of the Piece of chess */
	public static final int PIECE_SIDE_WIDTH = GRID_UNIT_LENGTH * 33 / 38;
	/* Colors */
	public static final Color COLOR_SOUTH = Color.orange;
	public static final Color COLOR_NORTH = Color.green;

	/* Show the caption or not */
	private boolean showCaption = true;
	/* visible on the board or not */
	private boolean visible = true;
	private Located located = null;
	/* The widget of the model */
	private JButton widget = null;

	public Piece() {}
	
	public Piece(int x, int y, byte type) {
		super(x, y, type);
		if (type > 0x10) {
			this.located = Located.NORTH;
		} else {
			this.located = Located.SOUTH;
		}
		// Init the widget
		widget = new JButton();
		widget.setAutoscrolls(false);
		widget.setOpaque(true);
		Border border = BorderFactory.createBevelBorder(BevelBorder.RAISED,
				Color.white, Color.white, Color.gray, Color.gray);
		widget.setBorder(border);
		widget.setFocusable(false);

		// Display the color of the piece
		switch (located) {
		case SOUTH:
			widget.setBackground(COLOR_SOUTH);
			break;
		case NORTH:
			widget.setBackground(COLOR_NORTH);
			break;
		}
	}

	/**
	 * Render the piece widget
	 */
	public void renderWidget() {
		// Set the widget to be visible or not
		widget.setVisible(visible);

		// Display the caption
		widget.setText(showCaption ? pieceTitle() : "");

		// Locate the piece according to piece.coordinate
		Rectangle rec = new Rectangle(getPointX() - PIECE_SIDE_WIDTH / 2,
				getPointY() - PIECE_SIDE_WIDTH / 2, PIECE_SIDE_WIDTH,
				PIECE_SIDE_WIDTH);
		widget.setBounds(rec);
	}

	/**
	 * To let the widget visible or not visible
	 */
	public void renderWidgetFlicker() {
		// Set the widget to be visible or not
		widget.setVisible(isVisible());
	}

	/**
	 * Add the action on the widget. This should be invoked only once when
	 * initializing
	 */
	public void addWidgetAction() {
		// Add an action, each piece(button) has new created Action
		PieceAction action = new PieceAction(this);
		widget.addActionListener(action);
		widget.addMouseListener(action);
	}

	/**
	 * Type exchange
	 * 
	 * @param p
	 */
	public void typeExchange(Piece second) {
		byte tmp = this.getType();
		this.setType(second.getType());
		second.setType(tmp);
	}
	/**
	 * Get Piece caption
	 */
	public String pieceTitle() {
		return pieceTitle(this.type) ;
	}
	/**
	 * Solider type <--> caption
	 */
	public String pieceTitle(byte type) {
		String title = "ERR";
		switch (type) {
		case GameBoard.FLAG_S:
		case GameBoard.FLAG_N:
			title = "军旗";
			break;
		case GameBoard.MINE_S:
		case GameBoard.MINE_N:
			title = "地雷";
			break;
		case GameBoard.BOMB_S:
		case GameBoard.BOMB_N:
			title = "炸弹";
			break;
		case GameBoard.SILING_S:
		case GameBoard.SILING_N:
			title = "司令";
			break;
		case GameBoard.JUNZHANG_S:
		case GameBoard.JUNZHANG_N:
			title = "军长";
			break;
		case GameBoard.SHIZHANG_S:
		case GameBoard.SHIZHANG_N:
			title = "师长";
			break;
		case GameBoard.LVZHANG_S:
		case GameBoard.LVZHANG_N:
			title = "旅长";
			break;
		case GameBoard.TUANZHANG_S:
		case GameBoard.TUANZHANG_N:
			title = "团长";
			break;
		case GameBoard.YINGZHANG_S:
		case GameBoard.YINGZHANG_N:
			title = "营长";
			break;
		case GameBoard.LIANZHANG_S:
		case GameBoard.LIANZHANG_N:
			title = "连长";
			break;
		case GameBoard.PAIZHANG_S:
		case GameBoard.PAIZHANG_N:
			title = "排长";
			break;
		case GameBoard.GONGBING_S:
		case GameBoard.GONGBING_N:
			title = "工兵";
			break;
		}
		return title;
	}

	/*
	 * Getters and Setters
	 */
	public boolean isShowCaption() {
		return showCaption;
	}

	public void setShowCaption(boolean showCaption) {
		this.showCaption = showCaption;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public Located getLocated() {
		return located;
	}

	public JButton getWidget() {
		return widget;
	}

	public void setWidget(JButton widget) {
		this.widget = widget;
	}

}
