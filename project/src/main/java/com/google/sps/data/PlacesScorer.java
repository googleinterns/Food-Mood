package com.google.sps.data;

import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.lang.Math;

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

public class PlacesScorer {

    private static final double RATING_WEIGHT = 0.7;
    private static final double DURATION_WEIGHT = 0.3;
    private static final double MAX_RATING = 5;
    private static final double MAX_DURATION = 60;
    private ImmutableMap<String, Double> durations;
    private ImmutableList<Place> places;

    // The entry point for a Google GEO API request.
    private static final GeoApiContext CONTEXT = new GeoApiContext.Builder()
        .apiKey(System.getenv("API_KEY"))
        .build();

    PlacesScorer(ImmutableList<Place> places, LatLng userLocation) {
        this.durations = getDurations(places, userLocation);
        this.places = places;
    }

   /**
   * Returns a map of place IDs to the score the place gets based on a scoring  algorithm.
   * @param places: A list of places we want to calculate their score
   * @return A map between a place ID to a double representing the place’s score
   */
    public ImmutableMap<String, Double> getScores() {
        Map<String, Double> scores = new HashMap<>();
        for (Place place: places) {
            scores.put("placeId", calcScore(place));
        }
        return ImmutableMap.copyOf(scores);
    }

    private double calcScore(Place place) {
        return
            RATING_WEIGHT * (place.rating() / MAX_RATING) +
            DURATION_WEIGHT * Math.max((durations.get("placeId" )/ MAX_DURATION), 1);
    }

    private ImmutableMap<String, Double> getDurations(ImmutableList<Place> places, LatLng destination) {
        LatLng[] origins = places.stream().map(place -> place.location()).toArray(LatLng[]::new);
        DistanceMatrixApiRequest distanceRequest =
            DistanceMatrixApi.newRequest(CONTEXT)
                .origins(origins)
                .destinations(destination)
                .mode(TravelMode.DRIVING);
        DistanceMatrix distanceMatrix = getDistanceResults(distanceRequest);
        // iterate over the rows of the matrix and get the duration from row[0].element
        // translate it to minutes and store on the map
        return ImmutableMap.copyOf();
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


// TODO: understand how to translate te int into minutes and how to implement bucketing
// TODO: move context setup to servlet and pass context to fetcher and scorer

}
