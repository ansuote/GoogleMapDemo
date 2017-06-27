package com.lkl.ansuote.demo.googlemapdemo.base.map.googlemap.mode.geocode;

import java.util.List;

/**
 * Created by huangdongqiang on 31/05/2017.
 */
public class GeocodeBean {
    private List<AddressComponent> address_components;
    private String formatted_address;
    //geometry
    private String place_id;
    private List<String> types;

    public List<AddressComponent> getAddress_components() {
        return address_components;
    }

    public void setAddress_components(List<AddressComponent> address_components) {
        this.address_components = address_components;
    }

    public String getFormatted_address() {
        return formatted_address;
    }

    public void setFormatted_address(String formatted_address) {
        this.formatted_address = formatted_address;
    }

    public String getPlace_id() {
        return place_id;
    }

    public void setPlace_id(String place_id) {
        this.place_id = place_id;
    }

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }
}
