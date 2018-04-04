package pt.ulisboa.tecnico.hdscoin.client;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

import pt.ulisboa.tecnico.hdscoin.crypto.CipheredMessage;
import pt.ulisboa.tecnico.hdscoin.crypto.CryptoManager;
import pt.ulisboa.tecnico.hdscoin.crypto.Message;
import pt.ulisboa.tecnico.hdscoin.interfaces.*;

public class ClientApplication {

    public static void main(String[] args) {
    	String host = (args.length < 1) ? null : args[0];
        Client client;
    	boolean exit=false;
    	boolean initing=false;
    	Scanner reader = new Scanner(System.in);
    	
    	while(!initing) {
    		System.out.println("Select a number to init:");
    		System.out.println("1-Alice");
    		System.out.println("2-Bob.");
    		System.out.println("3-Charly.");
    		String userNumer = reader.nextLine();
    		try {
	            switch (userNumer.trim()) {
	                case "1":  client=new Client(host, "alice");
	                         break;
	                case "2":  client=new Client(host, "bob");
	                         break;
	                case "3":  client=new Client(host, "charly");
	                		break;
	                default: System.out.println("\nThe '"+userNumer+ "' is not a valid number!");
	                		continue;
	            }
    		}catch(Exception e) {
    			continue;
    		}
    		exit=false;
	    	while(!exit) {
	    		
	    		System.out.println("\nChoose below options by its number...");
	    		System.out.println("1-Register.");
	    		System.out.println("0-Exit.");
	    		String registerOption = reader.nextLine();
	    		
	    		if(registerOption.equals("0")) {
	        		exit=true;
	        	}else if(registerOption.equals("1")) {
	        		client.register();
	        		boolean registered=true;
	        		while(registered){
	        			System.out.println("\nChoose below options by its number...");
		        		System.out.println("1-Send.");
			    		System.out.println("2-Check.");
			    		System.out.println("3-Receive.");
			    		System.out.println("4-Audit.");
			    		System.out.println("0-Exit.");
			    		String option = reader.nextLine();
			    		if(option.equals("0")) {
			    			registered=false;
			        	}else if(option.equals("1")) {
			        		if(client.clientHasMessageNotSent()){
			        			System.out.println("Resending last message...");
			        			client.reSend();
			        		}
			        		else{
				        		System.out.println("Amount:");
				        		String sendAmount = reader.nextLine();
				        		System.out.println("Destination (Available: Alice Bob Charly; you are "+client.getClientName().toUpperCase()+"):");
				        		String sendDestination = reader.nextLine();
				        		client.send(sendDestination, sendAmount);
			        		}
		    			}else if(option.equals("2")) {
			    			System.out.println("Destination (Available: Alice Bob Charly; you are "+client.getClientName().toUpperCase()+"):");
			    			String destination = reader.nextLine();
			    			client.check(destination.toLowerCase().trim());
			    		}else if(option.equals("3")) {
			    			System.out.println("Choose pending transfer by number and space (e.g. '1 2 3')");
			    			String pendingTransfer = reader.nextLine();
			        		client.receive(pendingTransfer);
			    		}else if(option.equals("4")) {
			    			System.out.println("Destination (Available: Alice Bob Charly; you are "+client.getClientName().toUpperCase()+"):");
			    			String destination = reader.nextLine();
			        		client.audit(destination.toLowerCase().trim());
			    		}else {
			    			System.out.println("The '"+option+ "' is not valid!");
			    		}
	        		}
	        	}else {
	    			System.out.println("The '"+registerOption+ "' is not valid!");
	    		}
    		}
	    	System.out.println("\nGoodBye! See you next time!");
    	}
    }
}
