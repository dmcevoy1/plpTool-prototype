package edu.asu.plp.tool.prototype;

import static edu.asu.plp.tool.prototype.util.Dialogues.showAlertDialogue;
import static edu.asu.plp.tool.prototype.util.Dialogues.showInfoDialogue;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import moore.fx.components.Components;
import moore.util.ExceptionalSubroutine;
import moore.util.Subroutine;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.io.FileUtils;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;

import edu.asu.plp.tool.backend.EventRegistry;
import edu.asu.plp.tool.backend.isa.ASMDisassembly;
import edu.asu.plp.tool.backend.isa.ASMFile;
import edu.asu.plp.tool.backend.isa.ASMImage;
import edu.asu.plp.tool.backend.isa.ASMInstruction;
import edu.asu.plp.tool.backend.isa.Assembler;
import edu.asu.plp.tool.backend.isa.Simulator;
import edu.asu.plp.tool.backend.isa.exceptions.AssemblerException;
import edu.asu.plp.tool.backend.isa.exceptions.SimulatorException;
import edu.asu.plp.tool.backend.plpisa.sim.PLPMemoryModule;
import edu.asu.plp.tool.backend.plpisa.sim.PLPRegFile;
import edu.asu.plp.tool.core.ISAModule;
import edu.asu.plp.tool.core.ISARegistry;
import edu.asu.plp.tool.prototype.devices.SetupDevicesandMemory;
import edu.asu.plp.tool.prototype.model.ApplicationSetting;
import edu.asu.plp.tool.prototype.model.ApplicationThemeManager;
import edu.asu.plp.tool.prototype.model.OptionSection;
import edu.asu.plp.tool.prototype.model.PLPOptions;
import edu.asu.plp.tool.prototype.model.PLPProject;
import edu.asu.plp.tool.prototype.model.MIPSProject;
import edu.asu.plp.tool.prototype.model.Project;
import edu.asu.plp.tool.prototype.model.QuickViewSection;
import edu.asu.plp.tool.prototype.model.SimpleASMFile;
import edu.asu.plp.tool.prototype.model.Submittable;
import edu.asu.plp.tool.prototype.model.Theme;
import edu.asu.plp.tool.prototype.model.ThemeRequestCallback;
import edu.asu.plp.tool.prototype.model.ThemeRequestEvent;
import edu.asu.plp.tool.prototype.util.Dialogues;
import edu.asu.plp.tool.prototype.view.CodeEditor;
import edu.asu.plp.tool.prototype.view.ConsolePane;
import edu.asu.plp.tool.prototype.view.CpuWindow;
import edu.asu.plp.tool.prototype.view.OutlineView;
import edu.asu.plp.tool.prototype.view.ProjectExplorerTree;
import edu.asu.plp.tool.prototype.view.QuickViewPanel;
import edu.asu.plp.tool.prototype.view.WatcherWindow;
import edu.asu.plp.tool.prototype.view.menu.options.OptionsPane;
import edu.asu.plp.tool.prototype.view.menu.options.sections.ApplicationSettingsPanel;
import edu.asu.plp.tool.prototype.view.menu.options.sections.EditorSettingsPanel;
import edu.asu.plp.tool.prototype.view.menu.options.sections.ProgrammerSettingsPanel;
import edu.asu.plp.tool.prototype.view.menu.options.sections.SimulatorSettingsPanel;

/**
 * Driver for the PLPTool prototype.
 *
 * The driver's only responsibility is to launch the PLPTool Prototype window. This class
 * also defines the window and its contents.
 *
 * @author Moore, Zachary
 * @author Hawks, Elliott
 * @author Nesbitt, Morgan
 *
 */
public class Main extends Application implements Controller
{
	public static final String APPLICATION_NAME = "PLPTool";
	public static final long VERSION = 0;
	public static final long REVISION = 1;
	public static final int DEFAULT_WINDOW_WIDTH = 1280;
	public static final int DEFAULT_WINDOW_HEIGHT = 720;

	private Simulator activeSimulator;
	private Stage stage;
	private TabPane openProjectsPanel;
	private BidiMap<ASMFile, Tab> openFileTabs;
	private ObservableList<PLPLabel> activeNavigationItems;
	private ObservableList<Project> projects;
	private Map<Project, ProjectAssemblyDetails> assemblyDetails;
	private ProjectExplorerTree projectExplorer;
	private ConsolePane console;
	private WatcherWindow watcher = null;
	private EmulationWindow emulationWindow = null;

	private ApplicationThemeManager applicationThemeManager;
	private OutlineView outlineView;
	private SetupDevicesandMemory devicesSetup = null;

	public static void main(String[] args)
	{
		launch(args);
	}

	public static File findDiskObjectForASM(ASMFile activeFile)
	{
		Project project = activeFile.getProject();
		String path = project.getPathFor(activeFile);
		if (path == null)
			return null;

		return new File(path);
	}

	private void onTabActivation(ObservableValue<? extends Tab> value, Tab old,
			Tab current)
	{
		ASMFile previousASM = openFileTabs.getKey(current);
		if (previousASM != null)
			previousASM.contentProperty().removeListener(this::updateOutline);

		ASMFile asmFile = openFileTabs.getKey(current);
		if (asmFile != null)
		{
			String content = asmFile.getContent();
			List<PLPLabel> labels = PLPLabel.scrape(content);
			outlineView.setModel(FXCollections.observableArrayList(labels));
			asmFile.contentProperty().addListener(this::updateOutline);
		}
	}

	private void updateOutline(ObservableValue<? extends String> value, String old,
			String current)
	{
		List<PLPLabel> labels = PLPLabel.scrape(current);
		outlineView.setModel(FXCollections.observableArrayList(labels));
	}

	@Override
	public void start(Stage primaryStage)
	{
		this.stage = primaryStage;
		primaryStage.setTitle(APPLICATION_NAME + " V" + VERSION + "." + REVISION);

		ApplicationSettings.initialize();
		ApplicationSettings.loadFromFile("settings/plp-tool.settings");

		EventRegistry.getGlobalRegistry().register(new ApplicationEventBusEventHandler());

		applicationThemeManager = new ApplicationThemeManager();

		this.assemblyDetails = new HashMap<>();
		this.openFileTabs = new DualHashBidiMap<>();
		this.openProjectsPanel = new TabPane();
		this.projectExplorer = createProjectTree();
		outlineView = createOutlineView();
		console = createConsole();
		console.println(">> Console Initialized.");

		openProjectsPanel.getSelectionModel().selectedItemProperty()
				.addListener(this::onTabActivation);

		ScrollPane scrollableProjectExplorer = new ScrollPane(projectExplorer);
		scrollableProjectExplorer.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollableProjectExplorer.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
		scrollableProjectExplorer.setFitToHeight(true);
		scrollableProjectExplorer.setFitToWidth(true);

		// Left side holds the project tree and outline view
		SplitPane leftSplitPane = new SplitPane();
		leftSplitPane.orientationProperty().set(Orientation.VERTICAL);
		leftSplitPane.getItems().addAll(scrollableProjectExplorer, outlineView);
		leftSplitPane.setDividerPositions(0.5, 1.0);
		leftSplitPane.setMinSize(0, 0);

		// Right side holds the source editor and the output console
		SplitPane rightSplitPane = new SplitPane();
		rightSplitPane.orientationProperty().set(Orientation.VERTICAL);
		rightSplitPane.getItems().addAll(Components.wrap(openProjectsPanel),
				Components.wrap(console));
		rightSplitPane.setDividerPositions(0.75, 1.0);
		rightSplitPane.setMinSize(0, 0);

		// Container for the whole view (everything under the toolbar)
		SplitPane explorerEditorSplitPane = new SplitPane();
		explorerEditorSplitPane.getItems().addAll(Components.wrap(leftSplitPane),
				Components.wrap(rightSplitPane));
		explorerEditorSplitPane.setDividerPositions(0.225, 1.0);
		explorerEditorSplitPane.setMinSize(0, 0);

		SplitPane.setResizableWithParent(leftSplitPane, Boolean.FALSE);

		//loadOpenProjects();

		Parent menuBar = createMenuBar();
		Parent toolbar = createToolbar();
		BorderPane mainPanel = new BorderPane();
		VBox topContainer = new VBox();
		topContainer.getChildren().add(menuBar);
		topContainer.getChildren().add(toolbar);
		mainPanel.setTop(topContainer);
		mainPanel.setCenter(explorerEditorSplitPane);

		int width = DEFAULT_WINDOW_WIDTH;
		int height = DEFAULT_WINDOW_HEIGHT;

		Scene scene = new Scene(Components.wrap(mainPanel), width, height);

		primaryStage.setScene(scene);

		String themeName = ApplicationSettings.getSetting(
				ApplicationSetting.APPLICATION_THEME).get();
		EventRegistry.getGlobalRegistry().post(new ThemeRequestEvent(themeName));

		primaryStage.show();
	}

