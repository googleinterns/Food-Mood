package com.google.sps.data;

import java.io.IOException;
import java.util.Comparator;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;

public class PlacesScorerRegisteredUser implements PlacesScorer {

    // The coefficients for the scoring algorithm, sum up to 1.
    private static final double RATING_WEIGHT = 0.5;
    private static final double DURATION_WEIGHT = 0.3;
    private static final double CUISINES_WEIGHT = 0.2;

    // The maximum possible rating as defined by the Google Places API.
    private static final double MAX_RATING = 5; // ################### Move this to interface? To factory and then pass it to each constructor?

    // The maximum durations in seconds, so that any duration higher than that
    // will not contribute to the place's score.
    private static final double MAX_DURATION_SECONDS = 40 * 60; // ################ Move this to interface? To factory and then pass it to each constructor?

    // A duratoins techer used for calculating driving durations.
    private DurationsFetcher durationsFetcher;

    // The ID of the user the scores are calculated for.
    private String userId;

    // A data accessor used for retrieving data from the user's data base.
    private DataAccessor dataAccessor;

    /**
     * PlacesScorerRegisteredUser constructor.
     *
     * @param geoApiContext the GeoApiContext used for all Google GEO API requests
     * @param userId the ID of the user the scores are calculated for
     */
    public PlacesScorerRegisteredUser(
            GeoApiContext geoApiContext, String inputUserId, DataAccessor inputDataAccessor) {
        this.durationsFetcher = new DurationsFetcher(geoApiContext);
        this.userId = inputUserId;
        this.dataAccessor = inputDataAccessor;
    }

    /**
     * {@inheritDoc}
     *
     * Calculates scores based on driving duration to the user’s location, rating,
     * and cuisines the user tends to prefer.
     */
    @Override
    public ImmutableMap<Place, Double> getScores(
            ImmutableList<Place> places, LatLng userLocation) {
        ImmutableMap<Place, Long> durations;
        ImmutableMap<String, Long> historicalCuisinesPreferences =
            dataAccessor.getPreferredCuisines(userId);
        try {
            durations =
                durationsFetcher.getDurations(places, userLocation, (long) MAX_DURATION_SECONDS);
        } catch (ApiException | InterruptedException | IOException e) {
            return scoreByRating(places); // TODO(Tal): log error
        }
        ImmutableMap.Builder<Place, Double> scores = new ImmutableMap.Builder<>();
        for (Place place : places) {
            scores.put(
                place, calculatePlaceScore(durations, place, historicalCuisinesPreferences));
        }
        return scores.build();
    }

    // Calculates a score for place,
    // score calculated by the place's rating and driving duration to the user's location.
    private double calculatePlaceScore(
            ImmutableMap<Place, Long> durations,
            Place place,
            ImmutableMap<String, Long> cuisinesPreferencesHistory) {
        double totalPreferences =
            cuisinesPreferencesHistory.values().stream().mapToDouble(Long::doubleValue).sum();
        double frequencyOfMostPreferedCuisine = place.cuisines().stream()
            .map(cuisine -> cuisinesPreferencesHistory.get(cuisine))
            .max(Comparator.comparing(Long::valueOf)).get() / totalPreferences ;
        return
            RATING_WEIGHT * (place.rating() / MAX_RATING)
            + DURATION_WEIGHT * Math.max(1 - (durations.get(place) / MAX_DURATION_SECONDS), 0)
            + CUISINES_WEIGHT * frequencyOfMostPreferedCuisine;
    }

    // Scores places by their rating only, used in case of errors in durations calculation.
    private ImmutableMap<Place, Double> scoreByRating(ImmutableList<Place> places) {
        return places.stream().collect(
            ImmutableMap.toImmutableMap(place -> place, place -> place.rating() / MAX_RATING));
    }
}
