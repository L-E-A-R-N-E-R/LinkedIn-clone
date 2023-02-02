/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 * Authors:
 * Emerson Jacobson (862215945)
 * Mohit Porwal (862325163)
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class ProfNetwork {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   /**
    * Creates a new instance of ProfNetwork
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public ProfNetwork (String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end ProfNetwork

   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
         if(outputHeader) {
            for(int i = 1; i <= numCol; i++){
            System.out.print(rsmd.getColumnName(i) + "\t");
            }
            System.out.println();
            outputHeader = false;
         }
         for (int i=1; i<=numCol; ++i)
            System.out.print(rs.getString (i) + "\t");
         System.out.println();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();

      // iterates through the result set and saves the data returned by the query.
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
         List<String> record = new ArrayList<String>();
         for (int i=1; i<=numCol; ++i)
            record.add(rs.getString (i));
         result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       if(rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
      Statement stmt = this._connection.createStatement ();

      ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
      if (rs.next())
         return rs.getInt(1);
      return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            ProfNetwork.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      ProfNetwork esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the ProfNetwork object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new ProfNetwork(dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("\nMAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
               boolean usermenu = true;
               while(usermenu) {
                  System.out.println("MAIN MENU");
                  System.out.println("---------");
                  System.out.println("1. Search People");
                  System.out.println("2. View User Profile");
                  System.out.println("3. Check Incoming Requests");
                  System.out.println("4. View Friends");
                  System.out.println("5. Send Message");
                  System.out.println("6. View Messages");
                  System.out.println("7. Update Profile");
                  System.out.println(".........................");
                  System.out.println("9. Log out");
                  switch (readChoice()){
                     case 1: SearchPeople(esql); break;
                     case 2: ViewUserProfile(esql, authorisedUser); break;
                     case 3: CheckIncomingRequests(esql, authorisedUser); break;
                     case 4: ViewFriends(esql, authorisedUser); break;
                     case 5: SendMessage(esql, authorisedUser); break;
                     case 6: ViewMessages(esql, authorisedUser); break;
                     case 7: UpdateProfile(esql, authorisedUser); break;
                     case 9: usermenu = false; break;
                     default : System.out.println("Unrecognized choice!"); break;
                  }
               }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
   public static void CreateUser(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();
         System.out.print("\tEnter user email: ");
         String email = in.readLine();

	      //Creating empty contact\block lists for a user
	      String query = String.format("INSERT INTO USR (userId, password, email) VALUES ('%s','%s','%s')", login, password, email);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
         System.err.println (e.getMessage ());
      }
   }//end

   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(ProfNetwork esql){
      try{
         System.out.print("\tEnter user login: ");
         String login = in.readLine();
         System.out.print("\tEnter user password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM USR WHERE userId = '%s' AND password = '%s'", login, password);
         int userNum = esql.executeQuery(query);
         if (userNum > 0)
            return login;
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

   public static void SearchPeople(ProfNetwork esql) {
      try {
         System.out.print("Enter name: ");
         String search = in.readLine();
         String query = String.format("SELECT userId, name, email FROM USR U WHERE LOWER(userId) LIKE '%%%s%%' OR LOWER(name) LIKE '%%%s%%'", search.toLowerCase(), search.toLowerCase());
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
         if (results.size() == 0) {
            System.out.println("No results.");
         } else {
            System.out.format("%-50s%-50s%-50s\n", "Username", "Full Name", "Email");
            System.out.format("------------------------------------------------------------------------------------------------------------------------------------------------------\n");
            for (List<String> entry : results) {
               for (String col : entry) {
                  System.out.format("%-50s", (col == null) ? "-----" : col);
               }
               System.out.println();
            }
         }

      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void ViewUserProfile(ProfNetwork esql, String authorisedUser) {
      System.out.print("Enter username of user to view: ");
      try{
         String user = in.readLine();
         String query = String.format("SELECT userId, email, name, dateOfBirth FROM USR WHERE userId = '%s'", user.trim());
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
         if (results.size() == 0) {
            System.out.format("No results for user with username %s\n", user);
            return;
         }
         List<String> info = results.get(0);
         System.out.format("%-50s%-50s%-50s%-50s\n", "Username", "Email", "Name", "Date of Birth");
         for (String i : info) {
            if (i == null) {
               System.out.format("%-50s", "------");
            } else {
               System.out.format("%-50s", i.trim());
            }
         }
         System.out.println();
         // System.out.format("%-50s%-50s%-50s%-50s\n\n", info.get(0).trim(), info.get(1).trim(), info.get(2).trim(), info.get(3).trim());

         System.out.println("What would you like to do?");
         System.out.println("1. Send connection request");
         System.out.println("2. View Friends");
         System.out.println("-------------");
         System.out.println("9. Return");
         switch (readChoice()) {
            case 1:
               // Need to compile list of my friends, friends of my friends, and friends of my friends friends
               // Theres probably a better way to do this with a single sql statement, but I didn't have time to
               // figure it out.
               List<List<String>> friends = esql.executeQueryAndReturnResult(String.format("SELECT F.connectionId FROM CONNECTION_USR F WHERE F.userId = '%s' AND status='Accept'", authorisedUser));

               if (friends.size() < 5) { // If <5 friends, can connect with anyone
                  esql.executeUpdate(String.format("INSERT INTO CONNECTION_USR (userId, connectionId, status) VALUES ('%s', '%s', 'Request')", authorisedUser, user));
                  System.out.println("Request Sent!");
               } else {
                  ArrayList<String> allowedFriends = new ArrayList<String>();
                  for (List<String> friend : friends) {
                     // Get friends of friends, add each one to list of allowedFriends
                     List<List<String>> fofs = esql.executeQueryAndReturnResult(String.format("SELECT F.connectionId FROM CONNECTION_USR F WHERE F.userId = '%s' AND status='Accept'", friend.get(0)));
                     for (List<String> fof : fofs) {
                        allowedFriends.add(fof.get(0));
                        List<List<String>> foffs = esql.executeQueryAndReturnResult(String.format("SELECT F.connectionId FROM CONNECTION_USR F WHERE F.userId = '%s' AND status='Accept'", fof.get(0)));
                        for (List<String> foff : foffs) {
                           allowedFriends.add(foff.get(0));
                        }
                     }
                  }
                  if (allowedFriends.contains(user)) {
                     esql.executeUpdate(String.format("INSERT INTO CONNECTION_USR (userId, connectionId, status) VALUES ('%s', '%s', 'Request')", authorisedUser, user));
                     System.out.println("Request Sent!");
                  } else {
                     System.out.println("Error, this user is outside of your social circle, cannot add as friend!");
                  }
               }
               break;
            case 2:
               List<List<String>> fofs = esql.executeQueryAndReturnResult(String.format("SELECT C.connectionId, name, email, dateOfBirth FROM USR U, CONNECTION_USR C WHERE C.userId = '%s' AND C.connectionId = U.userId AND C.status = 'Accept'" +
                                                                                 " UNION SELECT C.connectionId, name, email, dateOfBirth FROM USR U, CONNECTION_USR C WHERE C.connectionId = '%s' AND C.connectionId = U.userId AND C.status = 'Accept'", user, user));
               if (fofs.size() == 0) {
                  System.out.println("User has no friends!");
               } else {
                  System.out.format("%-50s%-50s%-50s%-50s\n", "Username", "Full Name", "Email", "DateOfBirth");
                  System.out.format("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
                  for (List<String> entry : fofs) {
                     for (String col : entry) {
                        System.out.format("%-50s", (col == null) ? "-----" : col);
                     }
                     System.out.println();
                  }
                  System.out.println();
               }
               break;
            case 9:
               break;
            default:

         }
      } catch(Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void CheckIncomingRequests(ProfNetwork esql, String authorisedUser) {
      String query = String.format("SELECT U.userId, U.name FROM CONNECTION_USR C, USR U WHERE connectionId = '%s' AND C.userId = U.userId AND status = 'Request'", authorisedUser);
      try {
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
         if (results.size() == 0) {
            System.out.println("No incoming connection requests.");
         } else {
            System.out.format("%-50s%-50s\n", "Username", "Name");
            System.out.format("----------------------------------------------------------------------------------------------------\n");
            for (List<String> entry : results) {
               for (String col : entry) {
                  System.out.format("%-50s", (col == null) ? "-----" : col);
               }
               System.out.println();
            }
         }
         while (true) {
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. Accept Request");
            System.out.println("2. Deny Request");
            System.out.println(".......................");
            System.out.println("9. Return to main menu");
            String user;
            switch(readChoice()) {
               case 1:
                  System.out.print("Enter username of user: ");
                  user = in.readLine().replace("\n", "");
                  query = String.format("UPDATE CONNECTION_USR SET status = 'Accept' WHERE connectionId = '%s' AND userId = '%s'", authorisedUser, user);
                  esql.executeUpdate(query);
                  System.out.format("Accepted request from %s!\n", user);
                  break;
               case 2:
                  System.out.print("Enter username of user: ");
                  user = in.readLine();
                  query = String.format("UPDATE CONNECTION_USR SET status = 'Reject' WHERE connectionId = '%s' AND userId = '%s'", authorisedUser, user);
                  esql.executeUpdate(query);
                  System.out.format("Rejected request from %s!\n", user);
                  break;
               case 9: return;
               default: System.out.println("Invalid choice!"); break;
            }
         }
      } catch (Exception e) {
         System.out.println(e.getMessage());
      }
   }

   public static void ViewFriends(ProfNetwork esql, String authorisedUser) {
      String query = String.format("SELECT C.connectionId, name, email, dateOfBirth FROM USR U, CONNECTION_USR C WHERE C.userId = '%s' AND C.connectionId = U.userId AND C.status = 'Accept' " +
                                   " UNION SELECT C.connectionId, name, email, dateOfBirth FROM USR U, CONNECTION_USR C WHERE C.connectionId = '%s' AND C.connectionId = U.userId AND C.status = 'Accept'", authorisedUser, authorisedUser, authorisedUser, authorisedUser);
      try {
         List<List<String>> results = esql.executeQueryAndReturnResult(query);
         if (results.size() == 0) {
            System.out.println("No results.");
         } else {
            System.out.format("%-50s%-50s%-50s%-50s\n", "Username", "Full Name", "Email", "DateOfBirth");
            System.out.format("--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
            for (List<String> entry : results) {
               for (String col : entry) {
                  System.out.format("%-50s", (col == null) ? "-----" : col);
               }
               System.out.println();
            }
         }
         System.out.println();
      } catch (Exception e) {
         System.out.println(e.getMessage());
      }
   }

   public static void SendMessage(ProfNetwork esql, String authorisedUser) {
      try {
         System.out.print("Enter username of user to send message to: ");
         String user = in.readLine();
         System.out.print("Enter message to sent them: ");
         String message = in.readLine();
         esql.executeUpdate(String.format("INSERT INTO MESSAGE (senderId, receiverId, contents, sendTime, deleteStatus, status) VALUES ('%s', '%s', '%s', CURRENT_TIMESTAMP, 0, 'Delivered')", authorisedUser, user, message));
         System.out.println("Message sent!\n");
      } catch (Exception e) {
         System.err.println(e.getMessage());
      }
   }

   public static void ViewMessages(ProfNetwork esql, String authorisedUser) {
      // A -> B
      // deleteStatus = 0  :  Both parties can see message
      // deleteStatus = 1  :  A deleted message, B can still see it
      // deleteStatus = 2  :  B deleted message, A can still see it
      // deleteStatus = 3  :  Both deletes message, neither can see it
      String query = String.format("SELECT senderId FROM MESSAGE WHERE receiverId='%s' AND (deleteStatus=0 OR deleteStatus=1) AND (status='Sent' OR status='Delivered' OR status='Read') " +
                                   "UNION SELECT receiverID FROM MESSAGE WHERE senderId='%s' AND (deleteStatus=0 OR deleteStatus=2) AND (status='Sent' OR status='Delivered' OR status='Read')", authorisedUser, authorisedUser);
      do {
         try{
            List<List<String>> results = esql.executeQueryAndReturnResult(query);
            if (results.size() == 0) {
               System.out.println("You have no messages\n");
               return;
            }
            System.out.println("You have messages from:");
            for (List<String> row : results) {
               System.out.println(row.get(0));
            }
            System.out.println("\nWhat would you like to do?");
            System.out.println("1. View messages from user");
            System.out.println("---------");
            System.out.println("9. Return to main menu");
            switch(readChoice()) {
               case 1:
                  System.out.print("Enter name of user: ");
                  String user = in.readLine();
                  // A -> B
                  // deleteStatus = 0  :  Both parties can see message
                  // deleteStatus = 1  :  A deleted message, B can still see it
                  // deleteStatus = 2  :  B deleted message, A can still see it
                  // deleteStatus = 3  :  Both deletes message, neither can see it
                  String get_msgs = String.format("SELECT msgId,contents,sendTime,senderId as sender FROM MESSAGE WHERE receiverId='%s' AND senderId='%s' AND (deleteStatus=0 OR deleteStatus=1) AND (status='Sent' OR status='Delivered' OR status='Read') " +
                                                  " UNION SELECT msgId,contents,sendTime,senderId as sender FROM MESSAGE WHERE senderId='%s' AND receiverId='%s' AND (deleteStatus=0 OR deleteStatus=2) AND (status='Sent' OR status='Delivered' OR status='Read')", authorisedUser, user, authorisedUser, user);
                  // System.out.println(get_msgs);
                  List<List<String>> messages = esql.executeQueryAndReturnResult(get_msgs);
                  for (List<String> message : messages) {
                     System.out.format("(%s) At %s %s said:\n\t%s\n", message.get(0), message.get(2), message.get(3).trim(), message.get(1));
                  }
                  System.out.println("What would you like to do?");
                  System.out.println("1. Delete message");
                  System.out.println("--------");
                  System.out.println("9. Return");
                  switch(readChoice()){
                     case 1:
                        System.out.print("Enter ID of message to delete: ");
                        String delete_msg = in.readLine();
                        String get_cur_status_q = String.format("SELECT deleteStatus,receiverId,senderId FROM MESSAGE WHERE ((receiverId='%s' AND senderId='%s') OR (senderId='%s' AND receiverId='%s')) AND msgId=%s", authorisedUser, user, authorisedUser, user, delete_msg);
                        List<List<String>> res = esql.executeQueryAndReturnResult(get_cur_status_q);
                        if (res.size() == 0) {
                           System.out.println("That message does not exist");
                           return;
                        }
                        String q = "";
                        String cur = res.get(0).get(0);
                        // Sender (A) = .get(2)
                        // Receiver (B) = .get(1)
                        // A -> B
                        // deleteStatus = 0  :  Both parties can see message
                        // deleteStatus = 1  :  A deleted message, B can still see it
                        // deleteStatus = 2  :  B deleted message, A can still see it
                        // deleteStatus = 3  :  Both deletes message, neither can see it
                        switch (cur) {
                           case "0":
                              if (res.get(0).get(2).trim().equals(authorisedUser)) { // If I am the sender (A)
                                 q = String.format("UPDATE MESSAGE SET deleteStatus='1' WHERE msgId='%s'", delete_msg);
                              } else {
                                 q = String.format("UPDATE MESSAGE SET deleteStatus='2' WHERE msgId='%s'", delete_msg);
                              }
                              break;
                           case "1":
                              q = String.format("UPDATE MESSAGE SET deleteStatus='3' WHERE msgId='%s'", delete_msg);
                              break;
                           case "2":
                              q = String.format("UPDATE MESSAGE SET deleteStatus='3' WHERE msgId='%s'", delete_msg);
                              break;
                        }
                        esql.executeUpdate(q);
                        System.out.println("Message deleted!");
                        break;
                     case 9:
                        return;
                     default:
                        System.out.println("Invalid choice");
                        break;
                  }
                  break;
               case 9:
                  return;
               default:
                  System.out.println("Invalid choice!");
                  break;
            }
         }catch(Exception e) {
            System.out.println(e.getMessage());
         }
      } while(true);
   }

   public static void UpdateProfile(ProfNetwork esql, String authorisedUser) {
      do {
         System.out.println("Update Profile");
         System.out.println("--------------");
         System.out.println("1. Update email");
         System.out.println("2. Update name");
         System.out.println("3. Update password");
         System.out.println(".........................");
         System.out.println("9. Return");
         switch(readChoice()) {
            case 1:
               try {
                  System.out.print("Enter new email: ");
                  String new_email = in.readLine();
                  String q = String.format("UPDATE USR SET email = '%s' WHERE userId = '%s'", new_email, authorisedUser);
                  esql.executeUpdate(q);
                  System.out.println("Updated email successfully!");
               } catch (Exception e) {
                  System.err.println(e.getMessage());
               }
               break;
            case 2:
               try {
                  System.out.print("Enter new name: ");
                  String new_name = in.readLine();
                  String q = String.format("UPDATE USR SET name = '%s' WHERE userId = '%s'", new_name, authorisedUser);
                  esql.executeUpdate(q);
                  System.out.println("Updated name successfully!");
               } catch (Exception e) {
                  System.err.println(e.getMessage());
               }
               break;
            case 3:
               try {
                  System.out.print("Enter new password: ");
                  String new_password = in.readLine();
                  String q = String.format("UPDATE USR SET password = '%s' WHERE userId = '%s'", new_password, authorisedUser);
                  esql.executeUpdate(q);
                  System.out.println("Updated password successfully!");
               } catch (Exception e) {
                  System.err.println(e.getMessage());
               }
               break;
            case 9:
               return;
            default:
               System.out.println("Invalid choice!");
               break;
         }
      } while(true);
   }

// Rest of the functions definition go in here

}//end ProfNetwork
