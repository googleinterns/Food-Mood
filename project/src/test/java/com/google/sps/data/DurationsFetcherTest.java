// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixElementStatus;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.Duration;
import com.google.maps.model.LatLng;

@RunWith(JUnit4.class)
public final class DurationsFetcherTest {

    // Used for double comparasions to avoid differences resulting from double representations.
    private static final double DELTA = 0.0001;

    private static final Place PLACE_1 = createPlace("placeId1");
    private static final Place PLACE_2 = createPlace("placeId2");
    private static final LatLng USER_LOCATION = new LatLng(33.12, 34.56);
    private static final String[] PLACES_ADDRESSES = {"Place1 Address", "Place2 Address"};
    private static final String[] USERS_ADDRESS = {"User's Address"};

    // Each DistanceMatrixRow represents the results for a single origin.
    // Since there is only one destination (the user's location) for all origins (the places),
    // each row will include one DistanceMatrixElement that holds the duration between a place
    // to the user location.
    // This method creates uniform durations between each origin to the destinations.
    private static DistanceMatrixRow[] createDistanceMatrixRows(Long... durationsInSeconds) {
        DistanceMatrixRow[] distanceMatrixRows = new DistanceMatrixRow[durationsInSeconds.length];
        for(int i = 0; i < durationsInSeconds.length; i++) {
            DistanceMatrixElement element = new DistanceMatrixElement();
            Duration duration = new Duration();
            duration.inSeconds = durationsInSeconds[i];
            element.duration = duration;
            DistanceMatrixRow distanceMatrixSingleRow = new DistanceMatrixRow();
            distanceMatrixSingleRow.elements = new DistanceMatrixElement[] {element};
            distanceMatrixRows[i] = distanceMatrixSingleRow;
        }
        return distanceMatrixRows;
    }

    private static final Place createPlace(String placeId) {
        return Place.builder()
        .setPlaceId(placeId)
        .setWebsiteUrl("place.com")
        .setPhone("+97250-0000-000")
        .setPriceLevel(4)
        .setLocation(new LatLng(32.08, 34.78))
        .setGoogleUrl("google.com")
        .setName("name")
        .setBusinessStatus(BusinessStatus.OPERATIONAL)
        .setCuisines(ImmutableSet.of("sushi", "hamburger"))
        .setRating(4)
        .build();
    }

    /** A DurationsFetcher instance to be tested. */
    private static DurationsFetcher durationsFetcher =
        new DurationsFetcher(GeoContext.getGeoApiContext());

    @Test
    public void getDurations_validPlaceList_returnsMapOfRelativeDurations() throws Exception {
        // Expected durations are calculated as follows:
        // Duration(place) = durationInMinutes / maxDuration (=40)
        DistanceMatrixRow[] distanceMatrixRow =
                    createDistanceMatrixRows(600L, 1800L); // 10 and 30 minutes durations
        distanceMatrixRow[0].elements[0].status = DistanceMatrixElementStatus.OK;
        distanceMatrixRow[1].elements[0].status = DistanceMatrixElementStatus.OK;
        DurationsFetcher spiedDurationsFetcher = spy(durationsFetcher);
        doReturn(new DistanceMatrix(PLACES_ADDRESSES, USERS_ADDRESS, distanceMatrixRow))
            .when(spiedDurationsFetcher)
            .getDistanceResults(any(DistanceMatrixApiRequest.class));

        ImmutableMap<Place, Double> result =
            spiedDurationsFetcher.getDurations(
                ImmutableList.of(PLACE_1, PLACE_2), USER_LOCATION);

        Double expectedDurationPlace1 = 0.25;
        Double expectedDurationPlace2 = 0.75;
        assertEquals(expectedDurationPlace1, result.get(PLACE_1), DELTA);
        assertEquals(expectedDurationPlace2, result.get(PLACE_2), DELTA);
    }

    @Test
    public void getDurations_emptyPlaceList_returnsEmptyMap() throws Exception {
        DurationsFetcher spiedDurationsFetcher = spy(durationsFetcher);
        doReturn(new DistanceMatrix(new String[0], new String[0], new DistanceMatrixRow[0]))
            .when(spiedDurationsFetcher)
            .getDistanceResults(any(DistanceMatrixApiRequest.class));

        assertEquals(
            ImmutableMap.of(),
            spiedDurationsFetcher.getDurations(ImmutableList.of(), USER_LOCATION));
    }

    @Test
    public void getDurations_invalidDurationStatus_durationsAreMaxDuration() throws Exception {
        // Expected duration for a place with an invalid status is the max duration,
        // so the durations are calculated as follows:
        // Duration(placeWithError) = maxDuration / maxDuration = 1
        DistanceMatrixRow[] distanceMatrixRow =
                createDistanceMatrixRows(600L, 1800L); // 10 and 30 minutes durations
        distanceMatrixRow[0].elements[0].status = DistanceMatrixElementStatus.ZERO_RESULTS;
        distanceMatrixRow[1].elements[0].status = DistanceMatrixElementStatus.NOT_FOUND;
        DurationsFetcher spiedDurationsFetcher = spy(durationsFetcher);
        doReturn(new DistanceMatrix(PLACES_ADDRESSES, USERS_ADDRESS, distanceMatrixRow))
            .when(spiedDurationsFetcher)
            .getDistanceResults(any(DistanceMatrixApiRequest.class));

        ImmutableMap<Place, Double> result =
            spiedDurationsFetcher.getDurations(
                ImmutableList.of(PLACE_1, PLACE_2), USER_LOCATION);

        Double expectedDuration = 1.0;
        assertEquals(expectedDuration, result.get(PLACE_1));
        assertEquals(expectedDuration, result.get(PLACE_2));
    }
}
