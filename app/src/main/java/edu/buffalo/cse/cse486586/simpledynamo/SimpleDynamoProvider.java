package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import static edu.buffalo.cse.cse486586.simpledynamo.MessageTypes.*;

public class SimpleDynamoProvider extends ContentProvider {
    // Network information variables.
    private static final String[] NODE_IDS = new String[] {"5554", "5556", "5558", "5560", "5562"};
    private static HashMap<String, String> REMOTE_PORTS = new HashMap<>();
    private static String[] HASHED_NODE_IDS = new String[5];
    private static final int NUMBER_OF_PORTS = NODE_IDS.length;
    private static final int SERVER_PORT = 10000;

    // Storage Variables.
    private static HashMap<String, String> QUERY_ALL_RESULT = new HashMap<>();
    private static HashMap<String, Integer> INSERT_NOTIFICATIONS = new HashMap<>();

    // Node specific attributes
    private static String myId;
    private static String myPort;
    private static String myHash;
    private static String myFReplicaPort;
    private static String myFReplicaHash;
    private static String mySReplicaPort;
    private static String mySReplicaHash;
    private static String myFCoordinatorPort;
    private static String myFCoordinatorHash;
    private static String mySCoordinatorPort;
    private static String mySCoordinatorHash;
    private static SharedPreferences sharedPreferences = null;

    // Other final variables.
    private static final String TAG = "MIGHT";
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    private static final int QUORUM_BAR = 2;
    private final String PREF_FILE = "SIMPLE_DYNAMO_MESSAGES";

    // Getters and Setters
    public SharedPreferences getSharedPreferences() {
        return sharedPreferences;
    }

    public void setSharedPreferences(SharedPreferences sharedPreferences) {
        SimpleDynamoProvider.sharedPreferences = sharedPreferences;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub

        setupUpNode();

        // Initialize the preferences provider.
        sharedPreferences = getContext().getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);

