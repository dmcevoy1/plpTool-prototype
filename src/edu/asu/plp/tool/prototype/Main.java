package edu.asu.plp.tool.prototype;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import moore.fx.components.Components;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import edu.asu.plp.tool.prototype.view.CodeEditor;

/**
 * Driver for the PLPTool prototype.
 * 
 * The driver's only responsibility is to launch the PLPTool Prototype window. This class
 * also defines the window and its contents.
 * 
 * @author Moore, Zachary
 *
 */
public class Main extends Application
{
	public static final String APPLICATION_NAME = "PLPTool";
	public static final long VERSION = 0;
	public static final long REVISION = 1;
	public static final int DEFAULT_WINDOW_WIDTH = 1280;
	public static final int DEFAULT_WINDOW_HEIGHT = 720;
	
	private TabPane openProjectsPanel;
	private BidiMap<SourceFileIdentifier, Tab> openProjects;
	
	public static void main(String[] args)
	{
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage)
	{
		primaryStage.setTitle(APPLICATION_NAME + " V" + VERSION + "." + REVISION);
		
		this.openProjects = new DualHashBidiMap<>();
		this.openProjectsPanel = new TabPane();
		Parent projectExplorer = createProjectTree();
		Parent outlineView = createOutlineView();
		Parent console = createConsole();
		Parent codeEditor = createCodeEditor();
		
		// Left side holds the project tree and outline view
		SplitPane leftSplitPane = new SplitPane();
		leftSplitPane.orientationProperty().set(Orientation.VERTICAL);
		leftSplitPane.getItems().addAll(Components.passiveScroll(projectExplorer),
				Components.wrap(outlineView));
		leftSplitPane.setDividerPositions(0.5, 1.0);
		
		// Right side holds the source editor and the output console
		SplitPane rightSplitPane = new SplitPane();
		rightSplitPane.orientationProperty().set(Orientation.VERTICAL);
		
		/* Commented this out for learning purposes at the moment
		 * 
		 *  rightSplitPane.getItems().addAll(Components.wrap(openProjectsPanel),
		 * 	Components.wrap(console));
		 * 
		 */
		
		rightSplitPane.getItems().addAll(Components.wrap(codeEditor),
						Components.wrap(console));
		rightSplitPane.setDividerPositions(0.75, 1.0);
		
		// Container for the whole view (everything under the toolbar)
		SplitPane explorerEditorSplitPane = new SplitPane();
		explorerEditorSplitPane.getItems().addAll(Components.wrap(leftSplitPane),
				Components.wrap(rightSplitPane));
		explorerEditorSplitPane.setDividerPositions(0.2, 1.0);
		
		loadOpenProjects();
		
		Parent toolbar = createToolbar();
		BorderPane mainPanel = new BorderPane();
		mainPanel.setTop(toolbar);
		mainPanel.setCenter(explorerEditorSplitPane);
		
		int width = DEFAULT_WINDOW_WIDTH;
		int height = DEFAULT_WINDOW_HEIGHT;
		primaryStage.setScene(new Scene(Components.wrap(mainPanel), width, height));
		primaryStage.show();
	}
	
	/**
	 * Creates a tab for the specified project, or selects the project, if the tab already
	 * exists.
	 * 
	 * @param project
	 *            The project to open
	 */
	private void openProject(String projectName, String fileName)
	{
		SourceFileIdentifier identifier = new SourceFileIdentifier(projectName, fileName);
		Tab tab = openProjects.get(identifier);
		
		if (tab == null) // Create new tab
		{
			// TODO: replace with actual content
			Node content = new CodeEditor();
			tab = addTab(openProjectsPanel, fileName, content);
			openProjects.put(identifier, tab);
		}
		
		// Activate the specified tab
		openProjectsPanel.getSelectionModel().select(tab);
	}
	
	private Tab addTab(TabPane panel, String projectName, Node contentPanel)
	{
		Tab tab = new Tab();
		tab.setText(projectName);
		tab.setContent(Components.wrap(contentPanel));
		tab.setOnClosed(new EventHandler<Event>() {
			@Override
			public void handle(Event event)
			{
				openProjects.removeValue(tab);
			}
		});
		panel.getTabs().add(tab);
		
		return tab;
	}
	
