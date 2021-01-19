package com.google.sps.data;

import java.util.Optional;

import com.google.maps.GeoApiContext;

public class PlacesScorerFactory {

    // The entry point for a Google GEO API request.
    private GeoApiContext context;

    // The user verifier used to get the user ID for PlacesScorerRegisteredUser construction.
    private UserVerifier userVerifier;

    // The DataAccessor passed to the PlacesScorerRegisteredUser constructor.
    private DataAccessor dataAccessor;

    // The DurationsFetcher used for calculating driving durations.
    private DurationsFetcher durationsFetcher;

    /**
     * PlacesScorerFactory constructor.
     *
     * @param geoApiContext the GeoApiContext used for all Google GEO API requests
     * @param verifier the UserVerifier used to get the user ID
     * @param accessor the DataAccessor used for PlacesScorerRegisteredUser construction
     */
    public PlacesScorerFactory(
            GeoApiContext geoApiContext, UserVerifier verifier, DataAccessor accessor) {
        this.context = geoApiContext;
        this.userVerifier = verifier;
        this.dataAccessor = accessor;
        this.durationsFetcher = new DurationsFetcher(geoApiContext);
    }

    /**
     * Returns a PlacesScorer used for places score calculation for signed-in
     * and non signed-in users.
     *
     * @param userIdToken the user's ID token
     * @return returns a PlacesScorer
     */
    public PlacesScorer create(String userIdToken) {
        Optional<String> optionalUserId = userVerifier.getUserIdByToken(userIdToken);
        if (optionalUserId.isPresent()) {
            return new PlacesScorerRegisteredUser(
                optionalUserId.get(),
                dataAccessor,
                durationsFetcher,
                new PlacesScorerUnregisteredUser(durationsFetcher));
        } else {
            return new PlacesScorerUnregisteredUser(durationsFetcher);
        }
    }
}
