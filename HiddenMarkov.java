import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * HiddenMarkov class to guess parts of speech of a string of words
 * @author William Toth and Chris Long
 */

public class HiddenMarkov {
    private Map<String,Double> currScores = new HashMap<String,Double>();
    private Set<String> currStates = new HashSet<String>();
    Map<String,Map<String,Double>> wordPosProb; //Map of words to a map of their POS to probabilities of that POS
    Map<String,Map<String,Double>> transitionMap; //Map of POS to map of pos they can transition to and the probabilities of these transitions

    /**
     * Viterbi algorithm (guesses POS for a string and returns an ArrayList of these POS)
     * @param words
     * @return
     */
    public List<String> viterbi (String words) {
        List<Map<String,String>> backtrack = new ArrayList<Map<String,String>> ();
        currScores = new HashMap<String,Double>();
        currStates = new HashSet<String>();
        String wordList[] = words.split(" "); //turn text into a list of words
        currScores.put("#", (double) 0); //add the # and score of 0
        currStates.add("#"); //add #
        for (int i = 0; i < wordList.length; i++) { //loop through text array
            Map<String,String> backpointers = new HashMap<String,String>();
            Set<String> nextStates = new HashSet<String>(); //set of next states
            Map<String,Double> nextScores = new HashMap<String,Double>(); //map of these next states to their scores
            for (String currState : currStates) {
                for (String nextState : transitionMap.get(currState).keySet()) { //loop through POS that currState transitions to
                    nextStates.add(nextState); //add this POS to next states
                    double score;
                    if (!wordPosProb.containsKey(wordList[i])) score = -1000;
                    else {
                        if (wordPosProb.get(wordList[i]).get(nextState) == null) score = -1000;
                        else score = wordPosProb.get(wordList[i]).get(nextState);
                    }
                    double nextScore = currScores.get(currState) + //probability of current state
                            transitionMap.get(currState).get(nextState) + //transition probability
                            score; //probability of next state
                    if (!nextScores.keySet().contains(nextState)) { //if next state POS isn't in next scores
                        nextScores.put(nextState, nextScore); //add next state POS with nextScore we just calculated
                        backpointers.put(nextState, currState); //add backpointer in map
                    } else if (nextScore > nextScores.get(nextState)) {
                        nextScores.replace(nextState, nextScore); //replace next state POS with nextScore we just calculated
                        backpointers.replace(nextState,currState); //replace backpointer in map
                    }
                }
            }
            backtrack.add(backpointers); //add backpointer map to list

            //make current next for next iteration of for loop
            currStates = nextStates;
            currScores = nextScores;
        }
        //section of code that creates predicted parts of speech from backtrack
        int count = backtrack.size()-1;
        //make ArrayList to return
        ArrayList<String> parts = new ArrayList<>();
        //set current max key to the first key in the map
        String maxPart = backtrack.get(count).keySet().iterator().next();
        //set current max score to the value of the first key
        double max = currScores.get(maxPart);
        //loop through each key in backtrack to compare scores
        for (String state : backtrack.get(count).keySet()) {
            double temp = currScores.get(state);
            //if a score is greater than the current max is found change the maxPart and max score
            if (temp > max) {
                maxPart = state;
                max = temp;
            }
        }
        //add the last part of speech for the sentence
        parts.add(maxPart);
        String state = maxPart;
        //loop until through the size of the backtrack list
        while (count > 0) {
            //add current part of speech that points to the next part of speech in the sentence
            parts.add(0, backtrack.get(count).get(state));
            //change the map to the prior one in the list
            state = backtrack.get(count).get(maxPart);
            count--;
        }
        //return the list of parts of speech
        return parts;
    }

    /**
     * method to train observation map
     * @param text
     * @param posString
     */
    public void trainObservations (String text, String posString) {

        wordPosProb = new HashMap<String, Map<String,Double>> (); //instantiate

        Map<String,Integer> posCount = new HashMap<String,Integer> (); //map for # of occurences of each POS

        String wordArray[] = text.split(" "); //split text whenever there is a space
        String posArray[] = posString.split(" "); //same for POS

        //add POS to counts map
        for (int i = 0; i < wordArray.length; i++) { //loop through number of words in text
            if (!posCount.containsKey (posArray[i])) posCount.put (posArray[i], 1); //If posCount deosn't have POS, add it
            else posCount.replace (posArray[i], posCount.get(posArray[i])+1); //If it does, increment it
            if (!wordPosProb.containsKey(wordArray[i])) { //If map of maps contains it
                Map<String, Double> wordMap = new HashMap<String, Double>(); //make a new hashmap for this pos
                wordMap.put(posArray[i], (double) 1); //put POS in map
                wordPosProb.put(wordArray[i], wordMap); //put the map just created as the value for the word
            } else {
                Map<String, Double> wordMap = wordPosProb.get(wordArray[i]); //retrieve POS map
                if (!wordMap.containsKey(posArray[i])) { //If map deosn't contain POS
                    wordMap.put(posArray[i], (double) 1); //add POS and count of 1
                } else {
                    wordMap.replace(posArray[i], wordMap.get(posArray[i]) + 1); //increment count
                }
            }
        }

        for (String word : wordPosProb.keySet()) { //loop through words
            for (String pos : wordPosProb.get(word).keySet()) { //loop through POS that have those words
                //convert count to log probability
                wordPosProb.get(word).replace (pos, Math.log(wordPosProb.get(word).get(pos) / posCount.get(pos)));
            }
        }

    }