	private Parent plpQuickRef()
	{
		// TODO: load this from a JSON file
		List<QuickViewSection> plp = new ArrayList<>();

		QuickViewSection instructionsRType = new QuickViewSection("R-Type Instructions");
		instructionsRType.addEntry("addu $rd, $rs, $rt", "rd = rs + rt");
		instructionsRType.addEntry("subu $rd, $rs, $rt", "rd = rs - rt");
		instructionsRType.addEntry("and $rd, $rs, $rt", "rd = rs & rt");
		instructionsRType.addEntry("or $rd, $rs, $rt", "rd = rs | rt");
		// TODO: add remaining R-Type instructions

		QuickViewSection instructionsIType = new QuickViewSection("I-Type Instructions");
		instructionsIType.addEntry("addiu $rt, $rs, imm", "rt = rs + SignExtend(imm)");
		instructionsIType.addEntry("andi $rt, $rs, imm", "rt = rs & ZeroExtend(imm)");
		instructionsIType.addEntry("ori $rt, $rs, imm", "rt = rs | ZeroExtend(imm)");
		// TODO: add remaining I-Type instructions

		QuickViewSection instructionsJType = new QuickViewSection("J-Type Instructions");
		instructionsJType.addEntry("j label", "PC = label");
		instructionsJType.addEntry("jal label", "ra = PC + 4; PC = label");

		QuickViewSection instructionsPsuedo = new QuickViewSection("Pseudo-Operations");
		instructionsPsuedo.addEntry("nop", "sll $0, $0, 0");
		instructionsPsuedo.addEntry("b label", "beq $0, $0, label");
		instructionsPsuedo.addEntry("move $rd, $rs", "or $rd, $0, $rs");
		// TODO: add remaining pseudo instructions

		QuickViewSection directives = new QuickViewSection("Assembler Directives");
		directives.addEntry(".org address",
				"Place subsequent statements starting from address");
		directives.addEntry("label:", "Label current memory location as label");
		directives.addEntry(".word value", "Write 32-bit value to the current address");
		directives.addEntry(".ascii \"string\"",
				"Place string starting from the current address");
		directives.addEntry(".asciiz \"string\"",
				"Place null-terminated string starting from the current address");
		directives.addEntry(".asciiw \"string\"",
				"Place word-aligned string starting from the current address");
		directives.addEntry(".space value",
				"Reserve value words starting from the current address");
		directives.addEntry(".equ symbol value",
				"Add a symbol and its associated value to the symbol table (a constant)");

		QuickViewSection registers = new QuickViewSection("Registers Usage Guide");
		registers.addEntry("$0, $zero", "The zero register");
		registers.addEntry("$1, $at", "Assembler temporary");
		registers.addEntry("$2-$3, $v0-$v1", "Return values");
		registers.addEntry("$4-$7, $a0-$a3", "Function arguments");
		registers.addEntry("$8-$17, $t0-$t9", "Temporaries");
		registers.addEntry("$18-$25, $s0-$s7", "Saved temporaries");
		registers.addEntry("$26-$27, $i0-$i1", "Interrupt temporaries");
		registers.addEntry("$28, $iv", "Interrupt vector");
		registers.addEntry("$29, $sp", "Stack pointer");
		registers.addEntry("$30, $ir", "Interrupt return address");
		registers.addEntry("$31, $ra", "Return address");

		QuickViewSection ioMap = new QuickViewSection("I/O Memory Map");
		ioMap.addEntry("0x00000000", "Boot ROM");
		ioMap.addEntry("0x10000000", "RAM");
		ioMap.addEntry("0xf0000000", "UART");
		ioMap.addEntry("0xf0100000", "Switches");
		ioMap.addEntry("0xf0200000", "LEDs");
		ioMap.addEntry("0xf0300000", "GPIO");
		ioMap.addEntry("0xf0400000", "VGA");
		ioMap.addEntry("0xf0500000", "PLPID");
		ioMap.addEntry("0xf0600000", "Timer");
		ioMap.addEntry("0xf0a00000", "Seven Segment Display");
		ioMap.addEntry("0xf0700000", "Interrupt Controller");

		plp.add(instructionsRType);
		plp.add(instructionsIType);
		plp.add(instructionsJType);
		plp.add(instructionsPsuedo);
		plp.add(directives);
		plp.add(registers);
		plp.add(ioMap);
		return new QuickViewPanel("PLP 5.2", plp);
	}

