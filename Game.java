import java.util.*;
import java.io.*;


/**
 * class to run Kevin Bacon game
 * @author William Toth and Christopher Long
 */

public class Game {
    private Graph<String, Set<String>> graph;  //instance variable for main graph
    private String center;  //center of the universe
    private Boolean gameRunning = true; //boolean for whether or not game is running
    private Map<String, String> aMap; //map of actor ids to actors
    private Map<String, String> mMap; //map of movie id to movie name
    private Map<String, Set<String>> mToA; //map of movies to a set of actors

    public Game (String actorFile, String movieFile, String actorToMovie) {
        //create graph from file data
        graphCreation(actorFile, movieFile, actorToMovie);
    }

    /**
     * Method to create graph
     * @param pathA
     * @param pathM
     * @param pathMtoA
     */
    public void graphCreation(String pathA, String pathM, String pathMtoA) {
        graph = new AdjacencyMapGraph<>();
        aMap = new HashMap<>();
        mMap = new HashMap<>();
        mToA = new HashMap<>();
        //create map from actor IDs to names
        mapCreation(aMap, pathA);
        //create map from movie IDs to movies
        mapCreation(mMap, pathM);
        //create map from movie to set of actors
        movieActorMapCreation(mToA, pathMtoA);
        //make a vertex for each actor
        for (String actor : aMap.keySet()) {
            graph.insertVertex(aMap.get(actor));
        }
        //loop through every movie
        for (String movie : mToA.keySet()) {
            //loop through each actor in a particular movie
            for (String actor1 : mToA.get(movie)) {
                //loop through the same set of actors as the line above
                for (String actor2 : mToA.get(movie)) {
                    //if the two actors aren't the same
                    if (!actor1.equals(actor2)) {
                        //if there isn't already an edge between the two actor vertices
                        if (!graph.hasEdge(actor1, actor2)) {
                            HashSet<String> temp = new HashSet<>();
                            temp.add(movie);
                            graph.insertUndirected(actor1, actor2, temp);
                        } else {
                            //add to an existing edge label (a set)
                            graph.getLabel(actor1, actor2).add(movie);
                        }
                    }
                }
            }
        }
        center = "Kevin Bacon"; //Setting center to Kevin Bacon
    }

