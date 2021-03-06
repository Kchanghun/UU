package com.example.uu;

import java.util.HashMap;
import java.util.Map;

public class userObject {
    private String userId;
    private String defaultPwd;
    private String userProfileUrl;
    private String userGender;
    private String userName;
    private String idToken;
    private String currentCrew;
    private String crewRole;
    private int userLevel;
    private int userRecruitJoinNumber;
    private Map<String, Object> recruitList = new HashMap<String, Object>();

    public String getCrewRole() {
        return crewRole;
    }

    public void setCrewRole(String crewRole) {
        this.crewRole = crewRole;
    }

    public Map<String, Object> getRecruitList() {
        return recruitList;
    }

    public void setRecruitList(Map<String, Object> recruitList) {
        this.recruitList = recruitList;
    }

    public String getCurrentCrew() {
        return currentCrew;
    }

    public void setCurrentCrew(String currentCrew) {
        this.currentCrew = currentCrew;
    }

    public int getUserLevel() {
        return userLevel;
    }

    public void setUserLevel(int userLevel) {
        this.userLevel = userLevel;
    }

    public int getUserRecruitJoinNumber() {
        return userRecruitJoinNumber;
    }

    public void setUserRecruitJoinNumber(int userRecruitJoinNumber) {
        this.userRecruitJoinNumber = userRecruitJoinNumber;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }

    public userObject() { } //빈 생성자 필수, firebase에서

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDefaultPwd() {
        return defaultPwd;
    }

    public void setDefaultPwd(String defaultPwd) {
        this.defaultPwd = defaultPwd;
    }

    public String getUserProfileUrl() {
        return userProfileUrl;
    }

    public void setUserProfileUrl(String userProfileUrl) {
        this.userProfileUrl = userProfileUrl;
    }
}
