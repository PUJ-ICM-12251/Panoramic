package com.example.panoramic.model;

import com.google.gson.annotations.SerializedName;
/*
@Getter
@AllArgsConstructor
@NoArgsConstructor(force = true)*/
public class
NightClub {
    @SerializedName(value = "Name")
    private String name;
    @SerializedName(value = "Address")
    private String address;
    @SerializedName(value = "Stars")
    private String stars;
    @SerializedName(value = "Ambiente")
    private String ambiente;
    @SerializedName(value = "Tipo")
    private String tipo;
    @SerializedName(value = "Image")
    private String image;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStars() {
        return stars;
    }

    public void setStars(String stars) {
        this.stars = stars;
    }

    public String getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(String ambiente) {
        this.ambiente = ambiente;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
