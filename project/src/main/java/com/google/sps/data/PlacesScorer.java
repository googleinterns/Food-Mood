package com.google.sps.data;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.lang.Math;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.google.maps.model.LatLng;

public class PlacesScorer {

    private static final double RATING_WEIGHT = 0.7;
    private static final double DURATION_WEIGHT = 0.3;
    private static final double MAX_RATING = 5;
    private static final double MAX_DURATION_SECONDS = 60 * 60;
    private ImmutableMap<Place, Double> durations;
    private ImmutableList<Place> places;

    public PlacesScorer(ImmutableList<Place> places, LatLng userLocation) {
        this.durations = getDurations(places, userLocation);
        this.places = places;
    }

   /**
   * Returns a map of a place and the score the place gets based on a scoring algorithm.
   * @param places: A list of places we want to calculate their score
   * @return A map between a place to a double representing the place’s score
   */
    public ImmutableMap<Place, Double> getScores() {
        Map<Place, Double> scores = new HashMap<>();
        for (Place place: places) {
            scores.put(place, calcScore(place));
        }
        return ImmutableMap.copyOf(scores);
    }

    private double calcScore(Place place) {
        return
            RATING_WEIGHT * (place.rating() / MAX_RATING) +
            DURATION_WEIGHT * Math.max(1 - (durations.get(place) / MAX_DURATION_SECONDS), 0);
    }

    // Returns the duration in seconds from each place on places list to the destination
    private ImmutableMap<Place, Double> getDurations(ImmutableList<Place> places, LatLng destination) {
        //TODO (M2): This function will call the Distance matrix API to calculate the durations.
        //Duration are hardcoded to 30 minutes temporarly.
        Double duration = 1800D;
        Map<Place, Double> placesDurations = places.stream()
            .collect(Collectors.toMap(place -> place, place -> duration));

        return ImmutableMap.copyOf(placesDurations);
    }
}
