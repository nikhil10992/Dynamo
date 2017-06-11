package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import static edu.buffalo.cse.cse486586.simpledynamo.MessageTypes.*;

/**
 * Created by Nikhil on 6/1/2017.
 */

class ServerTask extends AsyncTask<ServerSocket, String, Void> {
    private static final String TAG = "SERVER";
    private static final SimpleDynamoProvider providerObject = new SimpleDynamoProvider();

    @Override
    protected Void doInBackground(ServerSocket... sockets) {

        // Declare sockets and streams.
        ServerSocket serverSocket = sockets[0];
        ObjectInputStream objectInputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            while (true) {
                Socket receiverSocket = serverSocket.accept();
                try {
                    // Get the message object sent by the client.
                    objectInputStream = new ObjectInputStream(receiverSocket.getInputStream());
                    MessageObject receivedMsgObj = (MessageObject) objectInputStream.readObject();

                    Log.d(TAG, "Request received at :P: "  + receivedMsgObj.getReceiverPort() + " of type :MT: "
                            + receivedMsgObj.getMessageType() + " :K: " + receivedMsgObj.getKey() + " :V: "
                            + receivedMsgObj.getValue() + " from :RT: " + receivedMsgObj.getMessageType()
                            + " :RP: " + receivedMsgObj.getSenderPort());

                    switch (receivedMsgObj.getMessageType()){
                        case C_INSERT:
                            performOperation(receivedMsgObj);
                            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, receivedMsgObj);

                            break;
                    }

                    // Send delivery ack. One Ack should work for all.
                    dataOutputStream = new DataOutputStream(receiverSocket.getOutputStream());
                    dataOutputStream.writeUTF("OK");
                    dataOutputStream.flush();

                } catch (ClassNotFoundException cNFE) {
                    Log.e(TAG, "Server task ClassNotFoundException");
                }
            }
        } catch (UnknownHostException e) {
            Log.e(TAG, "Server task UnknownHostException.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "Server task IOException.");
            e.printStackTrace();
        } finally {
            // Close data out stream.
            if (dataOutputStream != null) {
                try {
                    dataOutputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Server task closing DataOutputStream");
                }
            } // Close input stream.
            if (objectInputStream != null) {
                try {
                    objectInputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Server task closing ObjectInputStream");
                }
            } // Close socket
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Server task closing Socket");
                }
            }
        }
        return null;
    }

    private void performOperation(MessageObject inputObject){
        switch (inputObject.getMessageType()){
            case C_INSERT:
            case TC_INSERT:
            case R_INSERT:
                SharedPreferences.Editor editor = providerObject.getSharedPreferences().edit();
                editor.putString(inputObject.getKey(), inputObject.getValue());
                editor.commit();
                Log.d(TAG, "performOperation: Inserted key value pair: " + inputObject.getKey() + " : " + inputObject.getValue() +  " on " + inputObject.getReceiverPort());
                break;
            case C_QUERY:
            case TC_QUERY:
            case R_QUERY:
                break;
            case QUERY_ALL: // For query all we add messages to input object
                break;
        }
    }
}
