package com.google.sps.data;

import java.io.IOException;
import java.util.Comparator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;

public class PlacesScorerRegisteredUser implements PlacesScorer {

    // The coefficients for the scoring algorithm, sum up to 1.
    private static final double RATING_WEIGHT = 0.5;
    private static final double DURATION_WEIGHT = 0.3;
    private static final double CUISINES_WEIGHT = 0.2;
    private static final double RATING_WEIGHT_NO_DURATIONS = 0.7;
    private static final double CUISINES_WEIGHT_NO_DURATIONS = 0.3;

    // The maximum possible rating as defined by the Google Places API.
    private static final double MAX_RATING = 5; // Consult where this should be

    // A duratoins techer used for calculating driving durations.
    private DurationsFetcher durationsFetcher;

    // The ID of the user the scores are calculated for.
    private String userId;

    // A data accessor used for retrieving data from the user's data base.
    private DataAccessor dataAccessor;

    // A data accessor used for retrieving data from the user's data base.
    private PlacesScorerUnregisteredUser scorerUnregisteredUser;

    /**
     * PlacesScorerRegisteredUser constructor.
     *
     * @param geoApiContext the GeoApiContext used for all Google GEO API requests
     * @param userId the ID of the user the scores are calculated for
     * @param durationsFetcher
     *     used for fetching the driving durations from each place to the user's location
     * @param inputScorerUnregistered used for scoring if there is no date stored for the user
     */
    public PlacesScorerRegisteredUser(
            String inputUserId,
            DataAccessor inputDataAccessor,
            DurationsFetcher inputDurationsFetcher,
            PlacesScorerUnregisteredUser inputScorerUnregistered) {
        this.userId = inputUserId;
        this.dataAccessor = inputDataAccessor;
        this.durationsFetcher = inputDurationsFetcher;
        this.scorerUnregisteredUser = inputScorerUnregistered;
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
        ImmutableMap<Place, Double> historicalCuisinesPreferences =
            calculatePreferenceFrequencyForPlace(places);
        if(historicalCuisinesPreferences.isEmpty()) {
            return scorerUnregisteredUser.getScores(places, userLocation);
        }
        ImmutableMap<Place, Double> durations;
        try {
            durations = durationsFetcher.getDurations(places, userLocation);
        } catch (ApiException | InterruptedException | IOException e) {
            return scoreByRatingandCuisines(places, historicalCuisinesPreferences);
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
            ImmutableMap<Place, Double> durations,
            Place place,
            ImmutableMap<Place, Double> cuisinesPreferencesHistory) {
        return
            RATING_WEIGHT * (place.rating() / MAX_RATING)
            + DURATION_WEIGHT * Math.max(1 - (durations.get(place)), 0)
            + CUISINES_WEIGHT * cuisinesPreferencesHistory.get(place);
    }

    // Creates a map between a place and the relative frequency the user
    // preferred the place's most preferred cuisine.
    private ImmutableMap<Place, Double> calculatePreferenceFrequencyForPlace(
            ImmutableList<Place> places) {
        ImmutableMap<String, Long> cuisinesPreferencesHistory;
        try {
            cuisinesPreferencesHistory = dataAccessor.getPreferredCuisines(userId);
        } catch(IllegalArgumentException e) {
            return ImmutableMap.of();
        }
        if (cuisinesPreferencesHistory.isEmpty()) {
            return ImmutableMap.of();
        }
        double totalPreferences =
            cuisinesPreferencesHistory.values().stream().mapToDouble(Long::doubleValue).sum();
            return places.stream().collect(
                ImmutableMap.toImmutableMap(
                    place -> place,
                    place ->
                        place.cuisines().stream()
                        .map(cuisine -> cuisinesPreferencesHistory.getOrDefault(cuisine, 0L))
                        .max(Comparator.comparing(Long::valueOf)).get() / totalPreferences));
    }

    // Scores places by their rating and cuisines only,
    // used in case of errors in durations calculation.
    private ImmutableMap<Place, Double> scoreByRatingandCuisines(
            ImmutableList<Place> places,
            ImmutableMap<Place, Double> cuisinesPreferencesHistory) {
        return places.stream().collect(
            ImmutableMap.toImmutableMap(place -> place,
            place ->
            RATING_WEIGHT_NO_DURATIONS * place.rating() / MAX_RATING
            + CUISINES_WEIGHT_NO_DURATIONS* cuisinesPreferencesHistory.get(place)));
    }
}
