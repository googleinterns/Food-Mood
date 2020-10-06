package com.google.sps.test;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import com.google.maps.model.LatLng;
import com.google.sps.data.PlacesFetcher;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.*;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public final class PlacesFetcherTest {

    
    /** The expected parameters for each place fetched by PlacesFetcher.fetch() */ 
    private static final String cuisineType = "sushi"; // TODO (talbarnahor): change to set of types
    private static final PriceLevel maxPriceLevel = PriceLevel.MODERATE; // TODO: map int from form to PrivceLevel
    private static final boolean OPEN_NOW = true;
    private static final PlaceType TYPE = PlaceType.RESTAURANT;
    private static final int MAX_DISTANCE = 5000; 
    private static final LatLng SEARCH_LOCATION = new LatLng(32.080576, 34.780641); // Rabin Square TLV

    /** Places to be used in tests. */
    private static final Place PLACE_1 = Place.create(/*name*/ "name1", /*websiteURL*/
    "website@google.com", /*phone number*/ "+97250-0000-000",  /*rating*/ 4, /*price level*/ 2,
    /*location*/ new LatLng(32.08066, 34.78071));
    private static final Place PLACE_2 = Place.create(/*name*/ "name2", /*websiteURL*/
    "website@google.com", /*phone number*/ "+97250-0000-000",  /*rating*/ 4, /*price level*/ 1,
    /*location*/ new LatLng(32.08074, 34.78059));

    /** A list of places to be used in tests */
    private static final ImmutableList<Place> placesLst = ImmutableList.of(PLACE_1, PLACE_2);

    /** Places to be used in tests. */
    private static final Place PLACE_1 = Place.create(/*name*/ "name1", /*websiteURL*/
    "website@google.com", /*phone number*/ "+97250-0000-000",  /*rating*/ 4, /*price level*/ 2,
    /*location*/ new LatLng(32.08066, 34.78071));
    private static final Place PLACE_2 = Place.create(/*name*/ "name2", /*websiteURL*/
    "website@google.com", /*phone number*/ "+97250-0000-000",  /*rating*/ 4, /*price level*/ 1,
    /*location*/ new LatLng(32.08074, 34.78059));

    /** A list of places to be used in tests */
    private static final ImmutableList<Place> placesLst = ImmutableList.of(PLACE_1, PLACE_2);

    @Test
    public void fetch_noSearchResults_returnsEmptyList() {
      MockedStatic<PlacesApiBridge> placesAPIMock = mock(PlacesApiBridge.class);
      when(searchResultsMock.PlacesApiBridge.getPlacesSearchResponse(
        any(GeoApiContext.class, String.class, LatLng.class, PriceLevel.class, boolean.class)))
        .thenReturn(new PlacesSearchResult[0]);
      ImmutableList<Place> expectedOutput = ImmutableList.of();
      assertEquals(expectedOutput, placesAPIMock.PlacesFetcher.fetch());             
    }

    @Test
    public void fetch_validSearchResults_returnsListOfPlaces() {
      MockedStatic<PlacesApiBridge> placesAPIMock = mock(PlacesApiBridge.class);
      when(searchResultsMock.PlacesApiBridge.getPlacesSearchResponse(
        any(GeoApiContext.class, String.class, LatLng.class, PriceLevel.class, boolean.class)))
        .thenReturn(List of results);
      ImmutableList<Place> expectedOutput = List of matching places;
      assertEquals(expectedOutput, placesAPIMock.PlacesFetcher.fetch());             
    }

}
