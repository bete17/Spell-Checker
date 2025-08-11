package cs106;

import static sbcc.Core.*;

import java.io.*;
import java.nio.file.*;

import static java.lang.Math.*;

public class Main {

	/**
	 * To create a runnable JAR file, follow the steps show under the section called Building an artifact in IntelliJ
	 * of this article:  https://lightrun.com/java/how-to-export-a-jar-from-intellij/
	 *
	 * Recommendations:
	 *     Set the jar output directory to be your project root.
	 *     Check the Include in Project Build checkbox so that it gets created on each build.*
	 *
	 * To run the Spell Checker:
	 * 
	 * 1) Open a terminal (Command Prompt on Windows) and navigate to where your .jar file is.
	 * 
	 * 2) Download dictionary.txt into this directory.
	 * 
	 * 3) Copy a text document into this directory.
	 * 
	 * 4) java -jar spellcheck.jar
	 * 
	 * 5) You'll be prompted to specify a document and a dictionary. On subsequent runs, you will not be prompted about
	 * the dictionary.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			printf("%nSPELL CHECKER v1.0%n%n");
			var bsc = new BasicSpellChecker();

			var docFilename = getDocFilename(args);

			// Load the document to spell check.
			print("Loading " + docFilename + "...");
			bsc.loadDocument(docFilename);
			var modifiedDoc = false;
			println(" complete.");

			var dictFilename = getDictFilename(args);

			var modifiedDict = false;

			// Import/load the dictionary
			if (dictFilename.endsWith(".pre")) {
				print("Loading dictionary:  " + dictFilename + "...");
				bsc.loadDictionary(dictFilename);
			} else {
				print("Importing dictionary:  " + dictFilename + "...");
				bsc.importDictionary(dictFilename);
				modifiedDict = true;
				dictFilename = dictFilename.substring(0, dictFilename.lastIndexOf(".")) + ".pre";
			}
			println(" complete.");

			println("Spell checking " + docFilename + "...");

			// Spell-check the document
			var result = bsc.spellCheck(false);

			while (result != null) {
				var selection = displayMenu(bsc, result);
				int index = parseInt(result[1]);
				switch (selection) {
				case "2":
				case "3":
					replaceWithSelection(bsc, result, selection, index);
					modifiedDoc = true;
					break;

				case "4":
					bsc.addWordToDictionary(result[0]);
					modifiedDict = true;
					break;

				case "5":
					print("Enter replacement text:  ");
					bsc.replaceText(index, index + result[0].length(), readLine());
					modifiedDoc = true;
					break;

				case "6":
					result = null;
					break;

				default:
					println("Unknown option:  " + selection);
					break;

				}

				if (result != null)
					result = bsc.spellCheck(true);
			}

			println("Spell checking complete.");

			saveDocument(bsc, docFilename, modifiedDoc);

			saveDict(bsc, dictFilename, modifiedDict);

		} catch (Exception e) {
			println("Error occurred:  " + e.getLocalizedMessage());
		}
	}


	private static void replaceWithSelection(BasicSpellChecker bsc, String[] result, String selection, int index) {
		bsc.replaceText(index, index + result[0].length(), selection.equals("2") ? result[2] : result[3]);
	}


	private static void saveDict(BasicSpellChecker bsc, String dictFilename, boolean modifiedDict) throws Exception {
		// Save the dictionary
		if (modifiedDict) {
			printf("%nSaving " + dictFilename + "...");
			bsc.saveDictionary(dictFilename);
			println(" completed.");
		}
	}


	private static void saveDocument(BasicSpellChecker bsc, String docFilename, boolean modifiedDoc)
			throws Exception {
		// Save the document
		if (modifiedDoc) {
			printf("%nSave document as (type ENTER overwrite):  ");
			var fn = readLine();
			if (fn.equals(""))
				fn = docFilename;
			bsc.saveDocument(fn);
		}
	}


	private static String getDictFilename(String[] args) throws IOException {
		var dictFilename = "dictionary.pre";
		if (args.length > 1)
			dictFilename = args[1];

		while (!Files.exists(Path.of(dictFilename))) {
			print("Enter the dictionary path: ");
			dictFilename = readLine();
		}
		return dictFilename;
	}


	private static String getDocFilename(String[] args) throws IOException {
		var docFilename = args.length > 0 ? args[0] : null;
		while (docFilename == null || !Files.exists(Path.of(docFilename))) {
			print("Enter the document path: ");
			docFilename = readLine();
		}
		return docFilename;
	}


	private static String displayMenu(BasicSpellChecker bsc, String[] result) throws IOException {
		int index = parseInt(result[1]);
		var sb = new StringBuilder(bsc.getText());
		sb.insert(index + result[0].length(), "]");
		sb.insert(index, "[");
		int start = max(0, index - 20);
		int end = min(sb.length(), index + 20);
		var unknownPhrase = sb.substring(start, end).replace("\r", " ").replace("\n", " ");
		for (int i = 0; i < 2; i++)
			println();
		println("[Unknown Word]:  " + unknownPhrase);
		println();
		println("1)  Ignore");
		println("2)  Replace with " + result[2]);
		println("3)  Replace with " + result[3]);
		println("4)  Add");
		println("5)  Edit");
		println("6)  Finish");
		println();
		var choice = readLine();
		return choice;
	}

}
