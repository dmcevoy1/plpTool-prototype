package edu.asu.plp.tool.backend.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.StringJoiner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import com.google.common.base.Joiner;

import edu.asu.plp.tool.backend.isa.ASMFile;
import edu.asu.plp.tool.backend.isa.Assembler;
import edu.asu.plp.tool.backend.isa.UnitSize;
import edu.asu.plp.tool.backend.isa.exceptions.AssemblerException;
import edu.asu.plp.tool.backend.plpisa.assembler.PLPAssembler;

public class AssembleConsole
{
	protected static String assemblerName;
	protected static Assembler assembler;
	
	protected static CommandLine commandLine;
	protected static Options options;
	
	protected static List<ASMFile> projectFiles;
	
	protected static boolean isBenchMarking;
	protected static StringJoiner fileJoiner;
	
	// Sample Projects
	protected static HashMap<String, String> exampleProjects;
	
	public static void main(String[] args)
	{
		configureStaticSettings();
		
		initializeCommandLineOptions();
		
		parseCLIArguments(args);
		
		configureEnteredSettings();
		
		long startTime = System.nanoTime();
		
		try
		{
			assembler = new PLPAssembler(projectFiles);
			assembler.assemble();
		}
		catch (AssemblerException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		
		long endTime = System.nanoTime();
		
		if (isBenchMarking)
			System.out.println(
					String.format("It took: %.2f seconds", (endTime - startTime) * 1e-9));
	}
	
	private static void configureStaticSettings()
	{
		UnitSize.initializeDefaultValues();
		exampleProjects = new HashMap<>();
		projectFiles = new ArrayList<>();
		
		exampleProjects.put("no-sub-directory-project",
				"examples/PLP Projects/memtest.plp");
		exampleProjects.put("file-count",
				"examples/PLP Projects/universe/stress/file_count/file-count.plp");
		exampleProjects.put("one-file",
				"examples/Stripped PLP Projects (ASM Only)/universe/encapsulated/one-file.asm");
		exampleProjects.put("file-length",
				"examples/Stripped PLP Projects (ASM Only)/universe/stress/file_length/main.asm");
	}
	
	private static void initializeCommandLineOptions()
	{
		options = new Options();
		options.addOption("h", "help", false, "show help");
		options.addOption("b", "benchmark", false, "enable benchmark timing ouput");
		options.addOption("a", "assembler", true,
				"set assembler from choices: plp, mips");
		options.addOption("p", "project", true, "set project path to assemble");
		options.addOption("f", "file", true, "set path of a single asm file to assemble");
		options.addOption("e", "example", true, "set example from choices: "
				+ Joiner.on(", ").join(exampleProjects.keySet()));
	}
	
	private static void parseCLIArguments(String[] args)
	{
		CommandLineParser parser = new DefaultParser();
		
		try
		{
			commandLine = parser.parse(options, args);
		}
		catch (ParseException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private static void configureEnteredSettings()
	{
		if (commandLine.hasOption("h"))
			printHelp();
			
		if (commandLine.hasOption("b"))
			isBenchMarking = true;
			
		if (commandLine.hasOption("a"))
		{
			assemblerName = commandLine.getOptionValue("a").toLowerCase();
			if (!assemblerName.equalsIgnoreCase("plp"))
			{
				System.out.println("Only assembler currently supported is plp.");
				System.exit(-1);
			}
		}
		
		File assembleFile;
		if (commandLine.hasOption("p"))
		{
			// TODO enforce correct project ending relative to project type (.plp for plp)
			// TODO parse project into list of asm files
			assembleFile = new File(commandLine.getOptionValue("p"));
			if (!setProjectFiles(assembleFile))
			{
				System.out.println(
						"Provided project file was not valid: " + assembleFile.getPath());
				System.exit(-1);
			}
			
			System.out.println("Projects are not currently supported");
			System.exit(-1);
		}
		else if (commandLine.hasOption("f"))
		{
			// TODO enforce correct file type (.asm for plp)
			assembleFile = new File(commandLine.getOptionValue("f"));
			if (!setProjectFiles(assembleFile))
			{
				System.out.println(
						"Provided file was not valid: " + assembleFile.getPath());
				System.exit(-1);
			}
		}
		else if (commandLine.hasOption("e"))
		{
			String exampleName = commandLine.getOptionValue("e").toLowerCase();
			if (exampleProjects.containsKey(exampleName))
			{
				assembleFile = new File(exampleProjects.get(exampleName));
				if (!setProjectFiles(assembleFile))
				{
					System.out.println(
							"Oops, something went wrong with the file path of this example!");
					System.exit(-1);
				}
			}
			else
			{
				System.out.println(
						"Unknown example was entered, found: " + exampleName + ".");
				System.out.println(
						"Please see the help (via -h or -help) for possible examples.");
				System.exit(-1);
			}
		}
		else
		{
			System.out.println("No project, file, or example specified to assemble.");
			System.exit(0);
		}
	}
	
	private static boolean setProjectFiles(File assembleFile)
	{
		try
		{
			if (isValidProject(assembleFile))
			{
				openProject(assembleFile);
				if (projectFiles != null)
					return true;
				else
					return false;
			}
			else if (isValidFile(assembleFile))
			{
				projectFiles.add(new ASMFile(assembleFile.getPath()));
				
				return true;
			}
			else
				return false;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		return false;
	}
	
	// TODO replace with new project standard / backward compatibility?
	private static void openProject(File assembleFile)
	{
		try (TarArchiveInputStream plpInputStream = new TarArchiveInputStream(
				new FileInputStream(assembleFile)))
		{
			projectFiles.clear();
			TarArchiveEntry entry = plpInputStream.getNextTarEntry();
			while ((entry = plpInputStream.getNextTarEntry()) != null)
			{
				if (!entry.isDirectory())
				{
					byte[] content = new byte[(int) entry.getSize()];
					int currentIndex = 0;
					while(currentIndex < entry.getSize())
					{
						plpInputStream.read(content, currentIndex, content.length - currentIndex);
						currentIndex++;
					}
					if(entry.getName().endsWith(".asm"))
						projectFiles.add(new ASMFile(new String(content), assembleFile.getPath()));
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	// TODO replace with correct external method
	private static boolean isValidProject(File assembleFile)
	{
		if (!assembleFile.isFile())
			return false;
		if (!assembleFile.getPath().endsWith(".plp"))
			return false;
		else
			return true;
	}
	
	// TODO replace with correct external method
	private static boolean isValidFile(File assembleFile)
	{
		if (!assembleFile.isFile())
			return false;
		if (!assembleFile.getPath().endsWith(".asm"))
			return false;
		else
			return true;
	}
	
	private static void printHelp()
	{
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Assembler Console",
				"Your one stop shop, when you are lost and don't know what to do!",
				options, "Thank you for using Assembler Console!");
		System.exit(0);
	}
	
	private static String getFileContents(Path path)
	{
		List<String> fileLines = null;
		try
		{
			fileLines = Files.readAllLines(path);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		if (fileLines != null)
		{
			fileJoiner = new StringJoiner("\n");
			for (int index = 0; index < fileLines.size(); index++)
			{
				fileJoiner.add(fileLines.get(index));
			}
			return fileJoiner.toString();
		}
		
		return null;
	}
}
