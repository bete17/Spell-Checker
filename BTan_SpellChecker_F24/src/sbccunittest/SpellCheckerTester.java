package sbccunittest;

import cs106.Dictionary;
import cs106.*;
import org.junit.*;
import org.w3c.dom.ranges.*;
import sbcc.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

import static java.lang.Math.*;
import static java.lang.System.*;
import static org.apache.commons.lang3.StringUtils.*;
import static org.junit.Assert.*;
import static sbcc.Core.*;
import static sbccunittest.SbccUnitTestSupport.*;

/**
 * 1/1/2023
 *
 * @author sstrenn
 */
public class SpellCheckerTester {

    public static String newline = getProperty("line.separator");
    public static int baseScore = 0;
    public static int extraCredit = 0;
    public static int maxScore = 66;
    public static boolean isZeroScore = false;
    public static String scoreNotes = "";

    public static ArrayList<String> suggestions = new ArrayList<>();


    @BeforeClass
    public static void beforeTesting() {
        baseScore = 0;
        extraCredit = 0;
        suggestions.add("""
                
                🧐 ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯
                """);
    }


    @AfterClass
    public static void afterTesting() throws IOException {
        if (isZeroScore) {
            baseScore = 0;
            extraCredit = 0;
        }
        println(lineSeparator() + scoreNotes + "Estimated score out of " + maxScore + " (w/o late penalties, etc.) = " + baseScore);
        println("Estimated extra credit (assuming on time submission) = " + extraCredit);
        provideSuggestions();

    }


    private static void provideSuggestions() throws IOException {

        checkForCompareTo();

        checkForInOrderImport();

        checkForIncorrectWordPattern();

        checkForLackOfIndexAdjustInReplaceText();

        if (suggestions.size() > 1)
            println(join(suggestions, ""));
    }

    private static void checkForLackOfIndexAdjustInReplaceText() throws IOException {
        if (Files.exists(Path.of("testSpellCheckReplaceOneUnknownWord_result"))
                && !Files.exists(Path.of("testSpellCheckReplaceUnknowns_result"))) {
            var scCode = readFile("./src/cs106/BasicSpellChecker.java");
            var replaceTextCode = getMethodCodeNoComments(scCode, "public\\s*void\\s*replaceText\\(.*\\).*\\{");
            var lines = replaceTextCode.split(lineSeparator());
            if (lines.length <= 3 && !replaceTextCode.contains("=")) {
                suggestions.add("""
                        
                        💡  Since testSpellCheckReplaceOneUnknownWord() passed but testSpellCheckReplaceUnknowns()
                            did not, and there is no assignment statement in your replaceText() method, it is possible
                            that your replaceText() method is not adjusting your spell-checking starting index.
                        
                            It is important to adjust the spell checker's starting index by the difference in 
                            length of the replacementText and the text being replaced.  This ensures that a call to spellCheck() 
                            with continueFromPrevious == true will proceed correctly after the replacement of text. 
                        """);

                var spellCheckCode = getMethodCodeNoComments(scCode, "public\\s*String\\[\\]\\s*spellCheck\\(.*\\).*\\{");
                if (spellCheckCode.contains(".find()")) {
                    suggestions.add("""
                            
                            💡  A couple of other useful tips for handling continueFromPrevious == true:
                            
                                1)  Make use of Matcher.find(int start), which takes a starting index.
                                    See https://docs.oracle.com/javase/8/docs/api/java/util/regex/Matcher.html#find-int-
                            
                                2)  When each word is found, update the starting index so that the next call to 
                                    find() will find the next word.  If you don't always update the starting index,
                                    you could have an infinite loop.
                            """);
                }
            }

        }
    }

