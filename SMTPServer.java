import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 * SMTPServer ... goes with SMTPClient
 * Allows multiple connect/send/disconnect cycles
 * As many clients as we wish ... one thread per client
 * @author Kyrren Love, Brenden Apo, Visalakshi Natulapati & Anthony Fierce
 * @version 4-30-2017
 */
public class SMTPServer extends JFrame {
   // GUI Components
   private JButton jbStart = new JButton("Start");
   private JTextArea jtaLog = new JTextArea(10, 35);
   private JTextArea jtaConnections = new JTextArea(10, 35);
   public static final long serialVersionUid = 01L;

   // Socket stuff
   private ServerSocket sSocket = null;
   public static final int SERVER_PORT = 42069;
   private ObjectOutputStream oos = null;
   private ObjectInputStream ois = null;
   
   private ServerThread serverThread = null;
   private char[] alphabet = new char[26];
   String letters = "abcdefghijklmnopqrstuvwxyz";
   //private int shift = 3;
   private static final int SHIFT=13; 
   private boolean inval = true;
   
   //client stuff
   private Socket cSocket;
   private PrintWriter fw;
   private PrintWriter pwt;
   private Scanner scn;
   private String label = "";
   private String email;
   private String nextMessage;
   private String s220="S: 220 ";//Connect code
   private String s250="S: 250 ";//Okay
   private String s354="S: 354 ";//End data of message
   private String s221="S: 221 ";//goodbye
   private String data2;
   
   private char tempChar = 'A';
   private String encoded = "";
   private boolean valid=true;
   private MailConstants mail;
   
   //vector for username and mail
   /**
    * main - main program
    */
   public static void main(String[] args) 
   {
      new SMTPServer();
   }
   
   /**
    * Constructor, draw and set up GUI
    * Do server stuff
    */
   public SMTPServer() 
   {
      // Window setup
      this.setTitle("SMTPServer");
      this.setSize(450, 500);
      this.setLocation(600, 50);
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      // NORTH components (Start/Stop button)
      JPanel jpNorth = new JPanel();
      jpNorth.setLayout(new FlowLayout(FlowLayout.RIGHT));
      jpNorth.add(jbStart);
      this.add(jpNorth, BorderLayout.NORTH);
      jbStart.addActionListener(
         new ActionListener(){
            public void actionPerformed(ActionEvent ae){
               switch(ae.getActionCommand()) {
                  case "Start":
                     doStart();
                     break;
                  case "Stop":
                     doStop();
                     break;
               }
            }
         } );
   
      // CENTER components
      JPanel jpCenter = new JPanel();
      jpCenter.add(new JScrollPane(jtaLog));
      this.add(jpCenter, BorderLayout.CENTER);
      
      JPanel jpSouth = new JPanel();
      jpSouth.add(new JLabel("Connected Clients: "));
      jpSouth.add(new JScrollPane(jtaConnections));
      this.add(jpSouth, BorderLayout.SOUTH);
      this.setVisible(true);
   } 
   
   public void doStart(){
      for(char i = 0; i < 26; i++) {
         alphabet[i] = (char)('a' + i);
      } 
      jbStart.setText("Stop");
   
      serverThread = new ServerThread();
      serverThread.start();
      jtaLog.append("Server started\n");
   }
   
   public void doStop(){
      jbStart.setText("Start");
      serverThread.stopServer();
   }
   
   
   /**
    * Class ServerThread
    * A thread used to start the server when the Start button is pressed
    * Also, the stop method will stop the server
    */
   class ServerThread extends Thread{
      public void run(){
         // Server stuff ... wait for a connection and process it
         try{sSocket = new ServerSocket(SERVER_PORT);}
         catch(IOException ioe){jtaLog.append("IO Exception (1): "+ ioe);
            return;}
          
         while(true){
            // Socket for the client
            Socket cSocket = null;
            try{cSocket = sSocket.accept();}
            catch(IOException ioe){jtaLog.append("IO Exception (2): "+ ioe);
               return;}   
            // Create a thread for the client
            ClientThread ct = new ClientThread(cSocket);
            ct.start();
            jtaLog.append("Client thread started\n");    
         }
      }
      
