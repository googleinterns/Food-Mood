package com.google.sps.data;

import java.io.IOException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixElementStatus;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

public class PlacesScorerImpl implements PlacesScorer {

    // The coefficients for the scoring algorithm, sum up to 1.
    private static final double RATING_WEIGHT = 0.7;
    private static final double DURATION_WEIGHT = 0.3;

    // The maximum possible rating as defined by the Google Places API.
    private static final double MAX_RATING = 5;

    // The maximum durations in seconds, so that any duration higher than that
    // will not contribute to the place's score.
    private static final long MAX_DURATION_SECONDS = 40 * 60;

    // The entry point for a Google GEO API request.
    private GeoApiContext context;

    /**
     * PlacesScorerImpl constructor.
     *
     * @param geoApiContext the GeoApiContext used for all Google GEO API requests
     */
    public PlacesScorerImpl(GeoApiContext geoApiContext) {
        this.context = geoApiContext;
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
            durations = getDurations(places, userLocation);
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
            + DURATION_WEIGHT
                * Math.max(1 - ((double) durations.get(place) / MAX_DURATION_SECONDS), 0);
    }

    // Scores places by their rating only, used in case of errors in durations calculation.
    private ImmutableMap<Place, Double> scoreByRating(ImmutableList<Place> places) {
        return places.stream().collect(
            ImmutableMap.toImmutableMap(place -> place, place -> place.rating() / MAX_RATING));
    }

    // Returns the duration in seconds from each place on places list to the destination
    private ImmutableMap<Place, Long> getDurations(ImmutableList<Place> places, LatLng destination)
            throws ApiException, InterruptedException, IOException {
        ImmutableMap.Builder<Place, Long> durations = new ImmutableMap.Builder<>();
        LatLng[] origins = places.stream()
            .map(place -> place.location()).toArray(LatLng[]::new);
        DistanceMatrixApiRequest distanceRequest =
            DistanceMatrixApi.newRequest(context)
                .origins(origins)
                .destinations(destination)
                .mode(TravelMode.DRIVING);
        DistanceMatrix distanceMatrix = getDistanceResults(distanceRequest);
        for (int i = 0; i < places.size(); i++) {
            DistanceMatrixElement element = distanceMatrix.rows[i].elements[0];
            if (element.status == DistanceMatrixElementStatus.OK) {
                durations.put(places.get(i), element.duration.inSeconds);
            } else { // TODO(Tal): decide if this place should be filtered out
                durations.put(places.get(i), MAX_DURATION_SECONDS);
            }
        }
        return durations.build();
    }

    /**
    * Queries Google Places API according to given query.
    *
    * @param distanceMatRequest A DistanceMatrixApiRequest with all places as origins
    *     and the user's location as the destination
    * @return A DistanceMatrix containig the distance and duration from each origin
    *     to the destination, each row in the matrix corresponds to an origin
    * @throws IOException Thrown when an I/O exception of some sort has occurred
    * @throws InterruptedException Thrown when a thread is occupied and interrupted
    * @throws ApiException Thrown if the API returned result is an error
    */
    @VisibleForTesting
    DistanceMatrix getDistanceResults(DistanceMatrixApiRequest distanceMatRequest)
            throws ApiException, InterruptedException, IOException {
        return distanceMatRequest.await();
    }
}
