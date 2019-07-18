/*
 * This is a java implementation of query rewriting.
 * Use case: Secure data sharing among Maritime ships.
 * */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

public class QueryRewriting2 {
	public static String prefix="";
	public static String select = "select";
	public static String selectValues = "";
	public static String where = "where";
	public static String queryString = "";
	public static String core = "";
	public static String openBracket="{";
	public static String closeBracket="}";
	public static String constantOutput = "";
	public static String service="SERVICE <http://192.168.56.103:3030/USNA>";
	
	public static void main(String[] args) throws IOException {
		String query = 
				"PREFIX ns: <http://www.usna.org/ns#> " +
						"SELECT ?User ?IncidentShip ?SARTeam ?Category "+
						"WHERE { "+
						"?User ns:contactTo ?SARTeam. "+
						"?User ns:isMemberOf ?IncidentShip. "+
						"?SARTeam ns:belongsTo ?Category ."+
						 "}";

		
		//Read in rule from a file
		String file ="/home/samdwise/eclipse-workspace/Jena-App/src/rule2.txt";
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String rule = reader.readLine();
		reader.close();
		
		//Analyze the query
		prefix = query.split("SELECT")[0]; //get the prefix from the query
		String temp = query.split("SELECT")[1];
		selectValues = temp.split("WHERE")[0].trim(); //get the select variables from the query
		temp = temp.split("WHERE")[1].trim();
		queryString = temp.substring(1,temp.length()-1).trim(); //get the query terms from query
		
		//Next call the queryRewriting function, pass the rule as argument
		//queryRewriting(rule);
		
		readRuleLineByLine(file,queryString);
	}
	public static void queryRewriting(String rule) throws IOException{
		String local = "";
		String remote = "";
		String query = RemoveCharacters(queryString); //remove special characters from the query string. 
		
		String ruleConsequence = preprocess(RemoveCharacters(SplitRule(rule)[1])); //preprocess rule consequence 
		String rulePredicate = preprocess(RemoveCharacters(SplitRule(rule)[0])).trim(); //preprocess rule predicate 
		
		String [] query_words = query.split(" ");
		String [] ruleConsequence_words = ruleConsequence.trim().split(" ");
		
		constantOutput = ruleConsequence_words[2].split(":")[1];
		
		String removeWord = "";
		String newQuery = "";
		int index = 0;
		for(int i=0; i< query_words.length; i++) {
			if(query_words[i].equals(ruleConsequence_words[1])) {
				core = ruleConsequence_words[1];
				index = i;
			}
		}
		String tempfix = "";
		for(int i=0; i< query_words.length; i++) {
		
			if((i == index+1)) {
				newQuery = newQuery+" "+ruleConsequence_words[2];
			}else {
				newQuery = newQuery+" "+query_words[i];
				
			}
			
		}
		newQuery = newQuery.replaceFirst(" ","");
		//Check for duplicate
		String [] newquery_words = newQuery.split("\\.");
		String [] rulePredicate_words = rulePredicate.split(" ");
		
		ArrayList<String> list=new ArrayList<String>();
		for(int i=0; i< newquery_words.length; i++) {
			list.add(newquery_words[i].trim()+".");
		}
		
		String temp = "";
		int count = 1;
		for(int j = 0; j< rulePredicate_words.length; j++) {
			
			temp = temp+" "+rulePredicate_words[j];
			if((count%3 == 0)) {
				String newquery_wordsTemp = temp;
				list.add(newquery_wordsTemp.trim()+".");
				temp = "";
			}
			count++;
		}
		
		System.out.println(list);
		//remove duplicate
		LinkedHashSet<String> lhs = new LinkedHashSet<String>();
		lhs.addAll(list);
	    list.clear();
	 
	     // Adding LinkedHashSet elements to the ArrayList
	     list.addAll(lhs);	     
	     String rewritenQuery = "";
	     ruleConsequence = ruleConsequence.replaceFirst(" ","");
	     ruleConsequence = ruleConsequence+" .";
	     String askPrefix = "PREFIX ns: <http://www.usna.org/ns#>  ASK ";
	     for(int i=0; i<list.size(); i++) {
	    	 String updatedquery = askPrefix+openBracket+" "+list.get(i)+" "+closeBracket;
	    	 if(list.get(i).contains(core)) {	    		 
	    	 }else if(askModel(updatedquery)) { // call function askModel to determine if a tripple is local or remote.
	    		 local = local+" "+list.get(i);
	    	 }else if((list.get(i)).equals(ruleConsequence)){
	    		 local = local+" "+list.get(i); //add local triples here
	    	 }
	    	 else {
	    		 remote = remote+" "+list.get(i); //add remote triples here.
	    	 } 
	    	 rewritenQuery = rewritenQuery+list.get(i);
	     }
	     
	     querying(local,remote);
	}
	