    private static void checkForIncorrectWordPattern() throws IOException {
        if (Files.exists(Path.of("testFind_result")) && !Files.exists(Path.of("testSpellCheckWithOneUnknownWord_result"))) {
            var scCode = readFile("./src/cs106/BasicSpellChecker.java");
            var spellCheckCode = getMethodCodeNoComments(scCode, "public\\s*String\\[\\]\\s*spellCheck\\(.*\\).*\\{");
            var lines = spellCheckCode.split(lineSeparator());
            if (lines.length > 6 && !scCode.contains("\\b[\\w']+\\b")) {
                suggestions.add("""
                        
                        💡  The word-matching pattern of \"\\\\b[\\\\w']+\\\\b\" was not found in BasicSpellChecker.java.
                            Use of that exact pattern is required to make it possible for the grade script and
                            your program to get equivalent results.  Note that you need an extra backslash in your
                            Java string for each backslash in the regex.  https://tinyurl.com/2baztmrr
                        """);

            }
        }
    }

    private static void checkForInOrderImport() throws IOException {
        if (Files.exists(Path.of("testAdd_result")) && Files.exists(Path.of("testImportFile_result"))) {
            if (readFile("testImportFile_result").equals("timed out")) {
                var dictCode = readFile("./src/cs106/BasicDictionary.java");
                var importMethodCode = getMethodCodeNoComments(dictCode, "public\\s*void\\s*importFile\\(String\\s*filename\\).*\\{");
                if (!importMethodCode.contains("shuffle(")) {
                    var p = Pattern.compile("for\\s*\\(.*?\\).*?add\\(.*?\\)", Pattern.DOTALL);
                    var m = p.matcher(importMethodCode);
                    if (m.find()) {
                        suggestions.add("""
                                
                                💡  It appears that testImportFile() timed out and that your importFile() may be
                                    adding an unshuffled list of words to the dictionary.  If so, consider using
                                    Collections.shuffle() to randomize the word order before adding them to the tree.  
                                
                                    NOTE:  This suggestion may not be valid if you are using another method to 
                                    achieve a balanced tree. 
                                """);

                    }

                }
            }
        }
    }

    private static void checkForCompareTo() throws IOException {
        // Check for use of compareTo() rather than compareToIgnoreCase()
        if (!Files.exists(Path.of("testFind_result"))) {
            var dictCode = readFile("./src/cs106/BasicDictionary.java");
            var findCode = getMethodCodeNoComments(dictCode, "public\\s*String\\[\\]\\s*find\\(.*\\).?\\{");
            if (findCode.contains(".compareTo(")) {
                suggestions.add("""
                        
                        💡  It appears that testFind() is not passing and that your find() method may be using
                            .compareTo() to compare strings.  find() should use .compareToIgnoreCase() to 
                            compare strings.
                        """);

            }
        }
    }


    @Test(timeout = 10000)
    public void testImportFile() throws Exception {
        writeFile("testImportFile_result", "timed out");

        Dictionary dictionary = new BasicDictionary();

        dictionary.importFile("full_dictionary.txt");

        assertNotNull("Dictionary.getRoot() should not be null.", dictionary.getRoot());

        int depth = getTreeDepth(dictionary);
        int maxDepth = 100;
        if (depth > maxDepth)
            fail("The tree depth is " + depth
                    + " is greater than the maximum allowed depth of " + maxDepth + ".");

        dictionary.save("full_dictionary.pre");
        String s = readFile("full_dictionary.pre");
        String[] parts = s.split("\n");
        assertEquals(175169, parts.length);

        baseScore += 5;
        writeFile("testImportFile_result", "passed");
    }


    @Test(timeout = 10000)
    public void testImportFileCompleteTree() throws Exception {
        Dictionary dictionary = new BasicDictionary();
        dictionary.importFile("full_dictionary.txt");

        var root = dictionary.getRoot();
        assertNotNull("Dictionary.getRoot() should not be null.", root);

        int depth = getTreeDepth(dictionary);
        int maxDepth = 18;
        if (depth > maxDepth)
            fail("The tree depth is " + depth
                    + ", which is > than the max allowed depth of " + maxDepth
                    + ".");

        dictionary.save("full_dictionary.pre");
        String s = readFile("full_dictionary.pre");
        String[] parts = s.split("\n");
        assertEquals(175169, parts.length);

        extraCredit += 5;

        int count = countNodes(root);
        int index = incompleteAtLevelOrderIndex(root, count);
        if (index >= 0)
            fail("Level order index " + index
                    + " is empty, so the tree is not complete.  But the tree has the right number of levels to get the extra credit.");
    }


