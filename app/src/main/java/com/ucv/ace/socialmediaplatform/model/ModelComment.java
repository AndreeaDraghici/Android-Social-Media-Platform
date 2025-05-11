package com.ucv.ace.socialmediaplatform.model;

/**
 * Model class representing a comment on a post in the social media platform.
 * Contains information such as the comment content, time of posting, and user details.
 */
public class ModelComment {

    /**
     * The unique identifier (CID) of the comment.
     */
    String cId;

    /**
     * The content of the comment.
     */
    String comment;

    /**
     * The time the comment was posted.
     */
    String ptime;

    /**
     * The profile picture or display picture (DP) of the user who made the comment.
     */
    String udp;

    /**
     * The unique identifier (UID) of the user who made the comment.
     */
    String uid;

    /**
     * The email of the user who made the comment.
     */
    String uemail;

    /**
     * The name of the user who made the comment.
     */
    String uname;

    /**
     * Default constructor.
     * Initializes an empty instance of the ModelComment class.
     */
    public ModelComment() {
        this.cId = "";
        this.comment = "";
        this.ptime = "";
        this.udp = "";
        this.uemail = "";
    }

    /**
     * Constructor with parameters to initialize a comment object with details.
     *
     * @param cId     The unique identifier of the comment.
     * @param comment The content of the comment.
     * @param ptime   The time the comment was posted.
     * @param udp     The profile picture of the user who made the comment.
     * @param uemail  The email of the user who made the comment.
     * @param uid     The unique identifier of the user who made the comment.
     * @param uname   The name of the user who made the comment.
     */
    public ModelComment(String cId, String comment, String ptime, String udp, String uemail, String uid, String uname) {
        this.cId = cId;
        this.comment = comment;
        this.ptime = ptime;
        this.udp = udp;
        this.uemail = uemail;
        this.uid = uid;
        this.uname = uname;
    }

    /**
     * Gets the unique identifier of the comment.
     *
     * @return The unique identifier of the comment.
     */
    public String getcId() {
        return cId;
    }

    /**
     * Sets the unique identifier of the comment.
     *
     * @param cId The unique identifier to set for the comment.
     */
    public void setcId(String cId) {
        this.cId = cId;
    }

    /**
     * Gets the content of the comment.
     *
     * @return The content of the comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the content of the comment.
     *
     * @param comment The content to set for the comment.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Gets the time the comment was posted.
     *
     * @return The time the comment was posted.
     */
    public String getPtime() {
        return ptime;
    }

    /**
     * Sets the time the comment was posted.
     *
     * @param ptime The time to set for when the comment was posted.
     */
    public void setPtime(String ptime) {
        this.ptime = ptime;
    }

    /**
     * Gets the profile picture of the user who made the comment.
     *
     * @return The profile picture of the user who made the comment.
     */
    public String getUdp() {
        return udp;
    }

    /**
     * Sets the profile picture of the user who made the comment.
     *
     * @param udp The profile picture to set for the user.
     */
    public void setUdp(String udp) {
        this.udp = udp;
    }

    /**
     * Gets the email of the user who made the comment.
     *
     * @return The email of the user who made the comment.
     */
    public String getUemail() {
        return uemail;
    }

    /**
     * Sets the email of the user who made the comment.
     *
     * @param uemail The email to set for the user.
     */
    public void setUemail(String uemail) {
        this.uemail = uemail;
    }

    /**
     * Gets the unique identifier of the user who made the comment.
     *
     * @return The UID of the user who made the comment.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the unique identifier of the user who made the comment.
     *
     * @param uid The UID to set for the user.
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Gets the name of the user who made the comment.
     *
     * @return The name of the user who made the comment.
     */
    public String getUname() {
        return uname;
    }

    /**
     * Sets the name of the user who made the comment.
     *
     * @param uname The name to set for the user.
     */
    public void setUname(String uname) {
        this.uname = uname;
    }
}
