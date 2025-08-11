package cs106;

import org.apache.commons.io.filefilter.*;

import java.util.*;
import java.util.regex.*;

import static sbcc.Core.*;

public class BasicSpellChecker implements SpellChecker {
    BasicDictionary dictionary = new BasicDictionary();
    String documentText = "";
    int lastIndex = 0;


    @Override
    public void importDictionary(String filename) throws Exception {
        dictionary.importFile(filename);
    }

    @Override
    public void loadDictionary(String filename) throws Exception {
        dictionary.load(filename);
    }

    @Override
    public void saveDictionary(String filename) throws Exception {
        dictionary.save(filename);
    }

    @Override
    public void loadDocument(String filename) throws Exception {
        documentText = readFile(filename);
    }

    @Override
    public void saveDocument(String filename) throws Exception {
        writeFile(filename, documentText);
    }

    @Override
    public String getText() {
        return documentText;
    }

    @Override
    public String[] spellCheck(boolean continueFromPrevious) {
        String regex = "\\b[\\w']+\\b";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(getText());
        String[] suggestion = {"", "", "", ""};
        if (!continueFromPrevious) {
            lastIndex = 0;
        }
        while (matcher.find(lastIndex)) {//iterating through the matched word

            String current_word = matcher.group().toLowerCase();
            int currentindex = matcher.start();
            int currentLastIndex = matcher.end();
            lastIndex = matcher.end();

            var dictionaryCheck = dictionary.find(current_word);
            if (dictionaryCheck != null) {//if it spots an unknown word
                suggestion[0] = current_word;//unknown word
                suggestion[1] = String.valueOf(currentindex);//current index
                suggestion[2] = dictionaryCheck[0] == null ? "" : dictionaryCheck[0];
                suggestion[3] = dictionaryCheck[1] == null ? "" : dictionaryCheck[1];//succeeding word
                lastIndex = currentLastIndex;//start of the next word index
                return suggestion;
            }
        }
        return null;//if the while loop exits without finding an uknown word
    }

    @Override
    public void addWordToDictionary(String word) {
        dictionary.add(word);
    }

    @Override
    public void replaceText(int startIndex, int endIndex, String replacementText) {


        // Normalize line endings in replacement text
        replacementText = replacementText.replace("\r\n", "\n");

        // Perform the replacement
        StringBuilder text = new StringBuilder(documentText);
        text.replace(startIndex, endIndex, replacementText);

        // Update the documentText
        documentText = text.toString(); // Ensure consistent line endings in the final document

        // Adjust lastIndex if it falls within or after the replaced range
        if (lastIndex >= startIndex) {
            int originalLength = endIndex - startIndex;
            int replacementLength = replacementText.length();
            lastIndex += (replacementLength - originalLength);
        }
    }
}
