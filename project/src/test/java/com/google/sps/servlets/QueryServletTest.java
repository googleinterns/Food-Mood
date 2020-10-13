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

package com.google.sps.servlets;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.Before;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.ArgumentMatchers.any;


@RunWith(JUnit4.class)
public final class QueryServletTest {

  // @Mock
  // HttpServletRequest request;
  // @Mock
  // HttpServletResponse response;

  // @Before
  // public void setUp() throws Exception {
  // }

  @Test
  public void getRequest_respondMaxNumOfPlaces() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    when(request.getParameter("fn")).thenReturn("Vinod");
    when(request.getParameter("ln")).thenReturn("Kashyap");

    when(response.getWriter()).thenReturn(pw);

    QueryServlet myServlet = new QueryServlet();
    myServlet.doGet(request, response);
  }

}
