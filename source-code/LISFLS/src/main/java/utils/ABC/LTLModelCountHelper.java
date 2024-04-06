package utils.ABC;

import automata.fsa.FSAToRegularExpressionConverter;
import automata.fsa.FSATransition;
import automata.fsa.FiniteStateAutomaton;
import de.uni_luebeck.isp.rltlconv.automata.*;
import de.uni_luebeck.isp.rltlconv.cli.Conversion;
import de.uni_luebeck.isp.rltlconv.cli.RltlConv;
import scala.Tuple2;
import scala.collection.Iterator;
import scala.collection.immutable.List;
import scala.collection.immutable.Map;
import scala.collection.immutable.Vector;
import scala.collection.immutable.VectorIterator;

import java.awt.*;
import java.io.*;
import java.util.HashMap;


public class LTLModelCountHelper {
	static final String[] parameters = new String[]{"@rltlconv.txt", "--formula", "--props", "--nba", "--min"};
	static ByteArrayOutputStream baos = new ByteArrayOutputStream();
	private void writeFile(String fname,String text) throws IOException{
		BufferedWriter output = null;
        try {
            File file = new File(fname);
            output = new BufferedWriter(new FileWriter(file));
            output.write(text);
        } catch ( IOException e ) {
            e.printStackTrace();
        } finally {
          if ( output != null ) {
            output.close();
          }
        }
	}
	
	private void runCommand(String cmd) throws IOException, InterruptedException{
		Process p = Runtime.getRuntime().exec(cmd);
		
		InputStream in = p.getInputStream();
    	InputStreamReader inread = new InputStreamReader(in);
    	BufferedReader bufferedreader = new BufferedReader(inread);
    	String aux;
    	String out = "";
	    while ((aux = bufferedreader.readLine()) != null) {
	    	out += aux+"\n";
	    }
	    if(out!="")
	    	writeFile("rltlconv-out.txt",out);
	    
	 // Leer el error del programa.
    	InputStream err = p.getErrorStream();
    	InputStreamReader errread = new InputStreamReader(err);
    	BufferedReader errbufferedreader = new BufferedReader(errread);
    	
	    while ((aux = errbufferedreader.readLine()) != null) {
	    	System.out.println("ERR: " + aux);
	    }
	   
	    // Check for failure
		if (p.waitFor() != 0) {
			System.out.println("exit value = " + p.exitValue());
		}
  
		// Close the InputStream
    	bufferedreader.close();
    	inread.close();
    	in.close();
   		// Close the ErrorStream
   		errbufferedreader.close();
   		errread.close();
   		err.close();
    		
   		if (p!=null) {
//   			InputStream is = p.getInputStream();
 //  			InputStream es = p.getErrorStream();
  			OutputStream os = p.getOutputStream();
//   				if (is!=null) is.close();
//   				if (es!=null) es.close();
			if (os!=null) os.close();
   		}
	}