	//RemoveCharacters function is for removing special character from string
	public static String RemoveCharacters(String sentence){
		String keyCharacters[] = {"<",">","SERVICE","{","}","(",")","-"};
		String resturnString = sentence;
		for(int i=0;i<keyCharacters.length;i++) {
			resturnString = resturnString.replace(keyCharacters[i],"");
		}
		
		return resturnString;
	}
	
	//Function used to split the rule into precedence and consequence 
	public static String[] SplitRule(String q2) {
		String[] arrOfString = null;
		arrOfString = q2.split("->");		
		return arrOfString;
	}
	
	
	//preprocess function is used to map symbols in rules with the correct domains and ranges from the model
	//returns the right tripples ready to be merged with query. 
	public static String preprocess(String value) throws IOException {
		value = RemoveCharacters(value).replaceAll("\\s{2,}"," ").trim();
		String [] splitString = value.split(" ");
		String updatedrule = "";
		List<String> lines = Files.readAllLines(Paths.get("/home/samdwise/eclipse-workspace/Jena-App/src/model.ttl"));
		for(int i=0; i< splitString.length; i++){
			if(i%3 == 0){
				String name = splitString[i+1];
			    String [] domain = new String[3];
			    String [] range = new String[3];
			   
			    for (String line : lines) {
			        if (line.contains(name)) {
			        	 if (line.contains("domain")) {
			        		 line = line.replaceAll("\\s{2,}"," ").trim();
			        		 domain = line.split(" ");
			        		 updatedrule = updatedrule+" ?"+domain[2].split(":")[1]+" "+name+" ";
			        	 }else if((line.contains("range")) && (splitString[i+2]).startsWith("ns")) {
			        		 updatedrule = updatedrule+" "+splitString[i+2];
			        	 }else if((line.contains("range"))) {
			        		
			        		 line = line.replaceAll("\\s{2,}"," ").trim();
			        		 range = line.split(" ");
			        		 updatedrule = updatedrule+" ?"+range[2].split(":")[1];
			        	 } 
			        }
			    }
			    
			    
			}
			
		}		
		return " "+updatedrule.replaceAll("\\s{2,}"," "); //return the mapped tripples
	    
	}
	
	//askModel is use to determine if a triple belongs to local endpoint or not
	public static boolean askModel(String szQuery) {	   
		Query query = QueryFactory.create(szQuery) ;
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://localhost:3030/USNA/query", query);
		    
		boolean b = qexec.execAsk();
		return b;
	}
	
	//function to query merge local part and rempte part of derived from rule together, and carry out query on both endpoint.
	public static void querying(String local, String remote) {
		
		//construct the new query
		String constructQuery2 = prefix+" "+select+" "+selectValues+" "+where+openBracket+" "+local+" "+service+" "+openBracket+" "+remote+" "+closeBracket+closeBracket;
		
		Query query2 = QueryFactory.create(constructQuery2);
		QueryExecution qe2 = QueryExecutionFactory.sparqlService(
				"http://localhost:3030/USNA/query", query2);
		
		ResultSet results1 = qe2.execSelect();
		String NS = "http://www.usna.org/ns#";

		Model rdfssExample = ModelFactory.createDefaultModel();
		Resource Response = rdfssExample.createResource(NS+constantOutput);
		String [] headers = selectValues.split(" ");
		
	
	    System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------");
		for(int i = 0; i< headers.length; i++) {
			System.out.print(headers[i].substring(1,headers[i].length()).trim()+"\t\t\t\t\t");
		}
		System.out.println();
		System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------");
		System.out.println();
		
		 try {
           while ( results1.hasNext() ) {
               QuerySolution soln = results1.nextSolution();
               Resource user = soln.getResource("User");
               Resource incidentShip = soln.getResource("IncidentShip");
               Resource SARTeam = soln.getResource("SARTeam");
               
               System.out.format("%10s %40s %40s %40s",
            		   user, incidentShip, SARTeam, Response);
		        System.out.println();
		        
               
           }
       } finally {
    	   qe2.close();
}
		 
	}

	public static void readRuleLineByLine(String file, String query) throws IOException {
		//String ruleConsequence = "";
		Scanner scanner = new Scanner(new File(file));  
		while (scanner.hasNextLine()) {  
			   String line = scanner.nextLine();
			   String ruleConsequence = preprocess(RemoveCharacters(SplitRule(line)[1]));
			   String [] ruleConsequence_words = ruleConsequence.trim().split(" ");
				
			   String constantt = ruleConsequence_words[1].split(":")[1];
			   if(query.contains(constantt)) {
				   queryRewriting(line);
			   }
			   
			   
			}	
	}
	
	
}
