package com.service.frame.rstmeet.type;

public enum CountryType {
    일본("JAPAN"),
    한국("KOREA");
    private String name;

    CountryType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
