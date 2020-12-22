package com.google.sps.data;

import com.google.common.collect.ImmutableList;
import com.google.maps.model.LatLng;

public class Test {
    private static final LatLng LOCATION = new LatLng(32.08074, 34.78059);
    private static final float RATING = 4;
    private static final int PRICE_LEVEL_INT = 2; // used for Places and UserPreferences
    private static final ImmutableList<String> CUISINES = ImmutableList.of("sushi", "hamburger");
    private static final boolean OPEN_NOW = true;


    private static final UserPreferences PREFERENCES =
        UserPreferences.builder()
        .setMinRating(RATING)
        .setMaxPriceLevel(PRICE_LEVEL_INT)
        .setLocation(LOCATION)
        .setCuisines(CUISINES)
        .setOpenNow(OPEN_NOW)
        .build();

    static final SearchRequestGenerator SEARCH_REQUEST_GENERATOR =
        new SearchRequestGeneratorImpl(GeoContext.getGeoApiContext());

    public static void main(String[] args) {
        PlacesFetcher fetcher = new PlacesFetcher(GeoContext.getGeoApiContext(), SEARCH_REQUEST_GENERATOR);
        ImmutableList<Place> results = fetcher.fetch(PREFERENCES);
        for (Place place : results) {
            System.out.println(place.name());
        }
    }
}
