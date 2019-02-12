import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 * SMTPClient ... goes with SMTPClient
 * Allows connections to a server to send mail
 * Can also retrieve mail from the server 
 * @author Kyrren Love, Brenden Apo, Visalakshi Natulapati & Anthony Fierce
 * @version 4-30-2017
 */
 
public class SMTPClient extends JFrame implements ActionListener{
   private JButton jbConnect = new JButton("Connect");
   private JTextField jtfServerIP = new JTextField(20);
   private JTextArea jta = new JTextArea(10,20);
   public static final long serialVersionUid = 01L;
   private boolean encrption = false;
   private boolean valid = true;
   
  //login gui
   private JFrame jf1;
   private JPanel jpN;
   private JTextField jtfUsername = new JTextField(20);
   private JTextField jtfPassword = new JTextField(20);
   private JTextField jtfEmailAdd = new JTextField(25);
   private JButton jbLogin = new JButton("Login");
   private JLabel jlUser = new JLabel("Username");
   private JLabel jlPassword = new JLabel("Password");
   private JLabel jlEmailAdd = new JLabel("Email Address");
   
  //Message gui
   private JFrame jf2;
   private JLabel jlFrom;
   private JTextField jtfFrom = new JTextField(25);
  //message to label
   private JLabel jlTo;
   private JTextField jtfTo = new JTextField(25);
   private JTextArea jtaMessage = new JTextArea(17, 35);
  //CC address text field(optional)
   private JLabel jlCCAddress = new JLabel("CC Address: ");
   private JTextField jtfCCAddress = new JTextField(25);
  //(Date label and text fields info)
   private JLabel jlDate = new JLabel("Date: ");
   private JTextField jtfDate = new JTextField(7);
  //subject text field and label info
   private JLabel jlSubject = new JLabel("Subject: ");
   private JTextField jtfSubject = new JTextField(15);

   private PrintWriter pwt;
   private Scanner scan;
   //private MailConstants mail;
   
   private JButton jbSendMail = new JButton("Send Mail");

   public static final int SERVER_PORT = 42069;
   private Socket socket = null;
   private MailConstants mail;
   
   private boolean inval=true;

  
   public static void main(String[] args) 
   {
      new SMTPClient();
   }//main

  
   public SMTPClient() 
   {
      this.setTitle("SMTP Client");
      this.setSize(475, 300);
      this.setLocation(100, 50);
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      this.setResizable(false);
      
      JPanel jpN = new JPanel();
         jpN.add(jtfServerIP);
         jpN.add(jbConnect);
         jbConnect.addActionListener(this);
      this.add(jpN, BorderLayout.NORTH);
      
      JPanel jpC = new JPanel();
         jpC.add(jta);
      this.add(jpC, BorderLayout.CENTER);
      
      jbLogin.addActionListener(this);
      jbSendMail.addActionListener(this);
      this.setVisible(true);
   }//constructor

   /** 
    * Button dispatcher
    */
    public void actionPerformed(ActionEvent ae){
      switch(ae.getActionCommand()){
         case "Connect":
            doConnect();
            break;
         case "Disconnect":
            doDisconnect();
            break;
         case "Login":
            doLogin();
            break;
         case"Send Mail":
            sendMail();
            System.out.println("Sent mail to server");
            break;
      }
   }

   /**
    * doConnect - Connect button
    */
   private void doConnect(){
      try{
         socket = new Socket(jtfServerIP.getText(), SERVER_PORT);
         pwt = new PrintWriter(socket.getOutputStream());
         scan = new Scanner(socket.getInputStream());
      }
      catch(IOException ioe){ioe.printStackTrace();}
      jta.append("Connected!\n");
      jbConnect.setText("Disconnect");
      login();
   }

   /**
    * doDisconnect - Disconnect button'
    */
   private void doDisconnect() {
      try {
         // Close the socket and streams
         socket.close();
         pwt.close();
         scan.close();
      }
      catch(IOException ioe) {ioe.printStackTrace();}
      jbConnect.setText("Connect");
   }
   
   public void login(){
      System.out.println("Login gui");
      jf1 = new JFrame();
      
      jpN = new JPanel(new GridLayout(0,1));
      jpN = new JPanel(new GridLayout(0,1));
         jpN.add(jlUser);
         jpN.add(jtfUsername);
         jpN.add(jlPassword);
         jpN.add(jtfPassword);
         jpN.add(jlEmailAdd);
         jpN.add(jtfEmailAdd);
      jf1.add(jpN, BorderLayout.NORTH);
      
      JPanel jpS = new JPanel();
         jpS.add(jbLogin);
      jf1.add(jpS, BorderLayout.SOUTH);
      
      jf1.setTitle("SMTP Client Login");
      jf1.setSize(450,300);
      jf1.setLocation(270,400);
      jf1.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
      jf1.setVisible(true);
   }
   
