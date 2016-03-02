package edu.asu.plp.tool.prototype.view;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class LEDDisplay extends BorderPane
{
	private static final int MINIMUM_SIZE = 100;
	private static final int NUMBER_OF_LEDS = 8;
	private static final int FONT_SIZE = 30;
	private static final String FONT_NAME = "Arial";
	private static final Paint FONT_COLOR = Color.WHITE;
	private static final String LIT_COLOR = "green";
	private static final String UNLIT_COLOR = "black";
	
	/**
	 * The state of each LED in this panel (on or off) where a bit set to 1 is "on" and a
	 * bit set to 0 is "off."
	 * <p>
	 * Each led corresponds to the bit at {@link #ledStates} >> index, where "index" is the
	 * index of the desired LED.
	 */
	private int ledStates;
	
	public LEDDisplay()
	{
		GridPane grid = new GridPane();
		for (int index = 0; index < NUMBER_OF_LEDS; index++)
		{
			int ledState = (ledStates >> index) & 1;
			boolean isLit = (ledState != 0);
			Node led = createLED(index, isLit);
			grid.add(led, index, 0);
		}
		
		setCenter(grid);
	}
	
	private Node createLED(int number, boolean isLit)
	{
		String labelText = Integer.toString(number);
		Label ledLabel = new Label(labelText);
		ledLabel.setFont(new Font(FONT_NAME, FONT_SIZE));
		ledLabel.setTextAlignment(TextAlignment.CENTER);
		ledLabel.setTextFill(FONT_COLOR);
		
		String style = "-fx-border-color: white; -fx-text-align: center; -fx-background-color:";
		style += (isLit) ? LIT_COLOR : UNLIT_COLOR;
		
		BorderPane led = new BorderPane();
		led.setMinHeight(MINIMUM_SIZE);
		led.setMinWidth(MINIMUM_SIZE);
		led.setStyle(style);
		led.setCenter(ledLabel);
		
		return led;
	}
	
	public void setLEDState(int ledIndex, boolean isLit)
	{
		// TODO
		throw new UnsupportedOperationException("Not yet implemented");
	}
	
	/**
	 * Alias for {@link #isLEDLit(int)}
	 * 
	 * @param ledIndex Index of the LED to retrieve the state of
	 * @return True if the LED is lit, false otherwise
	 */
	public boolean getLEDState(int ledIndex)
	{
		return isLEDLit(ledIndex);
	}
	
	private boolean isLEDLit(int ledIndex)
	{
		// TODO Auto-generated method stub return false;
		throw new UnsupportedOperationException("The method is not implemented yet.");
	}

	public void toggleLEDState(int ledIndex)
	{
		boolean newState = !getLEDState(ledIndex);
		setLEDState(ledIndex, newState);
	}
}