    /* This function counts the number of nodes in a binary tree */
    int countNodes(BinaryTreeNode node) {
        if (node == null)
            return (0);
        return (1 + countNodes(node.left) + countNodes(node.right));
    }


    /* This function checks if the binary tree is complete or not */
    boolean isComplete(BinaryTreeNode root, int index, int numNodes) {
        // An empty tree is complete
        if (root == null)
            return (true);

        // If index assigned to current node is more than
        // number of nodes in tree, then tree is not complete
        if (index >= numNodes)
            return (false);

        // Recur for left and right subtrees
        return (isComplete(root.left, 2 * index + 1, numNodes) &&
                isComplete(root.right, 2 * index + 2, numNodes));
    }


    static int incompleteAtLevelOrderIndex(BinaryTreeNode cursor, int numNodes) {
        int index = 0;
        var q = new ArrayDeque<BinaryTreeNode>();
        while (index < numNodes) {
            index++;
            if (index >= numNodes)
                break;
            if (cursor.left == null)
                return index;
            else
                q.add(cursor.left);

            index++;
            if (index >= numNodes)
                break;
            if (cursor.right == null)
                return index;
            else
                q.add(cursor.right);
            cursor = q.remove();
        }
        return -1;
    }


    public int getTreeDepth(Dictionary dictionary) {
        return getTreeDepth(dictionary.getRoot());
    }


    public int getTreeDepth(BinaryTreeNode node) {
        if (node == null)
            return 0;
        else
            return 1 + max(getTreeDepth(node.left), getTreeDepth(node.right));
    }


    @Test(timeout = 10000)
    public void testLoad() throws Exception {
        Dictionary dictionary = new BasicDictionary();
        dictionary.load("dict_14.pre");

        assertNotNull("Dictionary.getRoot() should not be null.", dictionary.getRoot());

        int depth = getTreeDepth(dictionary);
        assertEquals(6, depth);

        baseScore += 8;
    }


    @Test(timeout = 10000)
    public void testSave() throws Exception {
        Dictionary dictionary = new BasicDictionary();
        String[] words = {"bull", "are", "genetic", "cotton", "dolly",
                "florida", "each", "bull"};
        for (String word : words)
            dictionary.add(word);

        dictionary.save("test_save.pre");
        String s = readFile("test_save.pre");
        String[] parts = s.split("\n");

        assertEquals(words.length - 1, parts.length);
        for (int ndx = 0; ndx < parts.length; ndx++)
            assertEquals(words[ndx], parts[ndx].trim().toLowerCase());

        baseScore += 8;
    }

    @Test
    public void testAdd() throws IOException {
        Files.deleteIfExists(Path.of("testAdd_result"));

        Dictionary d = new BasicDictionary();
        for (var w : "AECBD".split(""))
            d.add(w);
        var n = d.getRoot();
        assertEquals("A", n.value);
        assertEquals("E", n.right.value);
        assertEquals("C", n.right.left.value);
        assertEquals("B", n.right.left.left.value);
        assertEquals("D", n.right.left.right.value);

        d = new BasicDictionary();
        for (var w : "EDCAB".split(""))
            d.add(w);
        n = d.getRoot();
        assertEquals("E", n.value);
        assertEquals("D", n.left.value);
        assertEquals("C", n.left.left.value);
        assertEquals("A", n.left.left.left.value);
        assertEquals("B", n.left.left.left.right.value);

        baseScore += 5;
        writeFile("testAdd_result", "passed");
    }

