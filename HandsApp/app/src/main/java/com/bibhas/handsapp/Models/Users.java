package com.bibhas.handsapp.Models;

/**
 * Created by AkshayeJH on 19/06/17.
 */

public class Users {

    private String id;
    private String name;
    private String image;
    private String status;
    private String thumb_image;
    private String mobile_no;
    private String device_token;


    public Users(){}

    public Users(String id, String name, String image, String status, String thumb_image, String mobile_no, String device_token, Long online) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb_image = thumb_image;
        this.mobile_no = mobile_no;
        this.device_token = device_token;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getMobile_no() {
        return mobile_no;
    }

    public void setMobile_no(String mobile_no) {
        this.mobile_no = mobile_no;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }
}
