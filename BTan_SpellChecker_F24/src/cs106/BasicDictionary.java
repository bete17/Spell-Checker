package cs106;

import java.io.*;
import java.util.*;

import static sbcc.Core.*;
import static org.apache.commons.lang3.StringUtils.*;

public class BasicDictionary implements Dictionary {
    /**
     * Instance Variable
     */
    private int count = 0;
    private BinaryTreeNode root;
    private final StringBuilder preOrderList = new StringBuilder();


    @Override
    /**
     * Take In Dictionary
     */
    public void importFile(String filename) throws Exception {
        List<String> lines = readFileAsLines(filename);

        // Eliminate duplicates (not necessary as there is no duplicates)
        Set<String> uniqueWords = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (String word : lines) {
            uniqueWords.add(word.trim());
        }

        List<String> sortedWords = new ArrayList<>(uniqueWords);
        insertRecursively(sortedWords, 0, sortedWords.size() - 1);
    }

    private void insertRecursively(List<String> words, int start, int end) {
        if (start > end) return;

        int mid = (start + end) / 2;
        add(words.get(mid)); // your original add()

        insertRecursively(words, start, mid - 1); // Left
        insertRecursively(words, mid + 1, end);   // Right
    }

    @Override
    public void load(String filename) throws Exception {
        var words = readFileAsLines(filename);//1 word per line
        for (var word : words) {
            add(word.trim());
        }
    }


    @Override
    public void save(String filename) throws Exception {
        if (root == null) {//do nothing if it's empty
            return;
        }
        writeFile(filename, preOrderList.toString());
    }

    @Override
    public String[] find(String word) {
        var current = root; //starting point
        String[] wordSuggestion = {"", ""};
        while (current != null) {//if the tree is filled
            var comparison = word.compareToIgnoreCase(current.value);//word compare to the current node string
            if (comparison == 0) {//if the word is in the dictionary
                return null;
            } else if (comparison < 0) {//if the word comes before
                wordSuggestion[1] = current.value;
                current = current.left;
            } else {//if the word comes after
                wordSuggestion[0] = current.value;
                current = current.right;
            }
        }
        return wordSuggestion;
    }

    @Override
    public void add(String word) {
        var nn = new BinaryTreeNode(word);
        var current = root;//another pointer to the root
        var done = false;
        if (root == null) {//if tree is empty
            root = nn;//add the node
            preOrderList.append(word).append('\n');
            count++;
        } else {//tree not empty
            while (!done) {
                if (current.value.compareToIgnoreCase(nn.value) < 0) {//if the current node comes before new node
                    if (current.right != null) {//if there's a node move it to the right
                        current = current.right;
                    } else {//add the value to the left
                        current.right = nn;
                        preOrderList.append(word).append('\n');
                        current = root;
                        done = true;
                        count++;
                    }
                } else if (current.value.compareToIgnoreCase(nn.value) > 0) {//if the new node is less than current
                    if (current.left != null) {
                        current = current.left;
                    } else {//add the value to the left
                        current.left = nn;
                        current = root;
                        preOrderList.append(word).append('\n');
                        done = true;
                        count++;
                    }

                } else {//if it's a duplicate because the other only case is ==0
                    return;
                }
            }
        }

    }

    @Override
    public BinaryTreeNode getRoot() {
        return root;
    }

    @Override
    public int getCount() {
        return count;
    }
}
