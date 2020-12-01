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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.maps.model.LatLng;

@RunWith(JUnit4.class)
public class PlacesScorerImplTest {

    // Used for double comparasions to avoid differences resulting from double representations.
    private static final double DELTA = 0.0001;

    // Place builder without name and rating to be used on tests.
    private static final Place.Builder PLACE_BUILDER =
        Place.builder()
        .setWebsiteUrl("place.com")
        .setPhone("+97250-0000-000")
        .setPriceLevel(4)
        .setLocation(new LatLng(32.08, 34.78))
        .setGoogleUrl("google.com")
        .setPlaceId("placeid1")
        .setBusinessStatus(BusinessStatus.OPERATIONAL);

    private static final LatLng USER_LOCATION = new LatLng(33.12, 34.56);
    private static final Place PLACE_1 = PLACE_BUILDER.setName("name1").setRating(3).build();
    private static final Place PLACE_2 = PLACE_BUILDER.setName("name2").setRating(5).build();
    private static final ImmutableList<Place> PLACES_TO_SCORE = ImmutableList.of(PLACE_1, PLACE_2);

    @Test
    public void getScores_validPlaceList_returnsMapOfCorrectScores() {
        // Expected scores are calculated by the following algorithm:
        // Score(place) = rating*0.7 + drivingETA*0.3, such that:
        // rating = place's rating / Max Rating
        // drivingETA = max{1 - durationInMinutes(=30) / 40, 0}
        ImmutableMap<Place, Double> result =
            new PlacesScorerImpl().getScores(PLACES_TO_SCORE, USER_LOCATION);

        Double expectedScorePlace1 = 0.495;
        Double expectedScorePlace2 = 0.775;
        assertEquals(expectedScorePlace1, result.get(PLACE_1), DELTA);
        assertEquals(expectedScorePlace2, result.get(PLACE_2), DELTA);
    }

    @Test
    public void getScores_emptyPlaceList_returnsEmptyMap() {
        assertEquals(
            ImmutableMap.of(),
            new PlacesScorerImpl().getScores(ImmutableList.of(), USER_LOCATION));
    }

}
