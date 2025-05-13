package com.ucv.ace.socialmediaplatform.model;

/**
 * Model class representing a user in the social media platform.
 * Contains user information like name, online status, email, profile image, and more.
 */
public class ModelUsers {

    /**
     * The name of the user.
     */
    String name;

    /**
     * The online status of the user (e.g., "online", "offline").
     */
    String onlineStatus;

    /**
     * The user the current user is typing to.
     */
    String typingTo;

    /**
     * The email of the user.
     */
    String email;

    /**
     * The profile image URL of the user.
     */
    String image;

    /**
     * The unique identifier (UID) of the user.
     */
    String uid;

    /**
     * The cover image URL of the user.
     */
    String cover;

    /**
     * The date of birth of the user.
     */
    private String about;

    /**
     * The date of birth of the user.
     */
    private String website;


    /**
     * Default constructor.
     * Initializes an empty instance of the ModelUsers class.
     */
    public ModelUsers() {
        this.email = "";
        this.name = "";
        this.onlineStatus = "";
        this.typingTo = "";
        this.uid = "";
        this.image = "";
        this.cover = "";
        this.about = "";
        this.website = "";
    }

    /**
     * Constructor with parameters to initialize a user object with details.
     *
     * @param name         The name of the user.
     * @param onlineStatus The online status of the user.
     * @param typingTo     The user that this user is typing to.
     * @param email        The email of the user.
     * @param image        The profile image of the user.
     * @param uid          The unique identifier (UID) of the user.
     * @param cover        The cover image URL of the user.
     */
    public ModelUsers(String name, String onlineStatus, String typingTo, String email, String image, String uid, String cover, String about, String website) {
        this.name = name;
        this.onlineStatus = onlineStatus;
        this.typingTo = typingTo;
        this.email = email;
        this.image = image;
        this.uid = uid;
        this.cover = cover;
        this.about = about;
        this.website = website;

    }

    /**
     * Gets the cover image URL of the user.
     *
     * @return The cover image URL.
     */
    public String getCover() {
        return cover;
    }

    /**
     * Sets the cover image URL of the user.
     *
     * @param cover The cover image URL to set.
     */
    public void setCover(String cover) {
        this.cover = cover;
    }

    /**
     * Gets the name of the user.
     *
     * @return The name of the user.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the user.
     *
     * @param name The name to set for the user.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the user that this user is typing to.
     *
     * @return The user that this user is typing to.
     */
    public String getTypingTo() {
        return typingTo;
    }

    /**
     * Sets the user that this user is typing to.
     *
     * @param typingTo The user to set that this user is typing to.
     */
    public void setTypingTo(String typingTo) {
        this.typingTo = typingTo;
    }

    /**
     * Gets the email of the user.
     *
     * @return The email of the user.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email of the user.
     *
     * @param email The email to set for the user.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the profile image of the user.
     *
     * @return The profile image URL or path of the user.
     */
    public String getImage() {
        return image;
    }

    /**
     * Sets the profile image of the user.
     *
     * @param image The profile image URL or path to set for the user.
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Gets the unique identifier (UID) of the user.
     *
     * @return The UID of the user.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the unique identifier (UID) of the user.
     *
     * @param uid The UID to set for the user.
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getOnlineStatus() {
        return onlineStatus;
    }

    public void setOnlineStatus(String onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }
}