    @Test
    public void testFind() throws Exception {
        Files.deleteIfExists(Path.of("testFind_result"));

        Dictionary dictionary = new BasicDictionary();
        String dictionaryPath = "dict_14.pre";
        dictionary.load(dictionaryPath);

        checkWord(dictionary, dictionaryPath, "cotton", null);
        checkWord(dictionary, dictionaryPath, "CottoN", null);
        checkWord(dictionary, dictionaryPath, "Cotto", new String[]{"bull", "cotton"});
        checkWord(dictionary, dictionaryPath, "mit", new String[]{"life", "mite"});
        checkWord(dictionary, dictionaryPath, "mite", null);
        checkWord(dictionary, dictionaryPath, "just", null);

        baseScore += 8;
        writeFile("testFind_result", "passed");
    }


    private void checkWord(Dictionary dictionary, String dictionaryPath, String word,
                           String[] expectedResult) {
        String[] result = dictionary.find(word);
        if (expectedResult != null) {
            if (result != null) {
                assertEquals(expectedResult[0], result[0]);
                assertEquals(expectedResult[1], result[1]);
            } else
                fail("The dictionary indicated that it found " + word
                        + ", but it should not have been found.");
        } else {
            if (result != null) {
                fail("The dictionary returned "
                        + (result.length > 0 ? result[0] : "an empty array")
                        + " but should have returned null because " + word
                        + " does exist in " + dictionaryPath);
            }
        }

    }


    @Test
    public void testLoadDocument() throws Exception {
        String dictionaryText = readFile("small_dictionary.txt");
        String[] words = dictionaryText.split(newline);
        Random rng = new Random();
        String doc = words[rng.nextInt(words.length)].trim() + " "
                + words[rng.nextInt(words.length)].trim() + " "
                + words[rng.nextInt(words.length)].trim() + " "
                + words[rng.nextInt(words.length)].trim() + " "
                + words[rng.nextInt(words.length)].trim();
        writeFile("random_doc.txt", doc);
        SpellChecker basicSpellChecker = new BasicSpellChecker();
        basicSpellChecker.loadDocument("random_doc.txt");
        String text = basicSpellChecker.getText();
        assertEquals(doc, text);

        baseScore += 2;
    }


    @Test
    public void testSpellCheckWithOneUnknownWord() throws Exception {
        Files.deleteIfExists(Path.of("testSpellCheckWithOneUnknownWord_result"));

        SpellChecker basicSpellChecker = new BasicSpellChecker();

        String dictionaryImportPath = "small_dictionary.txt";
        String dictionaryPath = "small_dictionary.pre";
        String documentPath = "small_document_one_unknown.txt";

        basicSpellChecker.importDictionary(dictionaryImportPath);
        basicSpellChecker.saveDictionary(dictionaryPath);

        basicSpellChecker.loadDocument(documentPath);

        String[] result;

        result = basicSpellChecker.spellCheck(false);
        if (result == null)
            fail("There should be one unknown word in " + documentPath
                    + " when the dictionary is "
                    + dictionaryImportPath);
        else {
            assertEquals("explosins", result[0]);
            assertEquals("87", result[1]);
            assertEquals("ever", result[2]);
            assertEquals("explosions", result[3]);
        }

        baseScore += 6;
        writeFile("testSpellCheckWithOneUnknownWord_result", "passed");
    }


    @Test
    public void testSpellCheckReplaceOneUnknownWord() throws Exception {
        Files.deleteIfExists(Path.of("testSpellCheckReplaceOneUnknownWord_result"));

        SpellChecker basicSpellChecker = new BasicSpellChecker();

        String dictionaryImportPath = "small_dictionary.txt";
        String dictionaryPath = "small_dictionary.pre";
        String documentPath = "small_document_one_unknown.txt";

        basicSpellChecker.importDictionary(dictionaryImportPath);
        basicSpellChecker.saveDictionary(dictionaryPath);

        basicSpellChecker.loadDocument(documentPath);

        String[] result;

        // Spell-check and find one word misspelled.
        result = basicSpellChecker.spellCheck(false);
        if (result == null)
            fail("There should be one unknown word in " + documentPath
                    + " when the dictionary is "
                    + dictionaryImportPath);
        else {
            assertEquals("explosins", result[0]);
            assertEquals("87", result[1]);
            assertEquals("ever", result[2]);
            assertEquals("explosions", result[3]);
        }

        // Replace it with the second suggestion.
        int startIndex = Integer.parseInt(result[1]);
        int endIndex = startIndex + result[0].length();
        basicSpellChecker.replaceText(startIndex, endIndex, result[3]);

        // Check against corrected.
        String text = basicSpellChecker.getText();
        String expected = readFile("small_document_one_unknown_corrected.txt");
        assertEquals(expected, text);

        baseScore += 6;
        writeFile("testSpellCheckReplaceOneUnknownWord_result", "passed");

    }


