package com.ucv.ace.socialmediaplatform.model;


public class ModelPost {
    String ptime, pcomments;
    String title;
    String uemail;
    String uid;

    String uimage;
    String uname, plike;
    String description;

    String udp;
    String pid;

    public String getUdp() {
        return udp;
    }

    public String getDescription() {
        return description;
    }

    public String getPtime() {
        return ptime;
    }

    public String getTitle() {
        return title;
    }

    public String getUemail() {
        return uemail;
    }

    public String getUid() {
        return uid;
    }

    public String getPlike() {
        return plike;
    }

    public String getPcomments() {
        return pcomments;
    }

    public String getUimage() {
        return uimage;
    }

    public ModelPost() {
    }

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
