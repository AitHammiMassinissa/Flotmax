package maxflow;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;

import java.io.*;
import java.util.concurrent.atomic.AtomicReference;

public class CreationAutoroute {

    private static Node source; // Noeud source
    private static Node sink; // Noeud de destination
    private static void setStyle(Graph g, MaxFlow mf) {
        g.edges().forEach((Edge e) -> {
            e.setAttribute("ui.style", "text-size: 15; text-alignment: along;");
            double flow = mf.getFlow(e);
            double cap = mf.getCapacity(e);
            e.setAttribute("ui.label", String.format("%.0f/%.0f", flow, cap));
            if (cap == flow) {
            	e.setAttribute("ui.style", "text-size: 15; text-alignment: along; fill-color: red;");
            }
        });

        g.nodes().forEach((Node n) -> {
            n.setAttribute("ui.label", n.toString());
            n.setAttribute("ui.style", "fill-color: black;"
                    + "stroke-mode: plain;"
                    + "text-color: white;"
                    + "stroke-width: 2;"
                    + "stroke-color: black;"
                    + "size: "+ "24, 24;");
        });
    }

	
	public static  FileSource liregraph(String path,Graph graph) {
		String filePath = path;
        Graph g = graph;
        FileSource fs = new FileSourceDGS();

        fs.addSink(g);

        try {
            fs.readAll(filePath);
        } catch( IOException e) {
            System.err.println("Impossible de générer le graph , soit le fichier est introuvable soi il y'a des erreur dans le fichier dgs");
            e.printStackTrace();
            System.exit(1);
        } finally {
            fs.removeSink(g);
        }
        
        return fs;
	}
	
	public static void affGraphArrSatu(MaxFlow max,Graph g) {
		  source = g.getNode("A");
	        sink = g.getNode("I");

	       System.setProperty("org.graphstream.ui", "swing");
	        g.edges().forEach((Edge e) -> { 
	            int cap = (int) e.getAttribute("cap"); 
	            MaxFlow maxFlow = max;
	            maxFlow.setCapacityAttribute("cap");
	            maxFlow.init(g);
	            maxFlow.setSource(source);
	            maxFlow.setSink(sink);
	            maxFlow.compute();
	            setStyle(g, maxFlow);	            
	        });
	      g.display(false);
	}
	
	
	public static void calcule_arr_aug(Graph g,AtomicReference<Edge> Am_Edge,AtomicReference<Integer> F_to_s ,Node s, Node sk) {
        // Calcul de l'arête à augmenter pour améliorer le flot
        AtomicReference<Edge> Améliorer_Edge = Am_Edge;
        AtomicReference<Integer> Flow_to_sink =F_to_s;
        g.edges().forEach((Edge e) -> { 
            int cap = (int) e.getAttribute("cap"); 
            // On augmente la capacité à 2500 
            e.setAttribute("cap", 2500);
            MaxFlow maxFlow = new MaxFlow(); 
            maxFlow.setCapacityAttribute("cap");
            maxFlow.init(g);
            maxFlow.setSource(s);
            maxFlow.setSink(sk);
            maxFlow.compute();
            //flot arrivant au sink
            Double Flow_to_sink_Temp = sink.enteringEdges().reduce(0.0, (acc, entering) -> {
                return acc + maxFlow.getFlow(entering);
            }, Double::sum);
            //stocker l'arête augmentée la valeur du flot entrant dans la destination Si le flot est plus élevé que précédemment
            if(Flow_to_sink_Temp > Flow_to_sink.get()) {
            	Améliorer_Edge.set(e);
                Flow_to_sink.set(Flow_to_sink_Temp.intValue());
            }
            // Remettre la capacité initiale
            e.setAttribute("cap", cap);
        });
	}
    
	
	public static void afficheGraphApresAug(Graph g) {
        source = g.getNode("A");
        sink = g.getNode("I");
        AtomicReference<Edge> Améliorer_Edge = new AtomicReference<Edge>();
        AtomicReference<Integer> Flow_to_sink =new AtomicReference<Integer>(0);
        calcule_arr_aug(g,Améliorer_Edge,Flow_to_sink,source,sink);
        System.out.println("Meilleur avantage à améliorer: " + Améliorer_Edge.get());
        System.out.println("Flux arrivant à sink : " + Flow_to_sink.get());

        Améliorer_Edge.get().setAttribute("cap", 2500);
        
        MaxFlow mf = new MaxFlow();
        mf.setCapacityAttribute("cap");
        mf.init(g);
        mf.setSource(source);
        mf.setSink(sink);
        mf.compute();
        setStyle(g, mf);
        g.display(false);   
	}
	
	public static Node getSource() {
		return source;
	}

	public static void setSource(Node source) {
		CreationAutoroute.source = source;
	}

	public static Node getSink() {
		return sink;
	}

	public static void setSink(Node sink) {
		CreationAutoroute.sink = sink;
	}
    public static void main(String[] args) {
        System.setProperty("org.graphstream.ui", "swing");
        Graph g = new SingleGraph("Graph origine");
        Graph g2 = new SingleGraph("Autoroutes");
       // Graphe avec les arètes saturées
        System.setProperty("org.graphstream.ui", "swing");
        FileSource fc = liregraph("src/main/resources/fichier.dgs",g);
        MaxFlow maxFlow = new MaxFlow();
        affGraphArrSatu(maxFlow,g);
        // Grap aprés amélioration
        FileSource fc2 = liregraph("src/main/resources/fichier.dgs",g2);
        MaxFlow maxFlow2 = new MaxFlow();
        for(int i=0;i<2;i++) {
        afficheGraphApresAug(g2);
        }
    }
}