        // Run the server using the server socket
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, "Can't create a ServerSocket");
        }
        return false;
    }

    private void setupUpNode() {
        // Get Port details
        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        myId = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf(Integer.parseInt(myId) * 2);

        // Setup all the hashing requirements
        for (int i = 0; i < NUMBER_OF_PORTS; i++) {

            String nodeId = NODE_IDS[i];
            String port = String.valueOf(Integer.parseInt(nodeId) * 2);

            try {
                String hash = genHash(nodeId);
                if (nodeId.equals(myId)) {
                    myHash = hash;
                }
                HASHED_NODE_IDS[i] = hash;
                REMOTE_PORTS.put(hash, port);
            } catch (NoSuchAlgorithmException e) {
                Log.d(TAG, "setupUpNode: Problem with hashing.");
            }
        }

        // Sort Hashed Values.
        Arrays.sort(HASHED_NODE_IDS);
        int index = Arrays.binarySearch(HASHED_NODE_IDS, myHash);

        myFReplicaHash = HASHED_NODE_IDS[(index + 1) % NUMBER_OF_PORTS];
        myFReplicaPort = REMOTE_PORTS.get(myFReplicaHash);

        mySReplicaHash = HASHED_NODE_IDS[(index + 2) % NUMBER_OF_PORTS];
        mySReplicaPort = REMOTE_PORTS.get(mySReplicaHash);

        // 4 can be replaced with NUMBER_OF_PORTS - 1
        myFCoordinatorHash = HASHED_NODE_IDS[(index + 4) % NUMBER_OF_PORTS];
        myFCoordinatorPort = REMOTE_PORTS.get(myFCoordinatorHash);

        // 3 can be replaced with NUMBER_OF_PORTS - 1
        mySCoordinatorHash = HASHED_NODE_IDS[(index + 3) % NUMBER_OF_PORTS];
        mySCoordinatorPort = REMOTE_PORTS.get(mySCoordinatorHash);

        Log.d(TAG, "setupUpNode: Node Details :P: " + myId + " :DP: " + myPort + " :HP: " + myHash);
        Log.d(TAG, "setupUpNode: First Successor Details :P: " + myFReplicaPort + " :HP: " + myFReplicaHash);
        Log.d(TAG, "setupUpNode: Second Successor Details :P: " + mySReplicaPort + " :HP: " + mySReplicaHash);
        Log.d(TAG, "setupUpNode: First Coordinator Details :P: " + myFCoordinatorPort + " :HP: " + myFCoordinatorHash);
        Log.d(TAG, "setupUpNode: Second Coordinator Details :P: " + mySCoordinatorPort + " :HP: " + mySCoordinatorHash);
    }

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
        String key = values.getAsString(KEY_FIELD);
        String value = values.getAsString(VALUE_FIELD);

        processOperation(key, value, C_INSERT);

        Integer bufferQuorum = new Integer(0);
        synchronized (bufferQuorum) {
            INSERT_NOTIFICATIONS.put(key, bufferQuorum);
            try {
                bufferQuorum.wait();
            } catch (InterruptedException e) {
                Log.d(TAG, "insert: Interrupted wait for insert of key " + key);
            }
        }
        return uri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    public void processOperation(String key, String value, String operation){
        String hashedKey = null;
        int coordinatorIndex = 0, replica1Index, replica2Index;
        String coordinator = null, replica1 = null, replica2 = null;

        switch (operation) {
            case QUERY_ALL:
                coordinator = myFReplicaPort;
                replica1 = mySReplicaPort;  // We will just be forwarding the query to the next node.
                replica2 = "NA";
                break;

            case GET_FROM_REPLICA:
                coordinator = myFReplicaPort;
                replica1 = "NA";  // We will just be forwarding the query to the next node.
                replica2 = "NA";
                break;

            case GET_FROM_FC:
                coordinator = myFCoordinatorPort;
                replica1 = "NA";
                replica2 = "NA";
                break;

            case GET_FROM_SC:
                coordinator = mySCoordinatorPort;
                replica1 = "NA";
                replica2 = "NA";
                break;

            default :
                try {
                    // Hash the key to be inserted.
                    hashedKey = genHash(key);

                    while (coordinatorIndex < NUMBER_OF_PORTS && hashedKey.compareTo(HASHED_NODE_IDS[coordinatorIndex]) > 0)
                        coordinatorIndex++;

                    coordinatorIndex = coordinatorIndex % NUMBER_OF_PORTS;
                    replica1Index = (coordinatorIndex + 1) % NUMBER_OF_PORTS;
                    replica2Index = (coordinatorIndex + 2) % NUMBER_OF_PORTS;

                    coordinator = REMOTE_PORTS.get(HASHED_NODE_IDS[coordinatorIndex]);
                    replica1 = REMOTE_PORTS.get(HASHED_NODE_IDS[replica1Index]);
                    replica2 = REMOTE_PORTS.get(HASHED_NODE_IDS[replica2Index]);
                } catch (NoSuchAlgorithmException e) {
                    Log.e(TAG, "processOperation: Problem with hashing.");
                }
                break;
        }

        // Build a message for the coordinator.
        MessageObject coordinatorMsgObj = new MessageObject();
        coordinatorMsgObj.setMessageType(operation);
        coordinatorMsgObj.setReceiverType(COORDINATOR);
        coordinatorMsgObj.setKey(key);
        coordinatorMsgObj.setValue(value);
        coordinatorMsgObj.setSenderPort(myPort);
        coordinatorMsgObj.setReceiverPort(coordinator);
        coordinatorMsgObj.setCoordinatorPort(coordinator);
        coordinatorMsgObj.setFReplicaPort(replica1);
        coordinatorMsgObj.setSReplicaPort(replica2);

        sendMessage(coordinatorMsgObj);

        Log.d(TAG, "processOperation: Port: " + myPort + " sending :MT: " + operation + " :K: " + key + " :V: " + value + " to :CORD: " + coordinator);
    }

    private void sendMessage(MessageObject sendMsgObj)
    {
        new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, sendMsgObj);
    }
}
