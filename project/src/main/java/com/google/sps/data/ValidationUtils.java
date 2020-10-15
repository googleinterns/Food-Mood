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

import static com.google.appengine.repackaged.com.google.common.base.Preconditions.checkArgument;

/**
 * A utility class for validation.
 */
public class ValidationUtils {

    /** The maximal valid rating value. */
    private static final float MAX_RATING = 5.0f;
    /** The minimal valid rating value. */
    private static final float MIN_RATING = 1.0f;
    /** The maximal valid price level value. */
    private static final int MAX_PRICE_LEVEL = 4;
    /** The minimal valid price level value. */
    private static final int MIN_PRICE_LEVEL = 0;

    /**
     * @param rating the rating of a place, should be represented by a number between 1-5.
     * @throws IllegalArgumentException
     */
    public static final void validateRating(float rating) throws IllegalArgumentException {
        checkArgument(rating >= MIN_RATING && rating <= MAX_RATING,
        "Rating should be between %s-%s", MIN_RATING, MAX_RATING);
    }

    /**
     * @param priceLevel the price level of a place, should be represented by a number between 0-4.
     * @throws IllegalArgumentException
     */
    public static final void validatePriceLevel(int priceLevel) throws IllegalArgumentException {
        checkArgument(priceLevel >= MIN_PRICE_LEVEL && priceLevel <= MAX_PRICE_LEVEL,
        "Price level should be between %s-%s", MIN_PRICE_LEVEL, MAX_PRICE_LEVEL);
    }

    private ValidationUtils() {
    }
}
