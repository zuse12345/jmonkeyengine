package com.jme3.network.message;

import com.jme3.network.serializing.Serializable;

/**
 * Represents a disconnect message.
 *
 * @author Lars Wesselius
 */
@Serializable()
public class DisconnectMessage extends Message {
    public static final String KICK = "Kick";
    public static final String USER_REQUESTED = "User requested";
    public static final String ERROR = "Error";
    public static final String FILTERED = "Filtered";

    private String reason;
    private String type;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