	public Nba ltl2nba(String formula) {
		parameters[0] = formula;

		PrintStream psOld = System.out;
		System.setOut(new PrintStream(baos));
		de.uni_luebeck.isp.rltlconv.cli.Main.main(parameters);
		System.setOut(psOld); // 恢复原来的输出路径
		Object res = baos.toString();
		baos.reset();
		Nba nba = (Nba) RltlConv.convert(res, Conversion.NBA());

		return nba.toNamedNba();
	}
	

			
	public String automata2RE(Nfa ltl_ba){
		
		FiniteStateAutomaton fsa = new FiniteStateAutomaton();
	
		//Map nodes to states ids
		java.util.Map<String,Integer> ids = new HashMap<>();
		//get initial node
		State in = ltl_ba.start().head(); //CUIDADO:que pasa si tenemos varios estados iniciales.

		//create and set initial state
		automata.State is = fsa.createState(new Point());
		fsa.setInitialState(is);
		
		//Map labels to ids
//		java.util.Map<String,Integer> labelIDs = new HashMap<>();
		
		Iterator<String> lit = ltl_ba.alphabet().iterator();
		while(lit.hasNext()){
			String l = lit.next();
//			System.out.println(l);
			if(!labelIDs.containsKey(l)){
				labelIDs.put("\""+l+"\"", ""+labelIDs.keySet().size());
			}
		}
		
		//initial node ids
		ids.put(in.name(), is.getID());
			
		Map<Tuple2<State, Sign>, List<DirectedState>> trans = (Map<Tuple2<State, Sign>, List<DirectedState>>) ltl_ba.transitions();
		Vector<Tuple2<Tuple2<State, Sign>, List<DirectedState>>> vector =  trans.toVector();
		VectorIterator<Tuple2<Tuple2<State, Sign>, List<DirectedState>>> ltl_ba_it = vector.iterator();
		while(ltl_ba_it.hasNext()){
			Tuple2<Tuple2<State, Sign>, List<DirectedState>> o = ltl_ba_it.next();
			State from = o._1()._1();
			//checks if ID exists
			int ID = 0;
			automata.State fromState = null;
			if (ids.containsKey(from.name())){
				ID = ids.get(from.name());
				fromState = fsa.getStateWithID(ID);
			}
			else{
				//create new state
				fromState = fsa.createState(new Point());
				//update ids
				ids.put(from.name(), fromState.getID());
				ID = fromState.getID();
			}
			
			//get Label
			String l = o._1()._2().toString();
			
//			String label = getLabel(l);
//			int base = 97;//a
//			System.out.println("l:" +l.toString());
//			String label = ""+Character.toChars(base+labelIDs.get(l))[0];
			String label = labelIDs.get(l).toString();
			
			Iterator<DirectedState> listIt = o._2().iterator();
			while(listIt.hasNext()){
				State to = listIt.next().state();
				//check if toState exists
				automata.State toState = null;
				
				if (ids.containsKey(to.name())){
					ID = ids.get(to.name());
					toState = fsa.getStateWithID(ID);
				}
				else{
					//create new state
					toState = fsa.createState(new Point());
					//update ids
					ids.put(to.name(), toState.getID());
					ID = toState.getID();
				}
				
				//add transition
				FSATransition t = new FSATransition(fromState,toState,label);
				fsa.addTransition(t);
			}
		}
		
		//add final states
		Iterator<State> ac_it = ltl_ba.accepting().iterator();
		while(ac_it.hasNext()){
			State a = ac_it.next();
			int ID = ids.get(a.name());
			automata.State as = fsa.getStateWithID(ID);
			fsa.addFinalState(as);
		}
		
		//convertToDFA
		//FiniteStateAutomaton dfa = (new NFAToDFA()).convertToDFA(fsa);
		
		//minimize automaton
		//Automaton m = (new Minimizer()).getMinimizeableAutomaton(dfa);
		
//		System.out.println(labelIDs);
		
		//removeEmptyTransitions(fsa);
//		System.out.println(fsa.toString());

		FSAToRegularExpressionConverter.convertToSimpleAutomaton(fsa);
//		System.out.println(fsa.toString());
		return FSAToRegularExpressionConverter.convertToRegularExpression(fsa);
	}
	
