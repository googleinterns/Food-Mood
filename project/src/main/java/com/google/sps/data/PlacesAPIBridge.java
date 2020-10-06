package com.google.sps.data;

import java.io.IOException;

import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.errors.ApiException;
import com.google.maps.model.LatLng;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.google.maps.model.PriceLevel;

public class PlacesAPIBridge {

    /**
     * The type of places that will be searched
     */
    private static final PlaceType TYPE = PlaceType.RESTAURANT;

    /**
     * The search radius for places
     */
    private static final int SEARCH_RADIUS = 5000; // TODO (M1): check at least 10 results, and if less extend radius


    /**
     * Queries Google Places API according to given params. In M1 they will be passed as UserPrefrences fields.
     * 
     * @param context The entry point for a Google GEO API request
     * @param cuisineType
     * @param location
     * @param maxPriceLevel
     * @param openNow
     * @return A PlacesSearchResponse which contains the search results 
     * @throws ApiException
     * @throws InterruptedException
     * @throws IOException
     */
    public PlacesSearchResult[] getPlacesSearchResponse(GeoApiContext context, String cuisineType,
            LatLng location, PriceLevel maxPriceLevel, boolean openNow)
            throws ApiException, InterruptedException, IOException {
        PlacesSearchResponse results = 
        PlacesApi.textSearchQuery(context, cuisineType, location)
            .radius(SEARCH_RADIUS)
            .maxPrice(maxPriceLevel)
            .openNow(openNow)
            .type(TYPE)
            .await();
        return results.results;
    }
    
}