      public void stopServer(){
         try{
            sSocket.close();  // This terminates any blocked accepts
            cSocket.close();
            fw.close();
            pwt.close();
            scn.close();
            jtaLog.append(label + "Client disconnected!\n");
         }
         catch(Exception e){jtaLog.append("Exception: " + e);}
      }
   }
   
   /**
    * Class ClientThread
    * A thread PER client to do the server side
    * stuff for one client
    */
   class ClientThread extends Thread {
      // Since attributes are per-object items, each ClientThread has its OWN
      // socket, unique to that client
      private Socket cSocket;
      private String label = "";
   
      // Constructor for ClientThread
      public ClientThread(Socket _cSocket) 
      {
         cSocket = _cSocket;
         label = cSocket.getInetAddress().getHostAddress() + " : " + cSocket.getPort() + " :: ";
      }
      
      // main program for a ClientThread
      public void run() {
         Scanner scn = null;
         PrintWriter pwt = null;
         jtaLog.append(label + "Client connected!\n");
         jtaConnections.append(label+"\n");
         try {
            // Set up IO
            scn = new Scanner(new InputStreamReader(cSocket.getInputStream()));
            pwt = new PrintWriter(new OutputStreamWriter(cSocket.getOutputStream()));
            fw = new PrintWriter(new FileWriter(new File(cSocket.getInetAddress().getHostAddress() + ".txt")));
         }
         catch(IOException ioe) 
         {
            jtaLog.append(label + "IO Exception (ClientThread): "+ ioe + "\n");
            return;
         }
         String username= scn.nextLine();
         String message = scn.nextLine();
         if(message.equals("ISTE121")||message.equals("server")){
            System.out.println("Server sending Accepted");
            pwt.println("ACCEPTED");
            pwt.println("220");
            System.out.println("Sent ACCEPTED");
            pwt.flush();
            synchronized(jtaLog){
               jtaLog.append(label + "Received: " + message + "\n");
               jtaLog.append(label + "Replied: ACCEPTED \n");
            }//synch
         }
         message=scn.nextLine();
         if(message.contains("HELO")){
            pwt.println("250 HELO "+ cSocket.getInetAddress().getHostAddress()+".org");
            pwt.flush();
         }
         Vector<String> recipient = new Vector<String>();
         message=scn.nextLine();
         System.out.println("Recievd MAIL FROM");
         if(message.contains("MAIL")){
            //String from = scn.nextLine();
            String from = message.substring(message.indexOf("<"), message.length()-1);
            pwt.println(s250+"OK");
            pwt.flush();
            jtaLog.append(from+"\n");
            jtaLog.append(s250+"OK \n");
            System.out.println(from);
            fw.println(from);
            fw.println(s250+"OK \n");
            fw.flush();
            //mail.setFrom(from);
         }
         while(valid==true){
            message=scn.nextLine();
            if(message.contains("RCPT")){
               //String rcptTo = scn.nextLine();
               String rcptTo = message.substring(message.indexOf("<"), message.length()-1);
               recipient.add(rcptTo);
               jtaLog.append(rcptTo+"\n");
               jtaLog.append(s250+"OK \n");
               pwt.println(s250+"OK");
               pwt.flush();
               fw.println(rcptTo);
               fw.println(s250+"OK \n");
               fw.flush();
               //mail.setTo(rcptTo);
            }
            message=scn.nextLine();
            System.out.println(message+": Supposed to be DATA");
            if(message.contains("DATA")){ 
               pwt.println("354");
               pwt.flush();
               String data= scn.nextLine(); 
               System.out.println("Recieved data");
               String messageMail="";
               if(data.equals("_ENCRYPTED_")){
                  while(scn.hasNextLine()){
                     data2 = scn.nextLine();
                     jtaLog.append(data2+"\n");
                     messageMail+=(data2+"\n");
                     for(int i=0;i<data2.length();i++){
                        tempChar = data2.charAt(i);
                        int index = letters.indexOf(tempChar);
                        if(index==-1){encoded+= tempChar;}
                        if(tempChar==' '){encoded+=" ";}
                        else{encoded += alphabet[(index + SHIFT) % 26];}
                     }//for
                     if(data2.equals(".")){
                        jtaLog.append(encoded+"\n");
                        System.out.println("Encoded: "+encoded);
                        jtaLog.append(s250+"OK \n");
                        pwt.println(s250+"OK");
                        pwt.flush();
                        fw.println(data);
                        fw.println(s250+"OK \n");
                        fw.flush();
                        System.out.println("processed data");
                        new Relay().start();
                        valid=false;
                     }
                     //mail.setMessage(messageMail);
                     System.out.println("Set mail constants message");
                  }
               }
               else{
                  System.out.println("Not encrypted");
                  while(true){
                     data2 = scn.nextLine();
                     System.out.println("EMAIL MESSAGE-------"+ data2);
                     jtaLog.append(data2+"\n");
                     fw.println(data2);
                     messageMail+=(data2+"\n");
                     if(data2.equals(".")){
                        jtaLog.append(s250+"OK \n");
                        pwt.println(s250+"OK");
                        pwt.flush();
                        fw.println(data);
                        fw.println(s250+"OK \n");
                        fw.flush();
                        data2 = scn.nextLine();
                        if(data2.contains("QUIT")){
                           pwt.println("221 Quit");
                           pwt.flush();
                           System.out.println("Client Quit");
                        }
                        System.out.println("processed data: Relay started");
                        new Relay().start();
                        break;
                     }
                  }
                  //mail.setMessage(messageMail);
                  System.out.println("Set mail constants message");
               }
               
            }
         }
         if(message.equals("MLBX")){
            //return mail back to client.
            pwt.println("You have no mail at this time. Come back later.");
         }
         else{
            System.out.println(message+":: Also Sent DECLINED");
            pwt.println("DECLINED");
            pwt.flush();
            synchronized(jtaLog){
               jtaLog.append(label + "Received: 11111" + message + "\n");
               jtaLog.append(s250+"Replied: OK\n");
            }//synch 
         }
         
      }  
   } // End of inner class
   
