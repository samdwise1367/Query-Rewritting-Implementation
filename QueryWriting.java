package usna;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
public class QueryWriting {

	public static String prefix="";
	public static String select = "select DISTINCT";
	public static String selectValues = "";
	public static String where = "where";
	public static String queryString = "";
	public static String core = "";
	public static String openBracket="{";
	public static String closeBracket="}";
	public static String constantOutput = "";
	public static String service="SERVICE <http://192.168.56.103:3030/USNA>";
	private static HashMap<String, String> mainData = new HashMap<String, String>();
	
	 public static void putData(String key ,String data){
         mainData.put(key, data);
      }

	 public static HashMap<String, String> getData() {
          return mainData;
      }
	
	public static void main(String[] args) throws IOException {
		long start = System.currentTimeMillis(); //start time
		String query = "PREFIX ns: <http://www.usna.org/ns#> " +
				"SELECT ?User ?Read" + 
				"WHERE { " + 
					"?Vessel ns:contacts ?SARCenter. " + 
					"?Organization ns:isMemberOf ?SARCenter. " +
					"?User ns:belongsToVessel ?Vessel. " +
					"?Organization ns:owns ?Read. "+
					"?User ns:hasReadAccess ?Read. " +
			"}";

		
			
		//Read in rule from a file
		String file ="/home/samdwise/eclipse-workspace/usna/src/usna/rule2.txt";
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String rule = reader.readLine();
		reader.close();
		
		//Analyze the query
		prefix = query.split("SELECT")[0]; //get the prefix from the query
		String temp = query.split("SELECT")[1];
		selectValues = temp.split("WHERE")[0].trim(); //get the select variables from the query
		
		System.out.println(selectValues);
		temp = temp.split("WHERE")[1].trim();
		queryString = temp.substring(1,temp.length()-1).trim(); //get the query terms from query
		
		//Next call the queryRewriting function, pass the rule as argument
		//queryRewriting(rule);
		String query0 = RemoveCharacters(queryString);
		readRuleLineByLine(file,query0);
		
		long end = System.currentTimeMillis();// end time
		
		NumberFormat formatter = new DecimalFormat("#0.00000");
		System.out.print("Execution time is " + formatter.format((end - start) / 1000d) + " seconds");
	}
	public static String queryRewriting(String rulePredicate, String ruleConsequence) throws IOException{
		String local = "";
		String remote = "";
		String query = RemoveCharacters(queryString); //remove special characters from the query string. 
		
		//String ruleConsequence = preprocess(RemoveCharacters(SplitRule(rule)[1]),query); //preprocess rule consequence 
		//String rulePredicate = preprocess(RemoveCharacters(SplitRule(rule)[0]),query).trim(); //preprocess rule predicate 
		
		String [] query_words = query.split(" ");
		String [] ruleConsequence_words = ruleConsequence.trim().split(" ");
		
		if(ruleConsequence_words[2].contains(":")) {
			//constantOutput = ruleConsequence_words[2].split(":")[1];
		}else {
			String temp = preprocess(ruleConsequence,query);
			System.out.println("I got here ohhhhh "+temp);
		}
		
		
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
				//System.out.println(query_words[j]);
				String newquery_wordsTemp = temp;
				list.add(newquery_wordsTemp.trim()+".");
				temp = "";
			}
			count++;
		}
		
		System.out.println("Duplicated list : "+list);
		
		//remove duplicate		
		Set<String> uniqueList = new HashSet<String>(list);
		list.clear();
		list = new ArrayList<String>(uniqueList); //let GC will doing free memory
		System.out.println("Removed Duplicate : "+list);
	     
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
	     
	     String constructQuery = openBracket+" "+local+" "+service+" "+openBracket+" "+remote+" "+closeBracket+closeBracket;
	     //querying(local,remote);
	     return constructQuery;
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
	
	//askModel is use to determine if a triple belongs to local endpoint or not
	public static boolean askModel(String szQuery) {	   
		Query query = QueryFactory.create(szQuery) ;
		QueryExecution qexec = QueryExecutionFactory.sparqlService("http://localhost:3030/USNA/query", query);
		    
		boolean b = qexec.execAsk();
		return b;
	}
	
	//function to query merge local part and rempte part of derived from rule together, and carry out query on both endpoint.
	public static void querying(String constructQuery2) {
		//construct the new query
		//String constructQuery2 = prefix+" "+select+" "+selectValues+" "+where+openBracket+openBracket+" "+local+" "+service+" "+openBracket+" "+remote+" "+closeBracket+closeBracket;
		//constructQuery2 = constructQuery2+" UNION "+" "+openBracket+" "+local+" "+service+" "+openBracket+" "+remote+" "+closeBracket+closeBracket+closeBracket;
		
		System.out.println(constructQuery2);
		Query query2 = QueryFactory.create(constructQuery2);
		QueryExecution qe2 = QueryExecutionFactory.sparqlService(
				"http://localhost:3030/USNA/query", query2);
		
		ResultSet results1 = qe2.execSelect();
		if(constantOutput == "") {
			ResultSetFormatter.out(System.out, results1);
		}else {
			String NS = "http://www.usna.org/ns#";
			Model rdfssExample = ModelFactory.createDefaultModel();
			String [] constant = constantOutput.trim().split(" ");
			
			String [] headers = selectValues.split(" ");
		    System.out.println("--------------------------------------------------------------------------------------------------------------------------------------------");
			for(int i = 0; i< headers.length; i++) {
				//System.out.print(headers[i].substring(1,headers[i].length()).trim()+"\t\t\t\t\t\t");
				System.out.print(headers[i].substring(1,headers[i].length()).trim()+"\t\t\t\t\t\t");
			}
			System.out.println();
			System.out.println("----------------------------------------------------------------------------------------------------------------------------------------------");
			System.out.println();
			
			try {
				   int count = 0;
		           while ( results1.hasNext() ) {
		        	   //Resource Response = rdfssExample.createResource(NS+constant[count]);
		        	   
		               QuerySolution soln = results1.nextSolution();
		               Resource first = soln.getResource(headers[0].substring(1,headers[0].length()).trim());
		               //Resource second = soln.getResource(headers[1].substring(1,headers[1].length()).trim());
		               //Resource third = soln.getResource(headers[2].substring(1,headers[2].length()).trim());
		               //Resource fourth = soln.getResource(headers[3].substring(1,headers[3].length()).trim());
		               for(int i=0; i< constant.length; i++) {
		            	   Resource Response = rdfssExample.createResource(NS+constant[i]);
		            	   System.out.format("%10s %50s",first, Response);
		            	   System.out.println();
					       //count++;
		               }
		               //System.out.format("%10s %50s",first, Response);
		               //System.out.format("%10s",first);
				       System.out.println();
				       //count++;
				      
				       //System.out.println(count);
		               
		           }
		       } finally {
		    	   qe2.close();
		}
			 
		}		 
	}

	public static void readRuleLineByLine(String file, String query) throws IOException {
		Scanner scanner = new Scanner(new File(file)); 
		int count = 0;
		String updatepredicate = "";
		String updateconsequence = "";
		String constructQuery = "";
		while (scanner.hasNextLine()) {  
			   String line = scanner.nextLine();
			   String rulePredicate = preprocess(RemoveCharacters(SplitRule(line)[0]),query);
			   String ruleConsequence = preprocess(RemoveCharacters(SplitRule(line)[1]),query);
			   if(ruleConsequence.trim() != null) {
				   String [] ruleConsequence_words = ruleConsequence.trim().split(" ");
				   String constantt = ruleConsequence_words[1].split(":")[1];
				   
				   updateconsequence = ruleConsequence;
				   if(query.contains(constantt)){
					   updatepredicate = rulePredicate.trim()+" "+updatepredicate.trim();					   
					   if(count < 1){
						   count++;
						   constructQuery = prefix+" "+select+" "+selectValues+" "+where+openBracket+queryRewriting(updatepredicate.trim(),updateconsequence.trim());
						   System.out.println(constructQuery);
						   if(ruleConsequence_words[2].contains(":")) {
								constantOutput = constantOutput +" "+ ruleConsequence_words[2].split(":")[1];
							}
						   
					   }else {
						   if(ruleConsequence_words[2].contains(":")) {
								constantOutput = constantOutput +" "+ ruleConsequence_words[2].split(":")[1];
							}
						   constructQuery = constructQuery + " UNION "+queryRewriting(updatepredicate.trim(),updateconsequence.trim());
					   }
					   updatepredicate = "";
					   //querying(local,remote);
				   } 
			   } 
			   
			}
		if(count < 1) {
			constructQuery = prefix+" "+select+" "+selectValues+" "+where+openBracket+queryString;
		}
		
		constructQuery = constructQuery+closeBracket;
		System.out.println(constructQuery);
		querying(constructQuery);
	}
	
	public static String preprocess(String value, String query) throws IOException {
		value = RemoveCharacters(value).replaceAll("\\s{2,}"," ").trim();
		String [] splitString = value.split(" ");
		String updatedrule = "";
		String [] query_words = query.split(" ");
		for(int i=0; i< splitString.length; i++){
			if(i%3 == 0){
				String predicate = splitString[i+1];
				String subject = "";
				String object = "";
				String getSubject = getData().get(splitString[i]);
				String getObject = getData().get(splitString[i+2]);
				for (int j =0; j< query_words.length; j++){
					if(predicate.contains(query_words[j])) {
						subject = query_words[j-1].replace(".","");
						object = query_words[j+1].replace(".","");
						updatedrule = updatedrule+" "+subject+" "+predicate+" "+object;
						putData(splitString[i],subject);
		        		putData(splitString[i+2],object);
						break;
					}else if(!splitString[i+2].startsWith("?")){
						object = splitString[i+2];
						if(getSubject != null) {
							subject = getSubject;
						}
						updatedrule = updatedrule+" "+subject+" "+predicate+" "+object;
						break;
					}else if(!splitString[i].startsWith("?")){
						subject = splitString[i];
						if(getObject != null) {
							object = getObject;
						}
						updatedrule = updatedrule+" "+subject+" "+predicate+" "+object;
					}					
				}
			}
		}
		return " "+updatedrule.replaceAll("\\s{2,}"," ").trim(); //return the mapped tripples
	    
	}
	

}
