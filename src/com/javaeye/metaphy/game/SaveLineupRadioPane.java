/**
 * @author Gou Ming Shi
 * http://metaphy.javaeye.com/
 * Nov 23, 2009
 * All Rights Reserved
 */
package com.javaeye.metaphy.game;

import static com.javaeye.metaphy.game.Game.GRID_UNIT_LENGTH;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.Border;

import com.javaeye.metaphy.action.operation.OperationSaveLineupFileCancel;
import com.javaeye.metaphy.action.operation.OperationSaveLineupFileOK;
import com.javaeye.metaphy.model.Located;

@SuppressWarnings("serial")
public class SaveLineupRadioPane extends JPanel {
	/* which one is selected */
	private Located choosed = Located.SOUTH;

	public SaveLineupRadioPane() {
		// Set the border
		Border paneBorder = BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "保存布局");
		this.setBorder(paneBorder);

		// Locate the RadioPanel on the Game Panel
		Rectangle rec = new Rectangle(GRID_UNIT_LENGTH * 12,
				GRID_UNIT_LENGTH * 12, GRID_UNIT_LENGTH * 5,
				GRID_UNIT_LENGTH * 3);
		this.setBounds(rec);

		this.setVisible(false);

		// Add RadioButtons and buttons
		ButtonGroup radiosGroup = new ButtonGroup();
		this.add(generateRadio(radiosGroup, "橙方", true));
		this.add(generateRadio(radiosGroup, "绿方", false));
		this.add(okCancel("确定"));
		this.add(okCancel("取消"));
	}

	/**
	 * Get the JRadioButton instance
	 * 
	 * @param radiosGroup
	 * @param caption
	 * @param selected
	 * @return
	 */
	private JRadioButton generateRadio(ButtonGroup radiosGroup, String caption,
			boolean selected) {
		JRadioButton radio = new JRadioButton(caption, selected);
		radiosGroup.add(radio);

		// Add action
		radio.addActionListener(new RadioAction(caption));
		return radio;
	}

	/**
	 * Inner action class
	 */
	private class RadioAction implements ActionListener {
		private String caption;

		public RadioAction(String caption) {
			this.caption = caption;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (caption.equalsIgnoreCase("橙方"))
				choosed = Located.SOUTH;
			else if (caption.equalsIgnoreCase("绿方"))
				choosed = Located.NORTH;
			
		}
	}

	/**
	 * Get a JButton instance
	 * 
	 * @param caption
	 * @return
	 */
	private JButton okCancel(String caption) {
		JButton button = new JButton(caption);
		button.setPreferredSize(new Dimension(GRID_UNIT_LENGTH
				+ GRID_UNIT_LENGTH * 7 / 10, GRID_UNIT_LENGTH * 7 / 10));
		button.setFocusable(false);

		if (caption.equalsIgnoreCase("确定"))
			button.addActionListener(new OperationSaveLineupFileOK(this));
		else
			button.addActionListener(new OperationSaveLineupFileCancel());

		return button;
	}

	public Located getChoosed() {
		return choosed;
	}

	public void setChoosed(Located choosed) {
		this.choosed = choosed;
	}

}
