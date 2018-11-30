package com.haze.sameer.peckati.RecyclerViewFollow;

public class FollowObject {
    private String email;
    private String uid;
    private String profile_pic;

    public FollowObject(String email, String uid, String profile_pic){
        this.email = email;
        this.uid = uid;
        this.profile_pic = profile_pic;
    }

    public String getUid(){
        return uid;
    }
    public void setUid(String uid){
        this.uid = uid;
    }

    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }

    public String getProfile_pic(){
        return profile_pic;
    }
    public void setProfile_pic(String profile_pic){
        this.profile_pic = profile_pic;
    }
}