    @Test
    public void testSpellCheckNoUnknownWords() throws Exception {
        SpellChecker basicSpellChecker = new BasicSpellChecker();

        String dictionaryImportPath = "small_dictionary.txt";
        String dictionaryPath = "small_dictionary.pre";
        String documentPath = "small_document.txt";

        basicSpellChecker.importDictionary(dictionaryImportPath);
        basicSpellChecker.saveDictionary(dictionaryPath);

        basicSpellChecker.loadDocument(documentPath);

        String[] result;

        result = basicSpellChecker.spellCheck(false);
        if (result != null)
            fail("There should be no unknown words in " + documentPath
                    + " when the dictionary is " + dictionaryPath);

        baseScore += 4;
    }


    @Test
    public void testSpellCheckReplaceUnknowns() throws Exception {
        Files.deleteIfExists(Path.of("testSpellCheckReplaceUnknowns_result"));

        SpellChecker basicSpellChecker = new BasicSpellChecker();

        String dictionaryImportPath = "small_dictionary.txt";
        String dictionaryPath = "small_dictionary.pre";
        String documentPath = "small_document_four_unknown.txt";

        basicSpellChecker.importDictionary(dictionaryImportPath);
        basicSpellChecker.saveDictionary(dictionaryPath);

        basicSpellChecker.loadDocument(documentPath);

        String[] result;

        // Find first unknown
        result = basicSpellChecker.spellCheck(false);
        if (result == null)
            fail("Failed to find the first unknown word in " + documentPath
                    + " when the dictionary is "
                    + dictionaryImportPath);
        else {
            assertEquals("explosins", result[0]);
            assertEquals("87", result[1]);
            assertEquals("ever", result[2]);
            assertEquals("explosions", result[3]);
        }

        // Replace it with the successor word
        int startIndex = Integer.parseInt(result[1]);
        int endIndex = startIndex + result[0].length();
        basicSpellChecker.replaceText(startIndex, endIndex, result[3]);

        // find the 2nd unknown (the word "which")
        result = basicSpellChecker.spellCheck(true);
        if (result == null)
            fail("Failed to find the second unknown word in " + documentPath
                    + " when the dictionary is "
                    + dictionaryImportPath);
        else {
            assertEquals("which", result[0]);
            assertEquals("130", result[1]);
            assertEquals("use", result[2]);
            assertEquals("with", result[3]);
        }

        // Add this word to the dictionary
        String wordToAdd = result[0];
        basicSpellChecker.addWordToDictionary(result[0]);

        // find the 3rd unknown (the word "vast")
        result = basicSpellChecker.spellCheck(true);
        if (result == null)
            fail("Failed to find the third unknown word in " + documentPath
                    + " when the dictionary is "
                    + dictionaryImportPath);
        else {
            assertEquals("vast", result[0]);
            assertEquals("275", result[1]);
            assertEquals("use", result[2]);
            assertEquals("which", result[3]);
        }

        // Find third unknown
        result = basicSpellChecker.spellCheck(true);
        if (result == null)
            fail("Failed to find the fourth unknown word in " + documentPath
                    + " when the dictionary is "
                    + dictionaryImportPath);
        else {
            assertEquals("cuosmos", result[0]);
            assertEquals("280", result[1]);
            assertEquals("cosmos", result[2]);
            assertEquals("dozen", result[3]);
        }

        // Replace it with the predecessor word
        startIndex = Integer.parseInt(result[1]);
        endIndex = startIndex + result[0].length();
        basicSpellChecker.replaceText(startIndex, endIndex, result[2]);

        // Verify document is correct
        String expectedText = readFile("small_document_four_unknown_corrected.txt");
        String actualText = basicSpellChecker.getText();
        assertEquals(expectedText, actualText);

        // Verify the saved document is correct
        basicSpellChecker
                .saveDocument("small_document_four_unknown_after_spellchecking.txt");
        actualText = readFile("small_document_four_unknown_after_spellchecking.txt");
        assertEquals(expectedText, actualText);

        // Verify the dictionary is correct
        basicSpellChecker.saveDictionary("small_dictionary_after_spellchecking.pre");
        String dictText = readFile("small_dictionary_after_spellchecking.pre");

        if (!dictText.contains(wordToAdd))
            fail("Dictionary file didn't contain " + wordToAdd + ".");

        baseScore += 4;
        writeFile("testSpellCheckReplaceUnknowns_result", "passed");
    }


