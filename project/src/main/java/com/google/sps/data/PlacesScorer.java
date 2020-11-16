package com.google.sps.data;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class PlacesScorer {

    private static final double RATING_WEIGHT = 0.7;
    private static final double DURATION_WEIGHT = 0.3;
    private static final double MAX_RATING = 5;

   /**
   * Returns a map of place IDs to the score the place gets based on a scoring  algorithm.
   * @param places: A list of places we want to calculate their score
   * @return A map between a place ID to a double representing the place’s score
   */
    public ImmutableMap<String, Double> getScores(ImmutableList<Place> places, LatLng userLocation) {
        Map<String, Double> scores = new HashMap<>();
        Map<String, Integer> durations = getDurations(places, userLocation);
        for (Place place: places) {
            scores.put("placeId", calcScore(place, durations)); // Is it better to have duration as a field so it doesn't need to be passed every time?
        }
        return ImmutableMap.copyOf(scores);
    }

    private double calcScore(Place place, Map<String, Integer> durations) {
        return RATING_WEIGHT * (place.rating() / MAX_RATING) +
        DURATION_WEIGHT * getDurationBucketVal(durations.get("placeId"));
    }

    private Map<String, Integer> getDurations(List<Place> places, LatLng destination){
        //implement with matrix API
    }

    private double getDurationBucketVal(int duration){
        //implement
    }

//TODO: think how to get the userLocation here
//TODO: understand how to translate te int into minutes and how to implement bucketing
}