	// This will eventually be used with the openProject() method, creating for
	// learning purposes
	private Parent createCodeEditor(){
		
		Node textEditor = new CodeEditor();
		return Components.wrap(textEditor);
		
	}

	private Parent createConsole()
	{
		// TODO: replace with relevant console window
		return Components.wrap(new TextArea());
	}
	
	private Parent createOutlineView()
	{
		// TODO: replace with relevant outline window
		return Components.wrap(new TextArea());
	}
	
	/**
	 * Restore all projects from a persistent data store, and call
	 * {@link #openProject(String, String)} for each
	 */
	private void loadOpenProjects()
	{
		// TODO: replace with actual content
	}
	
	/**
	 * Creates a project tree to display all known projects, and their contents. The tree
	 * orders projects as first level elements, with their folders and files being
	 * children elements.
	 * <p>
	 * This method is responsible for adding all appropriate listeners to allow navigation
	 * of the tree (and display it appropriately), including setting the background and
	 * any other applicable css attributes.
	 * <p>
	 * The returned tree will open a file in the {@link #openProjectsPanel} when a
	 * fileName is double-clicked.
	 * 
	 * @return A tree-view of the project explorer
	 */
	private Parent createProjectTree()
	{
		TreeItem<String> root = new TreeItem<String>("");
		root.setExpanded(true);
		
		TreeItem<String> project = new TreeItem<>("Assignment1");
		project.setExpanded(true);
		ObservableList<TreeItem<String>> sourceFiles = project.getChildren();
		sourceFiles.add(new TreeItem<String>("main.asm"));
		sourceFiles.add(new TreeItem<String>("sorting.asm"));
		sourceFiles.add(new TreeItem<String>("division.asm"));
		root.getChildren().add(project);
		
		project = new TreeItem<>("Assignment2");
		sourceFiles = project.getChildren();
		sourceFiles.add(new TreeItem<String>("main.asm"));
		sourceFiles.add(new TreeItem<String>("uart_utilities.asm"));
		root.getChildren().add(project);
		
		TreeView<String> treeView = new TreeView<String>(root);
		treeView.showRootProperty().set(false);
		treeView.setBackground(Background.EMPTY);
		
		treeView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event)
			{
				if (event.getClickCount() == 2)
				{
					TreeItem<String> selection = treeView.getSelectionModel()
							.getSelectedItem();
					TreeItem<String> parent = selection.getParent();
					if (parent != null && parent.getValue().length() > 0)
					{
						// Selection is a file
						openProject(parent.getValue(), selection.getValue());
					}
				}
			}
		});
		
		return Components.wrapTop(treeView);
	}
	
	/**
	 * Creates a horizontal toolbar containing controls to:
	 * <ul>
	 * <li>Create a new project
	 * <li>Add a new file
	 * <li>Save the current project
	 * <li>Open a new project
	 * <li>Assemble the current project
	 * </ul>
	 * 
	 * @return a Parent {@link Node} representing the PLP toolbar
	 */
	private Parent createToolbar()
	{
		HBox toolbar = new HBox();
		toolbar.setPadding(new Insets(0, 0, 0, 5));
		toolbar.setSpacing(5);
		ObservableList<Node> buttons = toolbar.getChildren();
		
		EventHandler<MouseEvent> listener;
		Node button;
		
		// TODO: replace event handlers with actual content
		button = new ImageView("toolbar_new.png");
		listener = (event) -> System.out.println("New Project Clicked");
		button.setOnMouseClicked(listener);
		buttons.add(button);
		
		button = new ImageView("menu_new.png");
		listener = (event) -> System.out.println("New File Clicked");
		button.setOnMouseClicked(listener);
		buttons.add(button);
		
		button = new ImageView("toolbar_open.png");
		listener = (event) -> System.out.println("Open Project Clicked");
		button.setOnMouseClicked(listener);
		buttons.add(button);
		
		button = new ImageView("toolbar_save.png");
		listener = (event) -> System.out.println("Save Project Clicked");
		button.setOnMouseClicked(listener);
		buttons.add(button);
		
		button = new ImageView("toolbar_assemble.png");
		listener = (event) -> System.out.println("Assemble Project Clicked");
		button.setOnMouseClicked(listener);
		buttons.add(button);
		
		return Components.wrap(toolbar);
	}
}