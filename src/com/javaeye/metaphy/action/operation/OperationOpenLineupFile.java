/**
 * @author Gou Ming Shi
 * http://metaphy.javaeye.com/
 * Sep 23, 2009
 * All Rights Reserved
 */
package com.javaeye.metaphy.action.operation;

import static com.javaeye.metaphy.game.Game.LINEUP_FILE_EXT;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import com.javaeye.metaphy.action.BaseAction;
import com.javaeye.metaphy.game.GameBoard;
import com.javaeye.metaphy.model.Located;
import com.javaeye.metaphy.model.Piece;

public class OperationOpenLineupFile extends BaseAction {
	/*
	 * Read the lineup file and re-lineup all pieces of the player
	 */
	public void actionPerformed(ActionEvent event) {
		GameBoard gameBoard = game.getGameBoard();
		ArrayList<Piece> pieces = game.getPanel().getPieces();
		JFileChooser chooser = new JFileChooser();
		try {
			chooser.setCurrentDirectory(new File("./src/res"));
			chooser.setFileFilter(new LineupFileFilter());
			chooser.showOpenDialog(game.getContainer());
			File f = chooser.getSelectedFile();
			if (f != null) {
				try {
					gameBoard.setLineupFile(f.toURI().toURL());
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
				// re-load for the model
				gameBoard.loadPieces(Located.SOUTH);
				// re-load for the view
				byte[][] board = gameBoard.getBoard();
				
				// pieces 前一半是North，后一半（从25开始）是South pieces
				int index = 25;
				for (int j = 11; j < GameBoard.BOARD_ARRAY_SIZE; j++) {
					for (int i = 0; i < GameBoard.BOARD_ARRAY_SIZE; i++) {
						// Re-load the pieces
						if (board[i][j] != 0x00) {
							Piece piece = pieces.get(index++);
							piece.setVisible(true);
							piece.setType(board[i][j]);
							piece.renderWidget();
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Filter of the lineup files
	 */
	private class LineupFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			return f.getName().toLowerCase().endsWith(LINEUP_FILE_EXT)
					|| f.isDirectory();
		}

		@Override
		public String getDescription() {
			return LINEUP_FILE_EXT.replace(".", "");
		}

	}
}