    /**
     * Method to train transition map
     * @param posString
     */
    public void trainTransitions (String posString) {
        transitionMap = new HashMap<String,Map<String,Double>> (); //map of transition probabilities

        String posWithStart = "# " + posString;

        String posArray[] = posWithStart.split(" "); //split text whenever there is a space

        //Add POS to map
        for (int i = 0; i < posArray.length; i++) { //loop through POS
            if (!transitionMap.containsKey(posArray[i])) transitionMap.put (posArray[i], new HashMap<String,Double> ());
        }

        //Add next POS counts to map (to be converted to probabilities later)
        for (int i = 0; i < posArray.length-1; i++) { //loop through POS indexes that have another POS after
            if (!transitionMap.get(posArray[i]).containsKey(posArray[i+1])) { //If a transition isn't in map
                transitionMap.get(posArray[i]).put(posArray[i+1], (double)1); //add add next POS map to map
            } else { //If next POS map already exists
                //increment count

            }
        }
        //convert counts to probabilities
        for (String pos : transitionMap.keySet()) { //loop though POS
            int numTrans = 0; //see how many times that POS has a POS after it

            for (String nextPOS : transitionMap.get(pos).keySet()) { //loop through POS neighbors
                numTrans += transitionMap.get(pos).get(nextPOS); //add the counts of those to to numTrans
            }
            for (String nextPOS : transitionMap.get(pos).keySet()) { //loop through POS neighbors again
                transitionMap.get(pos).replace(nextPOS, Math.log(transitionMap.get(pos).get(nextPOS) / numTrans)); //replace count with log probability
            }
        }
    }

    /**
     * Method to test our modeling on two files, giving a percent correctness
     * @param testFile
     * @param posFile
     */
    public void fileBasedTest (String testFile, String posFile) {

        String testString = readFiles(testFile); //string for test file
        String posString = readFiles(posFile); //POS string to compare it to

        int total = 0; //total words
        int correct = 0; //correct POS

        String posList[] = posString.split(" "); //Split into array

        List<String> testGuess = viterbi(testString); //Viterbi returns a list of guessed POS based on the string

        for (int pos = 0; pos < posList.length; pos++) { //loop through indices
            total++; //increment total
            if (posList[pos].equals(testGuess.get(pos))) { //if parts of speech line up
                correct++; //increment correct
            }
        }

        double percentCorrect = ((double) correct/total)*100; //get a double for percent correct
        String stringPercentCorrect = String.valueOf(percentCorrect); //convert to string
        String roundedPercent = stringPercentCorrect.substring (0,5); //go to 2 decimal places using sub string
        System.out.println("This model was " + roundedPercent + "% correct");
    }

