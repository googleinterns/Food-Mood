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

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import com.google.maps.model.LatLng;
import com.google.common.collect.ImmutableList;

@RunWith(JUnit4.class)
public final class PlaceTest {

  private static final float RATING = 4;
  private static final int PRICE_LEVEL = 2;
  private static final LatLng LOCATION = new LatLng(32.08074, 34.78059);
  private static final String NAME = "name";
  private static final String PHONE = "+97250-0000-000";
  private static final String WEBSITE = "website.com";
  private static final String GOOGLE_URL = "googleurl.com";
  private static final String PLACE_ID = "ChIJN1t_tDeuEmsRUsoyG83frY4";
  private static final BusinessStatus BUSINESS_STATUS = BusinessStatus.OPERATIONAL;
  private static final ImmutableList<String> CUISINES =
      ImmutableList.of("sushi", "hamburger");

  @Test
  public void build_invalidLowRating_throwsIllegalArgumentException() {
    int invalidLowRating = 0;

    assertThrows(IllegalArgumentException.class, () -> {
      getValidPlaceBuilder().setRating(invalidLowRating).build();
    });
  }

  @Test
  public void build_invalidHighRating_throwsIllegalArgumentException() {
    int invalidHighRating = 10;

    assertThrows(IllegalArgumentException.class, () -> {
      getValidPlaceBuilder().setRating(invalidHighRating).build();
    });
  }

  @Test
  public void build_invalidLowPriceLevel_throwsIllegalArgumentException() {
    int invalidLowPriceLevel = -1;

    assertThrows(IllegalArgumentException.class, () -> {
      getValidPlaceBuilder().setPriceLevel(invalidLowPriceLevel).build();
    });
  }

  @Test
  public void build_invalidHighPriceLevel_throwsIllegalArgumentException() {
    int invalidHighPriceLevel = 5;

    assertThrows(IllegalArgumentException.class, () -> {
      getValidPlaceBuilder().setPriceLevel(invalidHighPriceLevel).build();
    });
  }

  @Test
  public void build_validInput_returnsValidPlace() {
    Place place = getValidPlaceBuilder().build();
      assertAll("place",
          () -> assertEquals(NAME, place.name()),
          () -> assertEquals(WEBSITE, place.websiteUrl()),
          () -> assertEquals(PHONE, place.phone()),
          () -> assertEquals(RATING, place.rating()),
          () -> assertEquals(PRICE_LEVEL, place.priceLevel()),
          () -> assertEquals(LOCATION, place.location()),
          () -> assertEquals(GOOGLE_URL, place.googleUrl()),
          () -> assertEquals(PLACE_ID, place.placeId()),
          () -> assertEquals(BUSINESS_STATUS, place.businessStatus()),
          () -> assertEquals(CUISINES, place.cuisines())
      );
  }

  // Returns a Place builder that has valid values of all attributes.
  private Place.Builder getValidPlaceBuilder() {
    return Place.builder()
        .setName(NAME)
        .setWebsiteUrl(WEBSITE)
        .setPhone(PHONE)
        .setRating(RATING)
        .setPriceLevel(PRICE_LEVEL)
        .setLocation(LOCATION)
        .setGoogleUrl(GOOGLE_URL)
        .setPlaceId(PLACE_ID)
        .setBusinessStatus(BUSINESS_STATUS)
        .setCuisines(ImmutableList.of("sushi", "hamburger"));
  }
}