	private Parent mipsQuickRef() {
		List<QuickViewSection> mips = new ArrayList<>();

		QuickViewSection instructionsRType = new QuickViewSection("R-Type Instruction");
		instructionsRType.addEntry("add $rd, $rs, $rt" , "rd = rs + rt (Overflow Trap)");
		instructionsRType.addEntry("addu $rd, $rs, $rt", "rd = rs + rt");
		instructionsRType.addEntry("and $rd, $rs, $rt",	"rd = rs & rt");
		instructionsRType.addEntry("div $rs, $rt", "$LO = rs / rt; $HI = rs % rt");
		instructionsRType.addEntry("divu $rs, $rt", "$LO = rs / rt; $HI = rs % rt (Unsigned)");
		instructionsRType.addEntry("jr $rs", "PC = rs");
		instructionsRType.addEntry("mfhi $rd", "rd = $HI");
		instructionsRType.addEntry("mflo $rd", "rd = $LO");
		instructionsRType.addEntry("mult $rs, $rt", "rs * rt = $LO ");
		instructionsRType.addEntry("multu $rs, $rt", "rs * rt = $LO (Unsigned)");
		instructionsRType.addEntry("nor $rd, $rs, $rt", "rd = !(rs | rt)");
		instructionsRType.addEntry("xor $rd, $rs, $rt", "rd = rs ⊕ rt");
		instructionsRType.addEntry("or $rd, $rs, $rt", "rd = rs | rt");
		instructionsRType.addEntry("slt $rd, $rs, $rt", "if (rs < rt) ? rd = 1 : rd = 0");
		instructionsRType.addEntry("sltu $rd, $rs, $rt", "if (rs < rt) ? rd = 1 : rd = 0 (Unisgned)");
		instructionsRType.addEntry("sll $rd, $rt, Imm", "rd = rt << Imm");
		instructionsRType.addEntry("srl $rd, $rt, Imm", "rd = rt >> Imm");
		instructionsRType.addEntry("sra $rd, $rt, Imm", "rd = rt >> Imm (SignBit shifted in)");
		instructionsRType.addEntry("sub $rd, $rs, $rt", "rd = rs - rt");
		instructionsRType.addEntry("subu $rd, $rs, $rt", "rd = rs - rt (Unsigned)");

		/*instructionsRType.addEntry("", "");
		instructionsRType.addEntry("", "");*/


		QuickViewSection instructionsIType = new QuickViewSection("I-Type Instructions");
		instructionsIType.addEntry("addi $rt, $rs, imm","rt = rs + imm");
		instructionsIType.addEntry("addiu $rt, $rs, imm", "rt = rs + SignExtend(imm)");
		instructionsIType.addEntry("andi $rt, $rs, imm", "rt = rs & ZeroExtend(imm)");
		instructionsIType.addEntry("beq $rs, $rt, label", "if(rs == rt) ? PC = Label : PC += 4");
		instructionsIType.addEntry("bne $rs, $rt, label", "if(rs != rt) ? PC = Label : PC += 4");
		instructionsIType.addEntry("blez $rs, label", "if(rs <= 0) ? PC = label : PC += 4");
		instructionsIType.addEntry("bgez $rs, label", "if(rs >= 0) ? PC = label : PC += 4");
		instructionsIType.addEntry("beqz $rs, label", "if(rs == 0) ? PC = label : PC += 4");
		instructionsIType.addEntry("bltz $rs, label", "if(rs < 0) ? PC = label : PC += 4");
		instructionsIType.addEntry("bgtz $rs, label", "if(rs > 0) ? PC = label : PC += 4");
		instructionsIType.addEntry("bnez $rs, label", "if(rs != 0) ? PC = label : PC += 4");
		instructionsIType.addEntry("lbu $rt, offset($rs) ", "rt = MEM_8[rs + offset] (Unsigned)");
		instructionsIType.addEntry("lhu $rt, offset($rs)", "rt = MEM_16[rs + offset] (Unisgned)");
		instructionsIType.addEntry("lui $rt, imm", "rt = imm[31:16]");
		instructionsIType.addEntry("lw $rt, offset($rs)", "rt = MEM_32[rs + offset]");
		instructionsIType.addEntry("ori $rt, $rs, imm", "rt = rs | ZeroExtend(imm)");
		instructionsIType.addEntry("sb $rt, offset($rs)", "MEM[rs + offset] = (0xff rt");
		instructionsIType.addEntry("slti $rt, $rs, imm", "if (rs < Imm) ? rt = 1 : rt = 0");
		instructionsIType.addEntry("sltiu $rt, $rs, imm", "if (rs < Unsigned imm) ? rt = 1 : rt = 0");
		instructionsIType.addEntry("sw $rs, offset($rt)", "rt[offset] = rs");
		/*instructionsIType.addEntry("", "");
		instructionsIType.addEntry("", "");*/

		QuickViewSection instructionsJType = new QuickViewSection("J-Type Instruction");
		instructionsJType.addEntry("j label","PC = label");
		instructionsJType.addEntry("jal label","ra = PC += 8; PC = label");

		/*
		 *TODO: Find remaining MIPS pseudo-instructions
		 */

		QuickViewSection instructionsPsuedo = new QuickViewSection("Pseudo-Operations");
		instructionsPsuedo.addEntry("move $rt, $rs", " add $rt, $rs, $0");
		instructionsPsuedo.addEntry("li $rs, imm", "lui $rt, imm[31:16]; ori $rd, $rd, imm[15:0]");
		instructionsPsuedo.addEntry("la $rs, addr", "lui $rt, addr[31:16]; ori $rd, $rd, addr[15:0]");
		instructionsPsuedo.addEntry("bal label", "$ra = PC + 8, PC += label");
		instructionsPsuedo.addEntry("beqz $rs, label", "if ($rs == 0) ? PC += label : PC += 4");
		//instructionsPsuedo.addEntry("","");


		QuickViewSection directives = new QuickViewSection("Assembler Directive");
		directives.addEntry(".align n ", "align next datum on a 2^n byte boundary [NOT IMPLEMENTED]");
		directives.addEntry(".ascii \"string\"", "Store a string in memory");
		directives.addEntry(".asciiz \"string\"", "Store null-terminated string in memory");
		directives.addEntry(".byte b1,...,bn", "Store values b1 through bn in successive bytes of memory");
		directives.addEntry(".data <address>", "store data items in data segment. If optional <address> is present,"
						+ " items are stored at beginning of <address>");
		directives.addEntry(".double d1,...,dn", "Store double values d1 through dn in successive memory locations");
		directives.addEntry(".extern sym size", "declare datum sotred in sym is size "
						+ "bytes larger and is global. Accessed via register $gp");
		directives.addEntry(".float f1,...,fn","Store floating point values f1 through fn in successive memory locations");
		directives.addEntry(".globl sym", "declare that symbol sym is global and can be referenced from other files\n"
						+ ".globl stored in $gp");
		directives.addEntry(".half h1,...,hn", "Store 16-bit values h1 through hn in successive memory locations");
		directives.addEntry("label:", "Label current memory location as label");
		directives.addEntry(".space n","Allocate n bytes of space in currecnt data segment");
		directives.addEntry(".text <address>","Store items in user text segment."
						+ " Optional <address> to start storage at beginning of <address>");
		directives.addEntry(".word wq,...,wn","Store 32-bit words w1 through wn in successive memory ");


		QuickViewSection registers = new QuickViewSection("Register Usage Guide");
		registers.addEntry("$0, zero", "Hard-wired to 0");
		registers.addEntry("$1, $at", "Reserved for Pseudo-instructions, Assembler temporary");
		registers.addEntry("$2-$3, $v0-$v1", "Function return values");
		registers.addEntry("$4-$7, $a0-$a3", "Arguments/Parameters to functions");
		registers.addEntry("$8-$15, $t0-$t7", "Temporary variables");
		registers.addEntry("$16-$23, $s0-$s7", "Function variables");
		registers.addEntry("$24-$25, $t8-$t9", "Additional Temporaries");
		registers.addEntry("$26-$27, $k0-$k1", "Reserved for Kernel, volatile");
		registers.addEntry("$28, $gp", "Global Pointer");
		registers.addEntry("$29, $sp", "Stack Pointer");
		registers.addEntry("$30, $fp", "Frame Pointer");
		registers.addEntry("$31, $ra", "Return Address");


		QuickViewSection ioMap = new QuickViewSection("I/O Memory Map");
		ioMap.addEntry("0x00000000", "Boot ROM");
		ioMap.addEntry("0x10000000", "RAM");
		ioMap.addEntry("0xf0000000", "UART");
		ioMap.addEntry("0xf0100000", "Switches");
		ioMap.addEntry("0xf0200000", "LEDs");
		ioMap.addEntry("0xf0300000", "GPIO");
		ioMap.addEntry("0xf0400000", "VGA");
		ioMap.addEntry("0xf0500000", "PLPID");
		ioMap.addEntry("0xf0600000", "Timer");
		ioMap.addEntry("0xf0a00000", "Seven Segment Display");
		ioMap.addEntry("0xf0700000", "Interrupt Controller");

		mips.add(instructionsRType);
		mips.add(instructionsIType);
		mips.add(instructionsJType);
		mips.add(instructionsPsuedo);
		mips.add(directives);
		mips.add(registers);
		mips.add(ioMap);

		return new QuickViewPanel("MIPS 32", mips);
	}
	private File showOpenDialogue()
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");