    /**
     * method to read files and return a single string
     * @param path
     * @return
     */
    public String readFiles(String path) {
        BufferedReader input;
        String full = "";
        try {
            input = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            System.err.println("Cannot open file.\n" + e.getMessage());
            return full;
        }
        try {
            String line;
            //continue till the line read from the file is null (EOF)
            while ((line = input.readLine()) != null) {
                //split the line into two strings based on where the vertical line
                full = full + line + " ";
            }
        } catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }
        try {
            input.close();
        } catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }
        return full;
    }

    /**
     * Runs console-based test to predict parts of speech from a input string
     */
    public void consoleTest() {
        System.out.println("\nConsole-Based Test Method (enter sentence or type 'q' on a new line to quit test):");
        Scanner in = new Scanner(System.in);
        String testInput = in.nextLine();

        while (testInput.length() == 0) {
            System.out.println("Invalid entry, please give a sentence");
            testInput = in.nextLine();
        }
        //read till a valid input of 'q' is given to quit the console-based test
        while (testInput.length() != 0 && testInput.charAt(0) != 'q') {
            System.out.println(this.viterbi(testInput));
            testInput = in.nextLine();
            while (testInput.length() == 0) {
                System.out.println("Invalid entry, please give a sentence or type 'q' to quit");
                testInput = in.nextLine();
            }
        }
        System.out.println("Console Test Exited");
    }

    public static void main(String[] args) {
        /*
        //hard coded test probability maps
        Map<String,Map<String,Double>> emission = new HashMap<>();
        Map<String,Map<String,Double>> transmission = new HashMap<>();

        //set up observation map
        emission.put("and", new HashMap<>());
        emission.put("cat", new HashMap<>());
        emission.put("chase", new HashMap<>());
        emission.put("dog", new HashMap<>());
        emission.put("get", new HashMap<>());
        emission.put("watch", new HashMap<>());

        //fill observation map
        emission.get("and").put("CNJ",1.0);
        emission.get("cat").put("CNJ",0.0);
        emission.get("chase").put("CNJ",0.0);
        emission.get("dog").put("CNJ",0.0);
        emission.get("get").put("CNJ",0.0);
        emission.get("watch").put("CNJ",0.0);

        emission.get("and").put("N",0.0);
        emission.get("cat").put("N",0.417);
        emission.get("chase").put("N",0.0);
        emission.get("dog").put("N",0.417);
        emission.get("get").put("N",0.0);
        emission.get("watch").put("N",0.167);

        emission.get("and").put("NP",0.0);
        emission.get("cat").put("NP",0.0);
        emission.get("chase").put("NP",1.0);
        emission.get("dog").put("NP",0.0);
        emission.get("get").put("NP",0.0);
        emission.get("watch").put("NP",0.0);

        emission.get("and").put("V",0.0);
        emission.get("cat").put("V",0.0);
        emission.get("chase").put("V",0.222);
        emission.get("dog").put("V",0.0);
        emission.get("get").put("V",0.111);
        emission.get("watch").put("V",0.667);

        //set up transition map
        transmission.put("#", new HashMap<>());
        transmission.put("CNJ", new HashMap<>());
        transmission.put("N", new HashMap<>());
        transmission.put("NP", new HashMap<>());
        transmission.put("V", new HashMap<>());

        //fill transition map
        transmission.get("#").put("CNJ",0.0);
        transmission.get("#").put("N",0.714);
        transmission.get("#").put("NP",0.286);
        transmission.get("#").put("V",0.0);

        transmission.get("CNJ").put("CNJ",0.0);
        transmission.get("CNJ").put("N",0.333);
        transmission.get("CNJ").put("NP",0.333);
        transmission.get("CNJ").put("V",0.333);

        transmission.get("N").put("CNJ",0.25);
        transmission.get("N").put("N",0.0);
        transmission.get("N").put("NP",0.0);
        transmission.get("N").put("V",0.75);

        transmission.get("NP").put("CNJ",0.0);
        transmission.get("NP").put("N",0.0);
        transmission.get("NP").put("NP",0.0);
        transmission.get("NP").put("V",1.0);

        transmission.get("V").put("CNJ",0.111);
        transmission.get("V").put("N",0.667);
        transmission.get("V").put("NP",0.222);
        transmission.get("V").put("V",0.0);

        //set instance variables equal to the hard coded maps for the test
        test1.wordPosProb = emission;
        test1.transitionMap = transmission;
        System.out.println("VITERBI TESTING");
        //make up sentence to give to Viterbi method
        String testSentence = "dog and cat watch";
        //print current test sentence
        System.out.println("Test Sentence Given to Viterbi: " + testSentence);
        //print projected parts of speech for the sentence
        System.out.println("Parts of Speech Prediction: " + test1.viterbi(testSentence) + "\n");
        testSentence = "chase and cat and dog get watch";
        System.out.println("Test Sentence Given to Viterbi: " + testSentence);
        System.out.println("Parts of Speech Prediction: " + test1.viterbi(testSentence) + "\n");
        testSentence = "cat chase dog";
        System.out.println("Test Sentence Given to Viterbi: " + testSentence);
        System.out.println("Parts of Speech Prediction: " + test1.viterbi(testSentence) + "\n");
        testSentence = "chase watch dog get watch";
        System.out.println("Test Sentence Given to Viterbi: " + testSentence);
        System.out.println("Parts of Speech Prediction: " + test1.viterbi(testSentence) + "\n");
        testSentence = "the dog get cat";
        System.out.println("Test Sentence Given to Viterbi: " + testSentence);
        System.out.println("Parts of Speech Prediction: " + test1.viterbi(testSentence));
        System.out.println("\nSimple Test with Simple Training");
        //train maps on Simple file
        */
        /*
        //output performance stat
        test1.fileBasedTest("PS5/simple-test-sentences.txt", "PS5/simple-test-tags.txt");
        System.out.println("\nBrown Test with Brown Training");
        //train maps on Brown Corpus file
        test1.trainObservations(test1.readFiles("PS5/brown-train-sentences.txt"), test1.readFiles("PS5/brown-train-tags.txt"));
        test1.trainTransitions(test1.readFiles("PS5/brown-train-tags.txt"));
        //output performance stat
        test1.fileBasedTest("PS5/brown-test-sentences.txt", "PS5/brown-test-tags.txt");
        System.out.println("\nSimple Test with Brown Training");
        //train maps on Brown Corpus file
        test1.trainObservations(test1.readFiles("PS5/brown-train-sentences.txt"), test1.readFiles("PS5/brown-train-tags.txt"));
        test1.trainTransitions(test1.readFiles("PS5/brown-train-tags.txt"));
        //output performance stat
        test1.fileBasedTest("PS5/simple-test-sentences.txt", "PS5/simple-test-tags.txt");
        //run console based test method
        */
        HiddenMarkov test1 = new HiddenMarkov();
        test1.trainObservations(test1.readFiles("PS5/brown-train-sentences.txt"), test1.readFiles("PS5/brown-train-tags.txt"));
        test1.trainTransitions(test1.readFiles("PS5/brown-train-tags.txt"));
        test1.consoleTest();

    }
}