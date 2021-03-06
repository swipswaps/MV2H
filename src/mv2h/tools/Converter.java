package mv2h.tools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;

/**
 * The <code>Converter</code> class is used to convert another file format into
 * a format that can be read by the MV2H package (standard out).
 *
 * @author Andrew McLeod
 */
public class Converter {
	/**
	 * Options for MIDI voice calculation.
	 */
	public static boolean CHANNEL = false;
	public static boolean TRACK = false;

	/**
	 * Run the program, reading the MusicXMLParser output from standard in and printing to
	 * standard out.
	 *
	 * @param args Unused command line arguments.
	 */
	public static void main(String[] args) {
		int anacrusis = 0;

		File inFile = null;
		File outFile = null;

		// No args given
		if (args.length == 0) {
			argumentError("No arguments given");
		}

		for (int i = 0; i < args.length; i++) {
			switch (args[i].charAt(0)) {
				// ARGS
				case '-':
					if (args[i].length() == 1) {
						argumentError("Unrecognized option: " + args[i]);
					}

					switch (args[i].charAt(1)) {
						// anacrusis
						case 'a':
							i++;
							if (args.length <= i) {
								argumentError("No anacrusis length given with -a.");
							}
							try {
								anacrusis = Integer.parseInt(args[i]);
							} catch (NumberFormatException e) {
								argumentError("Anacrusis must be an integer.");
							}
							break;

						// input file
						case 'i':
							i++;
							if (args.length <= i) {
								argumentError("No input file given with -i.");
							}
							if (inFile != null) {
								argumentError("-i FILE can only be used once.");
							}
							inFile = new File(args[i]);
							if (!inFile.exists()) {
								argumentError("Input file " + inFile + " does not exist.");
							}
							break;

						// output file
						case 'o':
							i++;
							if (args.length <= i) {
								argumentError("No output file given with -o.");
							}
							if (outFile != null) {
								argumentError("-o FILE can only be used once.");
							}
							outFile = new File(args[i]);
							break;

						// Voice options
						case '-':
							switch (args[i].substring(2)) {
								case "channel":
									CHANNEL = true;
									break;

								case "track":
									TRACK = true;
									break;

								default:
									argumentError("Unrecognized option: " + args[i]);
							}
							break;

						// Error
						default:
							argumentError("Unrecognized option: " + args[i]);
					}
					break;

				// Error
				default:
					argumentError("Unrecognized option: " + args[i]);
			}
		}

		// Convert
		Converter converter = null;
		if (!CHANNEL && !TRACK) {
			CHANNEL = true;
			TRACK = true;
		}
		if (inFile == null) {
			argumentError("-i FILE is required.");
		}
		try {
			converter = new MidiConverter(inFile, anacrusis);
		} catch (IOException | InvalidMidiDataException e) {
			System.err.println("Error reading from " + inFile + ":\n" + e.getMessage());
			System.exit(1);
		}

		// Print result
		if (outFile != null) {
			try {
				FileWriter fw = new FileWriter(outFile);
				fw.write(converter.toString());
				fw.close();
			} catch (IOException e) {
				System.err.println("Error writing to " + outFile + ":\n" + e.getMessage());
				System.err.println();
				System.err.println("Printing to std out instead:");
				System.out.println(converter.toString());
			}

		} else {
			System.out.println(converter.toString());
		}
	}

	/**
	 * Some argument error occurred. Print the given message and the usage instructions to std err
	 * and exit.
	 *
	 * @param message The message to print to std err.
	 */
	private static void argumentError(String message) {
		StringBuilder sb = new StringBuilder(message).append('\n');

		sb.append("Usage: Converter [-i FILE] [-o FILE] [-a INT] [--VOICE_ARGS]\n\n");

		sb.append("-i FILE = Read input from the given midi FILE. Required.\n");
		sb.append("-o FILE = Print out to the given FILE.\n");
		sb.append("          If not given, print to std out.\n\n");

		sb.append("Voice specific args (can include multiple; defaults to all):\n");
		sb.append("  --channel = Use channel to separate parsed voices.\n");
		sb.append("  --track = Use track to separate parsed voices.\n\n");

		sb.append("-a INT = Set the length of the anacrusis (pick-up bar), in sub-beats.\n");

		System.err.println(sb);
		System.exit(1);
	}
}
