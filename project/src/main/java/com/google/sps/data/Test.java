package com.google.sps.data;

import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.google.maps.model.LatLng;

public class Test {
    private static final float RATING = 4;
    private static final int PRICE_LEVEL = 2;
    private static final LatLng LOCATION = new LatLng(32.08074, 34.78059);
    private static final ImmutableList<String> CUISINES = ImmutableList.of("sushi", "hamburger");
    private static final boolean OPEN_NOW = true;

    private static UserPreferences userPrefs = UserPreferences.builder()
        .setMinRating(RATING)
        .setMaxPriceLevel(PRICE_LEVEL)
        .setLocation(LOCATION)
        .setCuisines(CUISINES)
        .setOpenNow(OPEN_NOW).build();



    private static Place place1 = Place.builder()
        .setName("name1")
        .setWebsiteUrl("website.com")
        .setPhone("+97250-0000-000")
        .setRating(3)
        .setPriceLevel(PRICE_LEVEL)
        .setLocation(LOCATION)
        .setGoogleUrl("googleurl.com")
        .setPlaceId("ChIJN1t_tDeuEmsRUsoyG83frY4")
        .setBusinessStatus(BusinessStatus.OPERATIONAL).build();

    private static Place place2 = Place.builder()
        .setName("name2")
        .setWebsiteUrl("website.com")
        .setPhone("+97250-0000-000")
        .setRating(4)
        .setPriceLevel(PRICE_LEVEL)
        .setLocation(LOCATION)
        .setGoogleUrl("googleurl.com")
        .setPlaceId("ChIJN1t_tDeuEmsRUsoyG83frY4")
        .setBusinessStatus(BusinessStatus.OPERATIONAL).build();

    public static void main(String[] args) throws Exception {
      ImmutableList<Place> places = new PlacesFetcher().fetch(UserPreferences.builder()
        .setMinRating(RATING)
        .setMaxPriceLevel(PRICE_LEVEL)
        .setLocation(LOCATION)
        .setCuisines(CUISINES)
        .setOpenNow(OPEN_NOW).build());
     new PlacesScorer(places, new LatLng(32.08074, 34.78059)).getScores();

     /** ImmutableList<Place> places = Places.scoreSort(ImmutableList.of(place1, place2), userPrefs.location());
     for(Place place: places) {
       System.out.println(place.name());
     } */

     ImmutableList<Place> placesFetched = new PlacesFetcher().fetch(userPrefs);
     for (Place p: placesFetched) {
         System.out.println(p.googleUrl());
     }

    }
}