    /**
     * Method to create maps (aMap and mMap) that will serve as parameters for graphCreation
     * @param map
     * @param path
     */
    public void mapCreation(Map<String,String> map, String path) {
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            System.err.println("Cannot open file.\n" + e.getMessage());
            return;
        }
        try {
            String line;
            //continue till the line read from the file is null (EOF)
            while ((line = input.readLine()) != null) {
                //split the line into two strings based on where the vertical line
                String[] words = line.split("\\|");
                //add to map with key being the ID and value being the name/title
                map.put(words[0], words[1]);
            }
        } catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }
        try {
            input.close();
        } catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }
    }

    /**
     * Method to create a map (mToA) that will serve as a parameter for graphCreation
     * @param map
     * @param path
     */
    public void movieActorMapCreation(Map<String, Set<String>> map, String path) {
        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(path));
        } catch (FileNotFoundException e) {
            System.err.println("Cannot open file.\n" + e.getMessage());
            return;
        }
        try {
            String line;
            //continue till line read from the file is null (EOF)
            while ((line = input.readLine()) != null) {
                //split the line into two strings based on where the vertical line
                String[] words = line.split("\\|");
                //get names from maps based on IDs as keys
                String movie = mMap.get(words[0]);
                String actor = aMap.get(words[1]);
                //if a movie title hasn't been added to the map
                if (!map.containsKey(movie)) {
                    HashSet<String> temp = new HashSet<>();
                    temp.add(actor);
                    map.put(movie, temp);
                } else {
                    //add to an existing set of actor names
                    map.get(movie).add(actor);
                }
            }
        } catch (IOException e) {
            System.err.println("IO error while reading.\n" + e.getMessage());
        }
        try {
            input.close();
        } catch (IOException e) {
            System.err.println("Cannot close file.\n" + e.getMessage());
        }
    }



    /**
     * method to handle key being pressed
     * @param k
     */
    public void handleKeyPress (char k) {
        if (k == 'q') { //if key is 'q'
            gameRunning = false; //change boolean for whether or not game is running to false
            System.out.println("Game Over");
        }

        //changes center of the universe
        else if (k == 'u') {
            System.out.println("Who is the new center of the universe:");
            //takes user input
            Scanner in = new Scanner (System.in);
            String input = in.nextLine();

            if (graph.hasVertex(input)) { // checks if this person is in the universe
                center = input; //changes center to person
                //print info for new center of the universe
                System.out.println(center + " is now the center of the acting universe, connected to " +
                        (GraphLibrary.bfs(graph, center).numVertices()-1) + "/" +  graph.numVertices()+ " actors with average separation " +
                        GraphLibrary.averageSeparation(GraphLibrary.bfs(graph, center), center));
            }
            else System.out.println(input + " is not in the universe");
        }

        //computes path to center from person
        else if (k == 'p') {
            System.out.println("Enter person to create path from:");
            //user input
            Scanner in = new Scanner (System.in);
            String input = in.nextLine();

            //checks to see if this person is not in the universe
            if (!graph.hasVertex(input)) {
                System.out.println (input + " is not in the universe");
            }

            else {
                if (!GraphLibrary.bfs(graph, center).hasVertex(input)) { //checks if person is not connected to center
                    System.out.println(input + "'s number is infinity.");
                }
                else { //if connected to center
                    List<String> path = GraphLibrary.getPath (GraphLibrary.bfs(graph, center), input); //gets path to center
                    System.out.println(input + "'s number is " + (path.size() - 1)); //prints distance number from center
                    while (path.size() > 1) { //while loop to print path links and movies that connect them (path length is decremented each time)
                        String person1 = path.get(path.size()-1);
                        String person2 = path.get(path.size()-2);
                        System.out.println(person1 + " appeared in " + graph.getLabel(person1,person2) + " with " + person2);
                        path.remove(path.size()-1); //gets rid of last person in path
                    }
                }
            }
        }

        //prints actors that have infinite separation from center
        else if (k == 'i') {
            System.out.println("Actors with infinite separation from " + center + ": " +
                    GraphLibrary.missingVertices (graph, GraphLibrary.bfs(graph, center))); //calls missing vertices function from GraphLibrary
        }

        //Lists actors sorted by non infinite separation between low and high separation
        else if (k == 's') {
            try {
                System.out.println("Low:"); //Low separation
                Scanner in = new Scanner(System.in);
                int low = Integer.parseInt(in.nextLine());

                System.out.println("High:"); //High Separation
                in = new Scanner(System.in);
                int high = Integer.parseInt(in.nextLine());

                ArrayList<String> actorsInRange = new ArrayList<String>(); //List for actors with separation between low and high
                Graph<String, Set<String>> centerTree = GraphLibrary.bfs(graph, center); //graph tree for the center of the universe
                for (String vert : graph.vertices()) { //loop through vertices
                    List<String> path = GraphLibrary.getPath(centerTree, vert);
                    //if vertex has separation between low and high, add to list
                    if (path.size() >= low + 1 && path.size() <= high + 1) {
                        actorsInRange.add(vert);
                    }
                }

                //Nested Comparator class that will be used to sort this list
                class SeparationComparator implements Comparator<String> {
                    public int compare(String p1, String p2) { //takes in 2 pple
                        //get their paths
                        List<String> path1 = GraphLibrary.getPath(centerTree, p1);
                        List<String> path2 = GraphLibrary.getPath(centerTree, p2);
                        //compare path lengths
                        if (path1.size() < path2.size()) return -1;
                        else if (path1.size() == path2.size()) return 0;
                        else return 1;
                    }
                }

                //sort list with comparator
                actorsInRange.sort(new SeparationComparator());

                System.out.println(actorsInRange);
            }
            catch (NumberFormatException e) {
                System.err.println("Not an integer.\n" + e.getMessage());
            }
        }

        //Lists actors sorted by degree
        else if (k == 'd') {
            try {
                System.out.println("Low:"); //Low degree
                //user input
                Scanner in = new Scanner(System.in);
                int low = Integer.parseInt(in.nextLine());

                System.out.println("High:"); //High degree
                //user input
                in = new Scanner(System.in);
                int high = Integer.parseInt(in.nextLine());

                if (high < low) System.out.println("High must be greater than low."); //checks if high is less than low
                else {
                    ArrayList<String> actorsByDegree = new ArrayList<String>(); //List of actors with degrees between low and high
                    for (String vert : graph.vertices()) { //loops though vertices
                        //if actor degress is in range, add to list
                        if (graph.outDegree(vert) >= low && graph.outDegree(vert) <= high)
                        actorsByDegree.add(vert);
                    }

                    //Nested Comparator class that will be used to sort this list
                    class DegreeComparator implements Comparator<String> {
                        public int compare(String p1, String p2) { //compares degrees
                            if (graph.outDegree(p1) < graph.outDegree(p2)) return -1;
                            else if (graph.outDegree(p1) == graph.outDegree(p2)) return 0;
                            else return 1;
                        }
                    }

                    actorsByDegree.sort(new DegreeComparator()); //sorts list

                    System.out.println(actorsByDegree);
                }
            }
            catch (NumberFormatException e) {
                System.err.println("Not an integer.\n" + e.getMessage());
            }
        }

        //Lists top or bottom (depending on sign of user input) centers of the universe, sorted by average separation
        else if (k == 'c') {
            System.out.println("This may take up to a minute. Enter valid integer when program says 'Go:'");

            //Creating a map to store actors and their respective avg separation as centers of the universe
            //This allows for much faster run time (< 1 min and only runs bfs once for each vertex)
            //than doing it in the comparator, so sorting is much faster
            Map actorsToSep = new HashMap<String,Double> ();

            //Instantiating list of actors to be sorted by AvgSep
            ArrayList<String> actorsByAvgSep =  new ArrayList<String> ();

            for (String vert : graph.vertices()) { //Loop through vertices
                double avgSep = GraphLibrary.averageSeparation(GraphLibrary.bfs(graph,vert), vert); //get average separation
                actorsToSep.put (vert, avgSep); //insert actor name and avg separation into map
                actorsByAvgSep.add (vert); //add vertex to list
            }

            //Nested Comparator class that will be used to sort the list
            class AvgSepComparator implements Comparator<String> {
                public int compare(String p1, String p2) { //compares average separation for the string inputs
                    double p1Sep = (double) actorsToSep.get(p1); //gets average separation from map
                    double p2Sep = (double) actorsToSep.get(p2); //gets average separation from map
                    if (p1Sep < p2Sep) return -1;
                    else if (p1Sep == p2Sep) return 0;
                    else return 1;
                }
            }
            actorsByAvgSep.sort(new AvgSepComparator()); //Sorts list

            System.out.println("Go:"); //Go statement so user knows the program is ready for input

            try {

                Scanner in = new Scanner(System.in);
                int num = Integer.parseInt(in.nextLine());
                //prints error if the magnitude of the input is larger than the list size
                if (Math.abs(num) > actorsByAvgSep.size()) System.err.println("Magnitude larger than list size.");
                else {
                    ArrayList<String> smallList = new ArrayList<String> (); //small list of items to be printed
                    if (num < 0) { //if user entered a negative number, return the absolute value of that number of items from front of the list
                        smallList.addAll(actorsByAvgSep.subList(0,num*(-1)));
                    }
                    if (num > 0) { //if user entered a positive number, return that number of items from end of the list
                        smallList.addAll(actorsByAvgSep.subList(actorsByAvgSep.size()-num, actorsByAvgSep.size()));
                    }
                    System.out.println(smallList);
                }

            }
            catch (NumberFormatException e){ System.err.println("Not an integer.\n" + e.getMessage()); }
        }
    }


    public static void main(String[] args){

        //instantiating game
        Game game = new Game ("PS4/actors.txt", "PS4/movies.txt", "PS4/movie-actors.txt");

        //printing game info
        System.out.println("\nCommands (enter command letter and hit enter before typing in command parameters):");
        System.out.println("c <#>: list top (positive number) or bottom (negative) <#> centers of the universe, sorted by average separation");
        System.out.println("d <low> <high>: list actors sorted by degree, with degree between low and high");
        System.out.println("i: list actors with infinite separation from the current center");
        System.out.println("p <name>: find path from <name> to current center of the universe");
        System.out.println("s <low> <high>: list actors sorted by non-infinite separation from the current center, with separation between low and high");
        System.out.println("u <name>: make <name> the center of the universe");
        System.out.println("q: quit game\n");

        //prints center of the universe
        System.out.println(game.center + " is now the center of the acting universe, connected to " +
                (GraphLibrary.bfs(game.graph, game.center).numVertices()-1) + "/" +  game.graph.numVertices()+ " actors with average separation " +
                GraphLibrary.averageSeparation(GraphLibrary.bfs(game.graph, game.center), game.center));

        //while loop to see if game is running (if user has not pressed q yet)
        while (game.gameRunning) {
            //try catch block for reading ket pressed by user
            try {
                System.out.print("\n" + game.center + " game >\n"); //Stating what game it is (who is center of universe)
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                char input = (char) reader.read(); //cast to character
                game.handleKeyPress(input); //calls handleKeyPress function
            }
            catch (IOException e) { System.err.println(e.getMessage()); } //reader error
        }

    }

}
