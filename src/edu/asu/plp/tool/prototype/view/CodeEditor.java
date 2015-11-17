package edu.asu.plp.tool.prototype.view;

import java.awt.Color;

import javafx.embed.swing.SwingNode;
import javafx.scene.layout.BorderPane;

import javax.swing.CodeEditorPane;
import javax.swing.SwingUtilities;

public class CodeEditor extends BorderPane
{
	private CodeEditorPane textPane;
	
	public CodeEditor()
	{
		textPane = new CodeEditorPane();
		
		SwingNode swingNode = new SwingNode();
		createSwingContent(swingNode);
		setCenter(swingNode);
	}
	
	private void createSwingContent(SwingNode swingNode)
	{
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run()
			{
				textPane.setForeground(Color.red);
				textPane.setText("This is a Code Editor");
				swingNode.setContent(textPane);
			}
		});
	}
}
