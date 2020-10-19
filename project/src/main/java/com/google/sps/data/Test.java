package com.google.sps.data;

import com.google.common.collect.ImmutableList;
import com.google.maps.model.LatLng;

public class Test {
    private static final float RATING = 4;
    private static final int PRICE_LEVEL = 2;
    private static final LatLng LOCATION = new LatLng(32.08074, 34.78059);
    private static final ImmutableList<String> CUISINES = ImmutableList.of();
    private static final boolean OPEN_NOW = true;

    public static void main(String[] args) throws Exception {
        new PlacesFetcher().fetch(UserPrefrences.builder()
        .setMinRating(RATING)
        .setMaxPriceLevel(PRICE_LEVEL)
        .setLocation(LOCATION)
        .setCuisines(CUISINES)
        .setOpenNow(OPEN_NOW).build());
     }
}