	//Map labels to ids
	java.util.Map<String,String> labelIDs = new HashMap<>();
	public boolean encoded_alphabet = false;
public String automata2RE(Nba ltl_ba){
		
		FiniteStateAutomaton fsa = new FiniteStateAutomaton();
	
		//Map nodes to states ids
		java.util.Map<String,Integer> ids = new HashMap<>();
		//get initial node
		State in = ltl_ba.start().head(); //CUIDADO:que pasa si tenemos varios estados iniciales.

		//create and set initial state
		automata.State is = fsa.createState(new Point());
		fsa.setInitialState(is);

		//initial node ids
		ids.put(in.name(), is.getID());
			
		Map<Tuple2<State, Sign>, List<DirectedState>> trans = (Map<Tuple2<State, Sign>, List<DirectedState>>) ltl_ba.transitions();
		Vector<Tuple2<Tuple2<State, Sign>, List<DirectedState>>> vector =  trans.toVector();
		VectorIterator<Tuple2<Tuple2<State, Sign>, List<DirectedState>>> ltl_ba_it = vector.iterator();
		while(ltl_ba_it.hasNext()){
			Tuple2<Tuple2<State, Sign>, List<DirectedState>> o = ltl_ba_it.next();
			State from = o._1()._1();
			//checks if ID exists
			int ID = 0;
			automata.State fromState = null;
			if (ids.containsKey(from.name())){
				ID = ids.get(from.name());
				fromState = fsa.getStateWithID(ID);
			}
			else{
				//create new state
				fromState = fsa.createState(new Point());
				//update ids
				ids.put(from.name(), fromState.getID());
				ID = fromState.getID();
			}


			//get Label
			String l = o._1()._2().toString();
			if(!encoded_alphabet)
				setLabel(l);
			else
				setLabelEncoded(l);
//			if(!labelIDs.containsKey(l)){
//				labelIDs.put(l, labelIDs.keySet().size());
//			}
			
//			String label = getLabel(l);
			
//			int base = 97;//a
//			String label = l; //""+Character.toChars(base+labelIDs.get(l))[0];
//			String label = ""+Character.toChars(labelIDs.get(l))[0];
			String label = labelIDs.get(l);
			
			Iterator<DirectedState> listIt = o._2().iterator();
			while(listIt.hasNext()){
				State to = listIt.next().state();
				//check if toState exists
				automata.State toState = null;
				
				if (ids.containsKey(to.name())){
					ID = ids.get(to.name());
					toState = fsa.getStateWithID(ID);
				}
				else{
					//create new state
					toState = fsa.createState(new Point());
					//update ids
					ids.put(to.name(), toState.getID());
					ID = toState.getID();
				}
				
				//add transition
				FSATransition t = new FSATransition(fromState,toState,label);
				fsa.addTransition(t);
			}
		}
		
		//add final states
		Iterator<State> ac_it = ltl_ba.accepting().iterator();
		while(ac_it.hasNext()){
			State a = ac_it.next();
			int ID = ids.get(a.name());
			automata.State as = fsa.getStateWithID(ID);
			fsa.addFinalState(as);
		}

		FSAToRegularExpressionConverter.convertToSimpleAutomaton(fsa);
//		System.out.println(fsa.toString());
		return FSAToRegularExpressionConverter.convertToRegularExpression(fsa);
	}
	
	public String toABClanguage(String re){
		String abcStr = "";
		abcStr = re.replace("λ", "\"\"");
		abcStr = abcStr.replace("+", "|");
		return abcStr;
	}
	
	int base = 48;//start with char 0
	public void setLabel(String l) throws RuntimeException{
		if(labelIDs.containsKey(l)){
			return;
		}
		
		labelIDs.put(l, ""+Character.toChars(base)[0]); 
		
		//update base
		if(base==57)
			base = 65; //jump to A
		else if (base == 90)
			base = 97; //jump to a
		else
			base++;
			
		if(base > 122)
			throw new RuntimeException("Maximum number of characters reached.");

	}
	
	
	public int state = 97;//start with char a
	public void setLabelEncoded(String l) throws RuntimeException{
		if(labelIDs.containsKey(l)){
			return;
		}
		
		String label = ""+Character.toChars(state)[0]+Character.toChars(base)[0];
		labelIDs.put(l, label); 
		
		//update base
		if(base==57)
			base = 65; //jump to A
		else if (base == 90)
			base = 97; //jump to a
		else
			base++;
		
		if(base > 122){
			state++;
			base = 48;
		}
		
		if(state > 122)
			throw new RuntimeException("Maximum number of characters reached.");

	}
	
}
