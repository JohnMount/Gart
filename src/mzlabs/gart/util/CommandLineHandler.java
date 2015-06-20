package mzlabs.gart.util;

import java.io.File;
import java.io.PrintStream;
import java.text.ParseException;
import java.util.Date;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * handle command line arguments
 * @author johnmount
 *
 */
public class CommandLineHandler {
	public final String progName;
	public final Options options = new Options();
	
	public CommandLineHandler(final String progName) {
		this.progName = progName;
	}

	public void use(final PrintStream p) {
		p.println();
		HelpFormatter hf = new HelpFormatter();
		hf.printHelp(progName, options);
		p.println();
	}
	
	public CommandLine parse(final PrintStream p, final String[] args) throws ParseException {
		p.println("" + new Date() + "\tcwd: " + new File(".").getAbsolutePath());
		p.print("Running: " + progName);
		for(final String argi: args) {
			p.print(" " + argi);
		}
		p.println();
		CommandLine cmd = null;
		try {
			CommandLineParser parser = new GnuParser();
			cmd = parser.parse(options,args);
			// not allowing any left-over arguments, each argument must come from a flag
		} catch (Exception ex) {
			p.println("command line argument problem: " + ex);
			use(p);
			throw new ParseException(ex.toString(),0);
		}
		String[] leftOver = cmd.getArgs();
		if((leftOver!=null)&&(leftOver.length>0)) {
			StringBuilder b = new StringBuilder();
			b.append("command line argument problem, left over arguments:");
			for(final String li: leftOver) {
				b.append(" " + li);
			}
			use(p);
			p.println(b.toString());
			throw new ParseException(b.toString(),0);
		}
		return cmd;
	}
	
	public Option addRArg(final String nm, final String help) {
		Option argument = new Option(nm,true,help);
		argument.setRequired(true);
		options.addOption(argument);
		return argument; 
	}
	
	public Option addOArg(final String nm, final String help) {
		Option argument = new Option(nm,true,help);
		argument.setRequired(false);
		options.addOption(argument);
		return argument; 
	}
}
