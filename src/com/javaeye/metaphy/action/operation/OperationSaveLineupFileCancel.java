/**
 * @author Gou Ming Shi
 * http://metaphy.javaeye.com/
 * Nov 23, 2009
 * All Rights Reserved
 */
package com.javaeye.metaphy.action.operation;

import java.awt.event.ActionEvent;

import com.javaeye.metaphy.action.BaseAction;
import com.javaeye.metaphy.game.GamePanel;
import com.javaeye.metaphy.game.OperationButton.Operations;

public class OperationSaveLineupFileCancel extends BaseAction {
	/*
	 * Display the Radio-Panel
	 */
	public void actionPerformed(ActionEvent e) {
		GamePanel panel = game.getPanel();
		panel.getSaveLineupRadioPane().setVisible(false);

		// Show/Hide other Operation buttons
		panel.setAllOpertionButtonsVisible(true);

		panel.setOneOpertionButtonVisible(Operations.GIVE_UP, false);
		panel.setOneOpertionButtonVisible(Operations.PASS, false);
	}

}
