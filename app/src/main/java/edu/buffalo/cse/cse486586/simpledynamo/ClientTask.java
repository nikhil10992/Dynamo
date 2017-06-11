package edu.buffalo.cse.cse486586.simpledynamo;

import android.os.AsyncTask;
import android.util.Log;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by Nikhil on 6/1/2017.
 */

class ClientTask extends AsyncTask<MessageObject, String, MessageObject> {

    private static final String TAG = "CLIENT";

    @Override
    protected MessageObject doInBackground(MessageObject... messages) {
        // Declare sockets and streams.
        Socket destinationSocket = null;
        ObjectOutputStream objectOutputStream = null;
        DataInputStream dataInputStream = null;

        // Get message object and set destination
        MessageObject sendMsgObj = messages[0];
        String destinationPort = sendMsgObj.getReceiverPort();
        String acknowledgement = null;

        try {
            // Create socket for the destination port.
            destinationSocket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}), Integer.parseInt(destinationPort));

            // ObjectOutputStream as we are sending a message object.
            objectOutputStream = new ObjectOutputStream(destinationSocket.getOutputStream());
            objectOutputStream.writeObject(sendMsgObj);
            objectOutputStream.flush();

            destinationSocket.setSoTimeout(500);

            // Acknowledgement handling
            dataInputStream = new DataInputStream(destinationSocket.getInputStream());
            acknowledgement = dataInputStream.readUTF();

        } catch (UnknownHostException e) {
            Log.e(TAG, "doInBackground: Client task UnknownHostException");
            acknowledgement = "UnknownHostException";
        } catch (SocketTimeoutException e) {
            Log.e(TAG, "doInBackground: Client task SocketTimeoutException");
            acknowledgement = "SocketTimeoutException";
        } catch (IOException e) {
            Log.e(TAG, "doInBackground: Client task IOException");
            acknowledgement = "IOException";
        } finally {
            if (acknowledgement.equals("OK")) {
                Log.d(TAG, "doInBackground: Status: " + acknowledgement + " :Port: " + sendMsgObj.getSenderPort() + " :Delivered: "
                        + sendMsgObj.getMessageType() + " :K: " + sendMsgObj.getKey() + " :V: " + sendMsgObj.getValue()
                        + " to :RT: " + sendMsgObj.getReceiverType() + " :R: " + sendMsgObj.getReceiverPort());
            } else{
                Log.d(TAG, "doInBackground: Status: " + acknowledgement + " :Port: " + sendMsgObj.getSenderPort() + " :Failed to Deliver: "
                        + sendMsgObj.getMessageType() + " :K: " + sendMsgObj.getKey() + " :V: " + sendMsgObj.getValue()
                        + " to :RT: " + sendMsgObj.getReceiverType() + " :R: " + sendMsgObj.getReceiverPort());
            }
            // Close output stream
            if (objectOutputStream != null) {
                try {
                    objectOutputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Client task closing ObjectInputStream");
                }
            } // Close input stream.
            if (dataInputStream != null) {
                try {
                    dataInputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Client task closing DataInputStream");
                }
            } // Close socket
            if (destinationSocket != null) {
                try {
                    destinationSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Client task closing Socket");
                }
            }
        }
        return sendMsgObj;
    }

    @Override
    protected void onPostExecute(MessageObject checkObj) {
        super.onPostExecute(checkObj);
    }
}