    @Test
    public void testSpellCheckNoSuccessor() throws Exception {

        SpellChecker basicSpellChecker = new BasicSpellChecker();
        String dictionaryImportPath = "small_dictionary.txt";
        String dictionaryPath = "small_dictionary.pre";
        String documentPath = "small_document_test_no_successor.txt";

        basicSpellChecker.importDictionary(dictionaryImportPath);
        basicSpellChecker.saveDictionary(dictionaryPath);

        basicSpellChecker.loadDocument(documentPath);

        String[] result;

        // Find first unknown
        result = basicSpellChecker.spellCheck(false);
        if (result == null)
            fail("Failed to find the first unknown word in " + documentPath
                    + " when the dictionary is "
                    + dictionaryImportPath);
        else {
            assertEquals("zebras", result[0]);
            assertEquals("87", result[1]);
            assertEquals("with", result[2]);
            assertEquals("", result[3]);
        }

        baseScore += 2;
    }


    @Test
    public void testPmd() {

        // Try to ensure that PMD can run on Mac/linux
        try {
            var f = new File("./pmd_min/bin/run.sh");
            if (!f.canExecute())
                f.setExecutable(true);
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        try {
            execPmd("src" + File.separator + "cs106", "cs106.ruleset");
        } catch (Exception ex) {
            fail(ex.getMessage());
        }

        baseScore += 6;

    }

    @Test
    public void testUsesTemplateProjectCorrectly()
            throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

        verifyCoreVersion();
        long count = Files.find(Paths.get("lib"), Integer.MAX_VALUE,
                (path, basicFileAttributes) -> path.toFile().getName().matches("sbcccore.*.*.*.jar")).count();
        assertTrue(
                "This project doesn't appear to be based on a copy of JavaCoreTemplate because the sbcccore library was not found in the lib directory.  See https://github.com/ProfessorStrenn/JavaCoreTemplate#java-core-template---quickstart for help on using the JavaCoreTemplate.",
                count > 0);
        baseScore += 2;

    }

