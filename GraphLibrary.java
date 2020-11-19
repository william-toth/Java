import java.util.*;

/**
 * Static methods class for Kevin Bacon Game
 * @author William Toth and Christopher Long
 */

public class GraphLibrary {

    /**
     * Runs Breadth First Search on a given graph
     * @param g
     * @param source
     * @param <V>
     * @param <E>
     * @return
     */
    public static <V,E> Graph<V,E> bfs(Graph<V,E> g, V source) {
        Graph<V,E> pathGraph = new AdjacencyMapGraph<>(); //initialize backTrack
        pathGraph.insertVertex(source); //load start vertex with null parent
        Set<V> visited = new HashSet<>(); //Set to track which vertices have already been visited
        Queue<V> queue = new LinkedList<>(); //queue to implement BFS

        queue.add(source); //enqueue start vertex
        visited.add(source); //add start to visited Set
        while (!queue.isEmpty()) { //loop until no more vertices
            V u = queue.remove(); //dequeue
            pathGraph.insertVertex(u);
            for (V v : g.outNeighbors(u)) { //loop over out neighbors
                pathGraph.insertVertex(v);
                if (!visited.contains(v)) { //if neighbor not visited, then neighbor is discovered from this vertex
                    visited.add(v); //add neighbor to visited Set
                    queue.add(v); //enqueue neighbor
                    pathGraph.insertDirected(v, u, null); //save that this vertex was discovered from prior vertex
                }
            }
        }
        return pathGraph;
    }

    /**
     * Find path from the root to a given node
     * @param tree
     * @param v
     * @param <V>
     * @param <E>
     * @return
     */
    public static <V,E> List<V> getPath(Graph<V,E> tree, V v) {
        //if the tree is empty or tree doesn't include end vertex return an empty list
        if (tree.numVertices() == 0 || !tree.hasVertex(v)) {
            return new LinkedList<V>();
        }
        //start from end vertex and work backward to start vertex
        List<V> path = new LinkedList<V>(); //this will hold the path from start to end vertex
        V current = v; //start at end vertex
        //loop from end vertex back to start vertex
        while (current != null ) {
            path.add(0,current); //add this vertex to front of arraylist path
            if (tree.outDegree(current) != 0) {
                current = tree.outNeighbors(current).iterator().next(); //get vertex that discovered this vertex
            } else {
                current = null;
            }
        }
        return path;
    }

    /**
     * Find vertices that don't exist in a subgraph
     * @param graph
     * @param subgraph
     * @param <V>
     * @param <E>
     * @return
     */
    public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph) {
        //create a set to hold missing vertices
        Set<V> missingVerts = new HashSet<> ();
        //loop through every vertice in the main graph
        for (V vert : graph.vertices()) {
            //if subgraph doesn't have that vertice, add it to the set
            if (!subgraph.hasVertex(vert)) missingVerts.add(vert);
        }
        return missingVerts;
    }

    /**
     * Calculate the average separation between the tree graph root and all the other vertices in the graph
     * @param tree
     * @param root
     * @param <V>
     * @param <E>
     * @return
     */
    public static <V,E> double averageSeparation(Graph<V,E> tree, V root) {
        //create a list to hold the path lengths for each vertice besides the root
        List<Integer> pathLengths = new ArrayList<>();
        //call helper to start recursion and fill pathLengths list
        sepHelper(tree, pathLengths, root, 1);
        //if there are no paths from root, return 0
        if (pathLengths.isEmpty()) return 0;
        double sum = 0;
        //loop through every path and add the values to get a sum of lengths
        for (Integer length : pathLengths) {
            sum += length;
        }
        //divide by size of list
        return sum / pathLengths.size();
    }

    /**
     * Called by averageSeparation to help recurse through tree
     * @param tree
     * @param paths
     * @param root
     * @param level
     * @param <V>
     * @param <E>
     */
    public static <V,E> void sepHelper(Graph<V,E> tree, List<Integer> paths, V root, int level) {
        //if the subtree has no children (inNeighbors because it's a graph in this case) return 0
        if (tree.inDegree(root) == 0) return;
        //loop through each child of the tree
        for (V vert : tree.inNeighbors(root)) {
            //add length to list
            paths.add(level);
            //recurse on the children of the current vertex, while increasing the value of level
            sepHelper(tree, paths, vert, level + 1);
        }
    }

    /**
     * A main method to test all the static methods on a small scale graph
     * @param args
     */
    public static void main(String[] args) {
        //create a the graph included in the instructions for testing of the four static methods
        Graph<String, String> relationships = new AdjacencyMapGraph<String, String>();
        //add vertices
        relationships.insertVertex("Bacon");
        relationships.insertVertex("Alice");
        relationships.insertVertex("Bob");
        relationships.insertVertex("Charlie");
        relationships.insertVertex("Earl");
        relationships.insertVertex("Nobody");
        relationships.insertVertex("Nobody's Friend");
        //add undirected edges
        relationships.insertUndirected("Bacon", "Alice", "A movie");
        relationships.insertUndirected("Bacon", "Bob", "A movie");
        relationships.insertUndirected("Alice", "Bob", "A movie");
        relationships.insertUndirected("Alice", "Charlie", "D movie");
        relationships.insertUndirected("Bob", "Charlie", "C movie");
        relationships.insertUndirected("Charlie", "Earl", "B movie");
        relationships.insertUndirected("Nobody", "Nobody's Friend", "F movie");

        System.out.println(relationships);
        //run bfs on the graph with "Bacon" being the root for the test
        Graph<String, String> tree = GraphLibrary.bfs(relationships, "Bacon");
        System.out.println(tree);
        //get path from root ("Bacon") to "Charlie"
        System.out.println(GraphLibrary.getPath(tree, "Charlie"));
        //make a subgraph to test missingVertices method
        Graph<String, String> subGraph = new AdjacencyMapGraph<String, String>();
        subGraph.insertVertex("Nobody");
        subGraph.insertVertex("Nobody's Friend");
        subGraph.insertUndirected("Nobody", "Nobody's Friend", "F movie");
        //find what vertices are in the original graph but not in the subGraph
        System.out.println("Not in subgraph: " + GraphLibrary.missingVertices(relationships, subGraph));
        //find the average separation between the root ("Bacon" in this case) and the other vertices
        System.out.println("Average Separation (Bacon as root): " + GraphLibrary.averageSeparation(tree, "Bacon"));
    }
}
