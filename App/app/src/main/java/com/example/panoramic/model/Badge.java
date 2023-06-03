package com.example.panoramic.model;

import java.io.Serializable;

public class Badge implements Serializable {

    public String id;

    @Override
    public String toString() {
        return "Badge{" +
                "id='" + id + '\'' +
                ", url='" + url + '\'' +
                ", qr='" + qr + '\'' +
                ", name='" + name + '\'' +
                ", emoji='" + emoji + '\'' +
                '}';
    }

    public String url;
    public String qr;
    public String name;
    public String emoji;

}
