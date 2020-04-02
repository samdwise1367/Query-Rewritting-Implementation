package usna;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class DataGenerator2 {
	
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
		//for (int i = 0; i < 1000; i++) {
				for(int j = 0; j< 1000; j++ ) {
			    	  pr.println("ns:User"+(j+1)+"  "+"rdf:type"+"  "+"ns:"+"User .");
			    	  pr.println("ns:User"+(j+1)+"  "+"ns:belongsToVessel"+"  "+"ns:Ship"+(j+1)+" .");
			    	  if(j%2 == 0) {
			    			pr.println("ns:User"+(j+1)+"  "+"ns:hasVesselRole"+"  "+"ns:"+Role[0]+".");
			    	  }else {
			    			pr.println("ns:User"+(j+1)+"  "+"ns:hasVesselRole"+"  "+"ns:"+Role[1]+".");
			    	}
			    	  pr.println("ns:Ship"+(j+1)+"  "+"rdf:type"+"  "+"ns:"+"Vessel.");	
			    	  
			    	  if(j%3 == 0){
			    		  pr.println("ns:Ship"+(j+1)+"  "+"ns:hasStatus"+"  "+"ns:Distressed .");
			    		  pr.println("ns:Ship"+(j+1)+"  "+"ns:contacts"+"  "+"ns:RescueCoordinationCenter.");
			    		  pr.println("ns:Ship"+(j+1)+"  "+"ns:ownsVesselData"+"  "+"ns:VesselGeneralPassengerData.");
			    	  }
			    	  
			    	  pr.println("\n");
		    }
				//ns:Venus  ns:ownsVesselData	ns:VesselGeneralPassengerData .
				//ns:Venus ns:contacts	ns:RescueCoordinationCenter .
				//ns:Venus ns:hasStatus   ns:Distressed .
			//System.out.println(words[i]);
		//}
		pr.close();
		br.close();
		fr.close();
	}

}
