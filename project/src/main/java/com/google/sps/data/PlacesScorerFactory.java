package com.google.sps.data;

import com.google.maps.GeoApiContext;

public class PlacesScorerFactory {

    // The entry point for a Google GEO API request.
    private GeoApiContext context;

    /**
     * PlacesScorerFactory constructor.
     *
     * @param geoApiContext the GeoApiContext used for all Google GEO API requests
     */
    public PlacesScorerFactory(GeoApiContext geoApiContext) {
        this.context = geoApiContext;
    }

    /**
     * Returns a PlacesScorerRegisteredUser used for places score calculation for a signed in user.
     *
     * @param userId
     * @return returns PlacesScorer for a user that is signed in
     */
    public PlacesScorer create(String userId, DataAccessor dataAccessor) {
        return new PlacesScorerRegisteredUser(context, userId, dataAccessor);
    }

    /**
     * Returns a PlacesScorerUnregisteredUser used for places score calculation for
     * a user that is not signed in.
     *
     * @return returns PlacesScorer for a user that is not signed in
     */
    public PlacesScorer create() {
        return new PlacesScorerUnregisteredUser(context);
    }
}