		String plp6Extension = "*" + PLPProject.FILE_EXTENSION;
		String mipsExtension = "*" + MIPSProject.FILE_EXTENSION;
		// Add new types here(or scrap them all in favor of a one, true type)
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("All Project Files", "*.plp", plp6Extension, mipsExtension),
				new ExtensionFilter("PLP6 Project Files", plp6Extension),
				new ExtensionFilter("MIPS Project Files", mipsExtension),
				new ExtensionFilter("PLP Legacy Project Files", "*.plp"),
				new ExtensionFilter("All Files", "*.*"));

		return fileChooser.showOpenDialog(stage);
	}

	private File showExportDialogue(ASMFile exportItem)
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Export");
		fileChooser.setInitialFileName(exportItem.getName() + ".asm");

		String plp6Extension = "*" + PLPProject.FILE_EXTENSION;
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("PLP6 Project Files", plp6Extension),
				new ExtensionFilter("Legacy Project Files", "*.plp"),
				new ExtensionFilter("All PLP Project Files", "*.plp", plp6Extension),
				new ExtensionFilter("All Files", "*.*"));

		return fileChooser.showOpenDialog(stage);
	}

	private File showImportDialogue()
	{
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import ASM");

		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("ASM Files", "*.asm"),
				new ExtensionFilter("All Files", "*.*"));

		return fileChooser.showOpenDialog(stage);
	}


	@Override
	public void openProject()
	{
		File selectedFile = showOpenDialogue();
		if (selectedFile != null)
		{
			//If we are selecting the new format, that is a file with name .project.
			//In that case change the file to parent folder rather than file. - Harsha
			if(selectedFile.getName().endsWith(PLPProject.FILE_EXTENSION))
				selectedFile = selectedFile.getParentFile();
			openProjectFromFile(selectedFile);
		}
	}

	/**
	 * Loads the given file from disk using {@link Project#load(File)}, and adds the
	 * project to the project explorer.
	 * <p>
	 * If the project is already in the project explorer, a message will be displayed
	 * indicating the project is already open, and the project will be expanded in the
	 * project tree.
	 * <p>
	 * If the project is not in the tree, but a project with the same name is in the tree,
	 * then a message will be displayed indicating that a project with the same name
	 * already exists, and will ask if the user would like to rename one of the projects.
	 * If not, the dialogue will be closed and the project will not be opened.
	 *
	 * @param file
	 *            The file or directory (PLP6 only) containing the project to be opened
	 */
	private void openProjectFromFile(File file)
	{
		try
		{
			Project project = PLPProject.load(file);
			addProject(project);
		}
		catch (IOException e)
		{
			showAlertDialogue(e, "There was a problem loading the selected file");
		}
		catch (Exception e)
		{
			showAlertDialogue(e);
		}
	}

	private void addProject(Project project)
	{
		Project existingProject = getProjectByName(project.getName());
		if (existingProject != null)
		{
			if (existingProject.getPath().equals(project.getPath()))
			{
				// Projects are the same
				showInfoDialogue("This project is already open!");
				projectExplorer.expandProject(existingProject);
			}
			else
			{
				// Project with the same name already exists
				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle("Confirmation Dialog");
				alert.setGraphic(null);
				alert.setHeaderText(null);
				alert.setContentText("A project with the name \""
						+ project.getName()
						+ "\" already exists. In order to open this project, you must choose a different name."
						+ "\n\n"
						+ "Press OK to choose a new name, or Cancel to close this dialog.");

				Optional<ButtonType> result = alert.showAndWait();
				if (result.get() == ButtonType.OK)
				{
					boolean renamed = renameProject(project);
					if (renamed)
						addProject(project);
				}
			}
		}
		else
		{
			projects.add(project);
		}
	}

	private boolean renameProject(Project project)
	{
		TextInputDialog dialog = new TextInputDialog(project.getName());
		dialog.setTitle("Rename Project");
		dialog.setHeaderText(null);
		dialog.setGraphic(null);
		dialog.setContentText("Enter a new name for the project:");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent())
		{
			String newName = result.get();
			if (newName.equals(project.getName()))
			{
				showInfoDialogue("The new name must be different from the old name");
				return renameProject(project);
			}
			else
			{
				project.setName(newName);
			}
		}

		return false;
	}

	private Project getProjectByName(String name)
	{
		for (Project project : projects)
		{
			String projectName = project.getName();
			boolean namesAreNull = (projectName == null && name == null);
			if (namesAreNull || name.equals(projectName))
				return project;
		}

		return null;
	}

	private void navigateToLabel(PLPLabel label)
	{
		CodeEditor editor = getActiveCodeEditor();
		if (editor == null)
			throw new IllegalStateException("Cannot access active code editor");

		try
		{
			int lineNumber = label.getLineNumber();
			editor.jumpToLine(lineNumber);
		}
		catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	/**
	 * Creates a tab for the specified project, or selects the project, if the tab already
	 * exists.
	 *
	 * @param project
	 *            The project to open
	 */
	private void openFile(ASMFile file)
	{
		file.contentProperty().addListener(this::updateOutline);
		String fileName = file.getName();

		System.out.println("Opening " + fileName);
		Tab tab = openFileTabs.get(file);

		String str = file.getContent();

		if (tab == null)
		{
			// Create new tab
			CodeEditor content = createCodeEditor();
			tab = addTab(openProjectsPanel, fileName, content);
			openFileTabs.put(file, tab);

			// Set content
			if (file.getContent() != null)
				content.setText(file.getContent());
			else
				content.setText("");

			// Bind content
			file.contentProperty().bind(content);
		}

		// Activate the specified tab
		openProjectsPanel.getSelectionModel().select(tab);
	}

	/*private void printFile(ASMFile file)
	{

		System.out.println("selected file: " + file.getName());
	}*/

	@Override
	public void editCopy()
	{
		Tab tab = openProjectsPanel.getSelectionModel().getSelectedItem();

		CodeEditor ed = (CodeEditor)tab.getContent();

		ed.copySelectionToClipboard();

	}

	@Override
	public void editUndo()
	{
		Tab tab = openProjectsPanel.getSelectionModel().getSelectedItem();

		CodeEditor ed = (CodeEditor)tab.getContent();

		ed.undoText();

	}

	@Override
	public void saveActiveProjectAs()
	{
		Stage createProjectStage = new Stage();
		Parent myPane = saveAsMenu();
		Scene scene = new Scene(myPane, 600, 350);
		createProjectStage.setTitle("Save Project As");
		createProjectStage.setScene(scene);
		createProjectStage.setResizable(false);
		createProjectStage.show();
	}



	private Parent saveAsMenu()
	{
		BorderPane border = new BorderPane();
		border.setPadding(new Insets(20));
		GridPane grid = new GridPane();
		HBox buttons = new HBox(10);
		grid.setHgap(10);
		grid.setVgap(30);
		grid.setPadding(new Insets(10, 10, 10, 10));

		Label projectName = new Label();
		projectName.setText("New Project Name: ");
		projectName.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

		TextField projTextField = new TextField();
		projTextField.setText("Project Name");
		projTextField.requestFocus();
		projTextField.setPrefWidth(200);

		Label selectedProject = new Label();
		selectedProject.setText("Save Project: \"" + getActiveProject().getName()
				+ "\" as :");
		selectedProject.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

		Label projectLocation = new Label();
		projectLocation.setText("Location: ");
		projectLocation.setFont(Font.font("Arial", FontWeight.NORMAL, 16));

		TextField projLocationField = new TextField();
		projTextField.setPrefWidth(200);

		Button browseLocation = new Button();
		browseLocation.setText("Browse");
		browseLocation.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e)
			{
				String chosenLocation = "";
				DirectoryChooser directoryChooser = new DirectoryChooser();
				directoryChooser.setTitle("Choose Project Location");
				File file = directoryChooser.showDialog(null);
				if (file != null)
				{
					chosenLocation = file.getAbsolutePath().concat(
							File.separator + projTextField.getText());
					projLocationField.setText(chosenLocation);
				}

			}
		});

		Button saveAsButton = new Button("Save");
		saveAsButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e)
			{
				String projectName = projTextField.getText();
				String projectLocation = projLocationField.getText();
				if (projectName == null || projectName.trim().isEmpty())
				{
					Dialogues.showInfoDialogue("You entered an invalid Project Name");
				}
				else if (projectLocation == null || projectLocation.trim().isEmpty())
				{
					Dialogues.showInfoDialogue("You entered an invalid Project Location");

				}
				else
				{
					Project activeProject = getActiveProject();
					try
					{
						activeProject.saveAs(projectLocation);
					}
					catch (IOException ioException)
					{
						Dialogues.showAlertDialogue(ioException);
						ioException.printStackTrace();
					}
					Stage stage = (Stage) saveAsButton.getScene().getWindow();
					stage.close();
				}
			}
		});
		saveAsButton.setDefaultButton(true);
		Button cancelCreate = new Button("Cancel");
		cancelCreate.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e)
			{
				Stage stage = (Stage) cancelCreate.getScene().getWindow();
				stage.close();
			}
		});

		grid.add(projectName, 0, 0);
		grid.add(projTextField, 1, 0);
		// grid.add(selectedProject, 0, 1);
		grid.add(projectLocation, 0, 2);
		grid.add(projLocationField, 1, 2);
		grid.add(browseLocation, 2, 2);

		border.setTop(selectedProject);
		border.setCenter(grid);

		buttons.getChildren().addAll(saveAsButton, cancelCreate);
		buttons.setAlignment(Pos.BASELINE_RIGHT);
		border.setBottom(buttons);

		return Components.wrap(border);
	}

	private List<PLPLabel> scrapeLabelsInActiveTab()
	{
		Tab selectedTab = openProjectsPanel.getSelectionModel().getSelectedItem();
		if (selectedTab == null)
			return Collections.emptyList();
		else
		{
			ASMFile activeASM = openFileTabs.getKey(selectedTab);
			String content = activeASM.getContent();
			return PLPLabel.scrape(content);
		}
	}

	private CodeEditor createCodeEditor()
	{
		return new CodeEditor();
	}

	private Tab addTab(TabPane panel, String projectName, Node contentPanel)
	{
		Tab tab = new Tab();
		tab.setText(projectName);
		tab.setContent(contentPanel);
		tab.setOnClosed(new EventHandler<Event>() {
			@Override
			public void handle(Event event)
			{
				ASMFile asmFile = openFileTabs.removeValue(tab);
				if (asmFile != null)
				{
					asmFile.contentProperty().removeListener(Main.this::updateOutline);
				}
			}
		});
		tab.setOnSelectionChanged(new EventHandler<Event>() {
			@Override
			public void handle(Event event)
			{
				ASMFile activeFile = openFileTabs.getKey(tab);
				if (activeFile != null)
					projectExplorer.setActiveFile(activeFile);
			}
		});
		panel.getTabs().add(tab);

		return tab;
	}

	private ConsolePane createConsole()
	{
		ConsolePane console = new ConsolePane();
		ContextMenu contextMenu = new ContextMenu();

		MenuItem clearConsoleItem = new MenuItem("Clear");
		clearConsoleItem.setOnAction(e -> console.clear());
		contextMenu.getItems().add(clearConsoleItem);

		console.addEventHandler(ContextMenuEvent.CONTEXT_MENU_REQUESTED, event -> {
			contextMenu.show(console, event.getScreenX(), event.getScreenY());
			event.consume();
		});
		console.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY)
				contextMenu.hide();
		});

		return console;
	}

	private OutlineView createOutlineView()
	{
		List<PLPLabel> activeLabels = scrapeLabelsInActiveTab();
		activeNavigationItems = FXCollections.observableArrayList(activeLabels);

		OutlineView outlineView = new OutlineView(activeNavigationItems);
		outlineView.setOnAction(this::navigateToLabel);

		return outlineView;
	}

	/**
	 * Restore all projects from a persistent data store, and call
	 * {@link #openProject(String, String)} for each
	 */
	private void loadOpenProjects()
	{
		// TODO: replace with actual content
		try
		{
			PLPProject project;
			project = PLPProject.load(new File("examples/PLP Projects/memtest.plp"));
			projects.add(project);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
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
	private ProjectExplorerTree createProjectTree()
	{


		projects = FXCollections.observableArrayList();
		ProjectExplorerTree projectExplorer = new ProjectExplorerTree(projects);

		projectExplorer.setOnFileDoubleClicked(this::openFile);


		return projectExplorer;
	}

	/**
	 * Creates a horizontal toolbar containing controls to:
	 * <ul>
	 * <li>Create a new PLPProject
	 * <li>Add a new file
	 * <li>Save the current project
	 * <li>Open a new PLPProject
	 * <li>Assemble the current project
	 * </ul>
	 *
	 * @return a Parent {@link Node} representing the PLP toolbar
	 */
	private Parent createToolbar()
	{
		MainToolbar toolbar = new MainToolbar(this);
		return toolbar;
	}

	private Parent createMenuBar()
	{
		PLPToolMenuBarPanel menuBar = new PLPToolMenuBarPanel(this);
		return menuBar;
	}

	private void assemble(Project project)
	{
		Optional<ISAModule> optionalISA = project.getISA();
		if (optionalISA.isPresent())
		{
			ISAModule isa = optionalISA.get();
			Assembler assembler = isa.getAssembler();
			assemble(assembler, project);
		}
		else
		{
			// TODO: handle "no compatible ISA" case
			throw new UnsupportedOperationException("Not yet implemented");
		}
	}

	private void assemble(Assembler assembler, Project project)
	{
		try
		{
			ASMImage assembledImage = assembler.assemble(project);
			ProjectAssemblyDetails details = getAssemblyDetailsFor(project);
			details.setAssembledImage(assembledImage);
		}
		catch (AssemblerException exception)
		{
			console.error(exception.getLocalizedMessage());
		}
	}

	private ProjectAssemblyDetails getAssemblyDetailsFor(Project activeProject)
	{
		ProjectAssemblyDetails details = assemblyDetails.get(activeProject);

		if (details == null)
		{
			details = new ProjectAssemblyDetails(activeProject);
			assemblyDetails.put(activeProject, details);
		}

		return details;
	}

	private Project getActiveProject()
	{
		ASMFile activeFile = getActiveFile();
		if (activeFile == null)
			return null;
		else
			return activeFile.getProject();
	}

	private ASMFile getActiveFileInTabPane()
	{
		Tab selectedTab = openProjectsPanel.getSelectionModel().getSelectedItem();
		return openFileTabs.getKey(selectedTab);
	}

	private CodeEditor getActiveCodeEditor()
	{
		Tab activeTab = openProjectsPanel.getSelectionModel().getSelectedItem();
		if (activeTab == null)
			return null;

		Node tabContents = activeTab.getContent();
		if (tabContents != null)
			return (CodeEditor) tabContents;
		else
			return null;
	}

	private ASMFile getActiveFileInProjectExplorer()
	{
		Pair<Project, ASMFile> selection = projectExplorer.getActiveSelection();
		if (selection == null)
			return null;

		ASMFile selectedFile = selection.getValue();
		return selectedFile;
	}

	private ASMFile getActiveFile()
	{
		ASMFile selectedFile = getActiveFileInTabPane();
		if (selectedFile == null)
			return getActiveFileInProjectExplorer();
		else
			return selectedFile;
	}

	private ASMCreationPanel createASMMenu()
	{
		ASMCreationPanel createASMMenu = new ASMCreationPanel(this::createASM);
		for (Project project : projects)
		{
			String projectName = project.getName();
			createASMMenu.addProjectName(projectName);
		}
		return createASMMenu;
	}

	private void createASM(ASMCreationDetails details)
	{
		String projectName = details.getProjectName();
		String fileName = details.getFileName();

		Project project = getProjectByName(projectName);
		if (project != null)
		{
			SimpleASMFile createASM = new SimpleASMFile(project, fileName);
			project.add(createASM);
			openFile(createASM);
		}
		else
		{
			String message = "The project \"" + projectName + "\" could not be found";
			throw new IllegalArgumentException(message);
		}
	}

	@Override
	public void createNewProject()
	{
		Stage createProjectStage = new Stage();
		ProjectCreationPanel projectCreationPanel = projectCreateMenu();
		projectCreationPanel.setFinallyOperation(createProjectStage::close);

		Scene scene = new Scene(projectCreationPanel, 450, 350);
		createProjectStage.setTitle("Create New PLP Project");
		createProjectStage.setScene(scene);
		createProjectStage.setResizable(false);
		createProjectStage.show();
	}

	private ProjectCreationPanel projectCreateMenu()
	{
		ProjectCreationPanel projectCreationPanel = new ProjectCreationPanel();
		projectCreationPanel.addProjectType("PLP6", this::createProject);
		projectCreationPanel.addProjectType("PLP5 (Legacy)", this::createLegacyProject);
		projectCreationPanel.setSelectedType("PLP6");
		return projectCreationPanel;
	}

	private void createLegacyProject(ProjectCreationDetails details)
	{
		PLPProject project = new PLPProject(details.getProjectName());
		project.setPath(details.getProjectLocation());

		String sourceName = details.getMainSourceFileName();
		SimpleASMFile sourceFile = new SimpleASMFile(project, sourceName);
		project.add(sourceFile);
		tryAndReport(project::saveLegacy);
		projects.add(project);
		openFile(sourceFile);
	}

	private void createProject(ProjectCreationDetails details)
	{
		PLPProject project = new PLPProject(details.getProjectName());
		project.setPath(details.getProjectLocation());

		String sourceName = details.getMainSourceFileName();
		SimpleASMFile sourceFile = new SimpleASMFile(project, sourceName);
		project.add(sourceFile);
		tryAndReport(project::save);
		projects.add(project);
		openFile(sourceFile);
	}

	@Override
	public void createNewMIPSProject() {
		Stage createMIPSProjectStage = new Stage();
		MIPSProjectCreationPanel mipsProjectCreationPanel = mipsProjectCreateMenu();
		mipsProjectCreationPanel.setFinallyOperation(createMIPSProjectStage::close);

		Scene scene = new Scene(mipsProjectCreationPanel, 450, 350);
		createMIPSProjectStage.setTitle("Create New MIPS Project");
		createMIPSProjectStage.setScene(scene);
		createMIPSProjectStage.setResizable(false);
		createMIPSProjectStage.show();
	}

	private MIPSProjectCreationPanel mipsProjectCreateMenu() {
		MIPSProjectCreationPanel mipsProjectCreationPanel = new MIPSProjectCreationPanel();
		// for future versions, here is were different versions of MIPS can be added(to the drop-down menu anyway)
		mipsProjectCreationPanel.addProjectType("MIPS", this::createMIPSProject);
		mipsProjectCreationPanel.setSelectedType("MIPS");
		return mipsProjectCreationPanel;
	}

	private void createMIPSProject(ProjectCreationDetails details) {
		// TODO verify working
		MIPSProject project = new MIPSProject(details.getProjectName());
		project.setPath(details.getProjectLocation());

		String sourceName = details.getMainSourceFileName();
		SimpleASMFile sourceFile = new SimpleASMFile(project, sourceName);
		project.add(sourceFile);
		tryAndReport(project::save);
		projects.add(project);
		openFile(sourceFile);
	}

	private void tryAndReport(ExceptionalSubroutine subroutine)
	{
		try
		{
			subroutine.perform();
		}
		catch (Exception exception)
		{
			Dialogues.showAlertDialogue(exception);
		}
	}

	public void showEmulationWindow()
	{
		Stage createEmulationStage = new Stage();


		Scene scene = new Scene(emulationWindow, 1275, 700);
		createEmulationStage.setTitle("Emulation Window");
		createEmulationStage.setScene(scene);
		// createEmulationStage.setResizable(false);
		createEmulationStage.show();


	}

	public void openCpuViewWindow()
	{
		Stage createCpuStage = new Stage();
		CpuWindow cpuWindowView = new CpuWindow();

		Scene scene = new Scene(cpuWindowView, 1200, 700);
		createCpuStage.setTitle("PLP CPU Core Simulation");
		createCpuStage.setScene(scene);
		createCpuStage.show();
	}

	private boolean optionsMenuOkSelected(List<Submittable> submittables)
	{
		for (Submittable submittable : submittables)
		{
			if (!submittable.isValid())
				return false;
		}
		return true;
	}

	private HashMap<OptionSection, Pane> createOptionsMenuModel(
			List<Submittable> submittables)
	{
		HashMap<OptionSection, Pane> model = new LinkedHashMap<>();

		addApplicationOptionSettings(model, submittables);
		addEditorOptionSettings(model, submittables);
		addASimulatorOptionSettings(model, submittables);
		addProgrammerOptionSettings(model, submittables);

		// TODO Accept new things

		return model;
	}

	private void addApplicationOptionSettings(HashMap<OptionSection, Pane> model,
			List<Submittable> submittables)
	{
		PLPOptions applicationSection = new PLPOptions("Application");

		ObservableList<String> applicationThemeNames = FXCollections
				.observableArrayList();
		applicationThemeNames.addAll(applicationThemeManager.getThemeNames());

		// TODO acquire editor theme names
		// TODO add filters, disabling sounds retarded. Just filter and put non adjacent
		// at bottom
		ObservableList<String> editorThemeNames = FXCollections.observableArrayList();
		editorThemeNames.addAll("eclipse", "tomorrow", "xcode", "ambiance", "monokai",
				"twilight");

		ApplicationSettingsPanel applicationPanel = new ApplicationSettingsPanel(
				applicationThemeNames, editorThemeNames);
		submittables.add(applicationPanel);

		model.put(applicationSection, applicationPanel);
	}

	private void addEditorOptionSettings(HashMap<OptionSection, Pane> model,
			List<Submittable> submittables)
	{
		PLPOptions editorSection = new PLPOptions("Editor");
		ObservableList<String> fontNames = getAvailableFontNames();

		// TODO acquire editor modes
		ObservableList<String> editorModes = FXCollections.observableArrayList();
		editorModes.addAll("plp");

		EditorSettingsPanel editorPanel = new EditorSettingsPanel(fontNames, editorModes);
		submittables.add(editorPanel);

		model.put(editorSection, editorPanel);
	}

	private ObservableList<String> getAvailableFontNames()
	{
		ObservableList<String> fontNames = FXCollections.observableArrayList();
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		java.awt.Font[] fonts = graphicsEnvironment.getAllFonts();
		for (java.awt.Font font : fonts)
		{
			String fontName = font.getFontName();
			fontNames.add(fontName);
		}

		return fontNames;
	}

	private void addASimulatorOptionSettings(HashMap<OptionSection, Pane> model,
			List<Submittable> submittables)
	{
		PLPOptions simulatorSection = new PLPOptions("Simulator");

		SimulatorSettingsPanel simulatorPanel = new SimulatorSettingsPanel();
		submittables.add(simulatorPanel);

		model.put(simulatorSection, simulatorPanel);
	}

	private void addProgrammerOptionSettings(HashMap<OptionSection, Pane> model,
			List<Submittable> submittables)
	{
		PLPOptions programmerSection = new PLPOptions("Programmer");

		ProgrammerSettingsPanel programmerPanel = new ProgrammerSettingsPanel();
		submittables.add(programmerPanel);

		model.put(programmerSection, programmerPanel);
	}

	public class ApplicationEventBusEventHandler
	{
		private ApplicationEventBusEventHandler()
		{

		}

		@Subscribe
		public void applicationThemeRequestCallback(ThemeRequestCallback event)
		{
			if (event.requestedTheme().isPresent())
			{
				Theme applicationTheme = event.requestedTheme().get();
				try
				{
					stage.getScene().getStylesheets().clear();
					stage.getScene().getStylesheets().add(applicationTheme.getPath());
					return;
				}
				catch (MalformedURLException e)
				{
					console.warning("Unable to load application theme "
							+ applicationTheme.getName());
					return;
				}
			}

			console.warning("Unable to load application theme.");
		}

		@Subscribe
		public void deadEvent(DeadEvent event)
		{
			System.out.println("Dead Event");
			System.out.println(event.getEvent());
		}
	}

	@Override
	public void saveActiveProject()
	{
		Project activeProject = getActiveProject();
		tryAndReport(activeProject::save);
	}

	@Override
	public void printActiveFile()
	{
		CodeEditor activeEditor = getActiveCodeEditor();
		if (activeEditor == null)
		{
			Dialogues.showActionFailedDialogue("No file is open!");
			return;
		}

		PrinterJob printAction = PrinterJob.createPrinterJob();
		if (printAction == null)
		{
			Dialogues.showActionFailedDialogue("Unable to access system print utilities");
			return;
		}

		boolean notCancelled = printAction.showPrintDialog(stage);
		if (notCancelled)
		{
			boolean success = printAction.printPage(activeEditor);
			if (success)
				printAction.endJob();
			else
				Dialogues.showActionFailedDialogue("Print may have failed");
		}
	}

	@Override
	public void createNewASM()
	{
		if (projects.isEmpty())
		{
			Dialogues
					.showInfoDialogue("There are not projects open, please create a project first.");
		}
		else
		{
			Stage createASMStage = new Stage();
			ASMCreationPanel asmCreationMenu = createASMMenu();
			asmCreationMenu.setFinallyOperation(createASMStage::close);

			Scene scene = new Scene(asmCreationMenu, 450, 200);
			createASMStage.setTitle("New ASMFile");
			createASMStage.setScene(scene);
			createASMStage.setResizable(false);
			createASMStage.show();
		}
	}

	@Override
	public void importASM()
	{
		File importTarget = showImportDialogue();
		try
		{
			String content = FileUtils.readFileToString(importTarget);
			Project activeProject = getActiveProject();
			String name = importTarget.getName();

			ASMFile asmFile = new SimpleASMFile(activeProject, name);
			asmFile.setContent(content);
			activeProject.add(asmFile);
			activeProject.save();
		}
		catch (Exception exception)
		{
			Dialogues.showAlertDialogue(exception, "Failed to import asm");
		}
	}

	@Override
	public void exportASM()
	{
		// XXX: Consider moving this to a component
		ASMFile activeFile = getActiveFile();
		if (activeFile == null)
		{
			// XXX: possible feature: select file from a list or dropdown
			String message = "No file is selected! Open the file you wish to export, or select it in the ProjectExplorer.";
			Dialogues.showInfoDialogue(message);
			return;
		}

		File exportTarget = showExportDialogue(activeFile);
		if (exportTarget == null)
			return;

		if (exportTarget.isDirectory())
		{
			String exportPath = exportTarget.getAbsolutePath()
					+ activeFile.constructFileName();
			exportTarget = new File(exportPath);

			String message = "File will be exported to " + exportPath;
			Optional<ButtonType> result = Dialogues.showConfirmationDialogue(message);

			if (result.get() != ButtonType.OK)
			{
				// Export was canceled
				return;
			}
		}

		if (exportTarget.exists())
		{
			String message = "The specified file already exists. Press OK to overwrite this file, or cancel to cancel the export.";
			Optional<ButtonType> result = Dialogues.showConfirmationDialogue(message);

			if (result.get() != ButtonType.OK)
			{
				// Export was canceled
				return;
			}
		}

		String fileContents = activeFile.getContent();
		try
		{
			FileUtils.write(exportTarget, fileContents);
		}
		catch (Exception exception)
		{
			Dialogues.showAlertDialogue(exception, "Failed to export asm");
		}
	}

	@Override
	public void removeASM()
	{
		ASMFile activeFile = getActiveFile();
		if (activeFile == null)
		{
			// XXX: possible feature: select file from a list or dropdown
			String message = "No file is selected! Select the file you wish to remove in the ProjectExplorer, then click remove.";
			Dialogues.showInfoDialogue(message);
			return;
		}

		File removalTarget = findDiskObjectForASM(activeFile);
		if (removalTarget == null)
		{
			// XXX: show a confirmation dialogue to confirm removal
			String message = "Unable to locate file on disk. "
					+ "The asm \""
					+ activeFile.getName()
					+ "\" will be removed from the project \""
					+ activeFile.getProject().getName()
					+ "\" but it is suggested that you verify the deletion from disk manually.";
			Dialogues.showInfoDialogue(message);
			Project activeProject = activeFile.getProject();
			activeProject.remove(activeFile);
			return;
		}

		if (removalTarget.isDirectory())
		{
			// XXX: show a confirmation dialogue to confirm removal
			String message = "The path specified is a directory, but should be a file. "
					+ "The asm \""
					+ activeFile.getName()
					+ "\" will be removed from the project \""
					+ activeFile.getProject().getName()
					+ "\" but it is suggested that you verify the deletion from disk manually.";
			Exception exception = new IllegalStateException(
					"The path to the specified ASMFile is a directory, but should be a file.");
			Dialogues.showAlertDialogue(exception, message);
			return;
		}
		else
		{
			String message = "The asm \"" + activeFile.getName()
					+ "\" will be removed from the project \""
					+ activeFile.getProject().getName() + "\" and the file at \""
					+ removalTarget.getAbsolutePath() + "\" will be deleted.";
			Optional<ButtonType> result = Dialogues.showConfirmationDialogue(message);

			if (result.get() != ButtonType.OK)
			{
				// Removal was canceled
				return;
			}
		}

		if (!removalTarget.exists())
		{
			String message = "Unable to locate file on disk. The file will be removed from the project, but it is suggested that you verify the deletion from disk manually.";
			Dialogues.showInfoDialogue(message);
		}

		try
		{
			boolean wasRemoved = removalTarget.delete();
			if (!wasRemoved)
				throw new Exception("The file \"" + removalTarget.getAbsolutePath()
						+ "\" was not deleted.");
		}
		catch (Exception exception)
		{
			Dialogues
					.showAlertDialogue(
							exception,
							"Failed to delete asm from disk. It is suggested that you verify the deletion from disk manually.");
		}
	}

	@Override
	public void setMainASMFile()
	{
		ASMFile activeFile = getActiveFile();
		if (activeFile == null)
		{
			Dialogues.showActionFailedDialogue("No file is selected!");
			return;
		}

		Project activeProject = activeFile.getProject();
		String message = "The file \"" + activeFile.getName()
				+ "\" will be used as the main file for the project \""
				+ activeProject.getName() + "\"";
		Optional<ButtonType> result = Dialogues.showConfirmationDialogue(message);

		if (result.get() == ButtonType.OK)
		{
			int index = activeProject.indexOf(activeFile);
			Collections.swap(activeProject, 0, index);
		}
	}

	@Override
	public void exit()
	{
		stage.close();
		Platform.exit();
	}

	@Override
	public void clearConsole()
	{
		console.clear();
	}

	@Override
	public void clearAllBreakpoints()
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("The method is not implemented yet.");
	}

	@Override
	public void showNumberConverter()
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("The method is not implemented yet.");
	}

	@Override
	public void assembleActiveProject()
	{
		Project activeProject = getActiveProject();
		assemble(activeProject);
	}

	@Override
	public void simulateActiveProject()
	{
		Project activeProject = getActiveProject();
		String projectType = activeProject.getType();
		Optional<ISAModule> module = ISARegistry.get().lookupByProjectType(projectType);

		if (module.isPresent())
		{
			ISAModule isa = module.get();
			activeSimulator = isa.getSimulator();

			if(devicesSetup == null)
			{
				devicesSetup = new SetupDevicesandMemory(activeSimulator);
				devicesSetup.setup();
				activeSimulator.getAddressBus().enable_allmodules();
			}

			emulationWindow = new EmulationWindow(activeSimulator, devicesSetup);

			activeSimulator.loadProgram(getAssemblyDetailsFor(activeProject).getAssembledImage());

			activeSimulator.getAddressBus().setEmulationWindow(emulationWindow);


		}
		else
		{
			String message = "No simulator is available for the project type: ";
			message += projectType;
			Dialogues.showAlertDialogue(new IllegalStateException(message));
		}

		// TODO: open associated views? emulation window?
	}

	@Override
	public void downloadActiveProjectToBoard()
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("The method is not implemented yet.");
	}

	@Override
	public void stepSimulation()
	{
		performIfActive(() -> {
			try {
				activeSimulator.step();
			} catch (SimulatorException e) {
				console.println(e.getMessage());
			}
		});
	}

	private void performIfActive(Subroutine subroutine)
	{
		try
		{
			subroutine.perform();
		}
		catch (Exception exception)
		{
			throw new IllegalStateException("No simulator is active!", exception);
		}
	}

	@Override
	public void triggerSimulationInterrupt()
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("The method is not implemented yet.");
	}

	@Override
	public void resetSimulation()
	{
		performIfActive(activeSimulator::reset);
	}

	@Override
	public void runSimulation()
	{
		Project activeProject = getActiveProject();

		ProjectAssemblyDetails details = getAssemblyDetailsFor(activeProject);
		if (!details.isDirty())
		{
			activeSimulator.loadProgram(details.getAssembledImage());
			performIfActive(() -> {
				try {
					activeSimulator.run();
				} catch (SimulatorException e) {
					// pause on exception
					activeSimulator.pause();
					console.println(e.getMessage());
				}
			});
		}
		else
		{
			// TODO: handle "Project Not Assembled" case
			throw new UnsupportedOperationException("Not yet implemented");
		}
	}

	@Override
	public void changeSimulationSpeed(int requestedSpeed)
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("The method is not implemented yet.");
	}

	@Override
	public void stopSimulation()
	{
		performIfActive(activeSimulator::pause);
		performIfActive(activeSimulator::reset);
		activeSimulator = null;
		// TODO: deactivate simulation views (e.g. Emulation Window)
	}

	@Override
	public void loadModule()
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("The method is not implemented yet.");
	}

	@Override
	public void clearModuleCache()
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("The method is not implemented yet.");
	}

	@Override
	public void showQuickReference()
	{
		Stage stage = new Stage();
		Scene scene = null;

		// TODO: remove hard-coded numbers. Where did these even come from?
		// TODO account for different quick references, such as MIPs, x86, etc

		Pair selection = projectExplorer.getActiveSelection(); 				//returns a long string of the entire classpath
																			//starting at edu.asu.plp.....
		if(selection == null)
		{
			console.println("NO PROJECT TYPE DETECTED, PLEASE SELECT A PROJECT OR ASMFILE");
			return;
		}

		String selectionStr = selection.toString();
		String projectType = selectionStr.split("\\.")[6];					//Index 6 is where the actual project type is specified


		if(projectType.charAt(0) == 'P')
		{
			scene = new Scene(plpQuickRef(), 888, 500);
			stage.setTitle("PLP_XX Quick Reference"); //TODO: place version number here
		}
		else if(projectType.charAt(0) == 'M')
		{
			scene = new Scene(mipsQuickRef(), 900, 500);
			stage.setTitle("MIPS32 Quick Reference");
		}

		stage.setScene(scene);
		stage.show();
	}

	@Override
	public void showOnlineManual()
	{
		String webAddress = "https://code.google.com/p/progressive-learning-platform/wiki/UserManual";
		openWebPage(webAddress);
	}

	private void openWebPage(String webAddress)
	{
		try
		{
			if (Desktop.isDesktopSupported())
			{
				URI location = new URI(webAddress);
				Desktop.getDesktop().browse(location);
			}
			else
			{
				String cause = "This JVM does not support Desktop. Try updating Java to the latest version.";
				throw new Exception(cause);
			}
		}
		catch (Exception exception)
		{
			String recoveryMessage = "There was a problem opening the following webpage:"
					+ "\n" + webAddress;
			Dialogues.showAlertDialogue(exception, recoveryMessage);
		}
	}

	@Override
	public void reportIssue()
	{
		String webAddress = "https://github.com/zcmoore/plpTool-prototype/issues";
		openWebPage(webAddress);
	}

	@Override
	public void showIssuesPage()
	{
		String webAddress = "https://github.com/zcmoore/plpTool-prototype/issues/new";
		openWebPage(webAddress);
	}

	@Override
	public void showAboutPLPTool()
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("The method is not implemented yet.");
	}

	@Override
	public void showThirdPartyLicenses()
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("The method is not implemented yet.");
	}



	@Override
	public void showOptionsMenu()
	{
		List<Submittable> submittables = new ArrayList<>();
		Map<OptionSection, Pane> optionsMenuModel = createOptionsMenuModel(submittables);

		OptionsPane optionsPane = new OptionsPane(optionsMenuModel);
		Scene popupScene = new Scene(optionsPane);

		Stage popupWindow = new Stage(StageStyle.DECORATED);
		popupWindow.setTitle("Settings");
		popupWindow.initModality(Modality.WINDOW_MODAL);
		popupWindow.initOwner(stage);
		popupWindow.setScene(popupScene);

		popupWindow.setMinWidth(stage.getScene().getWidth() / 2);
		popupWindow.setMinHeight(stage.getScene().getHeight()
				- (stage.getScene().getHeight() / 3));

		popupScene.getStylesheets().addAll(stage.getScene().getStylesheets());

		optionsPane.setOkAction(() -> {
			if (optionsMenuOkSelected(submittables))
			{
				submittables.forEach(submittable -> submittable.submit());
				popupWindow.close();
			}
		});
		optionsPane.setCancelAction(() -> {
			popupWindow.close();
		});

		popupWindow.setOnCloseRequest((windowEvent) -> {
			popupWindow.close();
		});
		popupWindow.show();
	}

	@Override
	public void showWatcherWindow()
	{
		Stage stage = new Stage();
		// TODO: pass active memory module and register File to WatcherWindow
		//WatcherWindow watcherWindow = new WatcherWindow(new , new PLPRegFile());
		watcher = new WatcherWindow(activeSimulator.getAddressBus(), activeSimulator.getRegisterFile());//emulationWindow.getWatcherWindow();

		Scene scene = new Scene(watcher, 888, 500);
		stage.setTitle("Watcher Window");
		stage.setScene(scene);
		stage.show();
	}

	@Override
	public void showModuleManager()
	{
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("The method is not implemented yet.");
	}

	@Override
	public void saveAll()
	{
		for (Project project : projects)
		{
			tryAndReport(project::save);
		}
	}
}
