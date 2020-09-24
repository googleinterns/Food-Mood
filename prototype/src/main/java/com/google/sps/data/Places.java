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

import java.util.Comparator;
import java.util.Random;

/**
 * A utility class for Place objects.
 */
public final class Places {

  /**
   * A private constructor, so the utility class can't be instanciated.
   */
  private Places() { }

  /**
   * A Place comparator that compares places randomly.
   */
  public Comparator<Place> randomComparator = new Comparator<Place>() {
    @Override
    public int compare(final Place a, final Place b) {
      Random rand = new Random();
      return rand.nextBoolean() ? 1 : -1; //TO DO: are these magic numbers, 
      //or is this ok considering we're implementing a comparator?
    }
  };
}
