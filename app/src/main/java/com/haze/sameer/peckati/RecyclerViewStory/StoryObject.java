package com.haze.sameer.peckati.RecyclerViewStory;

public class StoryObject {
    private String email;
    private String uid;
    private String charOrStory;
    private String profilePic;
    private String username;

    public StoryObject(String username, String uid, String charOrStory, String profilePic){
        this.username = username;
        this.uid = uid;
        this.charOrStory = charOrStory;
        this.profilePic = profilePic;
    }

    public String getUid(){
        return uid;
    }
    public void setUid(String uid){
        this.uid = uid;
    }

    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }

    public String getEmail(){
        return email;
    }
    public void setEmail(String email){
        this.email = email;
    }

    public String getCharOrStory(){
        return charOrStory;
    }
    public void setCharOrStory(String charOrStory){
        this.charOrStory = charOrStory;
    }

    public String getProfilePic(){
        return profilePic;
    }
    public void setProfilePic(String profilePic){
        this.profilePic = profilePic;
    }

    @Override
    public boolean equals(Object obj) {

        boolean same = false;
        if(obj != null && obj instanceof StoryObject){
            same = this.uid == ((StoryObject) obj ).uid;
        }
        return same;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (this.uid == null ? 0 : this.uid.hashCode());
        return result;
    }
}
