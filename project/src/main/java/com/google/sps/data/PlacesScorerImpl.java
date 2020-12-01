package com.google.sps.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

public class PlacesScorerImpl implements PlacesScorer {

    // The coefficients for the scoring algorithm, sum up to 1.
    private static final double RATING_WEIGHT = 0.7;
    private static final double DURATION_WEIGHT = 0.3;

    // The maximum possible rating as defined by the Google Places API
    private static final double MAX_RATING = 5;

    // The maximum durations in seconds,
    // so that any duration higher than that will not contribute to the place's
    // score.
    private static final double MAX_DURATION_SECONDS = 40 * 60;

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
   * Returns a map of a place and the score the place gets based on a scoring algorithm.
   * @return A map between a place to a double representing the place’s score
   */
    @Override
    public ImmutableMap<Place, Double> getScores(
            ImmutableList<Place> places, LatLng userLocation) {
        ImmutableMap<Place, Long> durations = getDurations(places, userLocation);
        ImmutableMap.Builder<Place, Double> scores = new ImmutableMap.Builder<>();
        for (Place place : places) {
            scores.put(place, calcScoreOfPlace(durations, place));
        }
        return scores.build();
    }

    // Calculates a score for place,
    // score calculated by the place's rating and driving duration from the user's location.
    private double calcScoreOfPlace(ImmutableMap<Place, Long> durations, Place place) {
        return
            RATING_WEIGHT * (place.rating() / MAX_RATING)
            + DURATION_WEIGHT * Math.max(1 - (durations.get(place) / MAX_DURATION_SECONDS), 0);
    }

    // Returns the duration in seconds from each place on places list to the destination
    private ImmutableMap<Place, Long> getDurations(ImmutableList<Place> places, LatLng destination)
            throws ApiException, InterruptedException, IOException {
        Map<Place, Long> durations = new HashMap<>();
        LatLng[] origins = places.stream()
            .map(place -> place.location()).toArray(LatLng[]::new);
        DistanceMatrixApiRequest distanceRequest =
            DistanceMatrixApi.newRequest(CONTEXT)
                .origins(origins)
                .destinations(destination)
                .mode(TravelMode.DRIVING); // This is the default, just wrote it to bring to attention there are other options
        DistanceMatrix distanceMatrix = getDistanceResults(distanceRequest);
        for (int i = 0; i < places.size(); i++) {
            durations.put(places.get(i), distanceMatrix.rows[i].elements[0].duration.inSeconds);
            System.out.println(places.get(i).name() + ":" + distanceMatrix.rows[i].elements[0].duration.humanReadable);
        }
        durations.forEach((place, value) -> System.out.println(place.name() + ":" + value));
        return ImmutableMap.copyOf(durations);
    }


    /**
    * Queries Google Places API according to given query.
    *
    * @param distanceMatRequest A DistanceMatrixApiRequest with all places as origins
    *     and the user's location as the destination
    * @return A DistanceMatrix containig the distance and duration from each origin
    *     to the destination, each row in the matrix corresponds to an origin
    * @throws IOException
    * @throws InterruptedException
    * @throws ApiException
    */
    @VisibleForTesting
    DistanceMatrix getDistanceResults(DistanceMatrixApiRequest distanceMatRequest)
            throws ApiException, InterruptedException, IOException {
        return distanceMatRequest.await();
    }

     // TODO: move context setup to servlet and pass context to fetcher and scorer
}
