package edu.buffalo.cse.cse486586.simpledynamo;
import java.util.HashMap;

/**
 * Created by Nikhil on 6/1/2017.
 */

class MessageObject {
    private String messageType;
    private String receiverType;
    private String key;
    private String value;
    private String senderPort;
    private String receiverPort;
    private String coordinatorPort;
    private String fReplicaPort;
    private String sReplicaPort;
    private boolean failed;
    private boolean operationPending;
    private boolean acknowledged;
    private HashMap<String, String> valueStore;

    public MessageObject() {
        this.acknowledged = false;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getSenderPort() {
        return senderPort;
    }

    public void setSenderPort(String senderPort) {
        this.senderPort = senderPort;
    }

    public String getReceiverPort() {
        return receiverPort;
    }

    public void setReceiverPort(String receiverPort) {
        this.receiverPort = receiverPort;
    }

    public String getCoordinatorPort() {
        return coordinatorPort;
    }

    public void setCoordinatorPort(String coordinatorPort) {
        this.coordinatorPort = coordinatorPort;
    }

    public String getFReplicaPort() {
        return fReplicaPort;
    }

    public void setFReplicaPort(String fReplicaPort) {
        this.fReplicaPort = fReplicaPort;
    }

    public String getSReplicaPort() {
        return sReplicaPort;
    }

    public void setSReplicaPort(String sReplicaPort) {
        this.sReplicaPort = sReplicaPort;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean isOperationPending() {
        return operationPending;
    }

    public void setOperationPending(boolean operationPending) {
        this.operationPending = operationPending;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public HashMap<String, String> getValueStore() {
        return valueStore;
    }

    public void setValueStore(HashMap<String, String> valueStore) {
        this.valueStore = valueStore;
    }
}