   class Relay extends Thread{
      public void run(){
          try{
               Socket relaySocket = new Socket("10.180.100.2", SERVER_PORT);
               Scanner scn2 = new Scanner(new InputStreamReader(relaySocket.getInputStream()));
               PrintWriter pwt2 = new PrintWriter(new OutputStreamWriter(relaySocket.getOutputStream()));
               
               System.out.println("Relay started: ");
               pwt.println("server");
               pwt.println("server");
               pwt.flush();
               jtaLog.append("Relay Sent: ISTE121\n");
               String reply = scn.nextLine();
               jtaLog.append("Relay Reply: " + reply + "\n");
               if(reply.equals("ACCEPTED")){
                  System.out.println("Relay Accepted: ");
                  return;
               }
               pwt.println("HELO");
               pwt.flush();
               String msgneg = scn.nextLine();
               if(msgneg.contains("250")){
                  System.out.println("Recieved HELLO for mail");   
               }
               //pwt.println("MAIL FROM <"+mail.getFrom()+">");
               pwt.println("MAIL FROM <test@10.180.100.6>");
               pwt.flush(); 
               String msg = scn.nextLine();
               if(msg.contains("250")){
                  System.out.println("Recieved OK for mail");   
               } 
               pwt.println("RCPT TO <test@10.180.100.1>");
               pwt.flush(); 
               String msg2 = scn.nextLine();
               if(msg2.contains("250")){
                  System.out.println("Recieved Okay for RCPT");   
               }
               pwt.println("DATA");
               pwt.println("MAIL FROM <test@10.180.100.6>");
               pwt.println("RCPT TO <test@10.180.100.1>");
               pwt.println("test message for relay");
               //pwt.println(mail.getMessage());
               pwt.flush();
               String msg3 = scn.nextLine();
               if(msg3.contains("354")){
                  return;
               }
               //msg3 = scan.nextLine();
               System.out.println(msg3);
               if(msg3.contains("250")){
                  System.out.println("Recieved OK for DATA");
                  System.out.println("Recieved OK for DATA");
                  pwt.println("QUIT");
                  pwt.flush();
                  msg3=scn.nextLine();
                  if(msg3.contains("221")){
                     System.out.println("");
                  }
               }
            }//try
            catch(Exception e){}
      }
   }
}
//Bully: 129.21.69.37
//Apollo: 129.21.100.191
//Me: 10.180.100.6

