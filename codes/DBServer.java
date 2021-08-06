import sqlinterpreter.BaseSqlInterpreter;
import sqlinterpreter.SqlClassification;

import java.io.*;
import java.net.*;
import java.util.*;

class DBServer {
    public DBServer(int portNumber) {
        try {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            System.out.println("Server Listening");
            while(true) {processNextConnection(serverSocket);}
        } catch (IOException ioe) {
            System.err.println(ioe);
        }
    }

    private void processNextConnection(ServerSocket serverSocket) {
        try {
            Socket socket = serverSocket.accept();
            BufferedReader socketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            BufferedWriter socketWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            System.out.println("Connection Established");
            while (true){processNextCommand(socketReader, socketWriter);}

        } catch (IOException ioe) {
            System.err.println(ioe);
        } catch (NullPointerException npe) {
            System.out.println("Connection Lost");
        }
    }

    private void processNextCommand(BufferedReader socketReader, BufferedWriter socketWriter) throws IOException, NullPointerException {
        String incomingCommand = socketReader.readLine();
        //execution
        try {
            SqlClassification sqlDistributor = new SqlClassification();
            BaseSqlInterpreter specificInterpreter = sqlDistributor.getSpecificInterpreter(incomingCommand);
            specificInterpreter.sqlExecution();
            socketWriter.write("[OK]\n");
            if (specificInterpreter.queryResult != null) {
                for (List<String> tableRow : specificInterpreter.queryResult) {
                    StringBuilder values = new StringBuilder();
                    for (String value : tableRow) {
                        values.append(value);
                    }
                    socketWriter.write(values.toString() + "\n");
                }
            }
        } catch (Exception e) {
            socketWriter.write("[ERROR]:" + e.getMessage() + "\n");
        }
        socketWriter.write("\n" + ((char) 4) + "\n");
        socketWriter.flush();
    }

    public static void main(String[] args) {
        DBServer server = new DBServer(8888);
    }

}