    private void verifyCoreVersion()
            throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        try {
            var method = Core.class.getMethod("getSbccCoreVersion");

            String ver = (String) method.invoke(null);
            var parts = ver.split("\\.");
            if (parseInt(parts[0]) < 1 || parseInt(parts[1]) < 0 || parseInt(parts[2]) < 10)
                throw new RangeException((short) 0, "sbcccore version is " + ver + ", but must be at least 1.0.10");

        } catch (RangeException | NoSuchMethodException e) {
            isZeroScore = true;
            scoreNotes = "RESUBMISSION REQUIRED :  ************  See JUnit test results for help  ************";
            fail("RESUBMISSION REQUIRED.  This project appears to be based on a previous semester's project template.  See https://github.com/ProfessorStrenn/JavaCoreTemplate#java-core-template---quickstart for help on using the JavaCoreTemplate.");
        }
    }

    private static void execPmd(String srcFolder, String rulePath) throws Exception {

        File srcDir = new File(srcFolder);
        File ruleFile = new File(rulePath);

        verifySrcAndRulesExist(srcDir, ruleFile);

        ProcessBuilder pb;
        if (getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
            String pmdBatPath = ".\\pmd_min\\bin\\pmd.bat";
            String curPath = Paths.get(".").toAbsolutePath().toString();

            // Handle CS lab situation where the current dir is a UNC path
            if (curPath.startsWith("\\\\NEBULA\\cloud$")) {
                curPath = "N:\\" + substringAfter(curPath, "cloud$\\");
                pmdBatPath = curPath + pmdBatPath.substring(1);
            }
            pb = new ProcessBuilder(
                    pmdBatPath,
                    "-f", "text",
                    "-d", srcDir.getAbsolutePath(),
                    "-R", ruleFile.getAbsolutePath());
        } else {
            pb = new ProcessBuilder(
                    "./pmd_min/bin/run.sh", "pmd",
                    "-d", srcDir.getAbsolutePath(),
                    "-R", ruleFile.getAbsolutePath());
        }
        Process process = pb.start();
        int errCode = process.waitFor();

        switch (errCode) {

            case 1:
                String errorOutput = getOutput(process.getErrorStream());
                fail("Command Error:  " + errorOutput);
                break;

            case 4:
                String output = trimFullClassPaths(getOutput(process.getInputStream()));
                var lines = output.split(lineSeparator());
                var allMain = Arrays.stream(lines).allMatch(line -> line.contains("Main.java"));
                if (allMain)
                    return; // If all PMD messages refer to Main.java, it's not a failure.
                else {
                    output = checkCyclomaticComplexityHigh(output);
                    fail(output);
                }
                break;

        }

    }

    private static String checkCyclomaticComplexityHigh(String output) {
        if (output.contains("has a Standard Cyclomatic Complexity of")) {
            var hint = lineSeparator()
                    + "🧐 ⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
                    + lineSeparator()
                    + lineSeparator() + "\uD83D\uDCA1  To reduce the complexity of your method, consider extracting branching statements like "
                    + lineSeparator() + "    if's, switch's, or loops into a separate method.  NOTE: make a backup of your project first \uD83D\uDE05"
                    + lineSeparator() + "    See https://blog.jetbrains.com/idea/2020/12/3-ways-to-refactor-your-code-in-intellij-idea/."
                    + lineSeparator()
                    + lineSeparator() + "    Alternatively, check to see if your branching statements can be modified to reduce the number of branches.  "
                    + lineSeparator() + "    See https://www.youtube.com/watch?v=Mz-dAHpRung."
                    + lineSeparator();
            output += hint;
        }
        return output;
    }


    private static String trimFullClassPaths(String output) {
        // Shorten output to just the short class name, line, and error.
        String[] lines = output.split(getProperty("line.separator"));
        StringBuilder sb = new StringBuilder();
        for (String line : lines)
            sb.append(substringAfterLast(line, File.separator)).append(lineSeparator());

        return sb.toString();
    }


    private static void verifySrcAndRulesExist(File fileFolderToCheck, File ruleFile)
            throws Exception {
        if (!fileFolderToCheck.exists())
            throw new FileNotFoundException(
                    "The folder to check '" + fileFolderToCheck.getAbsolutePath()
                            + "' does not exist.");

        if (!fileFolderToCheck.isDirectory())
            throw new FileNotFoundException(
                    "The folder to check '" + fileFolderToCheck.getAbsolutePath()
                            + "' is not a directory.");

        if (!ruleFile.exists())
            throw new FileNotFoundException(
                    "The rule set file '" + ruleFile.getAbsolutePath()
                            + "' could not be found.");
    }


    private static String getOutput(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(getProperty("line.separator"));
            }
        } finally {
            if (br != null) br.close();
        }
        return sb.toString();

    }

}
