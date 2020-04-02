package usna;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GenerateData {
	
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		File file = new File("/home/samdwise/eclipse-workspace/usna/src/usna/data.ttl");
		FileWriter fr = new FileWriter(file, true);
		BufferedWriter br = new BufferedWriter(fr);
		PrintWriter pr = new PrintWriter(br);
		//pr.println("data");
				
		
		String str = "ns:Adam rdf:type ns:User .";
		String[] words = str.split(" ");
		String name = "Samson Sunday James John Adam Godwin Tosin Redwan Jeff Moses";
		String role = "VesselCaptain VesselCrew";
		String ship = "CrystalCruises JohnPaul Redmill TheQueenMary Titanic USSTruman Venus Ship1 Ship2 Ship3 Ship4";
		String [] Role = role.split(" ");
		String [] User = name.split(" ");
		String [] Ship = ship.split(" ");
		String classes [] = {"User","Role","Vessel"};
		for (int i = 0; i < classes.length; i++) {
			if(i == 0) {
				for(int j = 0; j< User.length; j++ ) {
			    	  pr.println("ns:"+User[j]+"\t"+"rtf:type"+"\t"+"ns:"+classes[i]+".");
			    	  pr.println("ns:"+User[j]+"\t"+"ns:belongsToVessel"+"\t"+"ns:"+Ship[j]+".");
			    }
				pr.println("\n");
			    	
		    }else if(i == 1) {
		    	for(int j = 0; j< User.length; j++ ) {
		    		if(j%2 == 0) {
		    			pr.println("ns:"+User[j]+"\t"+"ns:hasVesselRole"+"\t"+"ns:"+Role[0]+".");
		    		}else {
		    			pr.println("ns:"+User[j]+"\t"+"ns:hasVesselRole"+"\t"+"ns:"+Role[1]+".");
		    		}
			    	 
			    }
		    }else if(i == 2){
		    	//ns:CrystalCruises       rdf:type    ns:IncidentShip .
		    	for(int j = 0; j< Ship.length; j++ ){
		    		pr.println("ns:"+Ship[j]+"\t"+"rtf:type"+"\t"+"ns:"+classes[i]+".");
		    	}
		    	pr.println("\n");
		    }
			
			//System.out.println(words[i]);
		}
		pr.close();
		br.close();
		fr.close();
	}

}
