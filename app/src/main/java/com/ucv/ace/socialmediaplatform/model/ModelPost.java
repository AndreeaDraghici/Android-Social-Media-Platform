package com.ucv.ace.socialmediaplatform.model;

/**
 * Model class representing a post in the social media platform.
 * Contains information about the post such as description, timestamp, comments, likes, and user details.
 */
public class ModelPost {

    /**
     * The time the post was made.
     */
    String ptime;

    /**
     * The number of comments on the post.
     */
    String pcomments;

    /**
     * The title of the post.
     */
    String title;

    /**
     * The email of the user who made the post.
     */
    String uemail;

    /**
     * The unique identifier (UID) of the user who made the post.
     */
    String uid;

    /**
     * The profile image URL or path of the user who made the post.
     */
    String uimage;

    /**
     * The name of the user who made the post.
     */
    String uname;

    /**
     * The number of likes the post has received.
     */
    String plike;

    /**
     * The description or content of the post.
     */
    String description;

    /**
     * The profile picture or display picture (DP) of the user who made the post.
     */
    String udp;

    /**
     * The unique identifier (PID) of the post.
     */
    String pid;

    /**
     * Gets the time the post was made.
     *
     * @return The time the post was made.
     */
    public String getPtime() {
        return ptime;
    }

    /**
     * Gets the number of comments on the post.
     *
     * @return The number of comments on the post.
     */
    public String getPcomments() {
        return pcomments;
    }

    /**
     * Gets the title of the post.
     *
     * @return The title of the post.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the email of the user who made the post.
     *
     * @return The email of the user who made the post.
     */
    public String getUemail() {
        return uemail;
    }

    /**
     * Gets the unique identifier (UID) of the user who made the post.
     *
     * @return The UID of the user.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Gets the number of likes the post has received.
     *
     * @return The number of likes the post has.
     */
    public String getPlike() {
        return plike;
    }

    /**
     * Gets the profile image of the user who made the post.
     *
     * @return The profile image of the user.
     */
    public String getUimage() {
        return uimage;
    }

    /**
     * Gets the name of the user who made the post.
     *
     * @return The name of the user.
     */
    public String getUname() {
        return uname;
    }

    /**
     * Gets the description or content of the post.
     *
     * @return The description of the post.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the profile picture or display picture of the user who made the post.
     *
     * @return The profile picture of the user.
     */
    public String getUdp() {
        return udp;
    }

    /**
     * Gets the unique identifier (PID) of the post.
     *
     * @return The unique identifier of the post.
     */
    public String getPid() {
        return pid;
    }

    /**
     * Default constructor.
     * Initializes an empty instance of the ModelPost class.
     */
    public ModelPost() {
        this.description = "";
        this.pid = "";
        this.pcomments = "";
        this.plike = "";
        this.udp = "";
        this.ptime = "";
        this.title = "";
        this.uemail = "";
        this.uimage = "";
        this.uname = "";
    }

    /**
     * Constructor with parameters to initialize a post object with details.
     *
     * @param description The content or description of the post.
     * @param pid         The unique identifier (PID) of the post.
     * @param ptime       The time the post was created.
     * @param pcomments   The number of comments on the post.
     * @param title       The title of the post.
     * @param uemail      The email of the user who made the post.
     * @param uid         The unique identifier (UID) of the user who made the post.
     * @param uname       The name of the user who made the post.
     * @param plike       The number of likes the post has received.
     * @param udp         The profile picture or display picture of the user.
     * @param uimage      The profile image of the user who made the post.
     */
    public ModelPost(String description, String pid, String ptime, String pcomments, String title, String uemail, String uid, String uname, String plike, String udp, String uimage) {
        this.description = description;
        this.pid = pid;
        this.ptime = ptime;
        this.pcomments = pcomments;
        this.title = title;
        this.uemail = uemail;
        this.uid = uid;
        this.uname = uname;
        this.plike = plike;
        this.udp = udp;
        this.uimage = uimage;
    }
}