   public void composeMessage(){
      System.out.println("Send message gui");
      jf2 = new JFrame();
      jlFrom = new JLabel("From: ");
      jlTo = new JLabel("To: ");
      //JLabel jlServer = new JLabel("To Server: ");
      
      JPanel jpNorth=new JPanel();
      JPanel jpCenter = new JPanel();
      jpNorth.setLayout(new GridLayout(0,2));
      //add all labels and textfield to the JFrame
      jpNorth.add(jlFrom);
      jpNorth.add(jtfFrom);
      jtfFrom.setText(jtfEmailAdd.getText());
      
      jpNorth.add(jlTo);
      jpNorth.add(jtfTo);
      
      jpNorth.add(jlCCAddress);
      jpNorth.add(jtfCCAddress);
      
      jpNorth.add(jlDate);
      jpNorth.add(jtfDate);
      
      jpNorth.add(jlSubject);
      jpNorth.add(jtfSubject);
      
      jpCenter.add(new JScrollPane(jtaMessage));
      
      JPanel jpSouth = new JPanel();
      jpSouth.add(jbSendMail);
      
      jf2.add(jpNorth, BorderLayout.NORTH);
      jf2.add(jpCenter, BorderLayout.CENTER);
      jf2.add(jpSouth, BorderLayout.SOUTH);
      
      jf2.setTitle("SMTP Client Message");
      jf2.setSize(650,500);
      jf2.setLocation(900,50);
      jf2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
      jf2.setVisible(true);
   }
   
   public static String getIp() throws Exception {
       URL whatismyip = new URL("http://checkip.amazonaws.com");
       BufferedReader in = null;
       try {
           in = new BufferedReader(new InputStreamReader(
                   whatismyip.openStream()));
           String ip = in.readLine();
           return ip;
       } finally {
           if (in != null) {
               try {
                   in.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
       }
   }
   
   public void sendMail()
   {
     //while(valid=true){
      try{
         pwt.println("HELO " + getIp());
         pwt.flush();
         String msgneg = scan.nextLine();
         if(msgneg.contains("250")){
            System.out.println("Recieved HELLO for mail");   
         }
         
         pwt.println("MAIL FROM:<" + jtfFrom.getText() + ">");
         pwt.flush(); 
         String msg = scan.nextLine();
         if(msg.contains("250")){
            System.out.println("Recieved OK for mail");   
         } 
         
         pwt.println("RCPT TO:<" + jtfTo.getText() + ">");
         pwt.flush(); 
         String msg2 = scan.nextLine();
         if(msg2.contains("250")){
            System.out.println("Recieved Okay for RCPT");   
         }
         
         pwt.println("DATA");
         pwt.flush();
         String msg3 = scan.nextLine();
         if(msg3.contains("354")){
            return;
         }
         pwt.println("FROM:<" + jtfFrom.getText() + ">");
         pwt.println("TO:<" + jtfTo.getText() + ">");
         pwt.println(jtfCCAddress.getText());
         pwt.println("Date: " + jtfDate.getText());
         pwt.println("Subject: " + jtfSubject.getText());
         pwt.println(jtaMessage.getText());
         pwt.println(".");
         pwt.flush();
         //msg3 = scan.nextLine();
         System.out.println(msg3);
         if(msg3.contains("250")){
            System.out.println("Recieved OK for DATA");
            pwt.println("QUIT");
            pwt.flush();
            msg3=scan.nextLine();
            if(msg3.contains("221")){
               System.out.println("We Lit Blair");
            }
         }
        }//try
      catch(Exception e){e.printStackTrace();}
      //}
   }//sendMail

   /**
    * doSend - Send button'
    */
   private void doLogin() 
   {
      // Get the sentence, send to server, wait for reply
      pwt.println(jtfUsername.getText());
      pwt.println(jtfPassword.getText());
      pwt.flush();
      jta.append("Sent: "+jtfPassword.getText() + "\n");
      String reply = scan.nextLine();
      jta.append("Reply: " + reply + "\n");
      if(reply.equals("ACCEPTED")){
         composeMessage();
      }
   }
}//class