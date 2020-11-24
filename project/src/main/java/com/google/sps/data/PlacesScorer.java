package com.google.sps.data;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import com.google.maps.model.LatLng;

public class PlacesScorer {

    // The coefficients for the scoring algorithm, sum up to 1.
    private static final double RATING_WEIGHT = 0.7;
    private static final double DURATION_WEIGHT = 0.3;

    // The maximum possible rating as defined by the Google Places API
    private static final double MAX_RATING = 5;

    // The maximum durations in seconds,
    // so that any duration higher than that will not contribute to the place's score.
    private static final double MAX_DURATION_SECONDS = 40 * 60;

    // A mapping between a place and the driving duration from the place to the user's location.
    private ImmutableMap<Place, Double> durations;

    // The list of places which their score is calculated.
    private ImmutableList<Place> places;

    /**
     * Constructor for PlacesScorer, calculates the durations map used for scoring.
     *
     * @param placesToScore A list of places we want to calculate their score
     * @param userLocation The user's physical location used for duration calculations
     */
    public PlacesScorer(ImmutableList<Place> placesToScore, LatLng userLocation) {
        this.places = placesToScore;
        this.durations = getDurations(userLocation);
    }

   /**
   * Returns a map of a place and the score the place gets based on a scoring algorithm.
   * @return A map between a place to a double representing the place’s score
   */
    public ImmutableMap<Place, Double> getScores() {
        ImmutableMap.Builder<Place, Double> scores = new ImmutableMap.Builder<>();
        for (Place place : places) {
            scores.put(place, calcScore(place));
        }
        return scores.build();
    }

    private double calcScore(Place place) {
        return
            RATING_WEIGHT * (place.rating() / MAX_RATING)
            + DURATION_WEIGHT * Math.max(1 - (durations.get(place) / MAX_DURATION_SECONDS), 0);
    }

    // Returns the duration in seconds from each place on places list to the destination
    private ImmutableMap<Place, Double> getDurations(LatLng destination) {
        // TODO (M2): This function will call the Distance matrix API to calculate the durations.
        //Duration are hardcoded to 30 minutes temporarly.
        Double duration = 1800D;
        return places.stream()
            .collect(ImmutableMap.toImmutableMap(place -> place, place -> duration));
    }
}
