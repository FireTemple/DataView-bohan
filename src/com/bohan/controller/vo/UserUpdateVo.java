package com.bohan.controller.vo;

import java.io.Serializable;

/**
 * @ClassName UserUpdateVo
 * @Description TODO
 * @Author bohanxiao
 * @Data 3/6/21 7:14 PM
 * @Version 1.0
 **/
public class UserUpdateVo implements Serializable {

    private String username;
    private String email;
    private String organization;
    private String jobTitle;
    private String country;
    private String dropboxToken;

    public UserUpdateVo() {
    }

    public String getDropboxToken() {
        return dropboxToken;
    }

    public void setDropboxToken(String dropboxToken) {
        this.dropboxToken = dropboxToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public String getJobTitle() {
        return jobTitle;
    }

    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        return "UserUpdateVo{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", organization='" + organization + '\'' +
                ", jobTitle='" + jobTitle + '\'' +
                ", country='" + country + '\'' +
                ", dropboxToken='" + dropboxToken + '\'' +
                '}';
    }
}
