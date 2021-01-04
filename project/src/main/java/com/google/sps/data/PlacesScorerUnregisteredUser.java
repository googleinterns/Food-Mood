package com.google.sps.data;

import java.io.IOException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;

public class PlacesScorerUnregisteredUser implements PlacesScorer {

    // The coefficients for the scoring algorithm, sum up to 1.
    private static final double RATING_WEIGHT = 0.7;
    private static final double DURATION_WEIGHT = 0.3;

    // The maximum possible rating as defined by the Google Places API.
    private static final double MAX_RATING = 5;

    // The maximum durations in seconds, so that any duration higher than that
    // will not contribute to the place's score.
    private static final double MAX_DURATION_SECONDS = 40 * 60;

    // A duratoins techer used for calculating driving durations.
    private DurationsFetcher durationsFetcher;

    /**
     * PlacesScorerUnregisteredUser constructor.
     *
     * @param geoApiContext the GeoApiContext used for all Google GEO API requests
     */
    public PlacesScorerUnregisteredUser(GeoApiContext geoApiContext) {
        this.durationsFetcher = new DurationsFetcher(geoApiContext);
    }

    /**
     * {@inheritDoc}
     *
     * Calculates scores based on driving duration to the user’s location and rating
     */
    @Override
    public ImmutableMap<Place, Double> getScores(
            ImmutableList<Place> places, LatLng userLocation) {
        ImmutableMap<Place, Long> durations;
        try {
            durations =
                durationsFetcher.getDurations(places, userLocation, (long) MAX_DURATION_SECONDS);
        } catch (ApiException | InterruptedException | IOException e) {
            return scoreByRating(places); // TODO(Tal): log error
        }
        ImmutableMap.Builder<Place, Double> scores = new ImmutableMap.Builder<>();
        for (Place place : places) {
            scores.put(place, calculatePlaceScore(durations, place));
        }
        return scores.build();
    }

    // Calculates a score for place,
    // score calculated by the place's rating and driving duration to the user's location.
    private double calculatePlaceScore(ImmutableMap<Place, Long> durations, Place place) {
        return
            RATING_WEIGHT * (place.rating() / MAX_RATING)
            + DURATION_WEIGHT * Math.max(1 - (durations.get(place) / MAX_DURATION_SECONDS), 0);
    }

    // Scores places by their rating only, used in case of errors in durations calculation.
    private ImmutableMap<Place, Double> scoreByRating(ImmutableList<Place> places) {
        return places.stream().collect(
            ImmutableMap.toImmutableMap(place -> place, place -> place.rating() / MAX_RATING));
    }
}
