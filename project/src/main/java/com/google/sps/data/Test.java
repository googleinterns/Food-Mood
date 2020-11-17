package com.google.sps.data;

import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.google.maps.model.LatLng;

public class Test {
    private static final float RATING = 4;
    private static final int PRICE_LEVEL = 2;
    private static final LatLng LOCATION = new LatLng(40.7581, -73.9855);
    private static final ImmutableList<String> CUISINES = ImmutableList.of("blah");
    private static final boolean OPEN_NOW = true;

    public static void main(String[] args) throws Exception {
      /* new PlacesFetcher().fetch(UserPreferences.builder()
        .setMinRating(RATING)
        .setMaxPriceLevel(PRICE_LEVEL)
        .setLocation(LOCATION)
        .setCuisines(CUISINES)
        .setOpenNow(OPEN_NOW).build());*/

        BusinessStatus x = BusinessStatus.valueOf(Objects.toString("OPERATIONAL", "UNKNOWN"));
        System.out.println(x);
     }
}
