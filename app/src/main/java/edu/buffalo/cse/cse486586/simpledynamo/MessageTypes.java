package edu.buffalo.cse.cse486586.simpledynamo;

/**
 * Created by Nikhil on 6/2/2017.
 */

public final class MessageTypes {
    // Message variables.
    public static final String JOIN = "JOIN";

    public static final String C_INSERT = "C_INSERT";  // Insert message intended for a node serving as the actual coordinator.
    public static final String TC_INSERT = "TC_INSERT"; // Insert message intended for a node serving as a temporary coordinator. (while actual coordinator is down)
    public static final String R_INSERT = "RC_INSERT";  // Insert message intended for a node serving as a replica.

    public static final String C_QUERY = "C_QUERY";  // Insert message intended for a node serving as the actual coordinator.
    public static final String TC_QUERY = "TC_QUERY";  // Insert message intended for a node serving as a temporary coordinator. (while actual coordinator is down)
    public static final String R_QUERY = "R_QUERY";  // Insert message intended for a node serving as a replica.

    public static final String C_DELETE = "C_DELETE";  // Insert message intended for a node serving as the actual coordinator.
    public static final String TC_DELETE = "TC_DELETE";  // Insert message intended for a node serving as a temporary coordinator. (while actual coordinator is down)
    public static final String R_DELETE = "R_DELETE";  // Insert message intended for a node serving as a replica.

    public static final String QUERY_ALL = "QUERY_ALL";
    public static final String COORDINATOR = "COORDINATOR";
    public static final String REPLICA = "REPLICA";
    public static final String GET_FROM_REPLICA = "GET_R"; // As a coordinator get pending messages from your 1st replica.
    public static final String GET_FROM_FC = "GET_FC"; // As a replica get pending messages from your 1st coordinator.
    public static final String GET_FROM_SC = "GET_SC"; // As a replica get pending messages from your 2nd coordinator.
}
